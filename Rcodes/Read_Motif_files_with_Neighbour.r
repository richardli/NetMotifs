## Script to read multiple series of motif counts
## Created: 12-16-2015
## Last updated: 07-27-2015

# specify file directory and names
dir <- "../../MotifwithNeighbour/"
header <- c("0705") #, "0805week", "0809week")
index <- 0:7

# get file names
n0 <- length(header)
n1 <- length(index)
names <- list(n0)
names_nei <- list(n0)
for(i in 1:n0){
	names[[i]] <- paste(dir, header[i],"week", index, ".txt", sep = "")
	names_nei[[i]] <- paste(dir, header[i], "NeighbourMutual_week", index, ".txt", sep = "")
}

# start reading data into list
# motif file structure
# Y, time, m1 - m121, inFreq, outFreq, sSize, rSize, mSize, nSize
# Note m1 - m121 are not ordered as we want
# call reformat function before saving

source("Motif_format.r")
source("Dict_function.r")

allMotifs <- NULL
allMotifs_nei <- NULL
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

		# read raw
		tmp <- read.table(names_nei[[i]][j], sep = ",")
		# change order (now motif 1 to 120)
		motif2 <- changeOrder(tmp[, 3:123])
		# get other information
		Y2 <- tmp[, 1]
		T2 <- tmp[, 2]
		
		# get in to shape
		tmp2 <- cbind(motif2, Y2, T2, tmp[, 124:129], i, j)

		print(table(Y)/length(Y))
		allMotifs_nei <- rbind(allMotifs_nei, tmp2)

		# also save individual files
		colnames(tmp2) <- c(colnames(motif2), "Y", "T", 
							"inFreq", "outFreq", "sSize", "rSize", "mSize", "nSize", "Range", "Period")
		motif2 <- data.frame(tmp2)


		# now get dictionary
		dict1 <- default_dict() 
		dict1 <- t(t(dict1) * apply(motif[, 1:120], 2,function(x){median(x[x>0])}))
		
		# and save only those not signed up yet to clean file
		motif <- motif[-which(Y == -1), ]
		motif2 <- motif2[-which(Y == -1), ]

		write.table(motif[, 1:120], file = paste(dir, "clean/Motifs_g", i, "p", j, ".txt", sep = ""), col.names = F, row.names = F)
		write.table(motif2[, 1:120], file = paste(dir, "clean/MotifsMutual_g", i, "p", j, ".txt", sep = ""), col.names = F, row.names = F)
		write.table(dict1, file = paste(dir, "dict/clean_dict_g", i, "p", j, ".txt", sep = ""), col.names = F, row.names = F)

		motiflist <- list(motif = motif, motif2 = motif2)
		save(motiflist, file = paste(dir, "clean/Motifs_g", i, "p", j, ".rda", sep=""))
	}
}


# get a sampled set of nodes
set.seed(1)
size <- 1e4
for(i in 1:n0){
	for(j in 1:n1){
		load(paste(dir, "clean/Motifs_g", i, "p", j, ".rda", sep=""))
		N <- dim(motiflist$motif)[1]
		cat(paste("size of current period is", N, "\n"))
		sampled <- sample(1:N, size = size)
		motif <- motiflist$motif
		motif2 <- motiflist$motif2
		motiflist <- list(motif = motif[sampled, ], 
						  motif2 =motif2[sampled, ])

		dict1 <- default_dict() 
		dict1 <- t(t(dict1) * apply(motif[sampled, 1:120], 2,function(x){
			if(sum(x) == 0){
					return(0)
				}else{
					return(median(x[x>0]))
				}
			}))
		
		write.table(motif[sampled, 1:120], file = paste(dir, "clean/Motifs_Sampled_g", i, "p", j, ".txt", sep = ""), col.names = F, row.names = F)
		write.table(motif2[sampled, 1:120], file = paste(dir, "clean/MotifsMutual_Sampled_g", i, "p", j, ".txt", sep = ""), col.names = F, row.names = F)
		write.table(dict1, file = paste(dir, "dict/clean_dict_Sampled_g", i, "p", j, ".txt", sep = ""), col.names = F, row.names = F)
		save(motiflist, file = paste(dir, "clean/Motifs_Sampled_g", i, "p", j, ".rda", sep=""))
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




