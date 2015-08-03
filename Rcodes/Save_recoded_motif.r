##
## Code snippet for changing the rda file to txt for plain Java implementation
##
##

for(group in 1:3){
	for(period in 1:8){
		name <- paste("g", group, "p", period, sep = "")
		load(paste("../data/motif_counts/Motifs_", name, ".rda", sep = ""))
		mdata <- motif
		source("Dict_function.r")
		dict1 <- default_dict() 
		dict1 <- t(t(dict1) * apply(mdata[, 1:120], 2, function(x){median(x[x>0])}))
		mdata <- mdata[-which(mdata$Y == -1), ]
	
		write.table(mdata, row.names = F, col.names = F,
		file = paste("../data/motif_counts/clean_motifs_", name, ".txt", sep = ""))
		write.table(dict1, row.names = F, col.names = F, 
		file = paste("../data/motif_counts/clean_dict_", name, ".txt", sep = ""))
	}
}
