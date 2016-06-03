package data;

import com.google.common.collect.Sets;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * Class holding Information for each node
 *
 */

public class NodeMotif {
    // ID
    public int id;
    // label: whether the node is in the validation set
    // 0 or 1, 0 -> y = -1 (signed before motif counting period); 1 -> otherwise;
    public int label;
    // outcome: y = -1 if not in validation set; y = 0 if not signed up; y = 1 if signed up
    public int y;
    // time signed up
    public double t;
    // incoming calls frequency
    public int inFreq;
    // outgoing calls frequency
    public int outFreq;
    // List of sender
    public Set<Integer> sList = new HashSet<Integer>();
    // List of receiver
    public Set<Integer> rList = new HashSet<Integer>();
    // List of mutual contact
    public Set<Integer> mList = new HashSet<Integer>();
    // List of all contact
    public Set<Integer> nList = new HashSet<Integer>();
    // communication frequency for all contact
    public HashMap<Integer, Integer> nListFreq = new HashMap<Integer, Integer>();

    // Motif counts
    public HashMap<Integer, Integer> motif = new HashMap<Integer, Integer>();

    /**
     * function to remove all lists, but save the label and y
     */
    public void swipe() {
        this.inFreq = 0;
        this.outFreq = 0;
        this.sList = new HashSet<Integer>();
        this.rList = new HashSet<Integer>();
        this.mList = new HashSet<Integer>();
        this.nList = new HashSet<Integer>();
    }

    /**
     * helper function to count call frequency
     * @param id the index of the other node
     */
    public void countFreq(int id) {
        if (this.nListFreq.get(id) != null) {
            this.nListFreq.put(id, this.nListFreq.get(id) + 1);
        } else {
            this.nListFreq.put(id, 1);
        }
    }


    public NodeMotif(){
        this.swipe();
    }

    /** initialization with known negative outcome
     *
     * @param id the index of this node
     */
    public NodeMotif(int id) {
        this.id = id;
        this.y = 0;
        this.label = 0;
        this.t = 0;
    }

    /**
     * initialization with full information
     *
     * @param id index of this node
     * @param t time of signing up
     * @param y the outcome of this node
     * @param label whether or not this node is in validation set
     */
    public NodeMotif(int id, double t, int y, int label) {
        this.id = id;
        this.t = t;
        this.y = y;
        this.label = label;
    }

    /**
     * function to reset the lists
     */
    public void reset() {
        this.sList = new HashSet<Integer>();
        this.rList = new HashSet<Integer>();
        this.nList = new HashSet<Integer>();
        this.mList = new HashSet<Integer>();
        this.nListFreq = new HashMap<Integer, Integer>();
    }

    /**
     * function to count a outgoing call
     * @param n the index of the other node
     */
    public void sendto(Integer n) {
        if (this.id == n) {
            return;
        }
        this.sList.add(n);
        this.nList.add(n);
        this.countFreq(n);
    }

    /**
     * function to count a incoming call
     * @param n the index of the other node
     */
    public void recfrom(Integer n) {
        if (this.id == n) {
            return;
        }
        this.rList.add(n);
        this.nList.add(n);
        this.countFreq(n);
    }

    /**
     * @param n lower threshold of counting
     */
    public void thinFreq(int n) {
        HashSet<Integer> remain = new HashSet<Integer>();
        for (int node : this.nListFreq.keySet()) {
            if (this.nListFreq.get(node) >= n) remain.add(node);
        }
        this.rList = Sets.intersection(this.rList, remain);
        this.sList = Sets.intersection(this.sList, remain);
    }

    // get mutual and neighbor
    public void organize() {
        this.mList = Sets.intersection(this.sList, this.rList);
        this.sList = Sets.difference(this.sList, this.mList);
        this.rList = Sets.difference(this.rList, this.mList);

    }

    // helper function to get the set needed
    // indicators: s-1, r-2, m-3, n-4
    public Set<Integer> getNei(int t0) {
        Set<Integer> toget = new HashSet<Integer>();
        if (t0 == 1) {
            toget = this.sList;
        } else if (t0 == 2) {
            toget = this.rList;
        } else if (t0 == 3) {
            toget = this.mList;
        } else if (t0 == 4) {
            toget = this.nList;
        }
        // else return all neighbors
        return (toget);
    }


