##
## This script generates certain types of network structure
## And convert into `fake phone call records' for java to parse
##


##
##-------- Task 1: Generate all 3-motif types --------## 
##

adj.mat <- matrix(0, 3, 3)
library(R.utils)

# get lists of all possible edge lists between k nodes
# add: number system add offset to avoid repeat with others
# input: k = 3, pre = 3 names: e.g. (Alex, Bob, Christ)
# output:
#		 511 scenarios: e.g., scenario 123
#        Alex123a  Bob123bY
#		 Bob123bY  Christ123cN
#   number indicates which scenario
#   a, b, c indicates which node the name plays
#   Y, N indicates whether the node is connected to node a	
getEdges <- function(k, pre, offset){
	list <- NULL
	for(i in 1:(2^(k^2)-1)){

		# which cells are 1
		adj.vec <- as.numeric(strsplit(intToBin(i), split = "")[[1]])
		# fill the rest with 0s
		if(length(adj.vec) < k^2){
			adj.vec <- c(rep(0, k^2 - length(adj.vec)), adj.vec)
		}
		# attach to list
		exist <- which(adj.vec == 1)

		# example names: N1a, N1bN, Y1cY
		postfix <- c("N", "Y")
		connect.num <- 1 + c(sum(adj.vec[c(2, 4)])> 0, sum(adj.vec[c(3, 7)])> 0)
		connect <- c("", postfix[connect.num])
		names <- paste0(pre, i + offset, c("a", "b", "c"), connect)

		# rownames and colnames
		rownames <- rep(names, 3)
		colnames <- rep(names, each = 3)

		list <- rbind(list, 
					  cbind(rownames[exist], colnames[exist]))
	}
	return(list)
}


name_pre <- c("N", "Y")
edges <- NULL

# test
as.numeric(strsplit(intToBin(210), split = "")[[1]])
nyn <- getEdges(3, name_pre[c(1,2,1)], 0)

offset <- 0
for(mm1 in 1:2){
	for(mm2 in 1:2){
		for(mm3 in 1:2){
			# generate edge lists
			edges.tmp <- getEdges(3, pre = name_pre[c(mm1, mm2, mm3)], offset)
			edges <- rbind(edges, edges.tmp)
			offset <- dim(edges)[1]
		}
	}
}

# write the edge list
write.table(edges, file = "../data/3motif-all.txt", row.names = F, col.names = F, quote = FALSE)

# write the individual outcome
allnodes <- unique(as.vector(edges))
all_outcome <- substring(allnodes, first = 1, last = 1)
all_outcome <- as.numeric(all_outcome == "Y")
outcome <- cbind(allnodes, all_outcome)
write.table(outcome, file = "../data/3motif-outcome.txt", row.names = F, col.name = F, quote = F)




################################################################
# Process the motif count from above generated network
#
motif <- read.csv("../data/3motif.txt", header = F)
motif.y <- read.csv("../data/3motif_yes.txt", header = F)
motif.n <- read.csv("../data/3motif_no.txt", header = F)

colnames(motif) <- colnames(motif.y) <- colnames(motif.n) <- c("ID", "label", "Time", paste0("motif", 1:120), "inFreq", "outFreq", "s", "r", "m", "n")
# motif2 <- changeOrder(motif[, 4:124])
motif2 <- motif[, -c(1, 2, 3)]

# some random checks
# (BA, CA, CB) A: 22, B: 19, C:24
t(rbind(motif2[which(motif[, 1] == "N201a"),],
		motif2[which(motif[, 1] == "N201bY"),],
		motif2[which(motif[, 1] == "N201cY"),]))

# some random checks
# A: 66, B: 81, C:113
t(rbind(motif2[which(motif[, 1] == "Y13983a"),],
		motif2[which(motif[, 1] == "Y13983bY"),],
		motif2[which(motif[, 1] == "N13983cY"),]))

###############################################################
# Find co-occurrence of motifs
adj.list <- NULL
N <- dim(motif)[1]
m2 <- motif[, 4:123]
m2.y <- motif.y[, 4:123]
m2.n <- motif.n[, 4:123]

for(i in 1:N){
	self <- which(m2[i, ] > 0)
	yesN <- which(m2.y[i, ] > 0)
	noN <- which(m2.n[i, ] > 0)

	# label = -1: already signed up, label = 1: not
	y.self <- as.numeric(motif$label[i] == -1)
	
	if(length(self > 0) && length(yesN) > 0){
		for(j in 1:length(self)){
			for(k in 1:length(yesN)){
				adj.list <- rbind(adj.list, c(y.self, 1, self[j], yesN[k]))
			}
		}
	}

	if(length(self > 0) && length(noN) > 0){
		for(j in 1:length(self)){
			for(k in 1:length(noN)){
				adj.list <- rbind(adj.list, c(y.self, 0, self[j], noN[k]))
			}
		}
	}
	if(i %% 500 == 0) cat("*")
}

