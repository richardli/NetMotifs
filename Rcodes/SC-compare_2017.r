##
## Script to compare unsupervised SC runs with raw logistic regression
##
##
remove(list = ls())
library(gamlr)
library(ROCR)
library(xtable)
##---------------------------------------##
dir <- "/data/rwanda_anon/richardli/MotifwithNeighbour/"
month <- "6m0801"
experiment <- 2
ntrain <- 5000
ntest <- 20000
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

# if(experiment==3) dict <- 1

current <- read.table(paste0(dir, month, ".txt"), nrows = ntrain * 10)
current <- current[current[, 2] != -1, ]
Y0 <- current[1:ntrain, 2]
X0 <- as.matrix(current[1:ntrain, 4:123])
fit <- glm(Y0~X0, family = "binomial")


Y1 <- current[(ntrain+1) : (ntrain+ntest), 2]
X1 <- as.matrix(current[(ntrain+1) : (ntrain+ntest), 4:123])
coef1 <- fit$coefficients
coef1[is.na(coef1)] <- 0
betax <- cbind(1, X1) %*% as.matrix(coef1)

pred.raw <- prediction(betax, Y1)
perf.raw <- performance(pred.raw,"tpr","fpr")
auc.raw <- performance(pred.raw, "auc")@y.values[[1]]
f1.raw <- max(performance(pred.raw, "f")@y.values[[1]], na.rm=T)
auc.raw
# plot(perf.raw)

##---------------------------------------##
source("/data/rwanda_anon/richardli/NetMotif/Rcodes/Dict_function.r")
D <- default_dict()
if(dict == 1){
	D <- D[1:12, ]
}
scale <- apply(X0, 2, function(x){
	m=mean(x[x!=0])
	if(is.nan(m)) m <- 1
	return(m)
})
D <- t(t(D) / scale)
XD0 <- X0 %*% t(D)
XD1 <- X1 %*% t(D)
fit2 <- glm(Y0~XD0, family = "binomial")
coef2 <- fit2$coefficients
coef2[is.na(coef2)] <- 0
betaxd <- cbind(1, XD1) %*% as.matrix(coef2)

pred.rawD <- prediction(betaxd, Y1)
perf.rawD <- performance(pred.rawD,"tpr","fpr")
auc.rawD <- performance(pred.rawD, "auc")@y.values[[1]]
f1.rawD <- max(performance(pred.rawD, "f")@y.values[[1]], na.rm=T)
auc.rawD
# plot(perf.rawd)


##---------------------------------------##
##---------------------------------------##
# dir <- "../data/Jun2016/"
subdir <- "Mar2017/"
name <- paste0(month, "testA", experiment)

beta <- read.table(paste0(dir, subdir, name, "_beta.txt"), sep = ",")
alpha0 <- read.table(paste0(dir, subdir, name, "_alpha.txt"), sep = ",")
y0 <- read.table(paste0(dir,subdir, name, "_y.txt"), sep = ",")

alpha1 <- read.table(paste0(dir, subdir, name, "_alpha_test.txt"), sep = ",")
y1 <- read.table(paste0(dir,subdir, name, "_y_test.txt"), sep = ",")

# without graph
betaNG <- read.table(paste0(dir, subdir, name, "NG_beta.txt"), sep = ",")
alpha0NG <- read.table(paste0(dir, subdir, name, "NG_alpha.txt"), sep = ",")
y0NG <- read.table(paste0(dir,subdir, name, "NG_y.txt"), sep = ",")
alpha1NG <- read.table(paste0(dir, subdir, name, "NG_alpha_test.txt"), sep = ",")
y1NG <- read.table(paste0(dir,subdir, name, "NG_y_test.txt"), sep = ",")

# just checking, should all be 0
print(sum(y0[,2] != Y0))
print(sum(y0NG[,2] != Y0))
print(sum(y1[,2] != Y1))
print(sum(y1NG[,2] != Y1))


# theta <- read.table(paste0(dir, subdir, name, "_theta.txt"), sep = ",")
# theta <- as.matrix(theta)
	library(lattice)
	pdf(paste0(dir, subdir, name, "theta.pdf"), width = 5, height = 5)
	col.l <- colorRampPalette(c('white', 'red'))
	colnames(theta) <- rownames(theta) <- NULL
	levelplot(theta, col.regions = gray(100:0/100))
	dev.off()


