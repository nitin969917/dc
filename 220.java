// Q220: Bully Algorithm (Leader Election)
// Domain: Distributed Sports Match Synchronization
//
// CSV File: bully_cluster_220.csv
// node_id,is_alive,is_current_coordinator
// 1,FALSE,FALSE
// 2,TRUE,FALSE
// 3,TRUE,FALSE
// 4,TRUE,FALSE
// 5,FALSE,TRUE

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

class Main {
    static final String FILE_NAME = "bully_cluster_220.csv";

    public static void main(String[] args) {
        System.out.println("Bully Election Algorithm - Distributed Sports Match Synchronization");
        System.out.println();

        ArrayList<Integer> ids = new ArrayList<>();
        HashMap<Integer, Boolean> alive = new HashMap<>();
        int coordinator = -1;

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
                int node = Integer.parseInt(row[0].trim());
                ids.add(node);
                alive.put(node, row[1].trim().equalsIgnoreCase("TRUE"));
                if (row[2].trim().equalsIgnoreCase("TRUE")) {
                    coordinator = node;
                }
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Could not open " + FILE_NAME);
            return;
        }

        if (ids.size() == 0) {
            System.out.println("No nodes found in " + FILE_NAME);
            return;
        }

        Collections.sort(ids);

        System.out.println("Nodes read from CSV:");
        for (int node : ids) {
            String line;
            if (alive.get(node)) {
                line = "  Node " + node + " : ALIVE";
            } else {
                line = "  Node " + node + " : DOWN";
            }
            if (node == coordinator) {
                line = line + " (current coordinator)";
            }
            System.out.println(line);
        }
        System.out.println();

        if (coordinator == -1) {
            coordinator = ids.get(ids.size() - 1);
            System.out.println("No coordinator marked in CSV, assuming highest node " + coordinator + " was the coordinator");
        }

        if (alive.get(coordinator)) {
            System.out.println("Coordinator Node " + coordinator + " is alive. No election needed.");
            return;
        }

        System.out.println("Coordinator Node " + coordinator + " is not responding -> failure detected");

        // lowest alive node notices the failure first and starts the election
        int initiator = -1;
        for (int node : ids) {
            if (alive.get(node)) {
                initiator = node;
                break;
            }
        }

        if (initiator == -1) {
            System.out.println("No alive nodes left, election cannot be held");
            return;
        }

        System.out.println("Node " + initiator + " detects the failure and starts the election");
        System.out.println();

        // every node that replies OK takes over and holds its own election
        ArrayList<Integer> waiting = new ArrayList<>();
        ArrayList<Integer> done = new ArrayList<>();
        waiting.add(initiator);
        int leader = -1;

        while (waiting.size() > 0) {
            int current = waiting.remove(0);
            if (done.contains(current)) {
                continue;
            }
            done.add(current);

            System.out.println("Node " + current + " holds an election:");
            boolean gotOk = false;
            for (int node : ids) {
                if (node <= current) {
                    continue;
                }
                System.out.println("  Node " + current + " -> Node " + node + " : ELECTION");
                if (alive.get(node)) {
                    System.out.println("  Node " + node + " -> Node " + current + " : OK");
                    gotOk = true;
                    if (!waiting.contains(node) && !done.contains(node)) {
                        waiting.add(node);
                    }
                } else {
                    System.out.println("  Node " + node + " does not respond (down)");
                }
            }

            if (gotOk) {
                System.out.println("  Node " + current + " received OK, so it steps back and waits");
            } else {
                System.out.println("  Node " + current + " received no OK, so it wins the election");
                leader = current;
            }
            System.out.println();
        }

        System.out.println("Node " + leader + " broadcasts COORDINATOR message to lower nodes:");
        for (int node : ids) {
            if (node == leader) {
                continue;
            }
            if (alive.get(node)) {
                System.out.println("  Node " + leader + " -> Node " + node + " : COORDINATOR(" + leader + ")");
            } else {
                System.out.println("  Node " + node + " is down, message not delivered");
            }
        }
        System.out.println();

        System.out.println("Final status:");
        for (int node : ids) {
            if (node == leader) {
                System.out.println("  Node " + node + " : ALIVE (new coordinator)");
            } else if (alive.get(node)) {
                System.out.println("  Node " + node + " : ALIVE");
            } else {
                System.out.println("  Node " + node + " : DOWN");
            }
        }
        System.out.println("New coordinator is Node " + leader);
    }
}
