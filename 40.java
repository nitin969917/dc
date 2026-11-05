// Q40: Clock Synchronization - Cristian's Algorithm
// Domain: Adaptive Distributed Computing Framework for Task Execution
//
// CSV File: cristian_clocks_40.csv
// client_id,t0_send_ms,t_server_ms,t1_recv_ms
// C1,1000,1050,1100
// C2,3100,3100,3242
// C3,3870,3945,3920
// C4,3300,3401,3388
// C5,2420,2544,2600
// C6,2350,2443,2442
//
// T0 = time client sent the request, T_server = time reported by the time server,
// T1 = time client received the reply (all in ms on the client's own clock).

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class Main {
    static final String FILE_NAME = "cristian_clocks_40.csv";

    public static void main(String[] args) {
        System.out.println("Cristian's Clock Synchronization - Adaptive Distributed Computing Framework for Task Execution");
        System.out.println();

        String bestClient = "";
        int bestRtt = -1;
        int count = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] row = line.split(",");
                if (row.length < 4) {
                    continue;
                }
                String clientId = row[0].trim();
                int t0 = Integer.parseInt(row[1].trim());
                int ts = Integer.parseInt(row[2].trim());
                int t1 = Integer.parseInt(row[3].trim());

                System.out.println("Client " + clientId + ":");
                System.out.println("  T0 = " + t0 + " ms, T_server = " + ts + " ms, T1 = " + t1 + " ms");

                if (t1 < t0) {
                    System.out.println("  Invalid record (T1 is before T0), skipped");
                    System.out.println();
                    continue;
                }

                int rtt = t1 - t0;
                double half = rtt / 2.0;
                double estimated = ts + half;
                double offset = estimated - t1;

                System.out.println("  RTT = T1 - T0 = " + t1 + " - " + t0 + " = " + rtt + " ms");
                System.out.println("  Estimated server time = T_server + (RTT / 2) = " + ts + " + " + String.format("%.1f", half)
                        + " = " + String.format("%.1f", estimated) + " ms");
                System.out.println("  Clock offset = " + String.format("%.1f", estimated) + " - " + t1 + " = "
                        + String.format("%+.1f", offset) + " ms");
                if (offset > 0) {
                    System.out.println("  Client clock is behind the server by " + String.format("%.1f", offset) + " ms -> move it forward");
                } else if (offset < 0) {
                    System.out.println("  Client clock is ahead of the server by " + String.format("%.1f", -offset)
                            + " ms -> slow it down / set it back");
                } else {
                    System.out.println("  Client clock is already in sync with the server");
                }
                System.out.println("  Adjusted client time = " + t1 + " + (" + String.format("%+.1f", offset) + ") = "
                        + String.format("%.1f", estimated) + " ms  (accuracy +/- " + String.format("%.1f", half) + " ms)");
                System.out.println();

                count++;
                if (bestRtt == -1 || rtt < bestRtt) {
                    bestRtt = rtt;
                    bestClient = clientId;
                }
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Could not open " + FILE_NAME);
            return;
        }

        if (count == 0) {
            System.out.println("No valid records found");
        } else {
            System.out.println("Clients synchronized = " + count);
            System.out.println("Most accurate synchronization: " + bestClient + " (lowest RTT = " + bestRtt + " ms)");
        }
    }
}
