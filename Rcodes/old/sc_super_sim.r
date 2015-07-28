#####################################################################################
#####################################################################################
M <- 120
N <- 2000
D <- 20

# simulate a dictionary (D * M)
max <- 50
dict.true <- matrix(round(runif(D*M, 0, max)), D, M)
dict.sparse <- matrix(rbinom(D*M, 1, 0.1), D, M)
dict.true <- dict.true * dict.sparse

dict.confuse <- matrix(round(runif(D*M, 0, max)), D, M)
dict.sparse.confuse <- matrix(rbinom(D*M, 1, 0.05), D, M)
dict.confuse <- dict.confuse * dict.sparse.confuse

# simulate alpha (N * D)
a <- 3
b <- 3
# use the same p for each motif for now
p <- 0.1
alpha.true <- matrix(rgamma(N*D, shape = a, rate = b), N, D)
alpha.sparse <- matrix(rbinom(N*D, 1, p), N, D)
alpha.true <- alpha.true * alpha.sparse

# simulate X
lambda <- as.vector(alpha.true %*% dict.true)

# add some binary constructed dict to confuse
X <- matrix(rpois(N*M, lambda), N, M)
source("~/rjava_space/Rcodes/get_dict.r")
D.construct <- getdict()
D.construct <- D.construct * apply(X, 2, function(x){
					y <- 1
					if(length(which(x > 0)) > 0){
						y <- quantile(x[which(x >0)], .5)	
					}
					return(y)})
dict.confuse <- rbind(D.construct, dict.confuse)

# simulate Y
beta0 <- -5
beta <- c(rep(5, 5), rep(-3, 5), rep(0.1, D-10))
prob <- beta0 + alpha.true %*% beta + rnorm(N, sd = 0.5)
prob <- 1/(1+exp(-1*prob))
Y <- rbinom(n = length(prob), size = 1, prob = prob)
# assume we know correct dictionary for now
dict.use <- dict.true
dict.use <- dict.confuse
# dict.use <- rbind(dict.true, dict.confuse)
# D <- D * 2

# find K means dictionary
# fit <- kmeans(X, center = D, iter.max = 1000)

# find.nearest <- function(fit, x,  k = 1){
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
# which <- find.nearest(fit, X)
# dict.use  <- round(X[which, ], 1)

#####################################################################################
# # #####################################################################################
# setwd("~/rjava_space/Gibbs/src/")
# library(rJava)
# #library( "RWeka" )
# options( java.parameters = "-Xmx5g" )

# .jinit(".")
# .jaddClassPath(dir("~/rjava_space/library/", 
# 		full.names = TRUE))
# .jclassPath()

# obj = .jnew("SparseCoding")
# motif.j <- .jarray(as.matrix(X), dispatch=TRUE)
# dict.j <- .jarray(as.matrix(dict.use), dispatch = TRUE)

# T <- as.integer(2000)
# thin <- as.integer(1)
# burn <- as.integer(1000)
# seed <- as.integer(1)
# # prior parameters
# a <- 2
# b <- 2
# # uniform prior on gamma
# c <- 1
# d <- 1
# epsilon <- 10
# res <- .jcall(obj, "[[D", "main", 
# 		a,b,c,d,epsilon, 
# 		T, thin, burn, seed, motif.j, dict.j) 
# alpha = t(sapply(res, .jevalArray))
#####################################################################################
#####################################################################################
setwd("~/rjava_space/Gibbs/src/")
library(rJava)
#library( "RWeka" )
options( java.parameters = "-Xmx5g" )

.jinit(".")
.jaddClassPath(dir("~/rjava_space/library/", 
		full.names = TRUE))
.jclassPath()

obj = .jnew("SupervisedSparseCoding")
motif.j <- .jarray(as.matrix(X), dispatch=TRUE)
dict.j <- .jarray(as.matrix(dict.use), dispatch = TRUE)
Y.j <- .jarray(as.integer(Y), dispatch = TRUE)

T <- as.integer(200)
thin <- as.integer(1)
burn <- as.integer(100)
seed <- as.integer(1)
# prior parameters
a <- 2
b <- 2
# uniform prior on gamma
c <- 1
d <- 1
epsilon <- 30
# hyper prior on beta
tau_a <- 5
tau_b <- 1
# HM step size
stepHM = 0.5
#double a, double b, double c, double d, double epsilon,
# int T, int thin, int burn, int seed, 
# int[][] motif, double[][] dict, int[] y, double tau_a, double tau_b, 
# double stepHM
res <- .jcall(obj, "[[D", "main", 
		a,b,c,d,epsilon, 
		T, thin, burn, seed, 
		motif.j, dict.j, Y.j, tau_a, tau_b, stepHM, "trace_beta_noconfuse_less.txt") 
alpha.fit <- t(sapply(res, .jevalArray))
beta.fit <- read.table("trace_beta_noconfuse_less.txt", sep = ",", colClasses="numeric")

# direct logistic regression
lr.true <- coef(glm(Y ~ alpha.true, family = "binomial"))
allbeta <- c(beta0, beta)
pdf("trace_beta_confuse.pdf")
par(mfrow = c(2,2))
for(i in 1:dim(beta.fit)[2]){
	plot(beta.fit[, i] , type = "l", main = paste("beta", i-1), 
		 xlab = "itr",
	     ylim = range(c(beta.fit[,i], allbeta[i], lr.true[i]), na.rm = TRUE))
	if(i > length(beta)){
		abline(h = 0, col = "red")
	}else{
		abline(h = lr.true[i], col = "blue")
		abline(h = allbeta[i], col = "red")		
	}
}
dev.off()

