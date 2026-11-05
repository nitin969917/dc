# Q28: Multithreading (Concurrent Processing & Locks)
# Domain: Predictive Distributed Task Scheduler using ML
#
# CSV File: tasks_28.csv
# task_id,thread_name,burst_ms,priority,status
# T1,Worker_1,200,HIGH,PENDING
# T2,Worker_3,100,LOW,PENDING
# T3,Worker_1,400,MEDIUM,PENDING
# T4,Worker_2,150,LOW,PENDING
# T5,Worker_3,400,MEDIUM,COMPLETED
# T6,Worker_3,150,LOW,PENDING
#
# One thread is created for every thread_name in the CSV. Each thread runs its own
# tasks (HIGH first, then MEDIUM, then LOW). The work (sleep of burst_ms) happens in
# parallel, but updating the shared completed list / counter is done inside the lock.

import csv
import threading
import time

FILE_NAME = "tasks_28.csv"

lock = threading.Lock()
print_lock = threading.Lock()

completed = []
status = {}
total_burst = 0


def log(msg):
    # separate small lock only so that printed lines do not get mixed up
    print_lock.acquire()
    print(msg)
    print_lock.release()


def worker(name, tasks):
    global total_burst
    log(name + " started with " + str(len(tasks)) + " task(s)")

    for task in tasks:
        task_id = task[0]
        burst = task[1]
        log(f"{name} running {task_id} (priority {task[2]}, burst {burst} ms)")
        time.sleep(burst / 1000)

        log(f"{name} waiting for lock to update shared data for {task_id}")
        lock.acquire()
        log(f"{name} ACQUIRED lock")
        total_burst = total_burst + burst
        completed.append(task_id + " (" + name + ")")
        status[task_id] = "COMPLETED"
        log(f"{name} marked {task_id} as COMPLETED, total burst so far = {total_burst} ms")
        log(f"{name} RELEASED lock")
        lock.release()

    log(name + " finished all its tasks")


def main():
    print("Multithreading with Mutex Lock - Predictive Distributed Task Scheduler using ML")
    print()

    task_rows = []
    try:
        f = open(FILE_NAME, "r")
    except FileNotFoundError:
        print("Could not open " + FILE_NAME)
        return

    reader = csv.reader(f)
    next(reader)
    for row in reader:
        if len(row) < 5:
            continue
        task_id = row[0].strip()
        thread_name = row[1].strip()
        burst = int(row[2].strip())
        priority = row[3].strip().upper()
        state = row[4].strip().upper()
        status[task_id] = state
        if state != "PENDING":
            print(f"{task_id} is already {state}, it will not be processed again")
            continue
        task_rows.append([task_id, thread_name, burst, priority])
    f.close()

    # group the pending tasks by thread name, in priority order
    thread_names = []
    for task in task_rows:
        if task[1] not in thread_names:
            thread_names.append(task[1])

    threads = []
    for name in thread_names:
        my_tasks = []
        for level in ["HIGH", "MEDIUM", "LOW"]:
            for task in task_rows:
                if task[1] == name and task[3] == level:
                    my_tasks.append([task[0], task[2], task[3]])
        for task in task_rows:
            if task[1] == name and task[3] not in ["HIGH", "MEDIUM", "LOW"]:
                my_tasks.append([task[0], task[2], task[3]])
        t = threading.Thread(target=worker, args=(name, my_tasks))
        threads.append(t)

    print("Pending tasks: " + str(len(task_rows)) + ", threads created: " + str(len(threads)))
    print()

    for t in threads:
        t.start()
    for t in threads:
        t.join()

    print()
    print("All threads joined.")
    print("Final completed task list (in order of completion):")
    for i in range(len(completed)):
        print("  " + str(i + 1) + ". " + completed[i])

    print("Final task status:")
    for task_id in status:
        print("  " + task_id + " : " + status[task_id])

    expected = 0
    for task in task_rows:
        expected = expected + task[2]
    print(f"Total burst processed = {total_burst} ms (expected {expected} ms)")
    if total_burst == expected and len(completed) == len(task_rows):
        print("Shared data is consistent, no race condition occurred")
    else:
        print("Shared data is inconsistent!")


main()
