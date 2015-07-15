package data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Info {
    public ArrayList<Double> time = new ArrayList<Double>();
    public int value;
    public int group = 0;

    public Info(double newInfor) {
        this.time.add(newInfor);
    }

    public Info(double newInfor, int group) {
        this.time.add(newInfor);
        this.group = group;
    }

    public Info(int value) {
        this.value = value;
    }

    // add new time to the array list
    public void add(double newtime) {
        this.time.add(newtime);
    }

    public void print(long sender, long receiver, BufferedWriter w) throws IOException {
        StringBuilder output = new StringBuilder();
        output.append(Long.toString(sender)).append("|");
        output.append(Long.toString(receiver)).append("|");
        String sr = output.toString();
        for (int i = 0; i < this.time.size(); i++) {
            System.out.println(output.append(this.time.get(i).toString()).toString());
            w.write(sr + this.time.get(i).toString() + "\n");
        }
    }

}
