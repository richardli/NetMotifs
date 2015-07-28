##
## script to reduce size
##
# study.name <- "M1"
# parnames <- fit@model_pars
# parnames <- parnames[-length(parnames)]
# pars <- vector("list", length(parnames))

# for(i in 1: length(parnames)){
# 	par <- parnames[i]
# 	pars[[i]] <- extract(fit, pars = par, inc_warmup = FALSE, permuted = FALSE)
# }
# names(pars) <- parnames
# save(pars, file = paste(dir.out, study.name, "-reduced.rda", sep = ""))


##
## Scripts to monitor theta
##
remove(list = ls())
model <- "M1b"
load(paste("~/Dropbox/network_evolution/motif-data/results_richard_NoDyad/", model, "/", model, "-reduced.rda", sep = ""))
theta <- pars$theta
if(length(names(theta)) > 0){if(names(theta) == "theta") theta <- theta$theta}
if(length(dim(theta)) == 2){
	theta.new <- array(0, dim = c(dim(theta)[1], 1, dim(theta)[2]))
	theta.new[, 1, ] <- theta
	theta <- theta.new
}
theta.m <- monitor(theta, probs = c(0.05, 0.25, 0.5, 0.75, 0.95))
#library(xtable)
#xtable(theta.m[, -c(4, 8)])

##
## Scripts to plot theta 
##

theta.ss <- as.matrix(theta.m)
colnames(theta.ss)[4:8] <- c("low5", "low25", "median", "up25", "up5")
order <- seq(dim(theta.ss)[1], 1, -1)
theta.ss <- cbind(theta.ss, order)
theta.ss <- data.frame(theta.ss)

pdf(paste("~/Dropbox/network_evolution/motif-data/presentation-figures/model-figures/", model, "-theta-summary.pdf", sep = ""), height = 5, width = 8)
#pdf("test.pdf" , height = 5, width = 8 )
g <- ggplot(theta.ss, aes(x = mean, y = order))
g <- g + geom_segment(aes(x = low5, y = order, xend = up5, yend = order), 
			color = "darkslateblue")
g <- g + geom_segment(aes(x = low25, y = order, xend = up25, yend = order), 
			size = 1.5, color = "royalblue4")
g <- g + geom_point(color = "orange", size = 3)
g <- g + theme(panel.grid.minor.y = element_blank(), 
			   panel.grid.major.y = element_line(),
			   axis.text.y=element_blank(), 
			   axis.ticks.y=element_blank() )
g <- g + xlab("theta") + ylab("")
g <- g + geom_vline(xintercept = 0, 
	color = "red", alpha = 0.5, linetype = "longdash")
g 
dev.off()

##
## Scripts to traceplot theta (slightly better)
##
nitr <- dim(theta)[1]
nchain <- dim(theta)[2]
ndim <- dim(theta)[3]
names <- c("both MM user\nIncomplete Triangle\n(Side)", 
			"both MM user\nIncomplete Triangle\n(Center)", "both MM user\nTriangle", 
			"MM user closer\nIncomplete Triangle", 
			"non-MM user closer\nIncomplete Triangle", 
			"One of Each\nIncomplete Triangle", "One of each\nTriangle",
			"both non-MM user\nIncomplete Triangle\n(Side)", 
			"Both non-MM user\nIncomplete Triangle\n(Center)", "both non-MM user\nTriangle")
Iteration <- rep(seq(1, nitr), nchain * ndim)
Chain <- rep(0, nchain * nitr * ndim)
for(i in 1: nchain) Chain[((i-1) * nitr * ndim + 1) : (i * nitr * ndim )] <- i

par.temp <- rep(0, nitr * ndim)
for(i in 1: ndim) par.temp[((i-1) * nitr + 1) : (i * nitr)] <- names[i]
Parameter <- rep(par.temp, nchain)

value <- rep(0, nchain * nitr * ndim)
for(i in 1:nchain){
	for(j in 1:ndim){
		value[ ((i-1)*nitr*ndim + (j-1)*nitr + 1): 
			   ((i-1)*nitr*ndim + (j-1)*nitr + nitr)] <- theta[,i,j]
	}
}

theta.s <- data.frame(Iteration = Iteration, 
						Chain = as.factor(Chain), 
						Parameter = factor(Parameter, levels = names), 
						value = value)

pdf(paste("~/Dropbox/network_evolution/motif-data/presentation-figures/model-figures/", model, "-thetagg.pdf", sep = ""), height = 10, width = 17)
g <- ggplot(data = theta.s, aes (x = Iteration, y = value, color = Chain))
g <- g + geom_line(alpha = 0.5)
g <- g + facet_grid(. ~ Parameter)
g <- g + theme(strip.text.x = element_text(size = 12, lineheight = 1.1, vjust = 0.5))
g + geom_abline(slope = 0, intercept = 0, linetype = "dashed", colour = "red")
dev.off()