colnames(beta) <- c("intercept", rownames(D))
colnames(betaNG) <- c("intercept", rownames(D))
alpha0 <- as.matrix(alpha0)
alpha1 <- as.matrix(alpha1)
alpha0NG <- as.matrix(alpha0NG)
alpha1NG <- as.matrix(alpha1NG)

colnames(alpha0) <- colnames(alpha1) <- rownames(D)
colnames(alpha0NG) <- colnames(alpha1NG) <- rownames(D)
# fit3 <- glm(y0[,2]~as.matrix(alpha0), family = "binomial")
# beta3 <- matrix(fit3$coefficient, ncol=1)
# rownames(beta3) <- c("intercept", rownames(D))
# round(cbind(apply(beta, 2, median), beta3), 4)

beta_bayes <- apply(beta, 2, mean)
beta_bayesNG <- apply(betaNG, 2, mean)

# training AUC
uv <- cbind(1, alpha0) %*% beta_bayes
pred <- prediction(uv, Y0)
perf <- performance(pred,"tpr","fpr")
auc_train <- performance(pred, "auc")@y.values[[1]]
auc_train
# uv <- cbind(1, alpha0) %*% beta3
# pred <- prediction(uv, Y0)
# perf <- performance(pred,"tpr","fpr")
# auc_train_post <- performance(pred, "auc")@y.values[[1]]
# auc_train_post
# testing AUC
uv <- cbind(1, alpha1) %*% beta_bayes
pred <- prediction(uv, Y1)
perf_bayes <- performance(pred,"tpr","fpr")
auc_test<- performance(pred, "auc")@y.values[[1]]
f1_test <- max(performance(pred, "f")@y.values[[1]], na.rm=T)
auc_test
# uv <- cbind(1, alpha1) %*% beta3
# pred <- prediction(uv, y0[,2])
# perf <- performance(pred,"tpr","fpr")
# auc_test_post <- performance(pred, "auc")@y.values[[1]]
# auc_test_post

# training AUC NG
uv <- cbind(1, alpha0NG) %*% beta_bayesNG
pred <- prediction(uv, Y0)
perf <- performance(pred,"tpr","fpr")
auc_trainNG <- performance(pred, "auc")@y.values[[1]]
auc_trainNG
# testing AUC NG
uv <- cbind(1, alpha1NG) %*% beta_bayesNG
pred_NG <- prediction(uv, Y1)
perf_NG <- performance(pred_NG,"tpr","fpr")
auc_testNG <- performance(pred_NG, "auc")@y.values[[1]]
f1_testNG <- max(performance(pred_NG, "f")@y.values[[1]], na.rm=T)
auc_testNG

auc <- c(auc.raw, auc.rawD, auc_testNG, auc_test)
names(auc) <- c("raw", "rawD", "Bayes w/o adj", "Bayes")
print(auc) 
f1 <- c(f1.raw, f1.rawD, f1_testNG, f1_test)
names(f1) <- c("raw", "rawD", "Bayes w/o adj","Bayes")
print(f1) 

betas <- data.frame(rawD = fit2$coefficients, 
					NG = as.numeric(beta_bayesNG),
					Bayes = as.numeric(beta_bayes))
rownames(betas) <- names(beta_bayes)
print(round(betas, 2))



##################################################
## Plotting
tab1 <- cbind(auc, f1)
xtable(t(tab1))


pdf(paste0("~/Bitbucket-repos/NetMotif/figures/ROC", "Mar17", ".pdf"), width = 6, height = 6)
plot(perf_bayes, col = "red")
plot(perf_NG, add = TRUE, col = "orange")
plot(perf.raw, add = TRUE, col = "blue")
plot(perf.rawD, add = TRUE, col = "green")
legend("bottomright", c("LR with raw counts", "LR with projected counts", 
						"Proposed method without adjustment",
						"Proposed method with adjustment"),
		lty = c(1,1,1,1), col = c("blue", "green", "orange", "red"))
dev.off()

