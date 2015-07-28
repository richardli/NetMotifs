## Script to read multiple series of motif counts
## Created: 07-27-2015
## Last updated: 07-27-2015

# specify file directory and names
dir <- "../data/motif_counts/"
header <- c("0705week", "0805week", "0809week")
index <- 0:7

# get file names
n0 <- length(header)
n1 <- length(index)
names <- list(n0)
for(i in 1:n0){
	names[[i]] <- paste(dir, header[i], index, ".txt", sep = "")
}

# start reading data into list
# motif file structure
# Y, time, m1 - m121, inFreq, outFreq, sSize, rSize, mSize, nSize
# Note m1 - m121 are not ordered as we want
# call reformat function before saving

source("Motif_format.r")
allMotifs <- NULL
for(i in 1:n0){
	for(j in 1:n1){
		# read raw
		tmp <- read.table(names[[i]][j], sep = ",")
		# change order (now motif 1 to 120)
		motif <- changeOrder(tmp[, 3:123])
		# get other information
		Y <- tmp[, 1]
		T <- tmp[, 2]
		
		# get in to shape
		tmp2 <- cbind(motif, Y, T, tmp[, 124:129], i, j)

		print(table(Y)/length(Y))
		allMotifs <- rbind(allMotifs, tmp2)

		# also save individual files
		colnames(tmp2) <- c(colnames(motif), "Y", "T", 
							"inFreq", "outFreq", "sSize", "rSize", "mSize", "nSize", "Range", "Period")
		motif <- data.frame(tmp2)
		save(motif, file = paste("Motifs_g", i, "p", j, ".rda", sep=""))
	}
}
# more formating
colnames(allMotifs) <- c(colnames(motif), "Y", "T", 
	"inFreq", "outFreq", "sSize", "rSize", "mSize", "nSize", "Range", "Period")
allMotifs <- data.frame(allMotifs)

range.levels <- c("May-Jun, 2007", "May-Jun, 2008", "Sept-Oct, 2008")
allMotifs$Range[which(allMotifs$Range == 1)] <- range.levels[1]
allMotifs$Range[which(allMotifs$Range == 2)] <- range.levels[2]
allMotifs$Range[which(allMotifs$Range == 3)] <- range.levels[3]
allMotifs$Range <- factor(allMotifs$Range, levels = range.levels)
allMotifs$Period <- as.factor(allMotifs$Period)
save(allMotifs, file = "allMotifs.rda")

# calculate summary statistics
ranges <- rep(range.levels, each = n1)
periods <- rep(1:8, n0)
sMotifs <- data.frame(Range = ranges, Period = periods)

# function to calculate summary statistics for a period
my.summary <- function(allMotifs, range, period){
	tmp <- subset(allMotifs, (allMotifs$Range==range & allMotifs$Period==period))
	result <- rep(0, 120 * 3 + 2 + 4 + 3)
	names(result) <- c(paste("mtf.mean", 1:120), 
					   paste("mtf.mean.nz", 1:120), 
					   paste("mtf.per0", 1:120),
					   paste("mean", colnames(tmp)[123:128], sep = "."), 
					   "signed", "no_sign", "will_sign")

	counter <- 1
	# motif mean
	result[counter : (counter + 119)] <- apply(tmp[, 1:120], 2, mean)
	counter <- counter + 120
	# motif mean without zero
	result[counter : (counter + 119)] <- apply(tmp[, 1:120], 2, function(x){
		mean(x[x>0])})
	counter <- counter + 120
	# motif percentage zero
	result[counter : (counter + 119)] <- apply(tmp[, 1:120], 2, function(x){
		sum(x == 0)/length(x)})
	counter <- counter + 120
	
	# freqs mean
	result[counter : (counter + 1)] <- apply(tmp[, 123:124], 2, mean)
	counter <- counter + 2
	# sizes mean
	result[counter : (counter + 3)] <- apply(tmp[, 125:128], 2, mean)
	counter <- counter + 4
	# Y = -1 count
	result[counter] <- sum(tmp[, 121] == -1)
	counter <- counter + 1
	# Y = 0 count
	result[counter] <- sum(tmp[, 121] == 0)
	counter <- counter + 1
	# Y = 1 count
	result[counter] <- sum(tmp[, 121] == 1)
	counter <- counter + 1

	return(result)
}

summary.tmp <- matrix(0, length(sMotifs$Range), 369)
for(i in 1:length(sMotifs$Range)){
	summary.tmp[i, ] <- my.summary(allMotifs, range = sMotifs$Range[i], 
		period = sMotifs$Period[i])
}
colnames(summary.tmp) <- c(paste("mtf.mean", 1:120), 
						   paste("mtf.mean.nz", 1:120), 
						   paste("mtf.per0", 1:120),
						   paste("mean", colnames(allMotifs)[123:128], sep = "."), 
						   "signed", "no_sign", "will_sign")
sMotifs <- cbind(sMotifs, summary.tmp)
save(sMotifs, file = "allMotifs_summary.rda")




