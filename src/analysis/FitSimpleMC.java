package analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

import data.MCread;

public class FitSimpleMC {
    public static void main(String[] args) throws NumberFormatException, ParseException, IOException {
        HashMap<Long, Markov> nodes = new HashMap<Long, Markov>();
        String[] fileprefix = {"0701", "0702", "0703", "0704", "0705", "0706"};//,
        //	  	, "0707", "0708",
        //		"0709", "0710", "0711", "0712", "0801", "0802","0803" };

        int indexT = 0;
        for (int i = 0; i < fileprefix.length; i++) {
            String calldata = "/data/rwanda_anon/CDR/" + fileprefix[i] + "-Call.pai.sordate.txt";
            //String calldata = "/Dropbox/calltest.txt" + fileprefix[i];
            // load and parse data into triples
            // note here we for now does not consider phone conversation length...
            // 			since adding length the time is no longer sorted.
            //		It could easily be switched by first sorting the data by time + duration
            indexT = MCread.NaiveReadintoMC(calldata, nodes, false, 0);
            System.out.println("Finish reading" + fileprefix[i]);
        }

        // perform final update on the to-connect nodes and output
        File outputMC = new File("/data/rwanda_anon/richardli/outputMC.txt");
        BufferedWriter wMC = new BufferedWriter(new FileWriter(outputMC));
        for (Markov nodeitr : nodes.values()) {
            nodeitr.updateTofinal(indexT);
            nodeitr.Print(wMC);
        }
        wMC.close();
        System.out.println("finished printing output");
    }
}
