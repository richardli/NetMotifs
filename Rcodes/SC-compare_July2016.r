##
## Script to compare unsupervised SC runs with raw logistic regression
##
##
remove(list = ls())
library(gamlr)
library(ROCR)
##---------------------------------------##
## cd /data/rwanda_anon/richardli/MotifwithNeighbour
dir <- "July2016/"
month <- "0705"
week <- 1
sample.size <- 1e4
out <- NULL
dict <- 1 # 0 is all, 1 is first 12 only

lasso <- function(X, Y, X1, Y1){
	fit0.cv <- cv.gamlr(x = X, 
					     y = Y, 
					 family = "binomial")
	beta <- coef(fit0.cv, select = "min")
	pred <- predict(fit0.cv, X1, select = "min")
	perf <- prediction(as.matrix(pred), Y1)
	auc <- performance(perf, "auc")@y.values[[1]]

	return(list(fit = fit0.cv, 
		beta = as.numeric(beta),
		perf = perf, 
		auc = auc))
}

for(week in 1:3){

	current <- read.table(paste0(month, "week", week, ".txt"), nrows = sample.size * 10)
	current <- current[current[, 2] != -1, ]
	Y0 <- current[1:sample.size, 2]
	X0 <- as.matrix(current[1:sample.size, 4:123])
	fit <- glm(Y0~X0, family = "binomial")

	test <- read.table(paste0(month, "week", week+1, ".txt"), nrows = sample.size * 10)
	test <- test[test[, 2] != -1, ]
	Y1 <- test[1:sample.size, 2]
	X1 <- as.matrix(test[1:sample.size, 4:123])
	coef1 <- fit$coefficients
	coef1[is.na(coef1)] <- 0
	betax <- cbind(1, X1) %*% as.matrix(coef1)

	pred.raw <- prediction(betax, Y1)
	perf.raw <- performance(pred.raw,"tpr","fpr")
	auc.raw <- performance(pred.raw, "auc")@y.values[[1]]
	auc.raw
	# plot(perf.raw)

	##---------------------------------------##
	source("../NetMotif/Rcodes/Dict_function.r")
	D <- default_dict()
	if(dict == 1){
		D <- D[1:12, ]
	}
	XD0 <- X0 %*% t(D)
	XD1 <- X1 %*% t(D)
	fit2 <- glm(Y0~XD0, family = "binomial")
	coef2 <- fit2$coefficients
	coef2[is.na(coef2)] <- 0
	betaxd <- cbind(1, XD1) %*% as.matrix(coef2)

	pred.rawD <- prediction(betaxd, Y1)
	perf.rawD <- performance(pred.rawD,"tpr","fpr")
	auc.rawD <- performance(pred.rawD, "auc")@y.values[[1]]
	auc.rawD
	# plot(perf.rawd)


	##---------------------------------------##
	##---------------------------------------##
	# dir <- "../data/Jun2016/"
	name <- paste0(month, "week", week, "_dict", dict, "_g2_A")
	nameNG <- paste0(month, "week", week, "_dict", dict, "_ng_A")

	beta <- read.table(paste0(dir, name, "_beta.txt"), sep = ",")
	alpha0 <- read.table(paste0(dir, name, "_alpha.txt"), sep = ",")
	alpha <- alpha0[(sample.size + 1) : (sample.size * 2), ]
	alpha0 <- alpha0[1:sample.size, ]

	y0 <- read.table(paste0(dir, name, "_y.txt"), sep = ",")
	y <- y0[(sample.size + 1) : (sample.size * 2),2]
	y0 <- y0[1:sample.size,2]

	# theta <- read.table(paste0(dir, name, "_theta.txt"), sep = ",")

	colnames(beta) <- c("intercept", rownames(D))
	summary(beta)
	# beta <- matrix(apply(beta, 2, median), nrow = 1)
	alpha0 <- as.matrix(alpha0)
	alpha <- as.matrix(alpha)
	# theta <- as.matrix(theta)

	D <- default_dict() 
	if(dict == 1){
		D <- D[1:12, ]
	}
	colnames(alpha) <- rownames(D)
	colnames(alpha0) <- rownames(D)
	fit4 <- glm(y0~as.matrix(alpha0), family = "binomial")
	beta <- matrix(fit4$coefficient, nrow=1)
	# names(beta.check) <- colnames(beta)
	colnames(beta) <- c("intercept", rownames(D))

	# library(lattice)
	# col.l <- colorRampPalette(c('white', 'red'))
	# colnames(theta) <- rownames(theta) <- NULL
	# levelplot(theta, col.regions = gray(100:0/100))


	uv <- cbind(1, alpha) %*% apply(beta, 2, median)
	pred <- prediction(uv, y)
	perf <- performance(pred,"tpr","fpr")
	aucG <- performance(pred, "auc")@y.values[[1]]
	aucG
	# aucG <- NA
	# beta <- NA
	
	###------------------------------------------###
	# aucNG <- NA
	alpha0NG <- read.table(paste0(dir, nameNG, "_alpha.txt"), sep = ",")
	alphaNG <- alpha0NG[(sample.size + 1) : (sample.size * 2), ]
	alpha0NG <- alpha0NG[1:sample.size, ]

	y0NG <- read.table(paste0(dir, nameNG, "_y.txt"), sep = ",")
	yNG <- y0NG[(sample.size + 1) : (sample.size * 2),2]
	y0NG <- y0NG[1:sample.size,2]
	# betaNG <- read.table(paste0(dir, nameNG, "_beta.txt"), sep = ",")
	fit3 <- glm(y0NG~as.matrix(alpha0NG), family = "binomial")
	betaNG <- matrix(fit3$coefficient, nrow = 1)
	colnames(betaNG) <- c("intercept", rownames(D))

	alpha0NG <- as.matrix(alpha0NG)
	alphaNG <- as.matrix(alphaNG)
	betaNG <- as.matrix(betaNG)

	uvNG <- cbind(1, alphaNG) %*% t(betaNG)
	predNG <- prediction(uvNG, yNG)
	perfNG <- performance(predNG,"tpr","fpr")
	aucNG <- performance(predNG, "auc")@y.values[[1]]
	aucNG

	
	#--------------------------------------------------#
	# load(paste0("../data/Jun2016/", month, "week", week, "raw.rda"))
	
	auc <- c(auc.raw, auc.rawD, aucNG, aucG)
	names(auc) <- c("raw", "rawD", "noGraph", "Graph")
	print(auc) 

	betas <- data.frame(rawD = fit2$coefficients, 
						noGraph = as.numeric(betaNG),
						Graph = as.numeric(beta))
	print(round(betas, 2))


	#---------------------------------------------# 
	# redo everything with lasso
	lasso.raw <- lasso(X0, Y0, X1, Y1) 
	lasso.D <- lasso(XD0, Y0, XD1, Y1)
	lasso.G <- lasso(alpha0, Y0, alpha, Y1)
	lasso.NG <- lasso(alpha0NG, Y0, alphaNG, Y1)

	out[[week + 1]] <- list(pred.raw = pred.raw, 
				  pred.rawD = pred.rawD, 
				  pred.NG = predNG,
				  pred.G = pred, 
				  beta.raw = fit$coefficients, 
				  betas = betas,
				  auc = auc, 
				  logistic = list(fit, fit2, fit3, fit4),
				  lasso = list(lasso.raw, lasso.D,
				  	lasso.NG, lasso.G))
}
save(out, file = paste0(dir, month, "week0-3", "_dict", dict, "_", sample.size, "out.rda"))
# plot(perf.raw)
# plot(perf.rawD, add = TRUE, col = "blue")
# plot(perf, add = TRUE, col = "red")



# fit0.cv <- cv.glmnet(x = as.matrix(motif[, 1:120]), 
# 					 y = motif$Y, 
# 					 family = "binomial",  
# 					 type.measure = "auc")
# fit0 <- glmnet(x = as.matrix(alpha), y = outcome, family = "binomial", 
# 					lambda = fit.cv$lambda.min)
# coef0 <- fit0$beta