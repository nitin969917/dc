# Q207: Clock Synchronization - Berkeley's Algorithm
# Domain: Distributed E-Commerce Inventory Management
#
# CSV File: node_clocks_207.csv
# node_id,clock_time_sec,is_faulty
# N1,3600,FALSE
# N2,3581,FALSE
# N3,3539,FALSE
# N4,4559,TRUE
# N5,2463,TRUE
#
# The first non-faulty node acts as the time daemon (master).
# Nodes with is_faulty = TRUE are treated as outliers and left out of the average.

import csv

FILE_NAME = "node_clocks_207.csv"


def to_hms(sec):
    h = sec // 3600
    m = (sec % 3600) // 60
    s = sec % 60
    return f"{h:02d}:{m:02d}:{s:02d}"


def main():
    print("Berkeley Clock Synchronization - Distributed E-Commerce Inventory Management")
    print()

    names = []
    times = []
    faulty = []

    try:
        f = open(FILE_NAME, "r")
    except FileNotFoundError:
        print("Could not open " + FILE_NAME)
        return

    reader = csv.reader(f)
    next(reader)
    for row in reader:
        if len(row) < 3:
            continue
        names.append(row[0].strip())
        times.append(int(row[1].strip()))
        faulty.append(row[2].strip().upper() == "TRUE")
    f.close()

    master = -1
    for i in range(len(names)):
        if not faulty[i]:
            master = i
            break

    if master == -1:
        print("All nodes are faulty, synchronization not possible")
        return

    master_time = times[master]
    print("Time daemon (master): " + names[master] + " with clock " + str(master_time) + " sec")
    print()

    print("Step 1: Master polls the clock of every node")
    for i in range(len(names)):
        offset = times[i] - master_time
        print(f"  {names[i]} : {times[i]} sec ({to_hms(times[i])}), offset from master = {offset:+d} sec")
    print()

    print("Step 2: Identify and discard faulty / outlier clocks")
    total = 0
    count = 0
    for i in range(len(names)):
        if faulty[i]:
            print(f"  {names[i]} discarded (faulty clock {times[i]} sec)")
        else:
            total = total + (times[i] - master_time)
            count = count + 1
    if count == len(names):
        print("  No faulty nodes found")
    print()

    avg_offset = total / count
    avg_time = master_time + avg_offset
    print(f"Step 3: Average offset of {count} healthy nodes = {total} / {count} = {avg_offset:.2f} sec")
    print(f"        Synchronized time = {master_time} + ({avg_offset:.2f}) = {avg_time:.2f} sec ({to_hms(int(avg_time))})")
    print()

    print("Step 4: Relative adjustment sent to each healthy node")
    for i in range(len(names)):
        if faulty[i]:
            print(f"  {names[i]} : excluded from synchronization")
        else:
            adjust = avg_time - times[i]
            print(f"  {names[i]} : {times[i]} -> {avg_time:.2f}, adjustment = {adjust:+.2f} sec")


main()
