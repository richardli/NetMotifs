load("~/Dropbox/network_evolution/motif-data/data_0701-0706_indep/motif6month-indep.rda")
load("~/rjava_space/data/motif6month-all.rda")
motif <- data$motif
Y <- data$Y
toremove <- which(Y == -1)
motif <- motif[-toremove, ]
Y <- Y[-toremove]
remove(data)
set.seed(1)
sample <- sample(seq(1, length(Y)), 2e4)
motif <- motif[sample, ]
Y <- Y[sample]

library(glmnet)
library(ROCR)


##################################################################
##################################################################
## LASSO with all motif counts
##################################################################
##################################################################

fitfunc <- function(data){
	fit <- cv.glmnet(x = as.matrix(data[, 1:(dim(data)[2]-1)]), y = data$Y, 
				family = "binomial", type.measure = "auc")
	return(fit)
}
predfunc <- function(fit, new){
	pre <- predict(fit, s = "lambda.min",
			   newx = as.matrix(new[, 1:(dim(new)[2]-1)]), type = "response")	
	return(pre)
}
fit <- cvfold(Y = Y, X = as.matrix(motif), 
			  fitfunc = fitfunc, predfunc = predfunc, 
			  fold = 10, seed = 1)

fit.all <- cv.glmnet(x = as.matrix(motif), y = Y, 
				family = "binomial", type.measure = "auc")





# train <- 1: trunc(length(Y) * .8)
# test <- seq(1, length(Y))[-train]
# fit <- cv.glmnet(x = as.matrix(motif[train,]), y = Y[train], 
# 				family = "binomial", type.measure = "auc")
# # train AUC
# pre <- predict(fit, s = "lambda.min",
# 			   newx = as.matrix(motif[train,]), type = "response")
# pred <- prediction(pre, Y[train])
# performance(pred, "auc")
# # test AUC
# pre <- predict(fit, s = "lambda.min",
# 			   newx = as.matrix(motif[test,]), type = "response")
# pred <- prediction(pre, Y[test])
# performance(pred, "auc")
# # ROC curve
# perf <- performance(pred, "tpr", "fpr")
# pdf("ROC.pdf")
# plot(perf, main = "ROC Curve", col = 2, lwd = 2)
# abline(a=0,b=1,lwd=2,lty=2,col="gray")
# dev.off()

##################################################################
##################################################################
## LASSO with all reciporical counts
##################################################################
##################################################################
rec <- c(18, 32, 33, 105, 119, 120)
fit2 <- cvfold(Y = Y, X = as.matrix(motif[, rec]), 
			  fitfunc = fitfunc, predfunc = predfunc, 
			  fold = 10, seed = 1)
