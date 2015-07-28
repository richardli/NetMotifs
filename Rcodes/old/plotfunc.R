# motif plot function
motif.plot<- function(which, motif.label, v.size = 80, e.size = .5, t.size = 1, t.pos = -2, mfrow){

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

	orderlist <- c(order.nn, order.np, order.pp) 
	require(igraph)
	## helper function
	## input  :  len    : how many variation
	##			 j      : which variation
	##			 counter: which motif block
	## return :  ego: 2, signed-up: 1, not: 0
	getcolor <- function(len, j, counter){
		colorlist <- c("gainsboro", "gold", "red")
		if(len == 4 ){
			colors <- rbind(c(2,0,0), c(2,1,0),c(2,0,1), c(2, 1, 1))
		}
		if(len == 3 ){
			colors <- rbind(c(2,0,0), c(2,1,0), c(2, 1, 1))
		}
		if(len == 4  && counter > 43){
			colors <- rbind(c(0,0,2), c(0,1,2),c(1,0,2), c(1,1,2))
		}
		if(len == 3 && counter > 43){
			colors <- rbind(c(0,0,2), c(0,1,2), c(1,1,2))
		}
		if(len==2){
			colors <- rbind(c(2,0), c(2,1))	
		}
		if(len==1){
			return(colorlist[3])
		}
		return(colorlist[colors[j,]+1])
	}

	edges.all <-
	c(  1 , 2 , 1 , 1 , 2 , 3 , 3 , 1 , 3 , 1 , 3 , 3 , 3 ,
		2 , 1 , 2 , 1 , 3 , 2 , 3 , 2 , 3 , 1 , 2 , 3 , 1 ,
		3 , 2 , 3 , 1 , 3 , 1 , 2 , 3 , 3 , 1 , 3 , 2 , 1 ,
		3 , 3 , 1 , 3 , 2 , 3 , 1 , 3 , 1 , 1 , 2 , 1 , 3 ,
		3 , 1 , 1 , 3 , 2 , 1 , 1 , 3 , 3 , 1 , 1 , 3 , 2 ,
		3 , 1 , 1 , 3 , 2 , 1 , 3 , 2 , 2 , 1 , 3 , 3 , 2 ,
		1 , 3 , 1 , 2 , 1 , 2 , 1 , 2 , 3 , 3 , 1 , 2 , 1 ,
		3 , 2 , 1 , 2 , 1 , 3 , 3 , 1 , 2 , 1 , 3 , 3 , 2 ,
		1 , 2 , 1 , 1 , 2 , 2 , 1 , 1 , 2 , 3 , 2 , 3 , 1 ,
		2 , 1 , 3 , 3 , 3 , 3 , 1 , 3 , 1 , 3 , 2 , 3 , 3 ,
		2 , 3 , 1 , 3 , 2 , 3 , 2 , 3 , 3 , 1 , 2 , 3 , 1 ,
		3 , 3 , 1 , 2 , 3 , 1 , 3 , 2 , 3 , 2 , 2 , 3 , 3 ,
		2 , 1 , 2 , 2 , 3 , 1 , 3 , 2 , 3 , 1 , 2 , 2 , 3 ,
		1 , 3 , 2 , 2 , 3 , 2 , 1 , 3 , 1 , 3 , 1 , 3 , 1 ,
		2 , 1 , 3 , 2 , 2 , 1 , 3 , 3 , 2 , 1 , 1 , 2 , 2 ,
		1 , 3 , 1 , 3 , 2 , 1 , 3 , 1 , 2 , 2 , 1 , 3 , 1 ,
		2 , 3 , 2 , 1 )
	edges.all <- matrix(edges.all, ncol = 2)
	iso <- c(1,rep(2,3),rep(4,10), 3,3,4,4,3, rep(4, 10), 3,3,4,4,3)
	ecount <- c(1,1,1,2,rep(c(2,2,3),2), 3,3,4,  2,2,2,3,3,4,
				3,3,3,4,4,5,
				3,4,4,
				4,4,4,5,5,6)
	g <- vector("list", 121)
	counter <- 1
	counter.edges <- 1

	for(i in 0:33){
		len = iso[i+1]
		for(j in 1:len){
			edgecount <- ecount[i+1]
			edges <- edges.all[counter.edges:(counter.edges + edgecount-1), ]
			edgemat <- as.matrix(edges)
			## correct for igraph nonsense
			edgemat <- t(edgemat)
			g[[counter]] <- graph(edgemat)
			V(g[[counter]])$color <- getcolor(len, j, counter)
			counter <- counter+1
		}	
		counter.edges <- counter.edges + edgecount 
	}
	## coordinate of nodes in each plot
	cord <- vector("list", 3)
	cord[[3]] <- rbind(c(0,0),c(2,0),c(1,1.7))
	cord[[2]] <- rbind(c(0,0.7),c(2,0.7))
	par(mfrow = mfrow)
	for(i in 1:length(which)){
		nnode <- length(V(g[[ orderlist[which[i]] + 1 ]]))
		plot(g[[  orderlist[which[i]] + 1 ]], 
			layout = cord[[nnode]][V(g[[  orderlist[which[i]] + 1 ]]), ], 
			vertex.label="", vertex.size = v.size, 
			edge.arrow.size = e.size, edge.color = "black")
		text(x=0, y= t.pos, labels=motif.label[i], cex = t.size)
	}
}

