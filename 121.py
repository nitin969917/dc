# Q121: Load Balancing (Dynamic Workload Scheduling)
# Domain: Fault-Tolerant Distributed Emulator for Biometric Telemetry
#
# CSV File: workload_121.csv
# job_id,compute_units,assigned_worker,status
# J101,35,UNASSIGNED,PENDING
# J102,25,UNASSIGNED,PENDING
# J103,55,UNASSIGNED,PENDING
# J104,70,UNASSIGNED,PENDING
# J105,75,UNASSIGNED,PENDING
# J106,20,UNASSIGNED,PENDING
#
# Least-Connections / least-load strategy: every new job goes to the worker that
# currently has the smallest total compute load (ties go to the lower worker number).

import csv

FILE_NAME = "workload_121.csv"
WORKERS = ["Worker_1", "Worker_2", "Worker_3"]


def main():
    print("Dynamic Load Balancing (Least Load) - Fault-Tolerant Distributed Emulator for Biometric Telemetry")
    print()

    jobs = []
    try:
        f = open(FILE_NAME, "r")
    except FileNotFoundError:
        print("Could not open " + FILE_NAME)
        return

    reader = csv.reader(f)
    next(reader)
    for row in reader:
        if len(row) < 4:
            continue
        jobs.append([row[0].strip(), int(row[1].strip()), row[2].strip(), row[3].strip().upper()])
    f.close()

    load = [0, 0, 0]
    worker_jobs = [[], [], []]

    print("Worker nodes: " + ", ".join(WORKERS))
    print()
    print("Incoming job allocations:")
    for job in jobs:
        if job[3] != "PENDING":
            print("  " + job[0] + " skipped (status " + job[3] + ")")
            continue

        best = 0
        for i in range(1, len(WORKERS)):
            if load[i] < load[best]:
                best = i

        load[best] = load[best] + job[1]
        worker_jobs[best].append(job[0])
        job[2] = WORKERS[best]
        job[3] = "ASSIGNED"

        print(f"  {job[0]} ({job[1]} units) -> {WORKERS[best]}")
        line = "    Current load:"
        for i in range(len(WORKERS)):
            line = line + " " + WORKERS[i] + "=" + str(load[i])
        print(line)
    print()

    print("Balanced summary:")
    total = 0
    for i in range(len(WORKERS)):
        if len(worker_jobs[i]) == 0:
            names = "none"
        else:
            names = ", ".join(worker_jobs[i])
        print(f"  {WORKERS[i]} : {len(worker_jobs[i])} job(s), {load[i]} units -> {names}")
        total = total + load[i]

    highest = load[0]
    lowest = load[0]
    for i in range(1, len(WORKERS)):
        if load[i] > highest:
            highest = load[i]
        if load[i] < lowest:
            lowest = load[i]

    print(f"Total units = {total}, average per worker = {total / len(WORKERS):.2f}")
    print(f"Max load = {highest}, min load = {lowest}, imbalance = {highest - lowest} units")
    print()

    print("Updated job table:")
    print("  job_id,compute_units,assigned_worker,status")
    for job in jobs:
        print(f"  {job[0]},{job[1]},{job[2]},{job[3]}")


main()
