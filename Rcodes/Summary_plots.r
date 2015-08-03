## script to perform exploratory analysis for motif summary
## Last update: 07-27-2015

# load data
library(ggplot2)
library(reshape)
load("../Data/motif_counts/allMotifs_summary.rda")
dim(sMotifs)
class(sMotifs)

# first plot, signed-up v.s. not signed-up v.s. will sign-up
sMotifs.1 <- sMotifs[, c(1, 2, 369:371)]
norm <- sMotifs.1[, 3] + sMotifs.1[, 4] + sMotifs.1[, 5] 
sMotifs.1 <- melt(sMotifs.1, id=c("Range","Period"))
colnames(sMotifs.1)[3:4] <- c("Status", "Count")
sMotifs.1$Fraction <- sMotifs.1$Count / rep(norm, 3)

pdf("../figures/hist.pdf", width = 10, height = 6)
g1 <- ggplot(data = sMotifs.1)
g1 <- g1 + geom_bar(aes(x = Period, y = Count, fill = Status), 
					stat ="identity", alpha = 0.9)
g1 <- g1 + facet_wrap(~ Range)
g1 <- g1 + theme(strip.text.x = element_text(size = 12))
g1 <- g1 + scale_fill_manual(values = c("#009E73", "#56B4E9", "#E69F00"))
g1 <- g1 + ggtitle("Sign-up status over time")
g1 <- g1 + theme(plot.title = element_text(vjust = 2))
g1 <- g1 + scale_y_continuous(breaks= (c(1:10) * 1e5), 
				labels = c(paste(seq(0.1, 0.9, 0.1), "M", sep=""),"1M"))
g1
dev.off()

g2 <- ggplot(data = sMotifs.1)
g2 <- g2 + geom_bar(aes(x = Period, y = Fraction, fill = Status), 
			stat ="identity", alpha = 0.9)
g2 <- g2 + facet_wrap(~ Range)
g2 <- g2 + scale_fill_manual(values = c("#009E73", "#56B4E9", "#E69F00"))
g2 <- g2 + ggtitle("Sign-up status fraction over time")
g2

# second plot, draw each motif count as a line
sMotifs.2 <- sMotifs[, c(1:122)]
norm <- apply(sMotifs.2[, -c(1, 2)], 1, sum)
sMotifs.2 <- melt(sMotifs.2, id=c("Range","Period"))
colnames(sMotifs.2)[3:4] <- c("Motif", "Count")
sMotifs.2$Fraction <- sMotifs.2$Count / rep(norm, 120)
sMotifs.2$Motif.Index <- sapply(as.character(sMotifs.2$Motif), function(x){
	as.numeric(strsplit(x, " ")[[1]][2])
	})

g <- ggplot(data = sMotifs.2)
g <- g + geom_line(aes(x = Period, y = Count, color = Motif))
g <- g + geom_text(aes(x = 0.8, y = Count, 
					   label = Motif.Index, color = Motif), 
		           data = subset(sMotifs.2, Period == 1), size = 5)
g <- g + facet_wrap(~ Range)
g

g <- ggplot(data = sMotifs.2)
g <- g + geom_line(aes(x = Period, y = Fraction, color = Motif))
g <- g + geom_text(aes(x = 0.8, y = Fraction, label = Motif.Index, color = Motif), 
		data = subset(sMotifs.2, Period == 1), size = 5)
g <- g + facet_wrap(~ Range)
g

g <- ggplot(data = sMotifs.2)
g <- g + geom_bar(aes(x = Period, y = Fraction, fill = Motif.Index), stat ="identity")
g <- g + facet_wrap(~ Range)
g <- g + scale_fill_gradientn(colours = c("gray33", "gold3", "gold1", "gold"), 
			values = c(1/120, 33/120, 87/120, 1))
g <- g + ggtitle("Motif profile fraction over time")
g

# third plot, motif-specific plot
source("Motif_format.r")

glist <- NULL
for(i in 1:121){
	if(i == 61){
		glist[[61]] <- ggplot(mtcars, aes(x=wt, y=mpg)) + geom_blank()+ theme_bw()
		next
	}
	vname <- paste("mtf.mean", i - (i > 61))
	g <- ggplot(subset(sMotifs.2, Motif == vname))
	g <- g + geom_line(aes(x = Period, y = Fraction))
	g <- g + facet_wrap(~ Range) + ylim(c(0, 0.13))	
	glist[[i]] <- g
}

gg121("../figures/Motif_fraction_over_time.pdf", glist = glist)

glist <- NULL
for(i in 1:121){
	if(i == 61){
		glist[[61]] <- ggplot(mtcars, aes(x=wt, y=mpg)) + geom_blank()+ theme_bw()
		next
	}
	vname <- paste("mtf.mean", i - (i > 61))
	g <- ggplot(subset(sMotifs.2, Motif == vname))
	g <- g + geom_line(aes(x = Period, y = Count))
	g <- g + facet_wrap(~ Range) + ylim(c(0, 51.3))	
	glist[[i]] <- g
}

gg121("../figures/Motif_mean_over_time.pdf", glist = glist)

