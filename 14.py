# Q14: Clock Synchronization - Vector Clocks
# Domain: Distributed Smart Traffic Management System
#
# CSV File: vector_events_14.csv
# event_id,process_id,event_action,target_process
# E1,P1,SEND,P2
# E2,P2,SEND,P1
# E3,P2,SEND,P3
# E4,P3,RECEIVE,P2
# E5,P2,RECEIVE,P1
# E6,P1,RECEIVE,P2
#
# event_action is SEND, RECEIVE or INTERNAL. For SEND, target_process is the receiver.
# For RECEIVE, target_process is the process that sent the message. INTERNAL uses NONE.
# Processes are P1, P2, P3 so the vector is [V1, V2, V3].

import csv

FILE_NAME = "vector_events_14.csv"
N = 3


def happened_before(a, b):
    # a -> b if every entry of a <= b and at least one entry is smaller
    smaller = False
    for i in range(N):
        if a[i] > b[i]:
            return False
        if a[i] < b[i]:
            smaller = True
    return smaller


def main():
    print("Vector Clocks - Distributed Smart Traffic Management System")
    print()

    clock = [[0, 0, 0], [0, 0, 0], [0, 0, 0]]
    messages = {}
    event_ids = []
    event_vectors = []

    try:
        f = open(FILE_NAME, "r")
    except FileNotFoundError:
        print("Could not open " + FILE_NAME)
        return

    reader = csv.reader(f)
    next(reader)

    print("Processing events:")
    for row in reader:
        if len(row) < 4:
            continue
        event_id = row[0].strip()
        proc = row[1].strip().upper()
        action = row[2].strip().upper()
        target = row[3].strip().upper()

        p = int(proc[1:]) - 1
        if p < 0 or p >= N:
            print("  " + event_id + ": unknown process " + proc + ", skipped")
            continue

        if action == "SEND":
            clock[p][p] = clock[p][p] + 1
            key = proc + "->" + target
            if key not in messages:
                messages[key] = []
            messages[key].append(list(clock[p]))
            print(f"  {event_id}: {proc} SEND to {target}, {proc} increments V{p + 1} -> {clock[p]} (attached to message)")
        elif action == "RECEIVE":
            key = target + "->" + proc
            if key in messages and len(messages[key]) > 0:
                msg = messages[key].pop(0)
                old = list(clock[p])
                for i in range(N):
                    if msg[i] > clock[p][i]:
                        clock[p][i] = msg[i]
                clock[p][p] = clock[p][p] + 1
                print(f"  {event_id}: {proc} RECEIVE from {target}, max({old}, {msg}) then V{p + 1}+1 -> {clock[p]}")
            else:
                clock[p][p] = clock[p][p] + 1
                print(f"  {event_id}: no message from {target} to {proc} is waiting, treated as internal -> {clock[p]}")
        else:
            clock[p][p] = clock[p][p] + 1
            print(f"  {event_id}: {proc} INTERNAL event, {proc} increments V{p + 1} -> {clock[p]}")

        event_ids.append(event_id)
        event_vectors.append(list(clock[p]))
    f.close()

    print()
    print("Final vector clocks:")
    for i in range(N):
        print(f"  P{i + 1} = {clock[i]}")
    print()

    print("Event pair relations:")
    for i in range(len(event_ids)):
        for j in range(i + 1, len(event_ids)):
            e1 = event_ids[i]
            e2 = event_ids[j]
            v1 = event_vectors[i]
            v2 = event_vectors[j]
            if happened_before(v1, v2):
                print(f"  {e1} {v1} -> {e2} {v2} : Causally Preceding (V({e1}) < V({e2}))")
            elif happened_before(v2, v1):
                print(f"  {e2} {v2} -> {e1} {v1} : Causally Preceding (V({e2}) < V({e1}))")
            else:
                print(f"  {e1} {v1} || {e2} {v2} : Concurrent ({e1} || {e2})")


main()
