remove(list = ls())
month <- "0705"
n.month <- 3

load(paste0("../data/Jun2016/", month, "week0-3_dict1_20000out.rda"))

source("Dict_function.r")
dict1 <- default_dict()[1:12, ] 

all.coef <- NULL
for(i in 1:length(out)){
	method <- c("Raw", "Not Network Adjusted", "Network Adjusted")
	for(j in 2:4){
		coef <- summary(out[[i]]$logistic[[j]])$coefficients 
		rownames(coef) <- c("Intercept", rownames(dict1))
		all.coef <- rbind(all.coef, 
			data.frame(coef, 1:dim(coef)[1], 
					   type = rownames(coef),
					   type.num = 1:dim(coef)[1],
					   period = n.month, week = i, method = method[j - 1]))
	}
}

colnames(all.coef)[1:4] <- c("beta", "SE", "z.value", "pval")


range.levels <- c("May-Jun, 2007", "Sept-Oct, 2008")
all.coef$period[which(all.coef$period== 1)] <- range.levels[1]
all.coef$period[which(all.coef$period == 2)] <- range.levels[2]
all.coef$type <- factor(all.coef$type, levels = c("Intercept", rownames(dict1)))
all.coef$method <- factor(all.coef$method, levels = method)

library(ggplot2)
# same plot
library(jpeg) 
logoing_func<-function(logo, x, y, size){
  dims<-dim(logo)[1:2] #number of x-y pixels for the logo (aspect ratio)
  AR<-dims[1]/dims[2]
  par(usr=c(0, 1, 0, 1))
  rasterImage(logo, x-(size/2), y-(AR*size/2), x+(size/2), y+(AR*size/2), interpolate=TRUE,  xpd = NA )
}

x <- NULL
for(i in 1:12){
	x[[i]] <- readJPEG(paste0("../figures/dict", i, ".jpg"))
}



pdf(paste0("../figures/coef_", month, ".pdf"), width = 10, height = 9)
my.dodge <- position_dodge(width = .2)

g1 <- ggplot(data = subset(all.coef, type.num %in% 2:13))
g1 <- g1 + geom_point(aes(x = week, y = beta, color = method), 
					  size = 2, position = my.dodge)
# g1 <- g1 + geom_line(aes(x = week, y = beta, color = method), 
					 # alpha = .5, position = my.dodge)
g1 <- g1 + geom_errorbar(aes(x = week, y = beta, color = method,
						ymin = beta-1.96*SE, ymax = beta+1.96*SE), 
						size = .5, width = .2, alpha = .8, position = my.dodge)
g1 <- g1 + facet_wrap(~type, ncol = 3, scales = "free")
g1 <- g1 + scale_color_manual(values = c("#009E73", "#0072B2", "#D55E00"))
g1 <- g1 + geom_hline(yintercept=0, color = "red", 
	alpha = 0.5, linetype ="longdash")
g1 <- g1 + ggtitle("")+ theme_bw()+ theme(panel.margin.y = unit(3, "lines"))
g1 <- g1 + theme(plot.margin = unit(c(1,1,2,1), "cm"))
g1 <- g1 + scale_x_continuous(breaks = 1:10)
g1 <- g1 + xlab("")
g1 <- g1 + theme( panel.grid.minor = element_blank())
plot.new()
print(g1)
logoing_func(logo = x[[1]], x=0.13, y=0.8, size=0.06)
logoing_func(logo = x[[2]], x=0.40, y=0.8, size=0.06)
logoing_func(logo = x[[3]], x=0.67, y=0.8, size=0.06)

logoing_func(logo = x[[4]], x=0.13, y=0.52, size=0.06)
logoing_func(logo = x[[5]], x=0.40, y=0.52, size=0.06)
logoing_func(logo = x[[6]], x=0.67, y=0.52, size=0.06)

logoing_func(logo = x[[7]], x=0.13, y=0.24, size=0.06)
logoing_func(logo = x[[8]], x=0.40, y=0.24, size=0.06)
logoing_func(logo = x[[9]], x=0.67, y=0.24, size=0.06)

logoing_func(logo = x[[10]], x=0.13, y=-0.04, size=0.06)
logoing_func(logo = x[[11]], x=0.40, y=-0.04, size=0.06)
logoing_func(logo = x[[12]], x=0.67, y=-0.04, size=0.06)
dev.off()


source("Motif_plot.r")
ind <- list(pos.index = which(out[[1]]$lasso[[1]]$beta > 0), 
		    neg.index = which(out[[1]]$lasso[[1]]$beta < 0))
motif.matplot(outdir = "../figures/", filename = "0705-week0-raw",
	ind = ind, label = TRUE)

ind <- list(pos.index = which(out[[2]]$lasso[[1]]$beta > 0), 
		    neg.index = which(out[[2]]$lasso[[1]]$beta < 0))
motif.matplot(outdir = "../figures/", filename = "0705-week1-raw",
	ind = ind, label = TRUE)
