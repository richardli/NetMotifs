package analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by zehangli on 7/14/15.
 */
public class GlobalHelper {
    public static int absoluteStartingValue = 60928;
    public static String absoluteStartingValueString = "060928|00:00:00";

    /**
     * helper function to parse time
     *
     * @param field: string vector in the format of ["060101", "00:00:00"]
     * @return
     * @throws ParseException
     */
    public static double parseTime(String[] field) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd|HH:mm:ss");
        long time0 = format.parse(absoluteStartingValueString).getTime();
        StringBuilder timeString = new StringBuilder();
        timeString.append(field[2]);
        timeString.append("|");
        timeString.append(field[3]);
        long timenow = format.parse(timeString.toString()).getTime();
        return ((double) ((timenow - time0) / 1000 / (3600)));
    }

    /**
     * helper function to parse date
     *
     * @param date : string in the format of "060101"
     * @return
     * @throws ParseException
     */
    public static double parseDate(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd|HH:mm:ss");
        date = date + "|00:00:00";
        long time0 = format.parse("060101|00:00:00").getTime();
        long timenow = format.parse(date).getTime();
        return ((double) ((timenow - time0) / 1000 / 3600));
    }



    /**
     *
     * helper function to read file into a HashMap of sender <-> receivers, no start date restriction
     *
     * @param file
     * @param file          : String file name
     * @param enddate       : end date to read
     * @throws IOException
     * @throws ParseException
     */
    public static void mapread(String file, int enddate, HashMap<String, HashSet<String>> friendMap) throws IOException, ParseException {
        // int lastIndex = 0;
        mapread(file, Integer.MIN_VALUE, enddate, friendMap);
        //        int currentIndex = 60928;
//        String line;
//
//        BufferedReader br = new BufferedReader(new FileReader(file));
//        while ((line = br.readLine()) != null & currentIndex < enddate) {
//            String[] field = line.split("\\|");
//            if (field[4].charAt(0) != '-') {
//                String sender = field[0];
//                String receiver = field[1];
//
//                currentIndex = Integer.parseInt(field[2]);
//                if (this.friendmap.get(sender) != null) {
//                    this.friendmap.get(sender).add(receiver);
//                } else {
//                    this.friendmap.put(sender, new HashSet<String>());
//                    this.friendmap.get(sender).add(receiver);
//                }
//            }
//        }
//        br.close();
    }

    /**
     *
     * helper function to read file into a HashMap of sender <-> receivers
     *
     * @param file          : String file name
     * @param startdate     : start date in form of 60928 (06-09-28)
     * @param enddate       : end date to read
     * @throws ParseException
     * @throws NumberFormatException
     * @throws IOException
     */
    public static void mapread(String file, int startdate, int enddate, HashMap<String, HashSet<String>> friendMap) throws ParseException, NumberFormatException, IOException {
        // int lastIndex = 0;
        /** this is the start date: 06-09-28 **/
        int currentIndex = absoluteStartingValue;
        String line;

        BufferedReader br = new BufferedReader(new FileReader(file));
        while ((line = br.readLine()) != null & currentIndex < enddate) {
            String[] field = line.split("\\|");
            if (field[4].charAt(0) != '-') {
                String sender = field[0];
                String receiver = field[1];

                currentIndex = Integer.parseInt(field[2]);
                if (currentIndex >= startdate) {
                    if (friendMap.get(sender) != null) {
                        friendMap.get(sender).add(receiver);
                    } else {
                        friendMap.put(sender, new HashSet<String>());
                        friendMap.get(sender).add(receiver);
                    }
                }
            }
        }
        br.close();
    }
}
