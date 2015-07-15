package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import util.MathUtil;
import util.VectorUtil;

public class NodePara {
    // node ID
    long id;
    // list of id's the node send to / received by
    public ArrayList<Long> index2S = new ArrayList<Long>();
    public ArrayList<Long> index2R = new ArrayList<Long>();
    // the time points of each of the above element
    public ArrayList<Set<Integer>> time2S = new ArrayList<Set<Integer>>();
    public ArrayList<Set<Integer>> time2R = new ArrayList<Set<Integer>>();

    // *close* neighbour counts
    // list of id's that are two heaps away from the node
    public HashMap<Long, Integer> toconnect = new HashMap<Long, Integer>();
    // accumulated number of 2-heap away nodes daily
    public ArrayList<Integer> count2neiDaily = new ArrayList<Integer>();
    // accumulated number of neighbours
    public ArrayList<Integer> count1neiDaily = new ArrayList<Integer>();
    public boolean deleteflag;
    /*
	 * Special Alert:
	 * 		Most of the methods cannot handle self-loop!!
	 * 		Remove them from main file when using!!
	 * 
	 */

    // @param isSender: whether this node is sending to the node added
    // note this is initialization, doesn't need to check existence
    public NodePara(long me) { //, long id, int index, boolean isSender){
        this.id = me;
        this.deleteflag = false;
    }

    // add a new index when it is sender at a given time
    public void addasSender(long id, int index) {
        int where = this.index2S.indexOf(id);
        if (where != -1) {
            this.time2S.get(where).add(index);
        } else {
            this.index2S.add(id);
            Set<Integer> newtime = new HashSet<Integer>();
            newtime.add(index);
            this.time2S.add(newtime);
            // update neighbour counts
            if (!this.index2R.contains(id)) {
                VectorUtil.incToLenBy(this.count1neiDaily, index + 1, 1);
            }
        }
        // check if it is already a to-connect node before linking
        this.deleteflag = this.toconnect.containsKey(id);
        if (this.deleteflag) {
            this.toconnect.remove(id);
        }
    }

    // add a new index when it is receiver at a given time
    public void addasReceiver(long id, int index) {
        int where = this.index2R.indexOf(id);
        if (where != -1) {
            this.time2R.get(where).add(index);
        } else {
            this.index2R.add(id);
            Set<Integer> newtime = new HashSet<Integer>();
            newtime.add(index);
            this.time2R.add(newtime);
            // update neighbour counts
            if (!this.index2S.contains(id)) {
                VectorUtil.incToLenBy(this.count1neiDaily, index + 1, 1);
            }

        }
        // check if it is already a to-connect node before linking
        this.deleteflag = this.toconnect.containsKey(id);
        if (this.deleteflag) {
            this.toconnect.remove(id);
        }
    }

    // check if need to update to-connect nodes
	/* Usage:
	 * When a new edge is updated <a, b>
	 * perform:  a.checkToConnect(b)
	 * 			 b.checkToConnect(a)
	 * what it does: ( a.checkToConnect(b) )
	 * 		1. get all the nodes connected to b: n(b)
	 * 		2. check each of n(b)_i whether connected to a:
	 * 			if not: n(b)_i should be in a.toconnect
	 * 					if also not: 1. put it there with the current time 
	 *								 2. put in the other node's info this one
	 */

