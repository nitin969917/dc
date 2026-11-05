// Q295: Clock Synchronization - Vector Clocks
// Domain: Quiz Gaming Room System
//
// CSV File: vector_events_295.csv
// event_id,process_id,event_action,target_process
// E1,P1,SEND,P2
// E2,P2,SEND,P1
// E3,P2,RECEIVE,P1
// E4,P1,INTERNAL,NONE
// E5,P3,SEND,P1
// E6,P1,RECEIVE,P2
//
// event_action is SEND, RECEIVE or INTERNAL. For SEND, target_process is the receiver.
// For RECEIVE, target_process is the process that sent the message. INTERNAL uses NONE.
// Processes are P1, P2, P3 so the vector is [V1, V2, V3].

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

class Main {
    static final String FILE_NAME = "vector_events_295.csv";
    static final int N = 3;

    // a -> b if every entry of a <= b and at least one entry is smaller
    static boolean happenedBefore(int[] a, int[] b) {
        boolean smaller = false;
        for (int i = 0; i < N; i++) {
            if (a[i] > b[i]) {
                return false;
            }
            if (a[i] < b[i]) {
                smaller = true;
            }
        }
        return smaller;
    }

    public static void main(String[] args) {
        System.out.println("Vector Clocks - Quiz Gaming Room System");
        System.out.println();

        int[][] clock = new int[N][N];
        HashMap<String, ArrayList<int[]>> messages = new HashMap<>();
        ArrayList<String> eventIds = new ArrayList<>();
        ArrayList<int[]> eventVectors = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
            String line = br.readLine();

            System.out.println("Processing events:");
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] row = line.split(",");
                if (row.length < 4) {
                    continue;
                }
                String eventId = row[0].trim();
                String proc = row[1].trim().toUpperCase();
                String action = row[2].trim().toUpperCase();
                String target = row[3].trim().toUpperCase();

                int p = Integer.parseInt(proc.substring(1)) - 1;
                if (p < 0 || p >= N) {
                    System.out.println("  " + eventId + ": unknown process " + proc + ", skipped");
                    continue;
                }

                if (action.equals("SEND")) {
                    clock[p][p]++;
                    String key = proc + "->" + target;
                    if (!messages.containsKey(key)) {
                        messages.put(key, new ArrayList<int[]>());
                    }
                    messages.get(key).add(clock[p].clone());
                    System.out.println("  " + eventId + ": " + proc + " SEND to " + target + ", " + proc + " increments V" + (p + 1)
                            + " -> " + Arrays.toString(clock[p]) + " (attached to message)");
                } else if (action.equals("RECEIVE")) {
                    String key = target + "->" + proc;
                    if (messages.containsKey(key) && messages.get(key).size() > 0) {
                        int[] msg = messages.get(key).remove(0);
                        int[] old = clock[p].clone();
                        for (int i = 0; i < N; i++) {
                            if (msg[i] > clock[p][i]) {
                                clock[p][i] = msg[i];
                            }
                        }
                        clock[p][p]++;
                        System.out.println("  " + eventId + ": " + proc + " RECEIVE from " + target + ", max(" + Arrays.toString(old)
                                + ", " + Arrays.toString(msg) + ") then V" + (p + 1) + "+1 -> " + Arrays.toString(clock[p]));
                    } else {
                        clock[p][p]++;
                        System.out.println("  " + eventId + ": no message from " + target + " to " + proc
                                + " is waiting, treated as internal -> " + Arrays.toString(clock[p]));
                    }
                } else {
                    clock[p][p]++;
                    System.out.println("  " + eventId + ": " + proc + " INTERNAL event, " + proc + " increments V" + (p + 1)
                            + " -> " + Arrays.toString(clock[p]));
                }

                eventIds.add(eventId);
                eventVectors.add(clock[p].clone());
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Could not open " + FILE_NAME);
            return;
        }

        System.out.println();
        System.out.println("Final vector clocks:");
        for (int i = 0; i < N; i++) {
            System.out.println("  P" + (i + 1) + " = " + Arrays.toString(clock[i]));
        }
        System.out.println();

        System.out.println("Event pair relations:");
        for (int i = 0; i < eventIds.size(); i++) {
            for (int j = i + 1; j < eventIds.size(); j++) {
                String e1 = eventIds.get(i);
                String e2 = eventIds.get(j);
                String v1 = Arrays.toString(eventVectors.get(i));
                String v2 = Arrays.toString(eventVectors.get(j));
                if (happenedBefore(eventVectors.get(i), eventVectors.get(j))) {
                    System.out.println("  " + e1 + " " + v1 + " -> " + e2 + " " + v2 + " : Causally Preceding (V(" + e1 + ") < V(" + e2 + "))");
                } else if (happenedBefore(eventVectors.get(j), eventVectors.get(i))) {
                    System.out.println("  " + e2 + " " + v2 + " -> " + e1 + " " + v1 + " : Causally Preceding (V(" + e2 + ") < V(" + e1 + "))");
                } else {
                    System.out.println("  " + e1 + " " + v1 + " || " + e2 + " " + v2 + " : Concurrent (" + e1 + " || " + e2 + ")");
                }
            }
        }
    }
}
