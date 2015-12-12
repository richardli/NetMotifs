##
## Script to plot unsupervised SC results
##
##

# args <- commandArgs(trailingOnly = TRUE)
# name <- args[1]
library(ROCR)
library(reshape)
library(ggplot2)
group <- 1:3
period <- 1:8
n1 <- length(group)
n2 <- length(period)

all.coef <- NULL
source("Dict_function.r")
dict1 <- default_dict() 
# add line break to names
rownames(dict1) <- c(
	"MM user\ndyad", 
	"non MM user\ndyad", 
	"MM user\nincomplete triad",
	"MM user\ntriad center", 
	"MM user\ntriangle", 
	"MM user\ncloser", 
	"non MM user\ncloser", 
	"one of each\nincomplete triad", 
	"one of each\ntriangle", 
	"non MM user\nincomplete triad",
	"non MM user\n triad center", 
	"non MM user\ntriangle",
	"0 In-degree",
	"1 In-degree",
	"2 In-degree",
	"0 out-degree",
	"1 out-degree",
	"2 out-degree",
	"1 link", "2 links", "3 links", "4 links",
	"Motifs without\nMM user", "Motifs with\n1 MM user" , "Motifs with\n2 MM user")

for(i in group){
	for(j in period){
		name <- paste("g", i, "p", j, sep = "")
		# load alpha
		alpha <- read.table(paste("../data/alphas/run_alpha_", name, ".txt", sep = ""), sep = ",")
		print(".")

		# load Y
		load(paste("../data/motif_counts/Motifs_", name, ".rda", sep = ""))
		motif <- motif[-which(motif$Y == -1), ]

		ntrain <- (length(motif$Y) * 1)
		train <- sample(1:length(motif$Y), size = ntrain)
		test <- (1:length(motif$Y))[-train]

		# SC results
		data.sc <- data.frame(Y = motif$Y, alpha)
		colnames(data.sc) <- c("Y", rownames(dict1))

		fit <- glm(Y ~ ., data = data.sc[train, ], family = "binomial")
		coef <- summary(fit)$coefficients 
		rownames(coef) <- c("Intercept", rownames(dict1))
		all.coef <- rbind(all.coef, cbind(coef, 1:dim(coef)[1], i, j))
		# # pred <- as.matrix(cbind(1, alpha)) %*% coef[, 1, drop = F]
		# pred <- predict(fit, newdata = data.sc[test, ])

		# pred.sc <- prediction(pred, motif$Y[test])
		# perf.sc <- performance(pred.sc,"tpr","fpr")
		# auc.sc <- performance(pred.sc, "auc")@y.values[[1]]
		# auc.sc
		# plot(perf.sc)
		print("+")
	}
}

all.coef <- data.frame(all.coef, 
				 type = rep(c("Intercept", rownames(dict1)), n1*n2))
colnames(all.coef)[1:7] <- c("beta", "SE", "z.value", "pval", "type.num", "period", "week")

range.levels <- c("May-Jun, 2007", "May-Jun, 2008", "Sept-Oct, 2008")
all.coef$period[which(all.coef$period== 1)] <- range.levels[1]
all.coef$period[which(all.coef$period == 2)] <- range.levels[2]
all.coef$period[which(all.coef$period == 3)] <- range.levels[3]

all.coef$type <- factor(all.coef$type, levels = c("Intercept", rownames(dict1)))
save(all.coef, file = "../data/unsup-3periodsAll.rda")

set1 <- 4:13
set2 <- 14:23
set3 <- 20:26


# same plot
pdf("../figures/unsup-fig1.pdf", width = 12, height = 5.5)
g1 <- ggplot(data = subset(all.coef, type.num %in% set3))
g1 <- g1 + geom_point(aes(x = week, y = beta), 
					  size = 2)
g1 <- g1 + geom_line(aes(x = week, y = beta, color = period), 
					 alpha = .5)
g1 <- g1 + geom_errorbar(aes(x = week, y = beta, color = period,
						ymin = beta-1.96*SE, ymax = beta+1.96*SE), 
						size = .5, width = .5, alpha = .8)
g1 <- g1 + facet_grid(~ type)
g1 <- g1 + scale_color_manual(values = c("#009E73", "#0072B2", "#D55E00"))
g1 <- g1 + geom_hline(yintercept=0, color = "red", 
	alpha = 0.5, linetype ="longdash")
g1 <- g1 + ggtitle("")+ theme_bw()
g1 <- g1 + theme( panel.grid.minor = element_blank())
g1
dev.off()


pdf("../figures/unsup-fig2.pdf", width = 15, height = 5.5)
g1 <- ggplot(data = subset(all.coef, type.num %in% set1))
g1 <- g1 + geom_point(aes(x = week, y = beta), 
					  size = 2)
g1 <- g1 + geom_line(aes(x = week, y = beta, color = period), 
					 alpha = .5)
g1 <- g1 + geom_errorbar(aes(x = week, y = beta, color = period,
						ymin = beta-1.96*SE, ymax = beta+1.96*SE), 
						size = .5, width = .5, alpha = .8)
g1 <- g1 + facet_grid(~ type)
g1 <- g1 + scale_color_manual(values = c("#009E73", "#0072B2", "#D55E00"))
g1 <- g1 + geom_hline(yintercept=0, color = "red", 
	alpha = 0.5, linetype ="longdash")
g1 <- g1 + ggtitle("")+ theme_bw()
g1 <- g1 + theme( panel.grid.minor = element_blank())
g1
dev.off()



pdf("../figures/unsup-fig4.pdf", width = 15, height = 5.5)
g1 <- ggplot(data = subset(all.coef, type.num %in% set1))
g1 <- g1 + geom_point(aes(x = week, y = beta), 
					  size = 2)
g1 <- g1 + geom_line(aes(x = week, y = beta, color = type), 
					 alpha = .5)
g1 <- g1 + geom_errorbar(aes(x = week, y = beta, color = type,
						ymin = beta-1.96*SE, ymax = beta+1.96*SE), 
						size = .5, width = .1, alpha = .8)
g1 <- g1 + facet_grid(type ~ period, scales = "fixed")
# g1 <- g1 + scale_color_manual(values = c("#009E73", "#0072B2", "#D55E00"))
g1 <- g1 + geom_hline(yintercept=0, color = "red", 
	alpha = 0.5, linetype ="longdash")
g1 <- g1 + ggtitle("")+ theme_bw()
g1 <- g1 + theme( panel.grid.minor = element_blank())
g1
dev.off()


# separate panels
g1 <- ggplot(data = subset(all.coef, type.num %in% set1))
g1 <- g1 + geom_point(aes(x = week, y = beta), 
					  color = "orange2", size = 2)
g1 <- g1 + geom_line(aes(x = week, y = beta, color = period), 
					 alpha = .5)
g1 <- g1 + geom_errorbar(aes(x = week, y = beta, color = period,
						ymin = beta-1.96*SE, ymax = beta+1.96*SE), 
						size = .5, width = .5, alpha = .8)
g1 <- g1 + facet_grid(period ~ type)
g1 <- g1 + scale_color_manual(values=c("#009E73","#0072B2","#D55E00"), guide="none")
g1 <- g1 + geom_hline(yintercept=0, color = "red", 
	alpha = 0.5, linetype ="longdash")
g1 <- g1 + ggtitle("") + theme_bw()
g1 <- g1 + theme( panel.grid.minor = element_blank())
g1
