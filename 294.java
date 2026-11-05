// Q294: Load Balancing (Dynamic Workload Scheduling)
// Domain: Quiz Gaming Room System
//
// CSV File: workload_294.csv
// job_id,compute_units,assigned_worker,status
// J101,35,UNASSIGNED,PENDING
// J102,15,UNASSIGNED,PENDING
// J103,20,UNASSIGNED,PENDING
// J104,35,UNASSIGNED,PENDING
// J105,25,UNASSIGNED,PENDING
// J106,25,UNASSIGNED,PENDING
//
// Least-Connections / least-load strategy: every new job goes to the worker that
// currently has the smallest total compute load (ties go to the lower worker number).

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class Main {
    static final String FILE_NAME = "workload_294.csv";
    static String[] workers = {"Worker_1", "Worker_2", "Worker_3"};

    public static void main(String[] args) {
        System.out.println("Dynamic Load Balancing (Least Load) - Quiz Gaming Room System");
        System.out.println();

        // each job: job_id, compute_units, assigned_worker, status
        ArrayList<String[]> jobs = new ArrayList<>();
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
                jobs.add(new String[] {row[0].trim(), row[1].trim(), row[2].trim(), row[3].trim().toUpperCase()});
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Could not open " + FILE_NAME);
            return;
        }

        int[] load = new int[workers.length];
        ArrayList<ArrayList<String>> workerJobs = new ArrayList<>();
        for (int i = 0; i < workers.length; i++) {
            workerJobs.add(new ArrayList<String>());
        }

        System.out.println("Worker nodes: " + String.join(", ", workers));
        System.out.println();
        System.out.println("Incoming job allocations:");
        for (String[] job : jobs) {
            if (!job[3].equals("PENDING")) {
                System.out.println("  " + job[0] + " skipped (status " + job[3] + ")");
                continue;
            }
            int units = Integer.parseInt(job[1]);

            int best = 0;
            for (int i = 1; i < workers.length; i++) {
                if (load[i] < load[best]) {
                    best = i;
                }
            }

            load[best] = load[best] + units;
            workerJobs.get(best).add(job[0]);
            job[2] = workers[best];
            job[3] = "ASSIGNED";

            System.out.println("  " + job[0] + " (" + units + " units) -> " + workers[best]);
            String line = "    Current load:";
            for (int i = 0; i < workers.length; i++) {
                line = line + " " + workers[i] + "=" + load[i];
            }
            System.out.println(line);
        }
        System.out.println();

        System.out.println("Balanced summary:");
        int total = 0;
        for (int i = 0; i < workers.length; i++) {
            String names;
            if (workerJobs.get(i).size() == 0) {
                names = "none";
            } else {
                names = String.join(", ", workerJobs.get(i));
            }
            System.out.println("  " + workers[i] + " : " + workerJobs.get(i).size() + " job(s), " + load[i] + " units -> " + names);
            total = total + load[i];
        }

        int highest = load[0];
        int lowest = load[0];
        for (int i = 1; i < workers.length; i++) {
            if (load[i] > highest) {
                highest = load[i];
            }
            if (load[i] < lowest) {
                lowest = load[i];
            }
        }

        System.out.println("Total units = " + total + ", average per worker = " + String.format("%.2f", (double) total / workers.length));
        System.out.println("Max load = " + highest + ", min load = " + lowest + ", imbalance = " + (highest - lowest) + " units");
        System.out.println();

        System.out.println("Updated job table:");
        System.out.println("  job_id,compute_units,assigned_worker,status");
        for (String[] job : jobs) {
            System.out.println("  " + job[0] + "," + job[1] + "," + job[2] + "," + job[3]);
        }
    }
}
