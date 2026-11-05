# Q2: Bully Algorithm (Leader Election)
# Domain: TrustMesh: Blockchain-Enabled Trusted IoT Framework
#
# CSV File: bully_cluster_2.csv
# node_id,is_alive,is_current_coordinator
# 1,TRUE,FALSE
# 2,TRUE,FALSE
# 3,TRUE,FALSE
# 4,FALSE,FALSE
# 5,TRUE,FALSE
# 6,FALSE,TRUE

import csv

FILE_NAME = "bully_cluster_2.csv"


def main():
    print("Bully Election Algorithm - TrustMesh: Blockchain-Enabled Trusted IoT Framework")
    print()

    ids = []
    alive = {}
    coordinator = -1

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
        node = int(row[0].strip())
        ids.append(node)
        alive[node] = row[1].strip().upper() == "TRUE"
        if row[2].strip().upper() == "TRUE":
            coordinator = node
    f.close()

    if len(ids) == 0:
        print("No nodes found in " + FILE_NAME)
        return

    ids.sort()

    print("Nodes read from CSV:")
    for node in ids:
        if alive[node]:
            line = "  Node " + str(node) + " : ALIVE"
        else:
            line = "  Node " + str(node) + " : DOWN"
        if node == coordinator:
            line = line + " (current coordinator)"
        print(line)
    print()

    if coordinator == -1:
        coordinator = ids[len(ids) - 1]
        print("No coordinator marked in CSV, assuming highest node " + str(coordinator) + " was the coordinator")

    if alive[coordinator]:
        print("Coordinator Node " + str(coordinator) + " is alive. No election needed.")
        return

    print("Coordinator Node " + str(coordinator) + " is not responding -> failure detected")

    # lowest alive node notices the failure first and starts the election
    initiator = -1
    for node in ids:
        if alive[node]:
            initiator = node
            break

    if initiator == -1:
        print("No alive nodes left, election cannot be held")
        return

    print("Node " + str(initiator) + " detects the failure and starts the election")
    print()

    # every node that replies OK takes over and holds its own election
    waiting = [initiator]
    done = []
    leader = -1

    while len(waiting) > 0:
        current = waiting.pop(0)
        if current in done:
            continue
        done.append(current)

        print("Node " + str(current) + " holds an election:")
        got_ok = False
        for node in ids:
            if node <= current:
                continue
            print("  Node " + str(current) + " -> Node " + str(node) + " : ELECTION")
            if alive[node]:
                print("  Node " + str(node) + " -> Node " + str(current) + " : OK")
                got_ok = True
                if node not in waiting and node not in done:
                    waiting.append(node)
            else:
                print("  Node " + str(node) + " does not respond (down)")

        if got_ok:
            print("  Node " + str(current) + " received OK, so it steps back and waits")
        else:
            print("  Node " + str(current) + " received no OK, so it wins the election")
            leader = current
        print()

    print("Node " + str(leader) + " broadcasts COORDINATOR message to lower nodes:")
    for node in ids:
        if node == leader:
            continue
        if alive[node]:
            print("  Node " + str(leader) + " -> Node " + str(node) + " : COORDINATOR(" + str(leader) + ")")
        else:
            print("  Node " + str(node) + " is down, message not delivered")
    print()

    print("Final status:")
    for node in ids:
        if node == leader:
            print("  Node " + str(node) + " : ALIVE (new coordinator)")
        elif alive[node]:
            print("  Node " + str(node) + " : ALIVE")
        else:
            print("  Node " + str(node) + " : DOWN")
    print("New coordinator is Node " + str(leader))


main()
