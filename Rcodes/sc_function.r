#'
#' Function to perform sparse coding
#' 
#' @author Richard Li
#'
#' @param
#' @param
#' @param
#' @param
#' @param
#'
#'
#' @example

sc_run_unsup <- function(mdata, dict, Nsim = 4000, Burn = 0, Thin = 1, 
						 a = 10, b = 1, c = 1, d = 1, epsilon = 1000, 
						jpar = "-Xms40g", seed = 1, nSample = NULL, 
						libDir = NULL){
	require(rJava)
	# parse data
	motif <- mdata[, 1:120]
	Y <- mdata[, 121]
	T <- mdata[, 122]
	# remove nodes already signed-up from modeling
	toRemove <- which(Y == -1)
	motif <- motif[-toRemove, ]
	Y <- Y[-toRemove]
	T <- T[-toRemove]


	# sample data if needed
	set.seed(seed)
	if(!is.null(nSample)){
		sample <- sample(seq(1, length(Y)), size = nSample)
	}else{
		sample <- 1:length(Y)
	}
	
	# Java set up
	# setwd("../out/production/NetMotifs")
	options( java.parameters = jpar )
	.jinit(".")
	if(!is.null(libDir)){
		.jaddClassPath(dir(libDir, full.names = TRUE))	
	}	# .jclassPath()

	# parameters for java codes
	obj = .jnew("model/SparseCoding")
	motif.j <- .jarray(as.matrix(motif[sample, ]), dispatch=TRUE)
	dict.j <- .jarray(as.matrix(dict), dispatch = TRUE)
	
	T <- as.integer(Nsim)
	thin <- as.integer(Thin)
	burn <- as.integer(Burn)
	seed <- as.integer(seed)
	# # prior parameters
	# a <- 10
	# b <- 1
	# # uniform prior on gamma
	# c <- 1
	# d <- 1
	# epsilon <- 1000
	res <- .jcall(obj, "[[D", "main", 
			a,b,c,d,epsilon, 
			T, thin, burn, seed, motif.j, dict.j) 

	alpha = t(sapply(res, .jevalArray))
	return(alpha)
}

#'
#' Function to perform sparse coding
#' 
#' @author Richard Li
#'
#' @param
#' @param
#' @param
#' @param
#' @param
#'
#'
#' @example

sc_run_sup <- function(mdata, dict, Nsim = 4000, Burn = 0, Thin = 1, 
						 a = 10, b = 1, c = 1, d = 1, epsilon = 1000, 
						 tau_a = 5, tau_b = 1, stepHM = 0.1, 
						 betaFileOut = "betaFitted.txt",
						jpar = "-Xms40g", seed = 1, nSample = NULL, 
						libDir = NULL){
	require(rJava)
	# parse data
	motif <- mdata[, 1:120]
	Y <- mdata[, 121]
	T <- mdata[, 122]
	# remove nodes already signed-up from modeling
	toRemove <- which(Y == -1)
	motif <- motif[-toRemove, ]
	Y <- Y[-toRemove]
	T <- T[-toRemove]

	# sample data if needed
	set.seed(seed)
	if(!is.null(nSample)){
		sample <- sample(seq(1, length(Y)), size = nSample)
	}else{
		sample <- 1:length(Y)
	}
	
	# Java set up
	options( java.parameters = jpar )
	.jinit(".")
	if(!is.null(libDir)){
		.jaddClassPath(dir(libDir, full.names = TRUE))	
	}
	.jclassPath()

	# parameters for java codes
	obj <- .jnew("model/SupervisedSparseCoding")
	motif.j <- .jarray(as.matrix(motif[sample, ]), dispatch=TRUE)
	dict.j <- .jarray(as.matrix(dict), dispatch = TRUE)
	Y.j <- .jarray(as.integer(Y[sample]), dispatch = TRUE)
	
	T <- as.integer(Nsim)
	thin <- as.integer(Thin)
	burn <- as.integer(Burn)
	seed <- as.integer(seed)
	# # prior parameters
	# a <- 10
	# b <- 1
	# # uniform prior on gamma
	# c <- 1
	# d <- 1
	# epsilon <- 1000
	# # hyper prior on beta
	# tau_a <- 5
	# tau_b <- 1
	# # HM step size
	# stepHM = 0.1
	res <- .jcall(obj, "[[D", "main", 
			a,b,c,d,epsilon, 
			T, thin, burn, seed, 
			motif.j, dict.j, Y.j, tau_a, tau_b, stepHM, betaFileOut) 
	alpha <- t(sapply(res, .jevalArray))
	beta.fit <- read.table(betaFileOut, sep = ",", colClasses="numeric")
	return(list(alpha, beta.fit))
}