all.coef <- NULL
method <- c("LR", "Bayes Not Adjusted", "Bayes")
for(i in 1:dim(betas)[2]){
	dictnames <- names(beta_bayes)
	dictnames[1] <- "(Intercept)"
	if(i == 1){
		tmp <- summary(fit2)$coefficient
		coef <- matrix(NA, dim(tmp)[1], 3)
		coef[, 1] <- tmp[,1]
		coef[, 2] <- tmp[,1] - 1.96 * tmp[,2]
		coef[, 3] <- tmp[,1] + 1.96 * tmp[,2]
		dictnames <- gsub("XD0", "", rownames(tmp))
	}else if(i == 2){
		coef <- cbind(apply(beta, 2, mean), apply(beta,2,quantile,0.025),
					 apply(beta,2,quantile,0.975))
	}else if(i == 3){
	coef <- cbind(apply(betaNG, 2, mean), apply(betaNG,2,quantile,0.025),
					 apply(betaNG,2,quantile,0.975))
	}

	all.coef <- rbind(all.coef, 
					data.frame(coef,
					   type = dictnames,
					   type.num = 1:length(dictnames),
					   method = method[i]))
}
colnames(all.coef)[1:3] <- c("beta", "low", "high")
rownames(all.coef) <- NULL

library(ggplot2)
# same plot
library(jpeg) 
logoing_func<-function(logo, x, y, size, ratio){
  dims<-dim(logo)[1:2] #number of x-y pixels for the logo (aspect ratio)
  AR<-dims[1]/dims[2]
  par(usr=c(0, 1, 0, 1))
  rasterImage(logo, x-(size/2), y-(AR*size/2), x+(size/2)*ratio, y+(AR*size/2), interpolate=TRUE,  xpd = NA )
}

x <- NULL
for(i in 1:12){
	x[[i]] <- readJPEG(paste0("~/Bitbucket-repos/NetMotif/figures/dict", i, ".jpg"))
}


width <- 15
height <- 9
ratio <- height / (width + height)
pdf(paste0("~/Bitbucket-repos/NetMotif/figures/compare", "Mar17", ".pdf"), width = width, height = height)
my.dodge <- position_dodge(width = .2)
g1 <- ggplot(data = subset(all.coef, type.num %in% 2:13))
g1 <- g1 + geom_point(aes(x = method, y = beta, color = method), 
					  size = 2, position = my.dodge)
# g1 <- g1 + geom_line(aes(x = week, y = beta, color = method), 
					 # alpha = .5, position = my.dodge)
g1 <- g1 + geom_errorbar(aes(x = method, y = beta, color = method,
						ymin = low, ymax = high), 
						size = 1, width = .2, alpha = .8, position = my.dodge)
g1 <- g1 + facet_wrap(~type, ncol = 3, scales = "free")
# g1 <- g1 + scale_color_manual(values = c( "#0072B2", "#D55E00"))
g1 <- g1 + geom_hline(yintercept=0, color = "red", 
	alpha = 0.5, linetype ="longdash")
g1 <- g1 + ggtitle("")+ theme_bw()+ theme(panel.margin.y = unit(3, "lines"))
g1 <- g1 + theme(plot.margin = unit(c(1,1,2,1), "cm"))
# g1 <- g1 + scale_x_continuous(breaks = 1:10)
g1 <- g1 + xlab("")
g1 <- g1 + theme( panel.grid.minor = element_blank())
plot.new()
print(g1)
logoing_func(logo = x[[1]], x=0.15, y=0.8, size=0.06, ratio=ratio)
logoing_func(logo = x[[2]], x=0.45, y=0.8, size=0.06, ratio=ratio)
logoing_func(logo = x[[3]], x=0.74, y=0.8, size=0.06, ratio=ratio)

logoing_func(logo = x[[4]], x=0.15, y=0.52, size=0.06, ratio=ratio)
logoing_func(logo = x[[5]], x=0.45, y=0.52, size=0.06, ratio=ratio)
logoing_func(logo = x[[6]], x=0.74, y=0.52, size=0.06, ratio=ratio)

logoing_func(logo = x[[7]], x=0.15, y=0.24, size=0.06, ratio=ratio)
logoing_func(logo = x[[8]], x=0.45, y=0.24, size=0.06, ratio=ratio)
logoing_func(logo = x[[9]], x=0.74, y=0.24, size=0.06, ratio=ratio)

logoing_func(logo = x[[10]], x=0.15, y=-0.04, size=0.06, ratio=ratio)
logoing_func(logo = x[[11]], x=0.45, y=-0.04, size=0.06, ratio=ratio)
logoing_func(logo = x[[12]], x=0.74, y=-0.04, size=0.06, ratio=ratio)
dev.off()


