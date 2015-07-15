package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

public class AdjMat {
    HashMap<Long, Integer> dict;
    public int V;
    public int E;
    HashMap<Integer, Set<Integer>> slist;
    HashMap<Integer, Set<Integer>> rlist;
    public int[][] result = new int[2][6];
    public int norm = 0;
    public HashMap<Integer, int[][]> detail = new HashMap<Integer, int[][]>();


    public AdjMat() {
        this.dict = new HashMap<Long, Integer>();
        this.V = 0;
        this.E = 0;
        this.norm = 0;
        this.slist = new HashMap<Integer, Set<Integer>>();
        this.rlist = new HashMap<Integer, Set<Integer>>();
        this.detail = new HashMap<Integer, int[][]>();
    }

    // inherite the list of dict from the previous AdjMat object
    @SuppressWarnings("unchecked")
    public AdjMat(AdjMat adj) {
        this.dict = (HashMap<Long, Integer>) adj.dict.clone();
        this.V = adj.V;
        this.E = 0;
        this.norm = 0;
        this.slist = new HashMap<Integer, Set<Integer>>();
        this.rlist = new HashMap<Integer, Set<Integer>>();
        this.detail = new HashMap<Integer, int[][]>();
    }

    // clone prvious adjMat object
    @SuppressWarnings("unchecked")
    public void clone(AdjMat adj) {
        this.dict = (HashMap<Long, Integer>) adj.dict.clone();
        this.slist = (HashMap<Integer, Set<Integer>>) adj.slist.clone();
        this.rlist = (HashMap<Integer, Set<Integer>>) adj.rlist.clone();
        this.detail = new HashMap<Integer, int[][]>();
        this.V = adj.V;
        this.E = adj.E;
        this.norm = adj.norm;
    }

    // print adj information
    public void printinf() {
        System.out.println("Adjacency Matrix with " + (this.V) + " Nodes");
        System.out.println("Adjacency Matrix with " + (this.E) + " Edges");
    }