##
## Plot functions for 11 by 11 motif matrix plot
##
## input:
##       orderlist : how to permute the naive order
##       importdir: where the edge list file is stored
##		 outdir: directory to store plot
##       filename: filename of plot
##       label: T or F, whether the border should be drawn
##		 label.ind: individual label added to motifs
##       ind: list of positive and negative labels (in the output order(new order))

motif.matplot <- function(
		orderlist, 
		importdir = "~/Dropbox/network_evolution/motif-data/codes-richard/data/",
		outdir = "~/Dropbox/network_evolution/motif-data/codes-richard/plots/",
		filename = "motif", 
		label = FALSE, 
		label.ind = NULL,
		ind = NULL){

	require(igraph)
	## helper function
	## input  :  len    : how many variation
	##			 j      : which variation
	##			 counter: which motif block
	## return :  ego: 2, signed-up: 1, not: 0
	getcolor <- function(len, j, counter){
		colorlist <- c("gainsboro", "gold", "red")
		if(len == 4 ){
			colors <- rbind(c(2,0,0), c(2,1,0),c(2,0,1), c(2, 1, 1))
		}
		if(len == 3 ){
			colors <- rbind(c(2,0,0), c(2,1,0), c(2, 1, 1))
		}
		if(len == 4  && counter > 43){
			colors <- rbind(c(0,0,2), c(0,1,2),c(1,0,2), c(1,1,2))
		}
		if(len == 3 && counter > 43){
			colors <- rbind(c(0,0,2), c(0,1,2), c(1,1,2))
		}
		if(len==2){
			colors <- rbind(c(2,0), c(2,1))	
		}
		if(len==1){
			return(colorlist[3])
		}
		return(colorlist[colors[j,]+1])
	}

	edges.all <- read.csv(paste(importdir, "motifedges.csv", sep = ""), header=F)
	iso <- c(1,rep(2,3),rep(4,10), 3,3,4,4,3, rep(4, 10), 3,3,4,4,3)
	ecount <- c(1,1,1,2,rep(c(2,2,3),2), 3,3,4,  2,2,2,3,3,4,
				3,3,3,4,4,5,
				3,4,4,
				4,4,4,5,5,6)
	g <- vector("list", 121)
	counter <- 1
	counter.edges <- 1

	for(i in 0:33){
		len = iso[i+1]
		for(j in 1:len){
			edgecount <- ecount[i+1]
			edges <- edges.all[counter.edges:(counter.edges + edgecount-1), ]
			edgemat <- as.matrix(edges)
			## correct for igraph nonsense
			edgemat <- t(edgemat)
			g[[counter]] <- graph(edgemat)
			V(g[[counter]])$color <- getcolor(len, j, counter)
			counter <- counter+1
		}	
		counter.edges <- counter.edges + edgecount 
	}

	# get border color
	borderlist <- rep("white", 120)
	# positive
	borderlist[ind[[1]][which(ind[[1]]>0)]] <- "green"
	# negative
	borderlist[ind[[2]][which(ind[[2]]>0)]] <- "red"


	pdf(paste(outdir, filename, ".pdf", sep=""), width=121,height=121)

	## coordinate of nodes in each plot
	cord <- vector("list", 3)
	cord[[3]] <- rbind(c(0,0),c(2,0),c(1,1.7))
	cord[[2]] <- rbind(c(0,0.7),c(2,0.7))

	## coordinate of plots, leave out the center block to last
	rpos <- c(rep(1,11),rep(2,11),rep(3,11),rep(4,11),rep(5,11),rep(6,10),
		      rep(7,11),rep(8,11),rep(9,11),rep(10,11),rep(11,11), 6)
	cpos <- c(rep(seq(1,11), 5), seq(1,5), seq(7,11), rep(seq(1,11),5),6)
	pos <- cbind(rpos, cpos)

	## initiate 11 by 11 matrix
	par(mfrow=c(11,11))
	if(is.null(label.ind)){label.ind <- seq(1, 120)}
	for(i in 1:120){
		par(mfg = pos[i, ])
		nnode <- length(V(g[[ orderlist[i] + 1 ]]))
		plot(g[[  orderlist[i] + 1 ]], 
			layout = cord[[nnode]][V(g[[  orderlist[i] + 1 ]]), ], 
			vertex.label="", vertex.size = 100, 
			edge.arrow.size = 6, edge.color = "black")
		text(x=0, y=-.2, labels=label.ind[i], cex = 17)
		if(label){
			rect(par("usr")[1],par("usr")[3],par("usr")[2],par("usr")[4],border = borderlist[i], lwd = 50)			
		}
	}
	dev.off()
}

