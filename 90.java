// Q90: Ring Algorithm (Leader Election)
// Domain: Adaptive Fault-Tolerant Distributed Task Execution System
//
// CSV File: ring_nodes_90.csv
// node_id,next_node_id,is_active,priority_rank
// 1,2,TRUE,85
// 2,4,TRUE,94
// 3,6,TRUE,62
// 4,3,TRUE,69
// 5,1,TRUE,45
// 6,5,FALSE,48
//
// next_node_id gives the logical ring. The node with the highest ID was the old
// coordinator. The election token collects IDs of active nodes; inactive nodes are skipped.

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class Main {
    static final String FILE_NAME = "ring_nodes_90.csv";

    static HashMap<Integer, Integer> nextOf = new HashMap<>();
    static HashMap<Integer, Boolean> active = new HashMap<>();
    static HashMap<Integer, Integer> rank = new HashMap<>();

    // move to the next node in the ring, skipping inactive ones
    static int nextActive(int node, int total) {
        int nxt = nextOf.get(node);
        int steps = 0;
        while (!active.get(nxt) && steps < total) {
            System.out.println("  Node " + nxt + " is inactive, skipped");
            nxt = nextOf.get(nxt);
            steps++;
        }
        return nxt;
    }

    public static void main(String[] args) {
        System.out.println("Ring Election Algorithm - Adaptive Fault-Tolerant Distributed Task Execution System");
        System.out.println();

        ArrayList<Integer> ids = new ArrayList<>();

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
                int node = Integer.parseInt(row[0].trim());
                ids.add(node);
                nextOf.put(node, Integer.parseInt(row[1].trim()));
                active.put(node, row[2].trim().equalsIgnoreCase("TRUE"));
                rank.put(node, Integer.parseInt(row[3].trim()));
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Could not open " + FILE_NAME);
            return;
        }

        for (int node : ids) {
            if (!active.containsKey(nextOf.get(node))) {
                System.out.println("Node " + node + " points to unknown node " + nextOf.get(node) + ", ring is broken");
                return;
            }
        }

        System.out.println("Nodes read from CSV:");
        for (int node : ids) {
            String state;
            if (active.get(node)) {
                state = "ACTIVE";
            } else {
                state = "INACTIVE";
            }
            System.out.println("  Node " + node + " -> next " + nextOf.get(node) + ", " + state + ", priority rank " + rank.get(node));
        }

        int first = ids.get(0);
        String ring = String.valueOf(first);
        int node = nextOf.get(first);
        int count = 0;
        while (node != first && count < ids.size()) {
            ring = ring + " -> " + node;
            node = nextOf.get(node);
            count++;
        }
        System.out.println("Ring: " + ring + " -> " + first);
        System.out.println();

        int coordinator = first;
        for (int n : ids) {
            if (n > coordinator) {
                coordinator = n;
            }
        }

        if (active.get(coordinator)) {
            System.out.println("Coordinator Node " + coordinator + " is active. No election needed.");
            return;
        }

        System.out.println("Coordinator Node " + coordinator + " has failed (no reply) -> failure detected");

        int initiator = -1;
        for (int n : ids) {
            if (active.get(n) && (initiator == -1 || n < initiator)) {
                initiator = n;
            }
        }
        if (initiator == -1) {
            System.out.println("No active node left in the ring");
            return;
        }

        ArrayList<Integer> token = new ArrayList<>();
        token.add(initiator);
        System.out.println("Node " + initiator + " starts the election, token = " + token);
        System.out.println();
        System.out.println("ELECTION token circulation:");
        int current = initiator;
        while (true) {
            int nxt = nextActive(current, ids.size());
            if (nxt == initiator) {
                System.out.println("  Node " + current + " -> Node " + nxt + " : token is back at initiator " + token);
                break;
            }
            token.add(nxt);
            System.out.println("  Node " + current + " -> Node " + nxt + " : ELECTION, Node " + nxt + " adds its ID -> " + token);
            current = nxt;
        }
        System.out.println();

        int leader = token.get(0);
        for (int n : token) {
            if (n > leader) {
                leader = n;
            }
        }
        System.out.println("Highest ID in token " + token + " is " + leader);
        System.out.println("Node " + leader + " is elected as the new coordinator (priority rank " + rank.get(leader) + ")");
        System.out.println();

        System.out.println("COORDINATOR message circulation:");
        current = initiator;
        while (true) {
            int nxt = nextActive(current, ids.size());
            System.out.println("  Node " + current + " -> Node " + nxt + " : COORDINATOR(" + leader + ")");
            if (nxt == initiator) {
                break;
            }
            current = nxt;
        }
        System.out.println();
        System.out.println("All active nodes now know that Node " + leader + " is the coordinator");
    }
}