    // add new edge to AdjMat
    public void add(Long s, Long r, double rand, int max) {
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


    public int[] onetie(AdjMat newmat) {
        int same = 0;
        int ret = 0;
        for (int send : newmat.slist.keySet()) {
            // if the sender is new, we don't care
            // if the sender exist, check how many receiver also exist
            if (this.slist.get(send) != null) {
                Set<Integer> common = Sets.intersection(this.slist.get(send), newmat.slist.get(send));
                same += common.size();
            }
            if (this.rlist.get(send) != null) {
                Set<Integer> common = Sets.intersection(this.rlist.get(send), newmat.slist.get(send));
                ret += common.size();
            }
        }
        int[] result = new int[2];
        result[0] = same;
        result[1] = ret;
        return (result);
    }

    private void counttwotie(AdjMat newmat, Set<Integer> setB, Set<Integer> setB2, Set<Integer> outA, int nodeA, boolean saveIndiv) {
        Set<Integer> setC = new HashSet<Integer>();

        for (int nodeB : setB) {
            this.norm++;
            int[][] detailtemp = new int[2][6];

            // flag for there's any triangle completed
            boolean tie2 = false;

            if (this.rlist.get(nodeB) != null) {
                // type 3
                Set<Integer> inB = this.rlist.get(nodeB);
                if (outA.size() > 0) {
                    setC = Sets.intersection(outA, inB);
                    this.result[0][2] += setC.size();
                    if (setC.size() > 0 && saveIndiv) {
                        detailtemp[0][2] += setC.size();
                    }
                    tie2 = tie2 || (setC.size() > 0);
                }

                // type 5
                if (this.rlist.get(nodeA) != null) {
                    setC = Sets.intersection(this.rlist.get(nodeA), inB);
                    this.result[0][4] += setC.size();
                    if (setC.size() > 0 && saveIndiv) {
                        detailtemp[0][4] += setC.size();

                    }
                    tie2 = tie2 || (setC.size() > 0);
                }
            }

            if (this.slist.get(nodeB) != null) {
                // now check if there's one tie
                // type 2
                if (this.slist.get(nodeB).contains(nodeA)) {
                    tie2 = true;
                    this.result[0][1]++;
                    if (saveIndiv) {
                        detailtemp[0][1]++;
                    }
                }

                // type 4
                Set<Integer> outB = this.slist.get(nodeB);
                if (outA.size() > 0) {
                    setC = Sets.intersection(outA, outB);
                    this.result[0][3] += setC.size();
                    if (setC.size() > 0 && saveIndiv) {
                        detailtemp[0][3] += setC.size();
                    }
                    tie2 = tie2 || (setC.size() > 0);
                }
                // type 6
                if (this.rlist.get(nodeA) != null) {
                    setC = Sets.intersection(this.rlist.get(nodeA), outB);
                    this.result[0][5] += setC.size();
                    if (setC.size() > 0 && saveIndiv) {
                        detailtemp[0][5] += setC.size();
                    }
                    tie2 = tie2 || (setC.size() > 0);
                }
            }
            // if no one tie or two tie in this adjacency matrix
            if (!tie2) {
                this.result[0][0]++;
                detailtemp[0][0]++;
            }
            int next = this.detail.size();
            this.detail.put(next, detailtemp);
        }

		/*
         * Now consider repeated edges
		 * 
		 */
        if (setB2.size() > 0) {
            for (int nodeB : setB2) {
                int[][] detailtemp = new int[2][6];
                // flag for there's any triangle completed
                boolean tie2 = false;

                if (this.rlist.get(nodeB) != null) {
                    // type 3
                    Set<Integer> inB = this.rlist.get(nodeB);
                    setC = Sets.intersection(outA, inB);
                    this.result[1][2] += setC.size();
                    if (setC.size() > 0 && saveIndiv) {
                        detailtemp[1][2] += setC.size();
                    }
                    tie2 = tie2 || (setC.size() > 0);

                    // type 5
                    if (this.rlist.get(nodeA) != null) {
                        setC = Sets.intersection(this.rlist.get(nodeA), inB);
                        this.result[1][4] += setC.size();
                        if (setC.size() > 0 && saveIndiv) {
                            detailtemp[1][4] += setC.size();
                        }
                        tie2 = tie2 || (setC.size() > 0);
                    }
                }

                if (this.slist.get(nodeB) != null) {
                    // now check if there's one tie
                    // type 2
                    if (this.slist.get(nodeB).contains(nodeA)) {
                        tie2 = true;
                        this.result[1][1]++;
                        if (setC.size() > 0 && saveIndiv) {
                            detailtemp[1][1] += setC.size();
                        }
                    }

                    // type 4
                    Set<Integer> outB = this.slist.get(nodeB);
                    setC = Sets.intersection(outA, outB);
                    this.result[1][3] += setC.size();
                    if (setC.size() > 0 && saveIndiv) {
                        detailtemp[1][3] += setC.size();
                    }
                    tie2 = tie2 || (setC.size() > 0);

                    // type 6
                    if (this.rlist.get(nodeA) != null) {
                        setC = Sets.intersection(this.rlist.get(nodeA), outB);
                        this.result[1][5] += setC.size();
                        if (setC.size() > 0 && saveIndiv) {
                            detailtemp[1][5] += setC.size();
                        }
                        tie2 = tie2 || (setC.size() > 0);
                    }
                }
                // if no one tie or two tie in this adjacency matrix
                if (!tie2) {
                    this.result[1][0]++;
                    detailtemp[1][0] += setC.size();
                }
                int next = this.detail.size();
                this.detail.put(next, detailtemp);
            }
        }
    }


    public int[][] twotie(AdjMat newmat) {
		/* 
		 * the new edge is A -> B		 
		 * the motifs to check:
		 * 
		 * result[][0 or 1]:
		 * type 0: A   B
		 * type 1: A <- B
		 * type 2: A -> C -> B
		 * type 3: A -> C <- B
		 * type 4: A <- C -> B
		 * type 5: A <- C <- B
		 * 
		 */
        this.result = new int[2][6];
        this.detail = new HashMap<Integer, int[][]>();

        for (int nodeA : newmat.slist.keySet()) {
            Set<Integer> outA = new HashSet<Integer>();

            if (this.slist.get(nodeA) != null) {
                //result[0] += newmat.sList.get(nodeA).size(); continue;}
                outA = this.slist.get(nodeA);
                ;
            }


            // list of new ties sent by "send"

            Set<Integer> setB = new HashSet<Integer>();
            Set<Integer> setB2 = new HashSet<Integer>();
            if (outA.size() > 0) {
                setB = Sets.difference(newmat.slist.get(nodeA), outA);
                // count repeat edge
                setB2 = Sets.intersection(newmat.slist.get(nodeA), outA);
            } else {
                setB = newmat.slist.get(nodeA);
            }

            this.counttwotie(newmat, setB, setB2, outA, nodeA, true);
        }
        return (this.result);
    }


    // this method counts the number of no-tie's following each of the motif
    public int[][] notwotie(AdjMat newmat) {
        this.result = new int[2][6];
        this.detail = new HashMap<Integer, int[][]>();

		/* 
		 * the new edge is A -> B		 
		 * the motifs to check:
		 * 
		 * result[][0 or 1]:
		 * type 0: A   B
		 * type 1: A <- B
		 * type 2: A -> C -> B
		 * type 3: A -> C <- B
		 * type 4: A <- C -> B
		 * type 5: A <- C <- B
		 * 
		 */

        for (int nodeA : this.slist.keySet()) {
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

            for (int nei : outA) {
                // first include all neighbors with distance < 2
                if (this.rlist.get(nei) != null) {
                    set2nei = Sets.union(set2nei, this.rlist.get(nei));
                }
                if (this.slist.get(nei) != null) {
                    set2nei = Sets.union(set2nei, this.slist.get(nei));
                }
            }
            // then delet those A has sent to
            set2nei = Sets.difference(set2nei, outA);
            Set<Integer> temp = new HashSet<Integer>();
            temp.add(nodeA);
            set2nei = Sets.difference(set2nei, temp);

            // list of new ties sent by "send"

            Set<Integer> setB = new HashSet<Integer>();
            Set<Integer> setB2 = new HashSet<Integer>();

            if (newmat.slist.get(nodeA) != null) {
                setB = Sets.difference(set2nei, newmat.slist.get(nodeA));
                // count repeat edge
                setB2 = Sets.difference(outA, newmat.slist.get(nodeA));
            } else {
                setB = set2nei;
                setB2 = outA;
            }

            this.counttwotie(newmat, setB, setB2, outA, nodeA, true);
        }
        return (result);
    }

}
