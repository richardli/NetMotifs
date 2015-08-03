#'
#' Function to extract regular dictionary
#' 
#' @author Richard Li
#'
#' @param includeDyad boolean for including Dyad only dictionary entry
#'
#'
#'
default_dict <- function(includeDyad = TRUE){
	## arranging dictionay entries
	pos.dyad <- seq(88, 90)
	neg.dyad <- seq(1, 3)
	## link to two positive nodes
	pos.both.impTri <- seq(91, 99)
	pos.both.impTriCenter <- seq(100,105)
	pos.both.Tri <- seq(106, 120)
	## positive nodes is closer than negative nodes (in imcomp-Triangles)
	pos.closer <- seq(34, 42)
	pos.further <- seq(43,51)
	one.each <- seq(52, 87)
	## link to two negative nodes
	neg.both.impTri <- seq(4, 12)
	neg.both.impTriCenter <- seq(13,18)
	neg.both.Tri <- seq(19, 33)
	D <- matrix(0, 25, 120)
	D[1, pos.dyad] <- 1; D[2, neg.dyad] <- 1;
	D[3, pos.both.impTri] <- 1; D[4, pos.both.impTriCenter] <- 1; D[5, pos.both.Tri] <- 1;
	D[6, pos.closer ] <- 1; D[7, pos.further] <- 1; 
	D[8, one.each[1:9]] <- 1
	D[9, one.each[10:36]] <- 1
	D[10, neg.both.impTri] <- 1; D[11, neg.both.impTriCenter] <- 1; D[12, neg.both.Tri]<- 1

	#########################################################
	## more dict entries
	noYellow <- seq(1, 33)
	oneYellow <- seq(34, 90)
	twoYellow <- seq(91, 120)
	noDouble <- c(1,2,4,5,7, 8, 13, 14, 15,    
					19, 20, 22, 24,  34, 35, 37, 38, 43, 44, 46, 47, 
					52, 53, 54, 55, 61, 62, 63, 64, 67, 68, 70, 71, 88, 89, 
					91, 92, 94, 95, 100, 101, 102, 106, 107, 109, 111)

	oneDouble <- c(3, 6, 9, 10, 11, 16, 17, 21, 23, 25, 26, 27, 29, 30, 36, 
					39, 40, 41, 45, 48, 49, 50, 56, 57, 58, 59, 65, 66, 69, 
					72, 73, 74, 75, 76, 79, 80, 81, 82, 90, 93, 96, 97, 98, 103, 104,
					108, 110, 112, 113, 114, 116, 117 )
	twoDouble <- c(12, 18, 28, 31, 32, 42, 51, 60, 77, 78, 83, 84, 85, 86, 
					99, 105, 115, 118, 119)
	threeDouble <- c(33, 87, 120)

	indeg <- c(1, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 
			   1, 1, 2, 0, 2, 1, 2, 1, 1, 1, 2, 
			   2, 0, 0, 2, 2, 2, 1, 1, 1, 2, 2, 
			   1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 
			   1, 0, 0, 0, 1, 1, 1, 1, 1, 2, 0, 
			   2, 2, 1, 1, 2, 1, 1, 1, 1, 1, 
			   1, 2, 2, 2, 0, 0, 0, 2, 2, 2, 2, 
			   2, 2, 1, 1, 1, 1, 1, 1, 2, 2, 2, 
			   1, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 
			   1, 1, 2, 0, 2, 1, 2, 1, 1, 1, 2, 
			   2, 0, 0, 2, 2, 2, 1, 1, 1, 2, 2)
	outdeg <- c(0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 
				1, 1, 0, 2, 1, 2, 2, 1, 1, 1, 0, 
				0, 2, 2, 1, 1, 1, 2, 2, 2, 2, 2,
				0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 
				0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 
				1, 1, 2, 2, 2, 1, 1, 1, 1, 1,  
				1, 0, 0, 0, 2, 2, 2, 1, 1, 1, 1, 
				1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
				0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 
				1, 1, 0, 2, 1, 2, 2, 1, 1, 1, 0, 
				0, 2, 2, 1, 1, 1, 2, 2, 2, 2, 2)
	deg <- indeg + outdeg

	noIn <- which(indeg == 0)
	oneIn <- which(indeg == 1)
	twoIn <- which(indeg == 2)
	noOut <- which(outdeg == 0)
	oneOut <- which(outdeg == 1)
	twoOut <- which(outdeg == 2)
	# noDeg <- which(deg == 0)
	oneDeg <- which(deg == 1)
	twoDeg <- which(deg == 2)
	threeDeg <- which(deg == 3)
	fourDeg <- which(deg == 4)
	D[13, noIn] <- 1
	D[14, oneIn] <- 1
	D[15, twoIn] <- 1
	D[16, noOut] <- 1
	D[17, oneOut] <- 1
	D[18, twoOut] <- 1
	# D[19, noDeg] <- 1
	D[19, oneDeg] <- 1
	D[20, twoDeg] <- 1
	D[21, threeDeg] <- 1
	D[22, fourDeg] <- 1
	D[23, noYellow] <- 1
	D[24, oneYellow] <- 1
	D[25, twoYellow] <- 1

	dict.names <- c("pos-dyad", "neg-dyad", "both-pos-incomplete-triangle", 
				"both-pos-incomplete-triangle-center", "both-pos-triangle", 
				"pos-closer", "neg-closer", 
				"one-of-each-incomp", "one-of-each-comp",
				"both-neg-incomplete-triangle", 
				"both-neg-incomplete-triangle-center", "both-neg-triangle")
	dict.names <- c(dict.names, "0 In-degree", "1 In-degree", "2 In-degree", 
					"0 out-degree", "1 out-degree", "2 out-degree", 
					"1 link", "2 links", "3 links", "4 links", 
					"no MM user", "1 MM user", "2 MM user")
	rownames(D) <- dict.names

	if(!includeDyad){
		D <- D[-c(1, 2), ]
	} 

	return(D)
}

#'
#' Function to extract K-means dictionary
#' 
#' @author Richard Li
#'
#' @param K number of entries
#' @param data the motif count matrix (or normalized)
#' @param seed set seed
#' @param true.profile boolean, if set to TRUE, search for closet existing profiles
#' @param
#'
#'
#'
kmeans_dict <- function(K, data, seed, true.profile = FALSE){
	set.seed(seed)
	fit <-  kmeans(data, center = K, iter.max = 1000)
	# function to find the nearest existing profile
	find.nearest <- function(fit, x,  k = 1){
		x <- as.matrix(x)
		N <- dim(x)[1]
		M <- dim(x)[2]
		C <- length(fit$size)
		out <- NULL
		for(i in 1:C){
			center <- fit$centers[i, ]
			dist <- apply(x, 1, function(x){(x-center)^2})
			dist <- apply(t(dist), 1, sum)
			which <- order(dist)[1:k]
			out <- c(out, which)
		}
		return(out)
	}
	if(true.profile){
		which <- find.nearest(fit, data)
		dict <- motif[dict, ]
	}else{
		dict <- round(fit$centers)
	}
	return(dict)
}

#'
#' Function to extract Higher-order dictionary
#' 
#' @author Richard Li
#'
#' @param K order of motifs (up to 5 so far)
#'
#'
higher_dict <- function(K){
	if(K >= 5){
		stop("too large K")
	}
	if(K == 4){
		load("motif4.rda")
		return(motif4$motif4.all)
	}

}
