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

changeOrder.back <- function(motif, motif.order = NULL){
	if(dim(motif)[2] == 121){
		motif <- motif[, -1]
	}else if(dim(motif)[2] != 120){
		error("wrong input")
	}
	# now motif has 120 columns
	if(is.null(motif.order)){
		motif.order <- getorder()
	}
	motif <- motif[, order(motif.order)]
	colnames(motif) <- paste("M", seq(1:120), sep = "")
	return(motif)
}



