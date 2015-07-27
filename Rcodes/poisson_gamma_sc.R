load("~/Dropbox/network_evolution/motif-data/data_0701-0706_indep/motif6month-indep.rda")
motif <- data$motif
Y <- data$Y
toremove <- which(Y == -1)
motif <- motif[-toremove, ]
Y <- Y[-toremove]
fit1 <- kmeans(motif[which(Y == 1), ], center = 100, iter.max = 1000)
fit0 <- kmeans(motif[which(Y == 0), ], center = 20, iter.max = 1000)

dict <- rbind((fit0$centers), (fit1$centers))
round(apply(dict, 2, max), 2)

# gamma dist visual
#plot(seq(0, 10, len  = 100),dgamma(seq(0, 10, len  = 100), shape = 2, rate = 2), type = "l")
#abline(v = 1)
# beta dist visual
#plot(seq(0, 1, len  = 100),dbeta(seq(0, 1, len  = 100), 10, 10), type = "l")
motif <- motif[1:10000, ]
Y <- Y[1:10000]
N <- dim(motif)[1]
M <- dim(motif)[2]
P <- dim(dict)[1]
T <- 2000
burn <- 1500
# prior parameters
a <- 2
b <- 2
# uniform prior on gamma
c <- 1
d <- 1
epsilon <- 10
x <- motif
xsub <- array(0, dim = c(N, M, P))
alpha <- array(0, dim = c(N, P, T-burn))
z <- array(0, dim = c(N, P, T-burn))
gamma <- array(0, dim = c(P, T-burn))

# initialization
alpha.now <- array(rgamma(N*P, shape = a, rate = b), dim = c(N,P))
z.now <- array(0, dim = c(N,P))
gamma.now <- runif(P)

for(tt in 1:T){
	for(i in 1:N){
		for(j in 1:M){
			prob <- alpha.now[i, ] * dict[, j]
			if(sum(prob) == 0){
				xsub[i,j,] <- rep(0, P)
				next
			}  
			xsub[i,j,] <- rmultinom(1, size = x[i, j], prob = prob)	
		}
		
	}
	
	cat(".")
	for(i in 1:N){
		for(p in 1:P){
			term <- gamma.now[p] * b^a/gamma(a) * alpha.now[i,p]^(a-1)*exp(-b*alpha.now[i,p])
			z.now[i,p] <- rbinom(1, 1, 
				prob = term / (term + (1 - gamma.now[p])*exp(-epsilon * alpha.now[i,p])))
		}
	}
	cat(".")
	sumz <- apply(z.now, 2, sum)
	gamma.now <- rbeta(P, c + sumz, d + N - sumz)

	for(i in 1:N){
		for(p in 1:P){
			alpha.now[i,p] <- rgamma(1, shape = sum(xsub[i, , p]) + (a-1) * z.now[i,p] + 1, 
									   rate = sum(dict[p, ]) + b * z.now[i,p] + epsilon*(1-z.now[i,p]))
		}
	}
	cat("-")
	cat(round(mean(z.now), 6))
	if(tt > burn){
		alpha[,,tt-burn] <- alpha.now
		z[,,tt-burn] <- z.now
		gamma[,tt-burn] <- gamma.now
	}
}
###########################################################################
library(glmnet)
alpha.mean <- apply(alpha[,,((T-burn)/2+1):(T-burn)], c(1,2), mean)
fit.cv <- cv.glmnet(x = alpha.mean/apply(alpha.mean, 2, mean), y = Y, family = "binomial")
fit <- glmnet(x = alpha.mean/apply(alpha.mean, 2, mean), y = Y, family = "binomial", lambda = fit.cv$lambda.min)

# code to visulize
coef <- round(fit$beta,4)
for(i in which(coef != 0)){
	dict.entry <- round(dict[i, ], 0)
	non.empty <- which(dict.entry != 0)
	#print(length(non.empty))
	loading <- coef[i]
	pdf(paste("regs/Reg", i, ".pdf", sep = ""), height = 90, width = 90)
	motif.plot(non.empty, motif.label=dict.entry[non.empty], 
			   v.size = 80, e.size = 5, t.size = 20, t.pos = -0.5, 
			   mfrow = c(9,9))
	mtext(paste("Regression Coef =", loading), side=1, line=-20, outer=TRUE, cex=20)
	dev.off()
}

