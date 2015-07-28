# process result to produce plots
load("~/rjava_space/Gibbs/24dict-a10-eps1000.Rda")
beta <- scdata$beta
alpha <- scdata$alpha
summary(alpha)
summary(beta)

print(dim(beta))
beta <- beta[2000:3999, ]
# dict <- getdict()
colnames(beta) <- c("intercept", rownames(dict))


beta.monitor <- t(apply(beta, 2, function(x){return(quantile(x, 
	probs = c(0.05, 0.25, 0.5, 0.75, 0.95)))}))

colnames(beta.monitor)<- c("low5", "low25", "median", "up25", "up5")
order <- seq(dim(beta.monitor)[1], 1, -1)
beta.monitor <- data.frame(cbind(beta.monitor, order))
library(ggplot2)

pdf("~/beta_summary_v4_1.pdf", height = 5, width = 8)
this.monitor <- beta.monitor[(1:11), ]
this.order <- order[1:11]
g <- ggplot(this.monitor, aes(x = median, y = order))
g <- g + geom_segment(aes(x = low5, y = this.order, xend = up5, yend = this.order), 
			color = "darkslateblue")
g <- g + geom_segment(aes(x = low25, y = this.order, xend = up25, yend = this.order), 
			size = 1.5, color = "royalblue4")
g <- g + geom_point(color = "orange", size = 3)
g <- g + theme(panel.grid.minor.y = element_blank(), 
			   panel.grid.major.y = element_line(),
			   axis.text.y=element_blank(), 
			   axis.ticks.y=element_blank() )
g <- g + xlab("beta") + ylab("") 
g <- g + geom_vline(xintercept = 0, 
	color = "red", alpha = 0.5, linetype = "longdash")
g 
dev.off()

pdf("~/beta_summary_v4_2.pdf", height = 8, width = 8)
this.monitor <- beta.monitor[-(1:11), ]
this.order <- order[-(1:11)]
g <- ggplot(this.monitor, aes(x = median, y = this.order))
g <- g + geom_segment(aes(x = low5, y = this.order, xend = up5, yend = this.order), 
			color = "darkslateblue")
g <- g + geom_segment(aes(x = low25,y = this.order, xend = up25,yend = this.order), 
			size = 1.5, color = "royalblue4")
g <- g + geom_point(color = "orange", size = 3)
g <- g + theme(panel.grid.minor.y = element_blank(), 
			   panel.grid.major.y = element_line(),
			   # axis.text.y=element_blank(), 
			   axis.ticks.y=element_blank() )
g <- g + xlab("beta") + ylab("")
g <- g + scale_y_discrete(limit = this.order,
                     labels = rownames(this.monitor)) 
g <- g + geom_vline(xintercept = 0, 
	color = "red", alpha = 0.5, linetype = "longdash")
g 
dev.off()