    // helper function to get the set needed, with specified Y value
    // indicators: s-1, r-2, m-3, n-4
    public Set<Integer> getNei(int t0, int label, NodeMotifHashMap nodeMap) {
        Set<Integer> toget = new HashSet<Integer>();
        if (t0 == 1) {
            toget = this.sList;
        } else if (t0 == 2) {
            toget = this.rList;
        } else if (t0 == 3) {
            toget = this.mList;
        } else if (t0 == 4) {
            toget = this.nList;
        }
        Set<Integer> toreturn = new HashSet<Integer>();
        for (int node : toget) {
            if (nodeMap.nodes.get(node).label == label) {
                toreturn.add(node);
            }
        }
        // else return all neighbors
        return (toreturn);
    }


    // helper function to get 2-hop friends,
    //   i.e., type-t0-friend's type-t1-friend
    /*
	 * update Nov 10,2014, it could not be returning a set, needs to be a list
	 */
//	private ArrayList<Integer> get2hop(NodeMotifHashMap nodeMap, int t0, int t1){
//		ArrayList<Integer> hop2 = new ArrayList<Integer>();
//		Set<Integer> hop1 = this.getNei(t0);
//		for(int nei1 : hop1){
//			for(int nei2 : nodes.get(nei1).getNei(t1))
//			if(nei2 != this.id) hop2.add(nei2);
//		}
////		Set<Integer> temp = new HashSet<Integer>();
////		temp.add(this.id);
////		hop2 = Sets.difference(hop2, temp);
//		return(hop2);
//	}

    // helper function to get 2-hop friends, with specified y values
    //   i.e., type-t0-friend's type-t1-friend
	/*
	 * update Nov 10,2014, it could not be returning a set, needs to be a list
	 */
    private ArrayList<Integer> get2hop(NodeMotifHashMap nodeMap, int t0, int t1, int label0, int label1) {
        ArrayList<Integer> hop2 = new ArrayList<Integer>();
        Set<Integer> hop1 = this.getNei(t0, label0, nodeMap);
        for (int nei1 : hop1) {
            for (int nei2 : nodeMap.nodes.get(nei1).getNei(t1, label1, nodeMap))
                if (nei2 != this.id) hop2.add(nei2);
            //	hop2.addAll(nodeMap.nodes.get(nei1).getNei(t1, label1, nodeMap));
        }
//		Set<Integer> temp = new HashSet<Integer>();
//		temp.add(this.id);
//		hop2 = Sets.difference(hop2, temp);
        return (hop2);
    }

