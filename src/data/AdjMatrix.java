package data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

public class AdjMatrix {
    HashMap<Long, Integer> dict;
    public int V;
    public int E;
    public int norm = 0;
    HashMap<Integer, Set<Integer>> slist;
    HashMap<Integer, Set<Integer>> rlist;
    HashMap<Integer, Set<Integer>> hop2;
    public HashMap<Integer, int[]> motifcount = new HashMap<Integer, int[]>();


    public AdjMatrix() {
        this.dict = new HashMap<Long, Integer>();
        this.V = 0;
        this.E = 0;
        this.norm = 0;
        this.slist = new HashMap<Integer, Set<Integer>>();
        this.rlist = new HashMap<Integer, Set<Integer>>();
        this.hop2 = new HashMap<Integer, Set<Integer>>();
        // initialize motif count array
        this.motifcount = new HashMap<Integer, int[]>();
        for (int i = 0; i < 4; i++) {
            int[] temp = new int[17];
            this.motifcount.put(i, temp);
        }
    }

    // inherite the list of dict from the previous AdjMatrix object
    @SuppressWarnings("unchecked")
    public AdjMatrix(AdjMatrix adj) {
        this.dict = (HashMap<Long, Integer>) adj.dict.clone();
        this.V = adj.V;
        this.E = 0;
        this.norm = 0;
        this.slist = new HashMap<Integer, Set<Integer>>();
        this.rlist = new HashMap<Integer, Set<Integer>>();
        this.hop2 = (HashMap<Integer, Set<Integer>>) adj.hop2.clone();

        // initialize motif count array
        this.motifcount = new HashMap<Integer, int[]>();
        for (int i = 0; i < 4; i++) {
            int[] temp = new int[17];
            this.motifcount.put(i, temp);
        }
    }

    // clone prvious adjMat object
    @SuppressWarnings("unchecked")
    public void clone(AdjMatrix adj) {
        this.dict = (HashMap<Long, Integer>) adj.dict.clone();
        this.slist = (HashMap<Integer, Set<Integer>>) adj.slist.clone();
        this.rlist = (HashMap<Integer, Set<Integer>>) adj.rlist.clone();
        //hop2 is not cloned, since clone is copying newer adj to older one
        //this.hop2 = (HashMap<Integer, Set<Integer>>) adj.hop2.clone();
        this.motifcount = (HashMap<Integer, int[]>) adj.motifcount.clone();

        this.V = adj.V;
        this.E = adj.E;
        this.norm = adj.norm;
    }

