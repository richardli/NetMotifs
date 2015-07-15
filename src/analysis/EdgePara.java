package analysis;

public class EdgePara {
    // count holds the number of calls in different days
    public int count;
    public int lastindex;

    public EdgePara(int index) {
        this.count = 1;
        this.lastindex = index;
    }

    public boolean add(int index) {
        // flag to tell if a new edge is added to the table
        boolean addnew = false;
        if (index > lastindex) {
            this.count += 1;
            this.lastindex = index;
            addnew = true;
        }
        return addnew;
    }
}
