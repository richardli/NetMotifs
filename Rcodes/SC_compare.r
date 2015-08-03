##
## Script to compare unsupervised SC runs with raw logistic regression
##
##

# args <- commandArgs(trailingOnly = TRUE)
# name <- args[1]
library(ROCR)
name <- "g1p5"
load(paste("../data/motif_counts/Motifs_", name, ".rda", sep = ""))
load(paste("../data/unsup-runs/", name, "-unsup_short.rda", sep = ""))

alpha <- read.table(paste("../data/alphas/run_alpha_", name, ".txt", sep = ""), sep = ",")
source("Dict_function.r")
dict1 <- default_dict() 

motif <- motif[-which(motif$Y == -1), ]

dim(motif)
dim(alpha)
dim(coef)

ntrain <- (length(motif$Y) * .9)
train <- sample(1:length(motif$Y), size = ntrain)
test <- (1:length(motif$Y))[-train]

# SC results
data.sc <- data.frame(Y = motif$Y, alpha)
colnames(data.sc) <- c("Y", rownames(dict1))
fit <- glm(Y ~ ., data = data.sc[train, ], family = "binomial")
coef <- summary(fit)$coefficients 
rownames(coef) <- c("Intercept", rownames(dict1))
# pred <- as.matrix(cbind(1, alpha)) %*% coef[, 1, drop = F]
pred <- predict(fit, newdata = data.sc[test, ])

pred.sc <- prediction(pred, motif$Y[test])
perf.sc <- performance(pred.sc,"tpr","fpr")
auc.sc <- performance(pred.sc, "auc")@y.values[[1]]
auc.sc
plot(perf.sc)


# simple logistic results
data0<- data.frame(Y = motif$Y, motif[, 1:120])
colnames(data0) <- c("Y", colnames(motif[, 1:120]))
fit0 <- glm(Y ~ ., data = data0[train, ], family = "binomial")
coef0 <- summary(fit0)$coefficients 
pred <-predict(fit0, newdata = data0[test, ])

pred0 <- prediction(pred, motif$Y[test])
perf0 <- performance(pred0,"tpr","fpr")
auc0 <- performance(pred0, "auc")@y.values[[1]]
auc0
plot(perf0)

# simple grouped logistic results

gcounts <- as.matrix(motif[, 1:120]) %*% t(dict1)
data1 <- data.frame(Y = motif$Y, gcounts)
colnames(data1) <- c("Y", colnames(gcounts))
fit1 <- glm(Y ~ ., data = data1[train, ], family = "binomial")
coef1 <- coef(fit1)
pred <- predict(fit1, newdata = data1[test, ])

pred1 <- prediction(pred, motif$Y[test])
perf1 <- performance(pred1,"tpr","fpr")
auc1 <- performance(pred1, "auc")@y.values[[1]]
auc1
plot(perf1)


cbind(coef[, 1], coef1)


# fit0.cv <- cv.glmnet(x = as.matrix(motif[, 1:120]), 
# 					 y = motif$Y, 
# 					 family = "binomial",  
# 					 type.measure = "auc")
# fit0 <- glmnet(x = as.matrix(alpha), y = outcome, family = "binomial", 
# 					lambda = fit.cv$lambda.min)
# coef0 <- fit0$beta