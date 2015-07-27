cvfold <- function(Y, X, fitfunc, predfunc, fold, seed){
		size <- trunc(length(Y)/fold)
		auc.sum <- 0
		auc.sum.train <- 0
		set.seed(seed)
		rand <- sample(1:length(Y), length(Y))
		Y <- Y[rand]
		X <- X[rand, ]
		for(i in 1:fold){
			test <- seq(1 + (i-1) * size, size * i)
			if(i == fold) test <- seq(1 + (i-1) * size, length(Y))
			train <- seq(1, length(Y))[-test]

			data <- data.frame(cbind(as.matrix(X), Y))
			fit <- fitfunc(data[train, ])
			#print(which(coef(fit)!=0))
			# fit <- glm(Y~., data = data[train, ], family = "binomial")
			# train
			#pre <- predict(fit, newdata = data[train, ], type = "response")
			pre <- predfunc(fit, data[train, ])
			pred <- prediction(pre, data$Y[train])
			auc.sum.train <- auc.sum.train + performance(pred, "auc")@y.values[[1]]
			#print(auc.sum.train/i)
			#perf <- performance(pred, "tpr", "fpr")

			# test
			#pre <- predict(fit, newdata = data[test, ], type = "response")
			pre <- predfunc(fit, data[test, ])
			pred <- prediction(pre, data$Y[test])
			auc.sum <- auc.sum + performance(pred, "auc")@y.values[[1]]
			#perf <- performance(pred, "tpr", "fpr")
			#outall[[i]] <- list(auc = auc, perf = perf)
		}
		return(list(auc.train = auc.sum.train / fold, 
					auc.test = auc.sum / fold))
}



normType = 0
list <- c(10, 20, 40)
outall <- vector("list", length(list))
for(i in 1:length(list)){
	# load(paste("aucData-", normType, "k", list[i], ".rda", sep = ""))
	# outall[[i]] <- out
	# print(paste("normType", normType, "K", list[i], "AUC", out$auc, sep = "  -  "))

	load(paste("~/rjava_space/Gibbs/KmeanWP/alpha-", normType, "k", list[i], ".Rda", sep = ""))
	alpha <- scdata$alpha
	#alpha[alpha < 0.01] <- 0
	Y <- scdata$Y
	fitfunc <- function(data){
		fit <- glm(Y~., data = data, family = "binomial")
		return(fit)
	}
	predfunc <- function(fit, new){
		return(predict(fit, newdata = new[, 1:(dim(new)[2]-1)], type = "response"))
	}
	fit <- cvfold(Y = Y, X = alpha, fitfunc = fitfunc, predfunc = predfunc, 
				  fold = 10, seed = 1)
	print(paste("Train: normType", normType, "K", 
		list[i], "AUC", fit$auc.train, sep = "  -  "))
	print(paste("Test: normType", normType, "K", 
		list[i], "AUC", fit$auc.test, sep = "  -  "))	
}

#####################################################################
#####################################################################
#####################################################################
## fit all data to get coefficients
#####################################################################
#####################################################################
#####################################################################
load(paste("~/rjava_space/Gibbs/KmeanWP/WP-", normType, "k", list[i], ".RData", sep = ""))
data <- data.frame(cbind(as.matrix(alpha), Y))
fit <- glm(Y~., data = data, family = "binomial")
coef <- coef(fit)[-1]
pval <- summary(fit)$coefficients[,4]
pval <- pval[-1]

which.sig <- which(pval < 0.05)
for(i in 1:length(which.sig)){
	which.motif <- which(dict[which.sig[i], ] > 0)
	freq <- dict[which.sig[i], which.motif]
	pdf(paste("samplefit/samplefit-coef", coef[which.sig[i]], ".pdf", sep = ""), 
				height = 10, width = 10)
	motif.plot(which.motif, motif.label = freq, mfrow = c(5, 5), t.size = 2)
	dev.off()
}

#fit.lasso <- cv.glmnet(x = as.matrix(alpha), y = Y, 
#				family = "binomial", type.measure = "auc")

# pdf(paste("norm", normType, ".pdf", sep = ""))
# plot(outall[[1]]$perf, main = "ROC Curve", col = 2, lwd = 2)
# for(i in 2:length(outall)){
# 	lines(outall[[i]]$perf@x.values[[1]], outall[[i]]$perf@y.values[[1]], 
# 		col = 1+i, lwd = 2)	
# }
# legend("bottomright", col = seq(2, length(outall)+1), legend = list)
# abline(a=0,b=1,lwd=2,lty=2,col="gray")
# dev.off()