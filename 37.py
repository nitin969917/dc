# Q37: Clock Synchronization - Lamport's Logical Clock
# Domain: Adaptive Distributed Computing Framework for Task Execution
#
# CSV File: events_37.csv
# event_id,process_id,event_type,message_to,initial_clock
# E1,P1,SEND,P2,0
# E2,P3,INTERNAL,NONE,0
# E3,P2,RECEIVE,P1,0
# E4,P3,SEND,P1,0
# E5,P3,SEND,P1,0
# E6,P1,RECEIVE,P3,0
#
# event_type is SEND, RECEIVE or INTERNAL. For SEND, message_to is the receiver.
# For RECEIVE, message_to holds the process the message came from. INTERNAL uses NONE.
# initial_clock is the starting clock value of that process.
# Rules: SEND / INTERNAL -> L = L + 1,  RECEIVE -> L = max(L, T_msg) + 1

import csv

FILE_NAME = "events_37.csv"


def main():
    print("Lamport Logical Clock - Adaptive Distributed Computing Framework for Task Execution")
    print()

    clocks = {}
    messages = {}
    event_ids = []
    event_procs = []
    stamps = []

    try:
        f = open(FILE_NAME, "r")
    except FileNotFoundError:
        print("Could not open " + FILE_NAME)
        return

    reader = csv.reader(f)
    next(reader)

    print("Step-by-step clock updates:")
    for row in reader:
        if len(row) < 5:
            continue
        event_id = row[0].strip()
        proc = row[1].strip().upper()
        event_type = row[2].strip().upper()
        target = row[3].strip().upper()
        initial = int(row[4].strip())

        if proc not in clocks:
            clocks[proc] = initial
            print(f"  ({proc} starts with clock {initial})")

        old = clocks[proc]
        if event_type == "SEND":
            clocks[proc] = old + 1
            key = proc + "->" + target
            if key not in messages:
                messages[key] = []
            messages[key].append(clocks[proc])
            print(f"  {event_id}: {proc} SEND to {target}   L = {old} + 1 = {clocks[proc]}, message sent with T_msg = {clocks[proc]}")
        elif event_type == "RECEIVE":
            key = target + "->" + proc
            if key in messages and len(messages[key]) > 0:
                t_msg = messages[key].pop(0)
                clocks[proc] = max(old, t_msg) + 1
                print(f"  {event_id}: {proc} RECEIVE from {target}   L = max({old}, {t_msg}) + 1 = {clocks[proc]}")
            else:
                clocks[proc] = old + 1
                print(f"  {event_id}: no message from {target} to {proc} is waiting, treated as internal   L = {old} + 1 = {clocks[proc]}")
        else:
            clocks[proc] = old + 1
            print(f"  {event_id}: {proc} INTERNAL event   L = {old} + 1 = {clocks[proc]}")

        event_ids.append(event_id)
        event_procs.append(proc)
        stamps.append(clocks[proc])
    f.close()

    print()
    print("Final clock of each process:")
    for proc in clocks:
        print(f"  {proc} = {clocks[proc]}")
    print()

    # bubble sort on (timestamp, process id) to get the total order
    order = []
    for i in range(len(event_ids)):
        order.append(i)
    for i in range(len(order)):
        for j in range(len(order) - 1 - i):
            a = order[j]
            b = order[j + 1]
            if stamps[a] > stamps[b] or (stamps[a] == stamps[b] and event_procs[a] > event_procs[b]):
                order[j] = b
                order[j + 1] = a

    print("Final total causal order (sorted by timestamp, ties broken by process id):")
    chain = ""
    for k in range(len(order)):
        i = order[k]
        print(f"  {k + 1}. {event_ids[i]} at {event_procs[i]} with L = {stamps[i]}")
        if k > 0:
            chain = chain + " -> "
        chain = chain + event_ids[i] + "(" + str(stamps[i]) + ")"
    print("  " + chain)


main()
