package data;

public class MotifInstance {
    int t;
    int type;
    int[] count = new int[16];

    public MotifInstance(int t, int type, int[] motif) {
        this.t = t;
        this.type = type;
        this.count = motif;
    }
}