## plot the histogram or log histogram to file 
## note: 0's not plotted in either case
##
## motif      : the motif to plot
## outdir     : where to save plots
## filename   : name of file to be saved
## log        : whether or not take log
##
motif.hist <- function(
		motif,
		outdir = "~/Dropbox/network_evolution/motif-data/codes-richard/plots/",
		filename = "motif-hist", 
		log = TRUE){
	
	## coordinate of plots, leave out the center block to last
	rpos <- c(rep(1,11),rep(2,11),rep(3,11),rep(4,11),rep(5,11),rep(6,10),
		      rep(7,11),rep(8,11),rep(9,11),rep(10,11),rep(11,11), 6)
	cpos <- c(rep(seq(1,11), 5), seq(1,5), seq(7,11), rep(seq(1,11),5),6)
	pos <- cbind(rpos, cpos)

	pdf(paste(outdir, filename, ".pdf", sep = "") , 
		height = 121/5, width = 121/5)
	par(mfrow = c(11,11))
	for(i in 1:120){
		par(mfg = pos[i, ])
		if(length(unique(motif[,i])) < 3){
			plot(0,0, xaxt='n', yaxt='n', col = "white")
			text(0,0,
				label = paste("# of 1: ", round(length(which(motif[,i]==1)))), 
				cex = 1.5)
		}else{
			ratio <- length(which(motif[,i] == 0)) / dim(motif)[1]
			if(log){
				hist(log(motif[,i]), ylab = NULL, 
					main = colnames(motif)[i],
					xlab = paste("%0:",round(ratio, 4)*100), 
					cex.main = 2, cex.lab = 2, col.lab = "blue")
			}else{
				nonzero <- which(motif[,i] != 0)
				hist(motif[nonzero,i], ylab = NULL, 
					main = colnames(motif)[i],
					xlab = paste("%0:",round(ratio, 4)*100), 
					cex.main = 2, cex.lab = 2, col.lab = "blue")
			}
		}	
	}
	dev.off()
}