adj.list.unique <- unique(adj.list[, c(1,2,3,4)])

adj.mat <- vector("list", 4)
for(y0 in c(0, 1)){
	for(y1 in c(0, 1)){
		index <- y0 * 2 + y1 + 1
		mat <- matrix(0, 120, 120)
		
		config <- intersect(which(adj.list.unique[, 1] == y0),
							which(adj.list.unique[, 2] == y1))
		for(i in 1:length(config)){
			mat[adj.list.unique[config[i], 3], adj.list.unique[config[i], 4]] <- 1 
		}
		adj.mat[[index]] <- mat
	}
}

library(lattice)
col.l <- colorRampPalette(c('white', 'red'))

pdf("../figures/Delta_combine.pdf")
levelplot(adj.mat[[1]] + adj.mat[[2]] + adj.mat[[3]] + adj.mat[[4]], col.regions=col.l, main = "Combine", colorkey = F)
dev.off()

pdf("../figures/Delta_4panel.pdf")
par(mfrow=c(2,2), oma=c(2,0,2,0))
print(levelplot(adj.mat[[1]], col.regions=col.l, main = "0-0",colorkey = F), split=c(1, 1, 2, 2))
print(levelplot(adj.mat[[2]], col.regions=col.l, main = "0-1",colorkey = F), split=c(1, 2, 2, 2), newpage=FALSE)
print(levelplot(adj.mat[[3]], col.regions=col.l, main = "1-0",colorkey = F), split=c(2, 1, 2, 2), newpage=FALSE)
print(levelplot(adj.mat[[4]], col.regions=col.l, main = "1-1",colorkey = F), split=c(2, 2, 2, 2), newpage=FALSE)
dev.off()

write.table(adj.list.unique, file = "../data/Delta.txt", col.names = FALSE, row.names = FALSE, quote = FALSE)


####################################################
# add open triangle case
zero.in <- c(1, 4, 5, 6, 13, 14, 16, 34, 35, 36, 52, 54, 57)
zero.out <- c(7, 8, 9, 13, 15, 17, 37, 38, 39, 55, 58, 59)
zero.both <- c(10, 11, 12, 16, 17, 18, 40, 41, 42, 56, 58, 60)

one.in <- c(43, 44, 45, 53, 54, 56, 91, 92, 93, 100, 101, 103)
one.out <- c(46, 47, 48, 55, 58, 94, 95, 96, 100, 102, 104)
one.both <- c(49, 50, 51, 57, 59, 60, 97, 98, 99, 103, 104, 105)

adj.list.unique <- read.table("../data/Delta.txt")
for(i in zero.in){
	for(j in zero.out){
		adj.list.unique <- rbind(adj.list.unique, 
			c(0, 0, i, j))
	}
}
#(0, 0)
for(i in zero.both){
	for(j in zero.both){
		adj.list.unique <- rbind(adj.list.unique, 
			c(0, 0, i, j))
	}
}
for(i in one.in){
	for(j in one.out){
		adj.list.unique <- rbind(adj.list.unique, 
			c(1, 1, i, j))
	}
}
# (1, 1)
for(i in one.both){
	for(j in one.both){
		adj.list.unique <- rbind(adj.list.unique, 
			c(1, 1, i, j))
	}
}
# (1, 0)
for(i in zero.in){
	for(j in one.out){
		adj.list.unique <- rbind(adj.list.unique, 
			c(1, 0, i, j))
	}
}
for(i in zero.out){
	for(j in one.in){
		adj.list.unique <- rbind(adj.list.unique, 
			c(1, 0, i, j))
	}
}
for(i in zero.both){
	for(j in one.both){
		adj.list.unique <- rbind(adj.list.unique, 
			c(1, 0, i, j))
	}
}

# (0, 1)
for(i in one.in){
	for(j in zero.out){
		adj.list.unique <- rbind(adj.list.unique, 
			c(0, 1, i, j))
	}
}
for(i in one.out){
	for(j in zero.in){
		adj.list.unique <- rbind(adj.list.unique, 
			c(0, 1, i, j))
	}
}
for(i in one.both){
	for(j in zero.both){
		adj.list.unique <- rbind(adj.list.unique, 
			c(0, 1, i, j))
	}
}



adj.list.unique <- unique(adj.list.unique)
write.table(adj.list.unique, file = "../data/Delta2.txt", col.names = FALSE, row.names = FALSE, quote = FALSE)
