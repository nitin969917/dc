// Q250: Clock Synchronization - Lamport's Logical Clock
// Domain: Distributed Portfolio Management System
//
// CSV File: events_250.csv
// event_id,process_id,event_type,message_to,initial_clock
// E1,P1,SEND,P2,0
// E2,P3,SEND,P2,2
// E3,P2,RECEIVE,P1,0
// E4,P2,RECEIVE,P3,0
// E5,P2,SEND,P3,0
// E6,P3,RECEIVE,P2,2
//
// event_type is SEND, RECEIVE or INTERNAL. For SEND, message_to is the receiver.
// For RECEIVE, message_to holds the process the message came from. INTERNAL uses NONE.
// initial_clock is the starting clock value of that process.
// Rules: SEND / INTERNAL -> L = L + 1,  RECEIVE -> L = max(L, T_msg) + 1

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

class Main {
    static final String FILE_NAME = "events_250.csv";

    public static void main(String[] args) {
        System.out.println("Lamport Logical Clock - Distributed Portfolio Management System");
        System.out.println();

        LinkedHashMap<String, Integer> clocks = new LinkedHashMap<>();
        HashMap<String, ArrayList<Integer>> messages = new HashMap<>();
        ArrayList<String> eventIds = new ArrayList<>();
        ArrayList<String> eventProcs = new ArrayList<>();
        ArrayList<Integer> stamps = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
            String line = br.readLine();

            System.out.println("Step-by-step clock updates:");
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] row = line.split(",");
                if (row.length < 5) {
                    continue;
                }
                String eventId = row[0].trim();
                String proc = row[1].trim().toUpperCase();
                String eventType = row[2].trim().toUpperCase();
                String target = row[3].trim().toUpperCase();
                int initial = Integer.parseInt(row[4].trim());

                if (!clocks.containsKey(proc)) {
                    clocks.put(proc, initial);
                    System.out.println("  (" + proc + " starts with clock " + initial + ")");
                }

                int old = clocks.get(proc);
                if (eventType.equals("SEND")) {
                    clocks.put(proc, old + 1);
                    String key = proc + "->" + target;
                    if (!messages.containsKey(key)) {
                        messages.put(key, new ArrayList<Integer>());
                    }
                    messages.get(key).add(clocks.get(proc));
                    System.out.println("  " + eventId + ": " + proc + " SEND to " + target + "   L = " + old + " + 1 = " + clocks.get(proc)
                            + ", message sent with T_msg = " + clocks.get(proc));
                } else if (eventType.equals("RECEIVE")) {
                    String key = target + "->" + proc;
                    if (messages.containsKey(key) && messages.get(key).size() > 0) {
                        int tMsg = messages.get(key).remove(0);
                        clocks.put(proc, Math.max(old, tMsg) + 1);
                        System.out.println("  " + eventId + ": " + proc + " RECEIVE from " + target + "   L = max(" + old + ", " + tMsg
                                + ") + 1 = " + clocks.get(proc));
                    } else {
                        clocks.put(proc, old + 1);
                        System.out.println("  " + eventId + ": no message from " + target + " to " + proc
                                + " is waiting, treated as internal   L = " + old + " + 1 = " + clocks.get(proc));
                    }
                } else {
                    clocks.put(proc, old + 1);
                    System.out.println("  " + eventId + ": " + proc + " INTERNAL event   L = " + old + " + 1 = " + clocks.get(proc));
                }

                eventIds.add(eventId);
                eventProcs.add(proc);
                stamps.add(clocks.get(proc));
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Could not open " + FILE_NAME);
            return;
        }

        System.out.println();
        System.out.println("Final clock of each process:");
        for (String proc : clocks.keySet()) {
            System.out.println("  " + proc + " = " + clocks.get(proc));
        }
        System.out.println();

        // bubble sort on (timestamp, process id) to get the total order
        int n = eventIds.size();
        int[] order = new int[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                int a = order[j];
                int b = order[j + 1];
                boolean swap = stamps.get(a) > stamps.get(b)
                        || (stamps.get(a).equals(stamps.get(b)) && eventProcs.get(a).compareTo(eventProcs.get(b)) > 0);
                if (swap) {
                    order[j] = b;
                    order[j + 1] = a;
                }
            }
        }

        System.out.println("Final total causal order (sorted by timestamp, ties broken by process id):");
        String chain = "";
        for (int k = 0; k < n; k++) {
            int i = order[k];
            System.out.println("  " + (k + 1) + ". " + eventIds.get(i) + " at " + eventProcs.get(i) + " with L = " + stamps.get(i));
            if (k > 0) {
                chain = chain + " -> ";
            }
            chain = chain + eventIds.get(i) + "(" + stamps.get(i) + ")";
        }
        System.out.println("  " + chain);
    }
}