    // helper function to count isolated nodes.
    //  i.e. how many nodes only has you alone as friend of any type
    //       and the node itself has to have only one friend too
//	private int countISO(NodeMotifHashMap nodeMap, int t0){
//		int iso = 0;
//		Set<Integer> friends = getNei(t0);
//		if(friends.size() > 1){
//			return(0);
//		}
//		for(int fri : friends){
//			if(nodes.get(fri).nList.size() == 1) iso++;
//		}
//		return(iso);
//	}
//
//	// helper function to count isolated nodes and satisfy given Y value.
//	//  i.e. how many nodes only has you alone as friend of any type
//	//       and the node itself has to have only one friend too
//	private int countISO(NodeMotifHashMap nodeMap, int t0, int label){
//		int iso = 0;
//		Set<Integer> friends = getNei(t0);
//		if(friends.size() > 1){
//			return(0);
//		}
//		for(int fri : friends){
//			if(nodes.get(fri).nList.size() == 1 & nodes.get(fri).label == label) iso++;
//		}
//		return(iso);
//	}
//
    // helper function to count [new] isolated nodes and satisfy given Y value.
    //  i.e. how many nodes only has you alone as friend of any type
    //       but you could have more than one friend
    private int countISO_new(NodeMotifHashMap nodeMap, int t0, int label) {
        int iso = 0;
        Set<Integer> friends = getNei(t0);
        for (int fri : friends) {
            if (nodeMap.nodes.get(fri).nList.size() == 1 & nodeMap.nodes.get(fri).label == label) iso++;
        }
        return (iso);
    }

//	// calculate node motifs
//	public void motifCount(NodeMotifHashMap nodeMap){
//		// if iso by itself, return with type 0
//		if(this.nList.size() == 0){
//			this.motif.put(0,  1);
//			for(int i = 1; i < 34; i++){this.motif.put(i, 0);}
//			return;
//		}else{
//			this.motif.put(0,  0);
//		}
//		ArrayList<Integer> mm = get2hop(nodes, 3,3);
//		ArrayList<Integer> ms = get2hop(nodes, 3,1);
//		ArrayList<Integer> mr = get2hop(nodes, 3,2);
//		ArrayList<Integer> sm = get2hop(nodes, 1,3);
//		ArrayList<Integer> ss = get2hop(nodes, 1,1);
//		ArrayList<Integer> sr = get2hop(nodes, 1,2);
//		ArrayList<Integer> rm = get2hop(nodes, 2,3);
//		ArrayList<Integer> rs = get2hop(nodes, 2,1);
//		ArrayList<Integer> rr = get2hop(nodes, 2,2);
//
//		//this.motif.put(33, Sets.intersection(this.mList, mm).size() / 2 );
//		this.motif.put(33,  ListUtils.intersection(this.mlistlist, mm).size() / 2);
//
//		this.motif.put(32, ListUtils.intersection(this.slistlist, mm).size()     );
//		this.motif.put(31, ListUtils.intersection(this.rlistlist, mm).size()     );
//		this.motif.put(30, ListUtils.intersection(this.slistlist, sm).size() / 2 );
//		this.motif.put(29, ListUtils.intersection(this.rlistlist, rm).size() / 2 );
//		this.motif.put(28, ListUtils.intersection(this.rlistlist, sm).size()     );
//
//		this.motif.put(27, ListUtils.intersection(this.slistlist, mr).size()     );
//		this.motif.put(26, ListUtils.intersection(this.rlistlist, mr).size()     );
//		this.motif.put(25, ListUtils.intersection(this.rlistlist, ss).size()     );
//
//		this.motif.put(24, ListUtils.intersection(this.mlistlist, ms).size()     );
//		this.motif.put(23, ListUtils.intersection(this.slistlist, ms).size()     );
//		this.motif.put(22, ListUtils.intersection(this.rlistlist, ms).size()     );
//		this.motif.put(21, ListUtils.intersection(this.slistlist, ss).size()     );
//		this.motif.put(20, ListUtils.intersection(this.rlistlist, rs).size()     );
//		this.motif.put(19, ListUtils.intersection(this.rlistlist, sr).size()     );
//
//		this.motif.put(18, this.mList.size() * (this.mList.size() - 1) / 2
//				- this.motif.get(33) - this.motif.get(24));
//		this.motif.put(17, this.mList.size() * this.sList.size()
//				- this.motif.get(23) - this.motif.get(27)- this.motif.get(32));
//		this.motif.put(16, this.mList.size() * this.rList.size()
//				- this.motif.get(22) - this.motif.get(26)- this.motif.get(31));
//		this.motif.put(15, this.sList.size() * (this.sList.size() - 1 ) / 2
//				- this.motif.get(21) - this.motif.get(30));
//		this.motif.put(14, this.rList.size() * (this.rList.size() - 1 ) / 2
//				- this.motif.get(20) - this.motif.get(29));
//		this.motif.put(13, this.sList.size() * this.rList.size()
//				- this.motif.get(19) - this.motif.get(25) - this.motif.get(28));
//
//		//		if(this.motif.get(18) < 0){
//		//			System.out.println(18);
//		//			System.out.println( this.mList.size()  );
//		//			System.out.println(this.motif.get(33));
//		//			System.out.println(this.motif.get(24));
//		//		}
//		//		if(this.motif.get(17) < 0) System.out.println(17);
//		//		if(this.motif.get(16) < 0) System.out.println(16);
//		//		if(this.motif.get(15) < 0) System.out.println(15);
//		//		if(this.motif.get(14) < 0) System.out.println(14);
//		//		if(this.motif.get(13) < 0) System.out.println(13);
//		//
//
//		this.motif.put(12, ListUtils.subtract(mm, this.nlistlist).size());
//		this.motif.put(11, ListUtils.subtract(sm, this.nlistlist).size());
//		this.motif.put(10, ListUtils.subtract(rm, this.nlistlist).size());
//		this.motif.put(9, ListUtils.subtract(mr, this.nlistlist).size());
//		this.motif.put(8, ListUtils.subtract(sr, this.nlistlist).size());
//		this.motif.put(7, ListUtils.subtract(rr, this.nlistlist).size());
//		this.motif.put(6, ListUtils.subtract(ms, this.nlistlist).size());
//		this.motif.put(5, ListUtils.subtract(ss, this.nlistlist).size());
//		this.motif.put(4, ListUtils.subtract(rs, this.nlistlist).size());
//
//		this.motif.put(3, countISO(nodes, 3));
//		this.motif.put(2, countISO(nodes, 1));
//		this.motif.put(1, countISO(nodes, 2));
//
//	}

