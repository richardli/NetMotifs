
###################################################################
## this part of codes plot dictionaries into lines
##
#################################################################
## x: motif or dict
## nrow: number of profiles to plot each page
## k: cut-off point
Motif.lineplot <- function(x, nrow, k){
	n <- dim(x)[1]
	m <- dim(x)[2]
	par(mfrow=c(1,1))
	for(i in 1:(trunc(n/nrow))){
		plot(NA, xlim = c(0, m), ylim = c(0, k*nrow), xlab="motif", ylab="Count",yaxt='n', frame.plot=F)
		#axis(side = 1, at = seq(k, (k*nrow))
		abline(h = seq(k, (k*nrow), len = nrow))
		for(j in 1:nrow){
			height <- x[((i-1)*nrow + j), ]
			over <- which(height > k)
			height[over] <- k			
			segments(x0 = seq(1,m), x1 = seq(1,m), 
					 y0 = rep(k*(nrow-j),m), y1 = height + rep(k*(nrow-j),m))
		}
	}
}

Motif.lineplot(dict, 35, 10)

#################################################################
# This part of codes enumerate all K-motifs into adj matrix
# 	   (then into vector)
#
#################################################################
# of nodes in ego network
k <- 5
n.max <- k^2 - k
# which elements are the ego edges
# e.g.
#		[ 0, y, y, y,
#		  y, 0, n, n,	
#		  y, n, 0, n, 	
#		  y, n, n, 0 ]

with.ego <- c(1:(k-1), seq(k, by = k-1, len = k-1))
cb.all <- NULL
for(nlink in 1:n.max){
	# cb is the emuneration matrix, each col consist of one possibility
	cb <- t(as.matrix(combn(n.max, nlink)))
	# cb.ex is the expanded binary matrix for cb
	cb.ex <- matrix(0, dim(cb)[1], n.max)
	for(i in 1:dim(cb)[1]){cb.ex[i, cb[i, ]] <- 1}
	# taking care of boundary case
	if(dim(cb.ex)[1] > 1){
			ego.flag <- apply(cb.ex[, with.ego], 1, sum)
			cb.ex <- cb.ex[which(ego.flag > 0), ]
				# cb0 is the emuneration plus diag 0's
			cb0 <- 0
			for(j in 1:(k-1)){
				which.col <- ((j-1)*k + 1) : (j*k)
				cb0 <- cbind(cb0, cb.ex[, which.col], 0)
			}
				# check if all nodes are involved, if not, containing 3-motifs
				# 		then it overfits the data
				flag <- apply(cb0, 1, function(x){
					prod(rowSums(matrix(x, k, k, byrow =TRUE)) + 
						 colSums(matrix(x, k, k, byrow = TRUE)))
				})
				cb0 <- cb0[which(flag > 0), ]
	}else{
			cb0 <- 0
			for(j in 1:(k-1)){
				which.col <- ((j-1)*k + 1) : (j*k)
				cb0 <- c(cb0, cb.ex[, which.col], 0)
			}
	}

	if(length(cb0) > 0){
		cb.all <- rbind(cb.all, cb0)
	}
}
write.table(cb.all, file = paste("allcomb", k, ".txt", sep=""), row.names = F,
            col.names = F, sep = ",")

#################################################################
# Helper function for permuting motif orders
#
#################################################################
getorder <- function(){
	order.nn <- c(1, 3, 5, 
				  7, 19, 31, 11, 23, 35, 15, 27, 39,
				  43, 47, 50, 53, 57, 61,
				  64,88,100, 
				  68,104, 72,107, 
				  76,92,110, 
				  80,96,114, 
				  84,118)
	order.pp<-c(2,4,6, 10,22,34,14,26,38,18,30,42, 
		          46,49,52,56,60,63, 
				  67,91,103, 
				  71,106, 75,109, 
				  79,95,113, 
				  83,99,117, 
				  87,120)
	order.np<-c(8,20,32,12,24,36,16,28,40, # A - N - P
			   9,21,33,13,25,37,17,29,41, # A - P - N
			   44,45,48,51,54,55,58,59,62, # P - A - N
	 		   65,66,89,90, 101,102,
	 		   69,70,105,
	 		   73,74,108,
	 		   77,78,93,94,111,112,
	 		   81,82,97,98,115,116,
	 		   85,86,119)

	motif.order <- c(order.nn, order.np, order.pp) 
	return(motif.order)
}


changeOrder <- function(motif, motif.order = NULL){
	if(dim(motif)[2] == 121){
		motif <- motif[, -1]
	}else if(dim(motif)[2] != 120){
		error("wrong input")
	}
	# now motif has 120 columns
	if(is.null(motif.order)){
		motif.order <- getorder()
	}
	motif <- motif[, motif.order]
	colnames(motif) <- paste("mtf", seq(1:120))
	return(motif)
}
#################################################################
#################################################################
#################################################################
#################################################################
# the following is after running Java codes, get the output
# java file to look for is ReadSim.java
#################################################################
#################################################################
#################################################################
#################################################################
motif.raw <- read.table("~/rjava_space/data/allmotif4.txt", sep=",")
motif4 <- changeOrder(motif.raw[, 3:123])
motif4.nodyad <- motif4
motif4.nodyad[, c(1,2,3,88,89,90)] <- 0
motif4.nodyad <- motif4.nodyad[which(rowSums(motif4.nodyad) > 0), ]
motif4.all <- rbind(motif4, motif4.nodyad)
motif4.all <- unique(motif4.all[, c(1:120)])

motif4 <- list(motif4 = motif4, motif4.nodyad  = motif4.nodyad, motif4.all = motif4.all)
save(motif4, file = "~/rjava_space/data/motif4.rda")

motif.raw <- read.table("~/rjava_space/data/allmotif5.txt", sep=",")
motif5 <- changeOrder(motif.raw[, 3:123])
motif5.nodyad <- motif5
motif5.nodyad[, c(1,2,3,88,89,90)] <- 0
motif5.nodyad <- motif5.nodyad[which(rowSums(motif5.nodyad) > 0), ]
motif5.all <- rbind(motif5, motif5.nodyad)
motif5.all <- unique(motif5.all[, c(1:120)])
motif5 <- list(motif5 = motif5, motif5.nodyad  = motif5.nodyad, motif5.all = motif5.all)
save(motif5, file = "~/rjava_space/data/motif5.rda")

 dim(motif5.all)
 dim(unique(motif5[, c(1:120)]))
 dim(unique(motif5.nodyad[, c(1:120)]))
