## on linux, run "R CMD javareconf -e" first somehow?
## /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/javac -cp ~/rjava_space/ParallelColt/parallelcolt-0.9.4/ *.java math/*.java


# load("~/Dropbox/network_evolution/motif-data/data_0701-0706_indep/motif6month-indep.rda")
# load("~/rjava_space/data/motif6month-indep.rda")
args <- commandArgs(trailingOnly = TRUE)
normType = as.integer(args[1])
ncenter = as.integer(args[2])
Sup.indicator = as.integer(args[3])
print(Sup.indicator)

# source("~/rjava_space/Rcodes/get_dict.r")
# load("~/rjava_space/data/weekly/0501week0.rda")

source("rjava_space/Rcodes/get_dict.r")
load("rjava_space/data/weekly/0501week0.rda")

motif <- data$motif
Y <- data$Y
toremove <- which(Y == -1)
motif <- motif[-toremove, ]
Y <- Y[-toremove]
remove(data)

D.construct <- getdict()
D.construct <- D.construct * apply(motif, 2, function(x){return(quantile(x[which(x >0)], .5))})
# remove dyads
dict <- D.construct[-c(1,2), ]

# workspace <- paste("~/rjava_space/Gibbs/Dec28/WP-", normType, "k", ncenter, ".RData", sep = "")
# ###############################################################################
# if(file.exists(workspace)){
# 	print("load existing Kmean centers")
#     load(workspace)

# 	## update the constructed dicts
#     dict[1:12, ] <- D.construct[1:12, ]
	
# }else{
# 	#############################################
# 	#############################################
# 	set.seed(1)
	
# 	# function to remove NA and infinity
# 	clean <- function(x){
# 		x <- as.matrix(x)
# 		x[is.na(x)] <- 0
# 		x <- as.matrix(x)
# 		x[is.infinite(x)] <- 0
# 		return(as.matrix(x))
# 	}
# 	if(normType == 1){
# 		# norm by motif type
# 		motif.norm <- motif/apply(motif, 2, max)
# 		motif.norm <- clean(motif.norm)	
# 	}else if(normType == 2){
# 		# norm by person
# 		motif.norm <- motif/apply(motif, 1, sum)
# 		motif.norm <- clean(motif.norm)
# 	}else if(normType == 3){
# 		# norm by person then type
# 		motif.norm <- motif/apply(motif, 1, sum)
# 		motif.norm <- clean(motif.norm)
# 		motif.norm <- motif.norm/apply(motif.norm, 2, max)
# 		motif.norm <- clean(motif.norm)
# 	}else{
# 		motif.norm <- motif
# 	}

# 	fit <- kmeans(motif.norm, center = ncenter, iter.max = 1000)

# 	find.nearest <- function(fit, x,  k = 1){
# 		x <- as.matrix(x)
# 		N <- dim(x)[1]
# 		M <- dim(x)[2]
# 		C <- length(fit$size)
# 		out <- NULL
# 		for(i in 1:C){
# 			center <- fit$centers[i, ]
# 			dist <- apply(x, 1, function(x){(x-center)^2})
# 			dist <- apply(t(dist), 1, sum)
# 			which <- order(dist)[1:k]
# 			out <- c(out, which)
# 		}
# 		return(out)
# 	}

# 	dict <- find.nearest(fit, motif.norm)


# 	length(unique(dict))
# 	as.vector(apply(motif[dict, ], 2, min))
# 	as.vector(apply(motif[dict, ], 2, max))

# 	# since it's more conventient to load as double
# 	dict <-  as.matrix(motif[dict, ])
# 	dict <- rbind(D.construct[1:12, ], dict)
# 	dict <- round(dict, 1)
# 	save.image(workspace)
# }
#################################################################
#################################################################
# load("~/rjava_space/data/motif4.rda")
# dict4 <- motif4$motif4.all
# #dict <- dict4
# dict <- rbind(dict, dict4[sample(1:dim(dict4)[1], 20), ])
# round(apply(dict, 2, max), 2)
#################################################################
#################################################################
# setwd("~/rjava_space/Gibbs/src/")
setwd("/data/rwanda_anon/richardli/rjava_space/Gibbs/src/")
library(rJava)
#library( "RWeka" )
options( java.parameters = "-Xms40g" )

.jinit(".")
# .jaddClassPath(dir("~/rjava_space/library/", 
.jaddClassPath(dir("/data/rwanda_anon/richardli/rjava_space/library/", 
		full.names = TRUE))