    public String toString() {
        StringBuilder lineOut = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            lineOut.append(this.V);
            lineOut.append(",");
            lineOut.append(this.E);
            lineOut.append(Arrays.toString(this.motifcount.get(i)).replace("[", ",").replace("]", ""));
            lineOut.append("\n");
        }
        return lineOut.toString();
    }

    // print adj information
    public void printinf() {
        System.out.println("Adjacency Matrix with " + (this.V) + " Nodes");
        System.out.println("Adjacency Matrix with " + (this.E) + " Edges");
    }


    // add new edge to AdjMatrix
    public void add(String sender, String receiver, double rand, int max) {
        String sender2 = sender.replace("L", "0").replace("F", "1").replace("N", "2");
        String receiver2 = receiver.replace("L", "0").replace("F", "1").replace("N", "2");
        long s = Long.parseLong(sender2);
        long r = Long.parseLong(receiver2);
        if (s == r) return;

        int send, receive;
        if (rand < 1) {
            if (this.dict.get(s) == null && this.dict.get(r) == null) {
                double temp = Math.random();
                if (temp > rand) return;
            }
        }
        if (max > 0 && this.V + 1 > max) {
            if (this.dict.get(s) == null || this.dict.get(r) == null) {
                return;
            }
        }
        // add to dictionary
        if (this.dict.get(s) == null) {
            this.V++;
            send = this.V;
            this.dict.put(s, send);
        } else {
            send = this.dict.get(s);
        }
        if (this.dict.get(r) == null) {
            this.V++;
            receive = this.V;
            this.dict.put(r, receive);
        } else {
            receive = this.dict.get(r);
        }

        // add to adjacency matrix (sender side)
        if (this.slist.get(send) == null) {
            Set<Integer> rtemp = new HashSet<Integer>();
            rtemp.add(receive);
            this.slist.put(send, rtemp);
            this.E++;
        } else if (!this.slist.get(send).contains(receive)) {
            this.slist.get(send).add(receive);
            this.E++;
        }

        // add to adjacency matrix (receiver side)
        if (this.rlist.get(receive) == null) {
            Set<Integer> stemp = new HashSet<Integer>();
            stemp.add(send);
            this.rlist.put(receive, stemp);
        } else {
            this.rlist.get(receive).add(send);
        }
    }

    // method to count each motif by inputing the two edge lists
    private int[] countmotif(EdgeSet edgeA, EdgeSet edgeB) {
        Set<Integer> setC = new HashSet<Integer>();
        int[] count = new int[17];
        // case 15
        setC = Sets.intersection(edgeA.mut, edgeB.mut);
        count[15] += setC.size();
        // case 14
        setC = Sets.intersection(edgeA.out, edgeB.mut);
        count[14] += setC.size();
        // case 13
        setC = Sets.intersection(edgeA.in, edgeB.mut);
        count[13] += setC.size();
        // case 12
        setC = Sets.intersection(edgeA.mut, edgeB.out);
        count[12] += setC.size();
        // case 11
        setC = Sets.intersection(edgeA.mut, edgeB.in);
        count[11] += setC.size();
        // case 10
        setC = Sets.intersection(edgeA.out, edgeB.out);
        count[10] += setC.size();
        // case 9
        setC = Sets.intersection(edgeA.out, edgeB.in);
        count[9] += setC.size();
        // case 8
        setC = Sets.intersection(edgeA.in, edgeB.out);
        count[8] += setC.size();
        // case 7
        setC = Sets.intersection(edgeA.in, edgeB.in);
        count[7] += setC.size();
        //case 6
        count[6] += edgeB.mut.size() - count[15] - count[14] - count[13];
        //case 5
        count[5] += edgeA.mut.size() - count[15] - count[12] - count[11];
        //case 4
        count[4] += edgeB.out.size() - count[12] - count[10] - count[8];
        //case 3
        count[3] += edgeB.in.size() - count[11] - count[9] - count[7];
        //case 2
        count[2] += edgeA.out.size() - count[14] - count[10] - count[9];
        //case 1
        count[1] += edgeA.in.size() - count[13] - count[8] - count[7];
        //case 0
        //???
        return (count);
    }

    private void counttwotie(AdjMatrix newmat, Set<Integer> setB, Set<Integer> setB2, int nodeA, int t, EdgeMotif motifA) {

        // first iterate within setB, since setB2 is easier
        for (int nodeB : setB) {
            int tie = 0;
            if (newmat.slist.get(nodeA) != null) {
                if (newmat.slist.get(nodeA).contains(nodeB)) {
                    tie = 1;
                }
            }
            this.norm++;
            // flag for there's any triangle completed
            boolean findTie = false;
            EdgeSet edgeA = new EdgeSet(this, nodeA);
            EdgeSet edgeB = new EdgeSet(this, nodeB);
            int type = 0;
            int[] count = new int[16];

            // determine the link between A and B
            if (edgeA.out.contains(nodeB)) {
                type = 1;
            } else if (edgeA.in.contains(nodeB)) {
                type = 2;
            } else if (edgeA.mut.contains(nodeB)) {
                type = 3;
            }

            // determine the counts of each motif
            count = this.countmotif(edgeA, edgeB);
            for (int i = 0; i < count.length; i++) {
                if (count[i] > 0) findTie = true;
            }
            if (findTie == false) {
                count[0] = 1;
            }
            /*
			 * Now consider case 1 to 6 using setB2
			 * 
			 */
            motifA.add(nodeB, t, type, count);
            motifA.addresult(nodeB, t, tie);
        }
        // then iterate within setB2, since setB2 is not current 2hop neighbor, no need to check type 7 onwards
        for (int nodeB : setB2) {
            int tie = 0;
            if (newmat.slist.get(nodeA) != null) {
                if (newmat.slist.get(nodeA).contains(nodeB)) {
                    tie = 1;
                }
            }
            this.norm++;
            // not current neighbor, only in case 1 to 6
            EdgeSet edgeA = new EdgeSet(this, nodeA);
            EdgeSet edgeB = new EdgeSet(this, nodeB);
            int type = 0;
            int count[] = new int[16];
            count[1] += edgeA.in.size();
            count[2] += edgeA.out.size();
            count[3] += edgeB.in.size();
            count[4] += edgeB.out.size();
            count[5] += edgeA.mut.size();
            count[6] += edgeB.mut.size();
            if (count[1] + count[2] + count[3] + count[4] + count[5] + count[6] == 0) count[0] += 1;
            motifA.add(nodeB, t, type, count);
            motifA.addresult(nodeB, t, tie);
        }
    }

    private String formatString(int nodeA, int nodeB, int t, int tie, int type, int[] count) {
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < 16; index++) {
            if (count[index] != 0) {
                sb.append(nodeA);
                sb.append(",");
                sb.append(nodeB);
                sb.append(",");
                sb.append(t);
                sb.append(",");
                sb.append(tie);
                sb.append(",");
                sb.append(type * 16 + index);
                sb.append(",");
                sb.append(count[index] + "\n");
            }
        }
        return (sb.toString());
    }

    private void counttwotie(AdjMatrix newmat, Set<Integer> setB, Set<Integer> setB2, int nodeA, int t, BufferedWriter out) throws IOException {

        // first iterate within setB, since setB2 is easier
        for (int nodeB : setB) {
            int tie = 0;
            if (newmat.slist.get(nodeA) != null) {
                if (newmat.slist.get(nodeA).contains(nodeB)) {
                    tie = 1;
                }
            }
            this.norm++;
            // flag for there's any triangle completed
            boolean findTie = false;
            EdgeSet edgeA = new EdgeSet(this, nodeA);
            EdgeSet edgeB = new EdgeSet(this, nodeB);
            int type = 0;
            int[] count = new int[16];

            // determine the link between A and B
            if (edgeA.out.contains(nodeB)) {
                type = 1;
            } else if (edgeA.in.contains(nodeB)) {
                type = 2;
            } else if (edgeA.mut.contains(nodeB)) {
                type = 3;
            }

            // determine the counts of each motif
            count = this.countmotif(edgeA, edgeB);
            for (int i = 0; i < count.length; i++) {
                if (count[i] > 0) findTie = true;
            }
            if (findTie == false) {
                count[0] = 1;
            }
			/*
			 * Now consider case 1 to 6 using setB2
			 * 
			 */
            out.write(formatString(nodeA, nodeB, t, tie, type, count));
        }
        // then iterate within setB2, since setB2 is not current 2hop neighbor, no need to check type 7 onwards
        for (int nodeB : setB2) {
            int tie = 0;
            if (newmat.slist.get(nodeA) != null) {
                if (newmat.slist.get(nodeA).contains(nodeB)) {
                    tie = 1;
                }
            }
            this.norm++;
            // not current neighbor, only in case 1 to 6
            EdgeSet edgeA = new EdgeSet(this, nodeA);
            EdgeSet edgeB = new EdgeSet(this, nodeB);
            int type = 0;
            int count[] = new int[16];
            count[1] += edgeA.in.size();
            count[2] += edgeA.out.size();
            count[3] += edgeB.in.size();
            count[4] += edgeB.out.size();
            count[5] += edgeA.mut.size();
            count[6] += edgeB.mut.size();
            if (count[1] + count[2] + count[3] + count[4] + count[5] + count[6] == 0) count[0] += 1;
            out.write(formatString(nodeA, nodeB, t, tie, type, count));
        }
    }

    private void counttwotie(AdjMatrix newmat, Set<Integer> setB, Set<Integer> setB2, int nodeA, int t, EdgeMotif motifA, int tie) {
        for (int nodeB : setB) {
            this.norm++;
            // flag for there's any triangle completed
            boolean findTie = false;
            EdgeSet edgeA = new EdgeSet(this, nodeA);
            EdgeSet edgeB = new EdgeSet(this, nodeB);
            int type = 0;
            int[] count = new int[16];

            // determine the link between A and B
            if (edgeA.out.contains(nodeB)) {
                type = 1;
            } else if (edgeA.in.contains(nodeB)) {
                type = 2;
            } else if (edgeA.mut.contains(nodeB)) {
                type = 3;
            }

            // determine the counts of each motif
            count = this.countmotif(edgeA, edgeB);
            for (int i = 0; i < count.length; i++) {
                if (count[i] > 0) findTie = true;
            }
            if (findTie == false) {
                count[0] = 1;
            }
			/*
			 * Now consider case 1 to 6 using setB2
			 * 
			 */
            motifA.add(nodeB, t, type, count);
            motifA.addresult(nodeB, t, tie);
        }
    }

    private void counttwotie(AdjMatrix newmat, Set<Integer> setB, Set<Integer> setB2, int nodeA) {

        for (int nodeB : setB) {
            this.norm++;
            // flag for there's any triangle completed
            boolean findTie = false;
            EdgeSet edgeA = new EdgeSet(this, nodeA);
            EdgeSet edgeB = new EdgeSet(this, nodeB);
            int type = 0;
            int[] count = new int[17];

            // determine the link between A and B
            if (edgeA.out.contains(nodeB)) {
                type = 1;
            } else if (edgeA.in.contains(nodeB)) {
                type = 2;
            } else if (edgeA.mut.contains(nodeB)) {
                type = 3;
            }

            // determine the counts of each motif
            count = this.countmotif(edgeA, edgeB);
            for (int i = 0; i < count.length; i++) {
                if (count[i] > 0) findTie = true;
            }
            if (findTie == false) {
                count[0] = 1;
            }
            // the 17th count the how many edges counted
            count[16] = 1;
			/*
			 * Now consider case 1 to 6 using setB2
			 * 
			 */
            for (int i = 0; i < count.length; i++) {
                this.motifcount.get(type)[i] += count[i];
            }
        }
    }


    // without saving individual information
    public HashMap<Integer, int[]> tieyes(AdjMatrix newmat) {

        // find those who has sent in the new adj matrix
        for (int nodeA : newmat.slist.keySet()) {
            Set<Integer> setB = newmat.slist.get(nodeA);
            Set<Integer> setB2 = new HashSet<Integer>();
            if (setB.size() > 0) counttwotie(newmat, setB, setB2, nodeA);
        }
        return (motifcount);
    }

    // without the individual information
    public HashMap<Integer, int[]> tieno(AdjMatrix newmat) {

        // find those who has sent in this adj matrix
        Set<Integer> allnodes = Sets.union(this.slist.keySet(), this.rlist.keySet());
        for (int nodeA : allnodes) {
            Set<Integer> outA = new HashSet<Integer>();
            Set<Integer> inA = new HashSet<Integer>();

            if (this.slist.get(nodeA) != null) {
                outA = this.slist.get(nodeA);
            }
            if (this.rlist.get(nodeA) != null) {
                inA = this.rlist.get(nodeA);
            }

            // need find all neighbors of A with distance = 2 or those who sent to A without A's reply
            Set<Integer> set2nei = new HashSet<Integer>();
            set2nei = Sets.union(set2nei, outA);
            set2nei = Sets.union(set2nei, inA);

            for (int nei : set2nei) {
                // first include all neighbors with distance < 2
                if (this.rlist.get(nei) != null) {
                    set2nei = Sets.union(set2nei, this.rlist.get(nei));
                }
                if (this.slist.get(nei) != null) {
                    set2nei = Sets.union(set2nei, this.slist.get(nei));
                }
            }

            // update hop2 neighbors
            if (this.hop2.get(nodeA) != null) {
                this.hop2.put(nodeA, Sets.union(this.hop2.get(nodeA), set2nei));
            } else {
                this.hop2.put(nodeA, set2nei);
            }

            Set<Integer> temp = new HashSet<Integer>();
            temp.add(nodeA);
            set2nei = Sets.difference(set2nei, temp);

            Set<Integer> setB = new HashSet<Integer>();
            Set<Integer> setB2 = new HashSet<Integer>();

            if (newmat.slist.get(nodeA) != null) {
                setB = Sets.difference(set2nei, newmat.slist.get(nodeA));
                // count repeat edge
            } else {
                setB = set2nei;
            }
            if (setB.size() > 0) counttwotie(newmat, setB, setB2, nodeA);
        }
        return (motifcount);
    }

    // with the individual information
    public HashMap<Integer, int[]> tieyes(AdjMatrix newmat, int t, Allmotif indiv) {

        // find those who has sent in the new adj matrix
        for (int nodeA : newmat.slist.keySet()) {
            Set<Integer> setB = newmat.slist.get(nodeA);
            Set<Integer> setB2 = new HashSet<Integer>();

            if (setB.size() > 0) {
                EdgeMotif motifA;
                if (indiv.get(nodeA) != null) {
                    motifA = indiv.get(nodeA);
                } else {
                    indiv.put(nodeA, new EdgeMotif());
                    motifA = indiv.get(nodeA);
                }
                counttwotie(newmat, setB, setB2, nodeA, t, motifA, 1);
            }
        }
        return (motifcount);
    }

    // with the individual information
    public HashMap<Integer, int[]> tieno(AdjMatrix newmat, int t, Allmotif indiv) {

        // find those who has sent in this adj matrix
        Set<Integer> allnodes = Sets.union(this.slist.keySet(), this.rlist.keySet());
        for (int nodeA : allnodes) {
            Set<Integer> outA = new HashSet<Integer>();
            Set<Integer> inA = new HashSet<Integer>();

            if (this.slist.get(nodeA) != null) {
                outA = this.slist.get(nodeA);
            }
            if (this.rlist.get(nodeA) != null) {
                inA = this.rlist.get(nodeA);
            }

            // need find all neighbors of A with distance = 2 or those who sent to A without A's reply
            Set<Integer> set2nei = new HashSet<Integer>();
            set2nei = Sets.union(set2nei, outA);
            set2nei = Sets.union(set2nei, inA);

            for (int nei : set2nei) {
                // first include all neighbors with distance < 2
                if (this.rlist.get(nei) != null) {
                    set2nei = Sets.union(set2nei, this.rlist.get(nei));
                }
                if (this.slist.get(nei) != null) {
                    set2nei = Sets.union(set2nei, this.slist.get(nei));
                }
            }

            // update hop2 neighbors
            if (this.hop2.get(nodeA) != null) {
                this.hop2.put(nodeA, Sets.union(this.hop2.get(nodeA), set2nei));
            } else {
                this.hop2.put(nodeA, set2nei);
            }


            Set<Integer> temp = new HashSet<Integer>();
            temp.add(nodeA);
            set2nei = Sets.difference(set2nei, temp);

            Set<Integer> setB = new HashSet<Integer>();
            Set<Integer> setB2 = new HashSet<Integer>();

            if (newmat.slist.get(nodeA) != null) {
                setB = Sets.difference(set2nei, newmat.slist.get(nodeA));
                // count repeat edge
            } else {
                setB = set2nei;
            }

			/*
			 * SetB2 is the set of nodes EVER being hop2 to A while not in SetB or A's neighbor now
			 */
            //setB2 = Sets.difference(this.hop2.get(nodeA), setB);

            if (setB.size() > 0) {
                EdgeMotif motifA;
                if (indiv.get(nodeA) != null) {
                    motifA = indiv.get(nodeA);
                } else {
                    indiv.put(nodeA, new EdgeMotif());
                    motifA = indiv.get(nodeA);
                }
                counttwotie(newmat, setB, setB2, nodeA, t, motifA, 0);
            }
        }
        return (motifcount);
    }

    // with the individual information
    public HashMap<Integer, int[]> tieBoth(AdjMatrix newmat, int t, Allmotif indiv) {

        // iterate through all nodes
        Set<Integer> allnodes = Sets.union(this.slist.keySet(), this.rlist.keySet());
        //Set<Integer> oldnodes = new HashSet<Integer>();
        //oldnodes = this.hop2.keySet();
        allnodes = new HashSet<Integer>(Sets.union(allnodes, this.hop2.keySet()));

        HashMap<Integer, Set<Integer>> currenthop2 = new HashMap<Integer, Set<Integer>>();
        System.out.println(this.hop2.size());

        for (int nodeA : allnodes) {
            Set<Integer> outA = new HashSet<Integer>();
            Set<Integer> inA = new HashSet<Integer>();

            // get out and in nodes of A
            if (this.slist.get(nodeA) != null) {
                outA = this.slist.get(nodeA);
            }
            if (this.rlist.get(nodeA) != null) {
                inA = this.rlist.get(nodeA);
            }

            // need find all neighbors of A with distance < 2 or = 2
            Set<Integer> set2nei = new HashSet<Integer>();
            set2nei = Sets.union(set2nei, outA);
            set2nei = Sets.union(set2nei, inA);
            for (int nei : set2nei) {
                if (this.rlist.get(nei) != null) {
                    set2nei = Sets.union(set2nei, this.rlist.get(nei));
                }
                if (this.slist.get(nei) != null) {
                    set2nei = Sets.union(set2nei, this.slist.get(nei));
                }
            }

            // remove Node A himself from the neighbors
            Set<Integer> temp = new HashSet<Integer>();
            temp.add(nodeA);
            set2nei = Sets.difference(set2nei, temp);
            currenthop2.put(nodeA, set2nei);

            // update hop2 neighbors
            if (this.hop2.get(nodeA) != null) {
                this.hop2.put(nodeA, Sets.union(this.hop2.get(nodeA), set2nei));
            } else {
                this.hop2.put(nodeA, set2nei);
            }
        }
        System.out.println(this.hop2.size());
        for (int nodeA : this.hop2.keySet()) {

			/*
			 * Now setB is the nodes in A's hop2 neighbors
			 * 	   setB2 is the nodes not in current, but previous hop2 neighbors
			 */
            Set<Integer> set2nei = currenthop2.get(nodeA);
            Set<Integer> setB = set2nei;
            Set<Integer> setB2 = Sets.difference(this.hop2.get(nodeA), set2nei);

            EdgeMotif motifA;
            if (indiv.get(nodeA) != null) {
                motifA = indiv.get(nodeA);
            } else {
                indiv.put(nodeA, new EdgeMotif());
                motifA = indiv.get(nodeA);
            }
            counttwotie(newmat, setB, setB2, nodeA, t, motifA);

        }
        return (motifcount);
    }

    // with the individual information
    public HashMap<Integer, int[]> tieBoth(AdjMatrix newmat, int t, String path) throws IOException {
        BufferedWriter out = new BufferedWriter(new BufferedWriter(new FileWriter(path, true)));

        // iterate through all nodes
        Set<Integer> allnodes = Sets.union(this.slist.keySet(), this.rlist.keySet());
        //Set<Integer> oldnodes = new HashSet<Integer>();
        //oldnodes = this.hop2.keySet();
        allnodes = new HashSet<Integer>(Sets.union(allnodes, this.hop2.keySet()));

        HashMap<Integer, Set<Integer>> currenthop2 = new HashMap<Integer, Set<Integer>>();
        System.out.println(this.hop2.size());

        for (int nodeA : allnodes) {
            Set<Integer> outA = new HashSet<Integer>();
            Set<Integer> inA = new HashSet<Integer>();

            // get out and in nodes of A
            if (this.slist.get(nodeA) != null) {
                outA = this.slist.get(nodeA);
            }
            if (this.rlist.get(nodeA) != null) {
                inA = this.rlist.get(nodeA);
            }

            // need find all neighbors of A with distance < 2 or = 2
            Set<Integer> set2nei = new HashSet<Integer>();
            set2nei = Sets.union(set2nei, outA);
            set2nei = Sets.union(set2nei, inA);
            for (int nei : set2nei) {
                if (this.rlist.get(nei) != null) {
                    set2nei = Sets.union(set2nei, this.rlist.get(nei));
                }
                if (this.slist.get(nei) != null) {
                    set2nei = Sets.union(set2nei, this.slist.get(nei));
                }
            }

            // remove Node A himself from the neighbors
            Set<Integer> temp = new HashSet<Integer>();
            temp.add(nodeA);
            set2nei = Sets.difference(set2nei, temp);
            currenthop2.put(nodeA, set2nei);

            // update hop2 neighbors
            if (this.hop2.get(nodeA) != null) {
                this.hop2.put(nodeA, Sets.union(this.hop2.get(nodeA), set2nei));
            } else {
                this.hop2.put(nodeA, set2nei);
            }
        }
        System.out.println(this.hop2.size());
        for (int nodeA : this.hop2.keySet()) {

				/*
				 * Now setB is the nodes in A's hop2 neighbors
				 * 	   setB2 is the nodes not in current, but previous hop2 neighbors
				 */
            Set<Integer> set2nei = currenthop2.get(nodeA);
            Set<Integer> setB = set2nei;
            Set<Integer> setB2 = Sets.difference(this.hop2.get(nodeA), set2nei);

            counttwotie(newmat, setB, setB2, nodeA, t, out);
        }
        out.close();
        return (motifcount);
    }

    public void printdegree(String degPath, int t) throws IOException {
        BufferedWriter out = new BufferedWriter(new BufferedWriter(new FileWriter(degPath, true)));
        Set<Integer> allnodes = Sets.union(this.slist.keySet(), this.rlist.keySet());
        for (Integer node : allnodes) {
            EdgeSet counts = new EdgeSet(this, node);
            out.write(t + "," + node + "," + counts.in.size() + "," + counts.out.size() + "," + counts.mut.size() + "\n");
        }
        out.close();
    }


}
