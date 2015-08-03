##
## Script to plot unsupervised SC results
##
##

# args <- commandArgs(trailingOnly = TRUE)
# name <- args[1]
library(ROCR)
library(reshape)
library(ggplot2)
group <- c(1,3)
period <- 1:8
n1 <- length(group)
n2 <- length(period)

all.coef <- NULL
source("Dict_function.r")
dict1 <- default_dict() 

for(i in group){
	for(j in period){
		name <- paste("g", i, "p", j, sep = "")
		# to be removed
		if(i==3 && j %in% c(3:5)){
			load(paste("../data/unsup-runs/", name, "-unsup_short.rda", sep = ""))
			rownames(coef) <- c("Intercept", rownames(dict1))
			all.coef <- rbind(all.coef, cbind(coef, 1:dim(coef)[1], i, j))
			next
		}

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
colnames(all.coef)[1:7] <- c("beta", "SE", "z.value", "pval", "type.num", "group", "period")

range.levels <- c("May-Jun, 2007", "May-Jun, 2008", "Sept-Oct, 2008")
all.coef$group[which(all.coef$group== 1)] <- range.levels[1]
all.coef$group[which(all.coef$group == 2)] <- range.levels[2]
all.coef$group[which(all.coef$group == 3)] <- range.levels[3]

all.coef$type <- factor(all.coef$type, levels = c("Intercept", rownames(dict1)))

set1 <- 4:13
set2 <- 14:23
set3 <- 24:26
g1 <- ggplot(data = subset(all.coef, type.num %in% set1))
g1 <- g1 + geom_point(aes(x = period, y = beta), 
					  size = 2)
g1 <- g1 + geom_line(aes(x = period, y = beta, color = group), 
					 alpha = .5)
g1 <- g1 + geom_errorbar(aes(x = period, y = beta, color = group,
						ymin = beta-1.96*SE, ymax = beta+1.96*SE), 
						size = .5, width = .5, alpha = .8)
g1 <- g1 + facet_grid(~ type)
g1 <- g1 + scale_color_manual(values = c("#0072B2", "#D55E00"))
g1 <- g1 + geom_hline(yintercept=0, color = "red", 
	alpha = 0.5, linetype ="longdash")
g1 <- g1 + ggtitle("")
g1


g1 <- ggplot(data = subset(all.coef, type.num %in% set1))
g1 <- g1 + geom_point(aes(x = period, y = beta), 
					  color = "orange2", size = 2)
g1 <- g1 + geom_line(aes(x = period, y = beta, color = group), 
					 alpha = .5)
g1 <- g1 + geom_errorbar(aes(x = period, y = beta, color = group,
						ymin = beta-1.96*SE, ymax = beta+1.96*SE), 
						size = .5, width = .5, alpha = .8)
g1 <- g1 + facet_grid(group ~ type)
g1 <- g1 + scale_color_manual(values=c("#0072B2","#D55E00"), guide="none")
g1 <- g1 + geom_hline(yintercept=0, color = "red", 
	alpha = 0.5, linetype ="longdash")
g1 <- g1 + ggtitle("")
g1
