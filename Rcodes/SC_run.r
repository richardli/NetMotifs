args <- commandArgs(trailingOnly = TRUE)
name <- args[1]
load(paste("../data/motif_counts/Motifs_", name, ".rda", sep = ""))

source("SC_function.r")
mdata <- motif
source("Dict_function.r")
dict1 <- default_dict() 
dict1 <- t(t(dict1) * apply(mdata[, 1:120], 2, function(x){median(x[x>0])}))
#dict2 <- kmeans_dict(K = 20, data = mdata[, 1:120], seed = 1)
#dict3 <- higher_dict(4)

setwd("../src")

# run unsupervised
alpha <- sc_run_unsup(mdata, dict1, Nsim = 5000, jpar = "-Xms10g", nSample = NULL, 
					libDir = "../library/")

# run supervised 
supfit <- sc_run_sup(mdata, dict1, Nsim = 1000, Burn = 500, 
						nSample = 5000,
						a = 10, b = 1, c = 1, d = 1, epsilon = 1000, 
						tau_a = 10, tau_b = 2, stepHM = 0.1, 
						betaFileOut = paste(name,"betaFitted.txt",sep=""),
						jpar = "-Xms10g", seed = 1,
 						libDir = "../library/")



# 
outcome <- mdata$Y[-which(mdata$Y == -1)]
# library(glmnet)
# fit.cv <- cv.glmnet(x = as.matrix(alpha), y = outcome, family = "binomial",  
# 					type.measure = "auc")
# fit <- glmnet(x = as.matrix(alpha), y = outcome, family = "binomial", 
# 					lambda = fit.cv$lambda.min)
# coef <- fit$beta

fit <- glm(outcome ~ as.matrix(alpha), family = "binomial")
summary(fit)
coef <- summary(fit)$coefficients 
rownames(coef) <- c("Intercept", rownames(dict1))

results <- list(alpha = alpha, outcome = outcome, coef = coef)
save(results, file = paste(name, "-unsup.rda", sep = ""))