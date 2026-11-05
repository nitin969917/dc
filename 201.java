// Q201: Multithreading (Concurrent Processing & Locks)
// Domain: Distributed Book Issue / Library System
//
// CSV File: tasks_201.csv
// task_id,thread_name,burst_ms,priority,status
// T1,Worker_1,200,HIGH,PENDING
// T2,Worker_3,350,MEDIUM,PENDING
// T3,Worker_2,350,HIGH,PENDING
// T4,Worker_2,150,MEDIUM,PENDING
// T5,Worker_1,400,LOW,COMPLETED
//
// One thread is created for every thread_name in the CSV. Each thread runs its own
// tasks (HIGH first, then MEDIUM, then LOW). The work (sleep of burst_ms) happens in
// parallel, but updating the shared completed list / counter is done inside the lock.

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.ReentrantLock;

class Worker extends Thread {
    String name;
    ArrayList<String[]> tasks;

    Worker(String name, ArrayList<String[]> tasks) {
        this.name = name;
        this.tasks = tasks;
    }

    public void run() {
        System.out.println(name + " started with " + tasks.size() + " task(s)");

        for (String[] task : tasks) {
            String taskId = task[0];
            int burst = Integer.parseInt(task[2]);
            System.out.println(name + " running " + taskId + " (priority " + task[3] + ", burst " + burst + " ms)");
            try {
                Thread.sleep(burst);
            } catch (InterruptedException e) {
                System.out.println(name + " was interrupted");
            }

            System.out.println(name + " waiting for lock to update shared data for " + taskId);
            Main.lock.lock();
            try {
                System.out.println(name + " ACQUIRED lock");
                Main.totalBurst = Main.totalBurst + burst;
                Main.completed.add(taskId + " (" + name + ")");
                Main.status.put(taskId, "COMPLETED");
                System.out.println(name + " marked " + taskId + " as COMPLETED, total burst so far = " + Main.totalBurst + " ms");
                System.out.println(name + " RELEASED lock");
            } finally {
                Main.lock.unlock();
            }
        }

        System.out.println(name + " finished all its tasks");
    }
}

class Main {
    static final String FILE_NAME = "tasks_201.csv";

    static ReentrantLock lock = new ReentrantLock();
    static ArrayList<String> completed = new ArrayList<>();
    static LinkedHashMap<String, String> status = new LinkedHashMap<>();
    static int totalBurst = 0;

    public static void main(String[] args) {
        System.out.println("Multithreading with Mutex Lock - Distributed Book Issue / Library System");
        System.out.println();

        // each task: task_id, thread_name, burst_ms, priority
        ArrayList<String[]> taskRows = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] row = line.split(",");
                if (row.length < 5) {
                    continue;
                }
                String taskId = row[0].trim();
                String threadName = row[1].trim();
                String burst = row[2].trim();
                String priority = row[3].trim().toUpperCase();
                String state = row[4].trim().toUpperCase();
                status.put(taskId, state);
                if (!state.equals("PENDING")) {
                    System.out.println(taskId + " is already " + state + ", it will not be processed again");
                    continue;
                }
                taskRows.add(new String[] {taskId, threadName, burst, priority});
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Could not open " + FILE_NAME);
            return;
        }

        // group the pending tasks by thread name, in priority order
        ArrayList<String> threadNames = new ArrayList<>();
        for (String[] task : taskRows) {
            if (!threadNames.contains(task[1])) {
                threadNames.add(task[1]);
            }
        }

        String[] levels = {"HIGH", "MEDIUM", "LOW"};
        ArrayList<Worker> threads = new ArrayList<>();
        for (String name : threadNames) {
            ArrayList<String[]> myTasks = new ArrayList<>();
            for (String level : levels) {
                for (String[] task : taskRows) {
                    if (task[1].equals(name) && task[3].equals(level)) {
                        myTasks.add(task);
                    }
                }
            }
            for (String[] task : taskRows) {
                if (task[1].equals(name) && !task[3].equals("HIGH") && !task[3].equals("MEDIUM") && !task[3].equals("LOW")) {
                    myTasks.add(task);
                }
            }
            threads.add(new Worker(name, myTasks));
        }

        System.out.println("Pending tasks: " + taskRows.size() + ", threads created: " + threads.size());
        System.out.println();

        for (Worker t : threads) {
            t.start();
        }
        for (Worker t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.out.println("Main thread interrupted");
            }
        }

        System.out.println();
        System.out.println("All threads joined.");
        System.out.println("Final completed task list (in order of completion):");
        for (int i = 0; i < completed.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + completed.get(i));
        }

        System.out.println("Final task status:");
        for (String taskId : status.keySet()) {
            System.out.println("  " + taskId + " : " + status.get(taskId));
        }

        int expected = 0;
        for (String[] task : taskRows) {
            expected = expected + Integer.parseInt(task[2]);
        }
        System.out.println("Total burst processed = " + totalBurst + " ms (expected " + expected + " ms)");
        if (totalBurst == expected && completed.size() == taskRows.size()) {
            System.out.println("Shared data is consistent, no race condition occurred");
        } else {
            System.out.println("Shared data is inconsistent!");
        }
    }
}
