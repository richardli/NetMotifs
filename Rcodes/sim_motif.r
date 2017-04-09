## script to simulate network subgraphs as proof of concept


require(igraph)
size <- 20
# random graph
g <- erdos.renyi.game(size, 0.1)
plot(g, layout = layout_in_circle)

# g1 <- watts.strogatz.game(1, size, 3, 0.01)
# plot(g1, layout = layout_in_circle)


# generate node colors
MMuser <- sample(1:size, round(size * .2))

# generate events with undirected motifs

# generate contribution vector from undirected motifs

# generate outcome

# mask some outcome as unknown