    // calculate dyad motifs
//	public void dyadCount2(NodeMotifHashMap nodeMap){
//		this.motif.put(1,  countISO(nodes, 2, 0));
//		this.motif.put(2,  countISO(nodes, 2, 1));
//		this.motif.put(3,  countISO(nodes, 1, 0));
//		this.motif.put(4,  countISO(nodes, 1, 1));
//		this.motif.put(5,  countISO(nodes, 3, 0));
//		this.motif.put(6,  countISO(nodes, 3, 1));
//	}

    // calculate dyad motifs with new definition
    public void dyadCount2_new(NodeMotifHashMap nodeMap) {
        this.motif.put(1, countISO_new(nodeMap, 2, 0));
        this.motif.put(2, countISO_new(nodeMap, 2, 1));
        this.motif.put(3, countISO_new(nodeMap, 1, 0));
        this.motif.put(4, countISO_new(nodeMap, 1, 1));
        this.motif.put(5, countISO_new(nodeMap, 3, 0));
        this.motif.put(6, countISO_new(nodeMap, 3, 1));
    }

    // helper function to implement list intersection with set
    // e.g. (1,1,2,3) intersect (1, 2)  --> (1, 1, 2)
    public int intersect(ArrayList<Integer> list, Set<Integer> set) {
        int count = 0;
        for (int element : list) {
            if (set.contains(element)) count++;
        }
        return (count);
    }

    // helper function to implement list difference with set
    // e.g. (1,1,2,3) diff (1, 2)  --> (3)
    public int diff(ArrayList<Integer> list, Set<Integer> set) {
        int count = 0;
        for (int element : list) {
            if (!set.contains(element)) count++;
        }
        return (count);
    }

