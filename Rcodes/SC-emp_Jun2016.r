dir <- "~/0705week0/"
M0 <- read.table(paste0(dir, "0705No_week0.txt"))
M1 <- read.table(paste0(dir, "0705Yes_week0.txt"))
MM <- read.table(paste0(dir, "0705week0.txt"))
delta <- read.table(paste0(dir, "Delta2.txt"))

motif0 <- M0[, 4:123]
motif1 <- M1[, 4:123]
motif <- MM[, 4:123]

user <- which(MM[, 2] == -1)

newmat <- matrix(0, 120, 120)
delta.mat <- list(newmat, newmat, newmat, newmat)
for(i in 1:dim(delta)[1]){
	# (0, 0) -> 1, (0, 1) -> 2, 
	# (1, 0) -> 3, (1, 1) -> 4 
	k <- delta[i, 1] * 2 + delta[i, 2] + 1
	delta.mat[[k]][delta[i, 3], delta[i, 4]] <- 1
}

corr10 <- rep(NA, 120^2)
g10 <- rep(NA, 120^2)
count <- 1
for(i in 1:120){
	for(j in 1:120){
		corr10[count] <- cor(log(motif[user, i]+1), 
			 log(motif0[user, j]+1))
		g10[count] <- delta.mat[[4]][i, j]
		if(corr10[count] > 0.8){
			cat(i)
			cat(" ")
			cat(j)
			cat(" ")
			cat(g10[count])
			cat("\n")
		}
		count <- count + 1
	}	
}

corr11 <- rep(NA, 120^2)
g11 <- rep(NA, 120^2)
count <- 1
for(i in 1:120){
	for(j in 1:120){
		corr11[count] <- cor(log(motif[user, i]+1), 
			 log(motif1[user, j]+1))
		g11[count] <- delta.mat[[3]][i, j]
		if(corr0[count] > 0.8){
			cat(i)
			cat(" ")
			cat(j)
			cat(" ")
			cat(g11[count])
			cat("\n")
		}
		count <- count + 1
	}	
}


#####################################################
nonuser <- which(MM[, 2] != -1)
corr00 <- rep(NA, 120^2)
g00 <- rep(NA, 120^2)
count <- 1
for(i in 1:120){
	for(j in 1:120){
		corr00[count] <- cor(log(motif[nonuser, i]+1), 
			 log(motif0[nonuser, j]+1))
		g00[count] <- delta.mat[[1]][i, j]
		if(corr00[count] > 0.8){
			cat(i)
			cat(" ")
			cat(j)
			cat(" ")
			cat(g00[count])
			cat("\n")
		}
		count <- count + 1
	}	
}


corr01 <- rep(NA, 120^2)
g01 <- rep(NA, 120^2)
count <- 1
for(i in 1:120){
	for(j in 1:120){
		corr01[count] <- cor(log(motif[nonuser, i]+1), 
			 log(motif1[nonuser, j]+1))
		g01[count] <- delta.mat[[2]][i, j]
		if(corr01[count] > 0.8){
			cat(i)
			cat(" ")
			cat(j)
			cat(" ")
			cat(g01[count])
			cat("\n")
		}
		count <- count + 1
	}	
}


par(mfrow = c(2, 2))

hist(corr00[g00 == 0], 
	breaks = seq(-.1, 1, len = 50), xlim =  c(-0.1, 1), 
	xlab = "corr", main = "Corr(non-User, non-User neighbor)")
hist(corr0[g00 == 1],  col = "red",
	add = T, breaks = seq(-.1, 1, len = 50))

hist(corr01[g01 == 0], 
	breaks = seq(-.1, 1, len = 50), xlim =  c(-0.1, 1), 
	xlab = "corr", main = "Corr(non-User, User neighbor)")
hist(corr01[g01 == 1],  col = "red",
	add = T, breaks = seq(-.1, 1, len = 50))

hist(corr10[g10 == 0], 
	breaks = seq(-.1, 1, len = 50), xlim = c(-0.1, 1), 
	xlab = "corr", main = "Corr(User, non-User neighbor)")
hist(corr10[g10 == 1],  col = "red",
	add = T, breaks = seq(-.1, 1, len = 50))

hist(corr11[g11 == 0], 
	breaks = seq(-.1, 1, len = 50), xlim =  c(-0.1, 1), 
	xlab = "corr", main = "Corr(User, User neighbor)")
hist(corr11[g11 == 1],  col = "red",
	add = T, breaks = seq(-.1, 1, len = 50))



par(mfrow = c(2, 2))
hist(corr00[g00 == 1], col = "red",
	breaks = seq(-.1, 1, len = 50), xlim =  c(-0.1, 1), 
	xlab = "corr", main = "Corr(non-User, non-User neighbor)")
hist(corr01[g01 == 1], col = "red",
	breaks = seq(-.1, 1, len = 50), xlim =  c(-0.1, 1), 
	xlab = "corr", main = "Corr(non-User, User neighbor)")
hist(corr10[g10 == 1], col = "red",
	breaks = seq(-.1, 1, len = 50), xlim = c(-0.1, 1), 
	xlab = "corr", main = "Corr(User, non-User neighbor)")
hist(corr11[g11 == 1], col = "red",
	breaks = seq(-.1, 1, len = 50), xlim =  c(-0.1, 1), 
	xlab = "corr", main = "Corr(User, User neighbor)")


#####################################################

pdf(paste0(dir, "compare-user-with-non-user-neighbor.pdf", compress = TRUE), 
	width = 100, height = 100)
par(mar=c(1,1,1,1), mfrow = c(40, 40))
for(i in 1:120){
	for(j in 1:120){
		if(delta.mat[[3]][i, j] > 0){
			plot(jitter(log(motif[user, i] + 1)), 
		 		 jitter(log(motif0[user, j] + 1)), 
		 		 col = "#00000026", cex = 0.5, 
		 		 xlab = i, ylab = j)			
		}else{
			# plot.new()
		}
	}
	cat(".")
}
dev.off()



pdf(paste0(dir, "compare-user-with-user-neighbor.pdf"), 
	width = 100, height = 100, compress = TRUE)
par(mar=c(1,1,1,1), mfrow = c(40, 40))
for(i in 1:120){
	for(j in 1:120){
		if(delta.mat[[4]][i, j] > 0){
			plot(jitter(log(motif[user, i] + 1)), 
		 		 jitter(log(motif1[user, j] + 1)), 
		 		 col = "#00000026", cex = 0.5, 
		 		 xlab = i, ylab = j)			
		}else{
			# plot.new()
		}
	}
	cat(".")
}
dev.off()