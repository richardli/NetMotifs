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
getEdges <- function(k, pre){
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
		names <- paste0(pre, i, c("a", "b", "c"), connect)

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
nyn <- getEdges(3, name_pre[c(1,2,1)])

for(mm1 in 1:2){
	for(mm2 in 1:2){
		for(mm3 in 1:2){
			# generate edge lists
			edges.tmp <- getEdges(3, pre = name_pre[c(mm1, mm2, mm3)])
			edges <- rbind(edges, edges.tmp)
		}
	}
}

write.table(edges, file = "../data/3motif-all.txt", row.names = F, col.names = F, quote = FALSE)