    // calculate three-node motifs
    public void triCount(NodeMotifHashMap nodeMap, int label0, int label1) {
        // this counts the "left" angle
//		Set<Integer> mm = get2hop(nodes, 3,3, label0, label1);
//		Set<Integer> ms = get2hop(nodes, 3,1, label0, label1);
//		Set<Integer> mr = get2hop(nodes, 3,2, label0, label1);
//		Set<Integer> sm = get2hop(nodes, 1,3, label0, label1);
//		Set<Integer> ss = get2hop(nodes, 1,1, label0, label1);
//		Set<Integer> sr = get2hop(nodes, 1,2, label0, label1);
//		Set<Integer> rm = get2hop(nodes, 2,3, label0, label1);
//		Set<Integer> rs = get2hop(nodes, 2,1, label0, label1);
//		Set<Integer> rr = get2hop(nodes, 2,2, label0, label1);

        ArrayList<Integer> mm = get2hop(nodeMap, 3, 3, label0, label1);
        ArrayList<Integer> ms = get2hop(nodeMap, 3, 1, label0, label1);
        ArrayList<Integer> mr = get2hop(nodeMap, 3, 2, label0, label1);
        ArrayList<Integer> sm = get2hop(nodeMap, 1, 3, label0, label1);
        ArrayList<Integer> ss = get2hop(nodeMap, 1, 1, label0, label1);
        ArrayList<Integer> sr = get2hop(nodeMap, 1, 2, label0, label1);
        ArrayList<Integer> rm = get2hop(nodeMap, 2, 3, label0, label1);
        ArrayList<Integer> rs = get2hop(nodeMap, 2, 1, label0, label1);
        ArrayList<Integer> rr = get2hop(nodeMap, 2, 2, label0, label1);

        // divider for homophily graph
        int homo = 1;
        if (label0 == label1) {
            homo = 2;
        }
		/* update Nov 10, 2014, correct way to calculate intersection (with replicate)
		 * mm is always the larger, mlistlist has no replicate
		 *   List<Integer> test = ListUtils.subtract(mm, ListUtils.subtract(mm, this.mlistlist));
		 * Similarly, for set difference, to keep replicate,
		 *   List<Integer> test = ListUtils.subtract(mm, ListUtils.intersection(this.mlistlist, mm));
		 */
        this.motif.put(118 + label0 + label1, this.intersect(mm, this.mList) / homo);
        this.motif.put(114 + 2 * label0 + label1, this.intersect(mm, this.sList));
        this.motif.put(110 + 2 * label0 + label1, this.intersect(mm, this.rList));
        this.motif.put(107 + label0 + label1, this.intersect(sm, this.sList) / homo);
        this.motif.put(104 + label0 + label1, this.intersect(rm, this.rList) / homo);
        this.motif.put(100 + 2 * label0 + label1, this.intersect(rm, this.sList));

        this.motif.put(96 + 2 * label0 + label1, this.intersect(mr, this.sList));

//		if(label0 == label1){
//			System.out.println(mm.toString() + ":::" + this.mList.toString()
//					+":::"+this.intersect(mm, this.mList)  );
//		}
        this.motif.put(92 + 2 * label0 + label1, this.intersect(mr, this.rList));
        this.motif.put(88 + 2 * label0 + label1, this.intersect(rr, this.sList));

        this.motif.put(84 + 2 * label0 + label1, this.intersect(ms, this.mList));
        this.motif.put(80 + 2 * label0 + label1, this.intersect(ms, this.sList));
        this.motif.put(76 + 2 * label0 + label1, this.intersect(ms, this.rList));
        this.motif.put(72 + 2 * label0 + label1, this.intersect(ss, this.sList));
        this.motif.put(68 + 2 * label0 + label1, this.intersect(rs, this.rList));
        this.motif.put(64 + 2 * label0 + label1, this.intersect(rs, this.sList));

        // if homo, N1 = N2, the total count is N1 * (N2 -1) / 2
        // if not homo, it is N1 * N2
		/* the factor is a counter for symmetric cases
		/* if label0 = label1 = 1, the term to be subtracter should have index (2*label0 + label1)
		/* if (0,1) or (1,0), it should be both (label0 + label1) + (label0 + label1 + 1)
		 * 		then to avoid using motifs not counted yet, need to count only (label0 + label1),
		 * 			and adjust the other one later.
		 * Not also the factor is not applied to the last line of motifs, since they are also symmetric.
		 * So we need the factor to help find the correct index.
		 */
        int fac = label0 + label1;
        this.motif.put(61 + label0 + label1,
                this.getNei(3, label0, nodeMap).size()
                        * (this.getNei(3, label1, nodeMap).size() - homo + 1) / homo
                        - this.motif.get(84 + fac * label0 + label1) - this.motif.get(118 + label0 + label1));

        this.motif.put(57 + 2 * label0 + label1,
                this.getNei(3, label0, nodeMap).size()
                        * this.getNei(1, label1, nodeMap).size()
                        - this.motif.get(80 + 2 * label0 + label1) - this.motif.get(96 + 2 * label0 + label1) - this.motif.get(114 + 2 * label0 + label1));

        this.motif.put(53 + 2 * label0 + label1,
                this.getNei(3, label0, nodeMap).size()
                        * this.getNei(2, label1, nodeMap).size()
                        - this.motif.get(76 + 2 * label0 + label1) - this.motif.get(92 + 2 * label0 + label1) - this.motif.get(110 + 2 * label0 + label1));

        this.motif.put(50 + label0 + label1,
                this.getNei(1, label0, nodeMap).size()
                        * (this.getNei(1, label1, nodeMap).size() - homo + 1) / homo
                        - this.motif.get(72 + fac * label0 + label1) - this.motif.get(107 + label0 + label1));

        this.motif.put(47 + label0 + label1,
                this.getNei(2, label0, nodeMap).size()
                        * (this.getNei(2, label1, nodeMap).size() - homo + 1) / homo
                        - this.motif.get(68 + fac * label0 + label1) - this.motif.get(104 + label0 + label1));

        this.motif.put(43 + 2 * label0 + label1,
                this.getNei(2, label0, nodeMap).size()
                        * this.getNei(1, label1, nodeMap).size()
                        - this.motif.get(64 + 2 * label0 + label1) - this.motif.get(88 + 2 * label0 + label1) - this.motif.get(100 + 2 * label0 + label1));

        // adjust for symmetric case when first label and second label not equal
		/* i.e. for type 62, counting 0 <-> A <-> 1
		 *  it should be |A<->0| * |A<->1| - type 85 - type 86 - type 119
		 *  the calculation above only subtracts type 85 and type 119
		 *
		 *  since only the second time (0, 1) or (1, 0) is called that we know how to adjust
		 */

        if (label0 > label1) {
            this.motif.put(62, this.motif.get(62) - this.motif.get(86));
            this.motif.put(51, this.motif.get(51) - this.motif.get(74));
            this.motif.put(48, this.motif.get(48) - this.motif.get(70));
        }

        // here we could only use nList, since the previous set considers label
        this.motif.put(39 + 2 * label0 + label1, this.diff(mm, this.nList));
        this.motif.put(35 + 2 * label0 + label1, this.diff(sm, this.nList));
        this.motif.put(31 + 2 * label0 + label1, this.diff(rm, this.nList));
        this.motif.put(27 + 2 * label0 + label1, this.diff(mr, this.nList));
        this.motif.put(23 + 2 * label0 + label1, this.diff(sr, this.nList));
        this.motif.put(19 + 2 * label0 + label1, this.diff(rr, this.nList));
        this.motif.put(15 + 2 * label0 + label1, this.diff(ms, this.nList));
        this.motif.put(11 + 2 * label0 + label1, this.diff(ss, this.nList));
        this.motif.put(7 + 2 * label0 + label1, this.diff(rs, this.nList));
    }

