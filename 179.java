// Q179: Clock Synchronization - Berkeley's Algorithm
// Domain: Distributed Car Rental System
//
// CSV File: node_clocks_179.csv
// node_id,clock_time_sec,is_faulty
// N1,3600,FALSE
// N2,3513,FALSE
// N3,2207,TRUE
// N4,4681,TRUE
// N5,3511,FALSE
// N6,3657,FALSE
//
// The first non-faulty node acts as the time daemon (master).
// Nodes with is_faulty = TRUE are treated as outliers and left out of the average.

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class Main {
    static final String FILE_NAME = "node_clocks_179.csv";

    static String toHms(int sec) {
        int h = sec / 3600;
        int m = (sec % 3600) / 60;
        int s = sec % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public static void main(String[] args) {
        System.out.println("Berkeley Clock Synchronization - Distributed Car Rental System");
        System.out.println();

        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> times = new ArrayList<>();
        ArrayList<Boolean> faulty = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] row = line.split(",");
                if (row.length < 3) {
                    continue;
                }
                names.add(row[0].trim());
                times.add(Integer.parseInt(row[1].trim()));
                faulty.add(row[2].trim().equalsIgnoreCase("TRUE"));
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Could not open " + FILE_NAME);
            return;
        }

        int master = -1;
        for (int i = 0; i < names.size(); i++) {
            if (!faulty.get(i)) {
                master = i;
                break;
            }
        }

        if (master == -1) {
            System.out.println("All nodes are faulty, synchronization not possible");
            return;
        }

        int masterTime = times.get(master);
        System.out.println("Time daemon (master): " + names.get(master) + " with clock " + masterTime + " sec");
        System.out.println();

        System.out.println("Step 1: Master polls the clock of every node");
        for (int i = 0; i < names.size(); i++) {
            int offset = times.get(i) - masterTime;
            System.out.println("  " + names.get(i) + " : " + times.get(i) + " sec (" + toHms(times.get(i))
                    + "), offset from master = " + String.format("%+d", offset) + " sec");
        }
        System.out.println();

        System.out.println("Step 2: Identify and discard faulty / outlier clocks");
        int total = 0;
        int count = 0;
        for (int i = 0; i < names.size(); i++) {
            if (faulty.get(i)) {
                System.out.println("  " + names.get(i) + " discarded (faulty clock " + times.get(i) + " sec)");
            } else {
                total = total + (times.get(i) - masterTime);
                count++;
            }
        }
        if (count == names.size()) {
            System.out.println("  No faulty nodes found");
        }
        System.out.println();

        double avgOffset = (double) total / count;
        double avgTime = masterTime + avgOffset;
        System.out.println("Step 3: Average offset of " + count + " healthy nodes = " + total + " / " + count
                + " = " + String.format("%.2f", avgOffset) + " sec");
        System.out.println("        Synchronized time = " + masterTime + " + (" + String.format("%.2f", avgOffset) + ") = "
                + String.format("%.2f", avgTime) + " sec (" + toHms((int) avgTime) + ")");
        System.out.println();

        System.out.println("Step 4: Relative adjustment sent to each healthy node");
        for (int i = 0; i < names.size(); i++) {
            if (faulty.get(i)) {
                System.out.println("  " + names.get(i) + " : excluded from synchronization");
            } else {
                double adjust = avgTime - times.get(i);
                System.out.println("  " + names.get(i) + " : " + times.get(i) + " -> " + String.format("%.2f", avgTime)
                        + ", adjustment = " + String.format("%+.2f", adjust) + " sec");
            }
        }
    }
}
