# read back delta from file
dir <- "../../MotifwithNeighbour/"
header <- c("0705") #, "0805week", "0809week")
index <- 0:7
n0 <- length(header)
n1 <- length(index)


# delta

for(ii in 1:n0){
	for(jj in 1:n1){
		delta.raw <- read.table(paste(dir, "fit/DeltaSampled_g", ii, "p", jj, ".txt", sep = ""), sep = ",")
		M <- dim(delta.raw)[2]
		K <- dim(delta.raw)[1] / M
		delta <- array(0, dim = c(K, M, M))

		k <- 1
		j <- 1
		for(i in 1:dim(delta.raw)[1]){
			delta[k, j, ] <- as.numeric(delta.raw[i, ])
			j <- j + 1
			if(j == M + 1){
				j <- 1
				k <- k + 1
			}
		}

		dm <- apply(delta, c(2, 3), mean)

		dmin <- min(dm)
		dmax <- max(dm)
		dm0 <- dm * 0
		topPos <- quantile(dm[dm>0], 0.95)
		topNeg <- quantile(dm[dm<0], 0.05)
		dm0[which(dm > topPos)] <- 1
		dm0[which(dm < topNeg)] <- -1
		
		pdf(paste(dir, "figures/delta_mean_g", ii, "p", jj, ".pdf", sep = ""), height = 100, width = 100)
		image(1:ncol(dm), 1:nrow(dm), t(dm), col = heat.colors(12), zlim=c(dmin,dmax))
		dev.off()
		
		pdf(paste(dir, "figures/delta_top_g", ii, "p", jj, ".pdf", sep = ""), height = 100, width = 100)
		image(1:ncol(dm), 1:nrow(dm), t(dm0), col = c("red", "white", "green"), zlim=c(-1,1))
		dev.off()

		pdf(paste(dir, "figures/delta_trace_g", ii, "p", jj, ".pdf", sep = ""), height = 1000, width = 1000)
		par(mfrow = c(M,M))
		for(i in 1:M){
			for(j in 1:M){
				plot(1:K, delta[1:K, i, j], type = "l")
				abline(h = 0, lty = 2, col = "red")
			}
		}
		dev.off()

	}
}


# alpha
dict.names <- c(
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

all.coef  <- NULL
for(ii in 1:n0){
	for(jj in 1:n1){
		alpha.raw <- read.table(paste(dir, "fit/run_alpha_Sampled_g", ii, "p", jj, ".txt", sep = ""), sep = ",")
		load(paste(dir, "clean/Motifs_Sampled_g", ii, "p", jj, ".rda", sep=""))

		motif <- motiflist$motif
		ntrain <- (length(motif$Y) * 1)
		train <- sample(1:length(motif$Y), size = ntrain)
		test <- (1:length(motif$Y))[-train]

		# SC results
		data.sc <- data.frame(Y = motif$Y, alpha.raw)
		colnames(data.sc) <- c("Y", dict.names)

		fit <- glm(Y ~ ., data = data.sc[train, ], family = "binomial")
		coef <- summary(fit)$coefficients 
		rownames(coef) <- c("Intercept", dict.names)

		all.coef <- rbind(all.coef, cbind(coef, 1:dim(coef)[1], ii, jj))
	}
}


all.coef <- data.frame(all.coef, 
				 type = rep(c("Intercept", dict.names), n0*n1))
colnames(all.coef)[1:7] <- c("beta", "SE", "z.value", "pval", "type.num", "period", "week")

range.levels <- c("May-Jun, 2007", "May-Jun, 2008", "Sept-Oct, 2008")
all.coef$period[which(all.coef$period== 1)] <- range.levels[1]
all.coef$period[which(all.coef$period == 2)] <- range.levels[2]
all.coef$period[which(all.coef$period == 3)] <- range.levels[3]

all.coef$type <- factor(all.coef$type, levels = c("Intercept", dict.names))
save(all.coef, file = paste(dir, "fit/run_alpha_sampled_regression_8kall.rda", sep = ""))

set1 <- 4:13
set2 <- 14:23
set3 <- 20:26

library(ggplot2)
# same plot
pdf(paste(dir, "figures/unsup-fig1.pdf", sep=""), width = 12, height = 5.5)
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


pdf(paste(dir, "figures/unsup-fig2.pdf", sep=""), width = 15, height = 5.5)
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



pdf(paste(dir, "figures/unsup-fig3.pdf", sep=""), width = 15, height = 5.5)
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

set4 <- set1[c(1,4,5,8)]
pdf(paste(dir, "figures/unsup-fig4.pdf", sep=""), width = 8, height = 5)
g1 <- ggplot(data = subset(all.coef, type.num %in% set4))
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
g1 + guides(colour=FALSE)
dev.off()

