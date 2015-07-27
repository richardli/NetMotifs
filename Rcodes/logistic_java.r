x = matrix(rnorm(2000*100), 2000, 100)
beta0 <- -3
beta = c(rep(0, 10), rep(1, 20), seq(-1, -10), rep(0, 60))
#beta <- rnorm(100)
prob <- beta0 + x %*% beta
Y <- sign(prob)
Y[Y < 1] <- 0
data <- cbind(Y, x)
write.table(data, file = "logdata_temp.txt", row.names = F, col.names = F, sep = ",")

fit <- glm(Y~x, family ="binomial")
summary(fit)
plot(coef(fit)[-1] / (coef(fit)[1]), beta/beta0)

coef2 <- read.table("log_temp.txt")
beta2 = as.numeric(as.character(coef2[2:101,1]))
beta20 = as.numeric(as.character(coef2[1,1]))
betaj <- beta2 / beta20
pdf("~/test2.pdf")
plot(betaj, beta/beta0)
dev.off()

# setwd("~/rjava_space/Gibbs/src/")
# library(rJava)
# #library( "RWeka" )
# options( java.parameters = "-Xmx5g" )
# .jinit(".")
# .jaddClassPath(dir("~/rjava_space/library/", 
# 		full.names = TRUE))
# .jclassPath()

# obj = .jnew("LogisticRegression")
# x.j <- .jarray(as.matrix(x), dispatch=TRUE)
# y.j <- .jarray(as.vector(Y), dispatch = TRUE)
# N <- as.integer(dim(x)[1])
# M <- as.integer(dim(x)[2])
# # prior parameters
# res <- .jcall(obj, "[D", "main", 
# 		N, M, x.j, y.j) 
# alpha = t(sapply(res, .jevalArray))