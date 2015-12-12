library(ROCR)
library(reshape)
library(ggplot2)
group <- 1:3
period <- 1:3
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
		name <- paste("run_beta_g", i, "p", j, "v2.txt", sep="")
		# name <- paste("g", i, "p", j, sep = "")
		# load beta
		beta <- read.table(paste("../data/beta-fit/", name, sep = ""), sep = ",")
		print(dim(beta))
		tmp1 <- apply(beta, 2, mean)
		tmp2 <- apply(beta, 2, sd)
		tmp3 <- apply(beta, 2, function(x){quantile(x, 0.025)})
		tmp4 <- apply(beta, 2, function(x){quantile(x, 0.975)})		
		tmp5 <- apply(beta, 2, function(x){quantile(x, 0.05)})
		tmp6 <- apply(beta, 2, function(x){quantile(x, 0.95)})
		coef <- cbind(tmp1, tmp2, tmp3, tmp4, tmp5, tmp6)
		rownames(coef) <- c("Intercept", rownames(dict1)[-c(1,2)])
		all.coef <- rbind(all.coef, cbind(coef, 1:dim(coef)[1], i, j))
		print("+")
	}
}


all.coef <- data.frame(all.coef, 
				 type = rep(c("Intercept", rownames(dict1)[-(1:2)]), 
				 			n1*n2))
colnames(all.coef)[1:9] <- c("beta", "SE", "low95", "high95", "low90", "high90", "type.num", "period", "week")

range.levels <- c("May-Jun, 2007", "May-Jun, 2008", "Sept-Oct, 2008")
all.coef$period[which(all.coef$period== 1)] <- range.levels[1]
all.coef$period[which(all.coef$period == 2)] <- range.levels[2]
all.coef$period[which(all.coef$period == 3)] <- range.levels[3]

all.coef$type <- factor(all.coef$type, levels = c("Intercept", rownames(dict1)))

set1 <- 2:11
set2 <- 12:21
set3 <- 18:24


# same plot
pdf("../figures/sup-fig1.pdf", width = 12, height = 5.5)
g1 <- ggplot(data = subset(all.coef, (type.num %in% set3)))
g1 <- g1 + geom_point(aes(x = week, y = beta), 
					  size = 2)
g1 <- g1 + geom_line(aes(x = week, y = beta, color = period), 
					 alpha = .5)
g1 <- g1 + geom_errorbar(aes(x = week, y = beta, color = period,
						ymin = low90, ymax = high90), 
						size = .5, width = .2, alpha = .8)
g1 <- g1 + facet_grid(~ type)
g1 <- g1 + scale_color_manual(values = c("#009E73", "#0072B2", "#D55E00"))
# g1 <- g1 + scale_color_manual(values = c("#009E73","#D55E00"))
g1 <- g1 + geom_hline(yintercept=0, color = "red", 
	alpha = 0.5, linetype ="longdash")
g1 <- g1 + ggtitle("")+ theme_bw()
g1 <- g1 + theme( panel.grid.minor = element_blank())
g1
dev.off()


pdf("../figures/sup-fig2.pdf", width = 15, height = 5.5)
g1 <- ggplot(data = subset(all.coef, (type.num %in% set1)))
g1 <- g1 + geom_point(aes(x = week, y = beta), 
					  size = 2)
g1 <- g1 + geom_line(aes(x = week, y = beta, color = period), 
					 alpha = .5)
g1 <- g1 + geom_errorbar(aes(x = week, y = beta, color = period,
						ymin = low90, ymax = high90), 
						size = .5, width = .2, alpha = .8)
g1 <- g1 + facet_grid(~ type)
g1 <- g1 + scale_color_manual(values = c("#009E73", "#0072B2", "#D55E00"))
# g1 <- g1 + scale_color_manual(values = c("#009E73","#D55E00"))
g1 <- g1 + geom_hline(yintercept=0, color = "red", 
	alpha = 0.5, linetype ="longdash")
g1 <- g1 + ggtitle("")+ theme_bw()
g1 <- g1 + theme( panel.grid.minor = element_blank())
g1
dev.off()