motif.matplot.aniplot <- function(
		orderlist, 
		importdir = "~/Dropbox/network_evolution/motif-data/codes-richard/data/",
		label = FALSE, 
		ind = NULL){

	require(igraph)
	## helper function
	## input  :  len    : how many variation
	##			 j      : which variation
	##			 counter: which motif block
	## return :  ego: 2, signed-up: 1, not: 0
	getcolor <- function(len, j, counter){
		colorlist <- c("gainsboro", "gold", "red")
		if(len == 4 ){
			colors <- rbind(c(2,0,0), c(2,1,0),c(2,0,1), c(2, 1, 1))
		}
		if(len == 3 ){
			colors <- rbind(c(2,0,0), c(2,1,0), c(2, 1, 1))
		}
		if(len == 4  && counter > 43){
			colors <- rbind(c(0,0,2), c(0,1,2),c(1,0,2), c(1,1,2))
		}
		if(len == 3 && counter > 43){
			colors <- rbind(c(0,0,2), c(0,1,2), c(1,1,2))
		}
		if(len==2){
			colors <- rbind(c(2,0), c(2,1))	
		}
		if(len==1){
			return(colorlist[3])
		}
		return(colorlist[colors[j,]+1])
	}

	edges.all <- read.csv(paste(importdir, "motifedges.csv", sep = ""), header=F)
	iso <- c(1,rep(2,3),rep(4,10), 3,3,4,4,3, rep(4, 10), 3,3,4,4,3)
	ecount <- c(1,1,1,2,rep(c(2,2,3),2), 3,3,4,  2,2,2,3,3,4,
				3,3,3,4,4,5,
				3,4,4,
				4,4,4,5,5,6)
	g <- vector("list", 121)
	counter <- 1
	counter.edges <- 1

	for(i in 0:33){
		len = iso[i+1]
		for(j in 1:len){
			edgecount <- ecount[i+1]
			edges <- edges.all[counter.edges:(counter.edges + edgecount-1), ]
			edgemat <- as.matrix(edges)
			## correct for igraph nonsense
			edgemat <- t(edgemat)
			g[[counter]] <- graph(edgemat)
			V(g[[counter]])$color <- getcolor(len, j, counter)
			counter <- counter+1
		}	
		counter.edges <- counter.edges + edgecount 
	}

	# get border color
	borderlist <- rep("white", 121)
	# positive
	borderlist[ind[[1]][which(ind[[1]]>0)]] <- "green"
	# negative
	borderlist[ind[[2]][which(ind[[2]]>0)]] <- "red"


	dev.hold()

	## coordinate of nodes in each plot
	cord <- vector("list", 3)
	cord[[3]] <- rbind(c(0,0),c(2,0),c(1,1.7))
	cord[[2]] <- rbind(c(0,0.7),c(2,0.7))

	## coordinate of plots, leave out the center block to last
	rpos <- c(rep(1,11),rep(2,11),rep(3,11),rep(4,11),rep(5,11),rep(6,10),
		      rep(7,11),rep(8,11),rep(9,11),rep(10,11),rep(11,11), 6)
	cpos <- c(rep(seq(1,11), 5), seq(1,5), seq(7,11), rep(seq(1,11),5),6)
	pos <- cbind(rpos, cpos)

	## initiate 11 by 11 matrix
	par(mfrow=c(11,11))
	for(i in 1:120){
		par(mfg = pos[i, ])
		nnode <- length(V(g[[ orderlist[i] + 1 ]]))
		plot(g[[  orderlist[i] + 1 ]], 
			layout = cord[[nnode]][V(g[[  orderlist[i] + 1 ]]), ], 
			vertex.label="", vertex.size = 100, 
			edge.arrow.size = 6, edge.color = "black")
		text(x=0, y=-.2, labels=i, cex = 17)
		if(label){
			rect(par("usr")[1],par("usr")[3],par("usr")[2],par("usr")[4],border = borderlist[i], lwd = 50)			
		}
	}
	ani.pause()
}