.jclassPath()
set.seed(2)
# sample <- sample(seq(1, length(Y)), size = 8000)
sample <- 1:trunc(length(Y)/2)
table(Y[sample])
##################################################################
## Unsupervised SC
###################################################################
if(Sup.indicator == 0){
	obj = .jnew("SparseCoding")
	motif.j <- .jarray(as.matrix(motif[sample, ]), dispatch=TRUE)
	dict.j <- .jarray(as.matrix(dict), dispatch = TRUE)

	T <- as.integer(4000)
	thin <- as.integer(1)
	burn <- as.integer(0)
	seed <- as.integer(1)
	# prior parameters
	a <- 10
	b <- 1
	# uniform prior on gamma
	c <- 1
	d <- 1
	epsilon <- 1000
	res <- .jcall(obj, "[[D", "main", 
			a,b,c,d,epsilon, 
			T, thin, burn, seed, motif.j, dict.j) 


	alpha = t(sapply(res, .jevalArray))
	scdata <- list(alpha = alpha, Y = Y[sample])
	fit <- glm(scdata$Y ~ scdata$alpha, family = "binomial")
	save(scdata, file =  paste("~/rjava_space/Gibbs/Jan04/Unsupervised-", normType, "k", ncenter, ".Rda", sep = ""))
	# alpha <- array(0, dim = c(N, P, T-burn))
	# for(i in 1:(T-burn)){
	# 	alpha[,,i] = .jevalArray(res[[i]], simplify = TRUE)	
	# }
}

##################################################################
## Supervised SC
###################################################################
if(Sup.indicator == 1){
	obj = .jnew("SupervisedSparseCoding")
	motif.j <- .jarray(as.matrix(motif[sample, ]), dispatch=TRUE)
	dict.j <- .jarray(as.matrix(dict), dispatch = TRUE)
	Y.j <- .jarray(as.integer(Y[sample]), dispatch = TRUE)

	T <- as.integer(4000)
	thin <- as.integer(1)
	burn <- as.integer(0)
	seed <- as.integer(1)
	# prior parameters
	a <- 10
	b <- 1
	# uniform prior on gamma
	c <- 1
	d <- 1
	epsilon <- 1000
	# hyper prior on beta
	tau_a <- 5
	tau_b <- 1
	# HM step size
	stepHM = 0.1
	# betapath = paste("Beta-normType",normType,"k-", ncenter, ".txt", sep = "")
	betapath = "week0.txt"
	mes = "week 0, 23 dictionary"
	res <- .jcall(obj, "[[D", "main", 
			a,b,c,d,epsilon, 
			T, thin, burn, seed, 
			motif.j, dict.j, Y.j, tau_a, tau_b, stepHM, betapath) 
	alpha = t(sapply(res, .jevalArray))
	beta.fit <- read.table(betapath, sep = ",", colClasses="numeric")
	scdata <- list(alpha = alpha, beta = beta.fit)
	save(scdata, file =  paste("~/rjava_space/Gibbs/Jan04/Supervised-", normType, "k", ncenter, ".Rda", sep = ""))	
}	

##################################################################
##################################################################
## LASSO with sparse coding motif
##################################################################
##################################################################
# library(glmnet)
# library(ROCR)
# train <- 1: trunc(length(Y) * .8)
# test <- seq(1, length(Y))[-train]

# data <- data.frame(cbind(as.matrix(alpha), Y))
# fit <- glm(Y~., data = data[train, ], family = "binomial")
# pre <- predict(fit, newdata = data[test, ], type = "response")
# pred <- prediction(pre, data$Y[test])

# # fit <- cv.glmnet(x = as.matrix(alpha[train,]), y = Y[train], 
# # 				family = "binomial", type.measure = "auc")
# # pre <- predict(fit, s = "lambda.min",
# # 			   newx = as.matrix(alpha[test,]), type = "response")
# # pred <- prediction(pre, Y[test])

# auc <- performance(pred, "auc")@y.values[[1]]
# perf <- performance(pred, "tpr", "fpr")
# out <- list(auc = auc, perf = perf)
# save(out, file = paste("~/rjava_space/Gibbs/Dec28/aucData-", normType, "k", ncenter, ".rda", sep = ""))

# pdf(paste("~/rjava_space/Gibbs/Dec28/ROC-", normType, "k", ncenter, ".pdf", sep = ""))
# plot(perf, main = "ROC Curve", col = 2, lwd = 2)
# abline(a=0,b=1,lwd=2,lty=2,col="gray")
# dev.off()