    /**
     * calculate motifs with label
     * @param nodeMap HashMap of all the NodeMotifs
     */
    public void motifCount_wlabel(NodeMotifHashMap nodeMap) {
        // if iso by itself, return with type 0
        for (int i = 1; i < 121; i++) {
            this.motif.put(i, 0);
        }

        if (this.nList.size() == 0) {
            this.motif.put(0, 1);
            return;
        } else {
            this.motif.put(0, 0);
        }
        // count dyad
        this.dyadCount2_new(nodeMap);

		/*
		 * NOTE: the order to call (0,1) and (1,0) should not be changed!
		 */
        this.triCount(nodeMap, 0, 0);
        this.triCount(nodeMap, 0, 1);
        this.triCount(nodeMap, 1, 0);
        this.triCount(nodeMap, 1, 1);
    }

    public void printTo(BufferedWriter sc, int nvar) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(this.y)
        .append(",")
        .append(this.t)
        .append(",");

        for (int i = 0; i < nvar - 1; i++) {
            sb.append(this.motif.get(i))
            .append(",");
        }
        sb.append(this.motif.get(nvar - 1));

        // add columns to check outlier is indeed removed
        sb.append(",");
        sb.append(this.inFreq + ",")
        .append(this.outFreq + ",")
        .append(this.sList.size() + ",")
        .append(this.rList.size() + ",")
        .append(this.mList.size() + ",")
        .append(this.nList.size())
        .append("\n");
        sc.write(sb.toString());
    }

}