    public void checkToConnect(HashMap<Long, NodePara> nodes, NodePara otherEnd, int index) {
        // initialize count to one day before now
        int newToconnect = 0;
        if (this.deleteflag) newToconnect = -1;

        for (long id1 : otherEnd.index2R) {
            if (this.id == id1) continue;
            if ((!this.index2R.contains(id1)) & (!this.index2S.contains(id1))) {
                if (!this.toconnect.containsKey(id1)) {
                    this.toconnect.put(id1, index);
                    nodes.get(id1).toconnect.put(this.id, index);
                    VectorUtil.incToLenBy(nodes.get(id1).count2neiDaily, index + 1, 1);
                    newToconnect += 1;
                }
            }
        }
        for (long id2 : otherEnd.index2S) {
            if (this.id == id2) continue;
            if ((!this.index2R.contains(id2)) & (!this.index2S.contains(id2))) {
                if (!this.toconnect.containsKey(id2)) {
                    this.toconnect.put(id2, index);
                    nodes.get(id2).toconnect.put(this.id, index);
                    VectorUtil.incToLenBy(nodes.get(id2).count2neiDaily, index + 1, 1);
                    newToconnect += 1;
                }
            }
        }
        VectorUtil.incToLenBy(this.count2neiDaily, index + 1, newToconnect);
    }

    // perform at the end of everything, update the total count of the to-connect nodes
    public void updateToConnectFinal(int totalDays) {
        VectorUtil.extElement(this.count2neiDaily, totalDays + 1);
        VectorUtil.extElement(this.count1neiDaily, totalDays + 1);
    }
    //-----------------------------------------------------------------------------//
    //-----------------------------------------------------------------------------//
    //-----------------------------------------------------------------------------//
    //-----------------------------------------------------------------------------//
	/* count the total number of possible entries in the N*N*T matrix for node i
	 * i.e. S_{i . .}[i] or S_{. j .}[j]
	 * Note here we assume every neighbour in the subnetwork of node i 
	 *  	is possible receiver/sender, so sum over i or j gives same results
	 * Similarly the triangle-to-close is not directed either 
	 * 
	 * Further aggregation: 
	 * 		 sum over all node this value gives S_{...}
	 */

    public int getAllPossible(int indexT) {
        int combineSize = MathUtil.UnionTwoArrayLists(this.index2R, this.index2S);
        int allPossible = this.count2neiDaily.get(this.count2neiDaily.size() - 1) +
                combineSize * (indexT + 1);

        return (allPossible);
    }
	
		
	/*
	 * get marginal count on this node
	 * computes S_{i . k}[i, ] or S_{ . j k}[j, ]
	 * returns a vector of length T
	 * Note: repeat this for each node, get S_{i . k} and S{. j k}
	 * 
	 * Further aggregation:
	 * 		vector sum over all nodes for either s/r get S{. . k}
	 */

    public int[] sumNodebyTime(int indexT, boolean asSender) {
        int[] dailyCount = new int[indexT + 1];
        Set<Integer> collapsedTime = new HashSet<Integer>();
        if (asSender) {
            Iterator<Set<Integer>> itr = this.time2S.iterator();
            while (itr.hasNext()) {
                collapsedTime.addAll(itr.next());
            }
        } else {
            Iterator<Set<Integer>> itr = this.time2R.iterator();
            while (itr.hasNext()) {
                collapsedTime.addAll(itr.next());
            }
        }
        for (int timetoadd : collapsedTime) {
            dailyCount[timetoadd] = 1;
        }
        return (dailyCount);
    }

    /*
     * get marginal count on each neighbour
     * computes S_{i j .}[i, ]
     */
    public ArrayList<Integer> sumNodeByReceiver() {
        ArrayList<Integer> sendcount = new ArrayList<Integer>();
        if (this.index2S.size() == 0) {
            sendcount.add(0);
        } else {
            for (int itr = 0; itr < this.time2S.size(); itr++) {
                sendcount.add(this.time2S.get(itr).size());
            }
        }
        return sendcount;
    }

    /*
     * get marginal count on each neighbour
     * computes S_{i j .}[, j]
     */
    public ArrayList<Integer> sumNodeBySender() {
        ArrayList<Integer> receivecount = new ArrayList<Integer>();
        if (this.index2R.size() == 0) {
            receivecount.add(0);
        } else {
            for (int itr = 0; itr < this.time2R.size(); itr++) {
                receivecount.add(this.time2R.get(itr).size());
            }
        }
        return receivecount;
    }
}
