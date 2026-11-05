# Q337: Ring Algorithm (Leader Election)
# Domain: Distributed File Storage System (DFS)
#
# CSV File: ring_nodes_337.csv
# node_id,next_node_id,is_active,priority_rank
# 1,2,TRUE,85
# 2,3,TRUE,73
# 3,4,TRUE,55
# 4,5,TRUE,87
# 5,1,FALSE,78
#
# next_node_id gives the logical ring. The node with the highest ID was the old
# coordinator. The election token collects IDs of active nodes; inactive nodes are skipped.

import csv

FILE_NAME = "ring_nodes_337.csv"


def next_active(node, next_of, active, total):
    # move to the next node in the ring, skipping inactive ones
    nxt = next_of[node]
    steps = 0
    while not active[nxt] and steps < total:
        print("  Node " + str(nxt) + " is inactive, skipped")
        nxt = next_of[nxt]
        steps = steps + 1
    return nxt


def main():
    print("Ring Election Algorithm - Distributed File Storage System (DFS)")
    print()

    ids = []
    next_of = {}
    active = {}
    rank = {}

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
        node = int(row[0].strip())
        ids.append(node)
        next_of[node] = int(row[1].strip())
        active[node] = row[2].strip().upper() == "TRUE"
        rank[node] = int(row[3].strip())
    f.close()

    for node in ids:
        if next_of[node] not in active:
            print("Node " + str(node) + " points to unknown node " + str(next_of[node]) + ", ring is broken")
            return

    print("Nodes read from CSV:")
    for node in ids:
        if active[node]:
            state = "ACTIVE"
        else:
            state = "INACTIVE"
        print(f"  Node {node} -> next {next_of[node]}, {state}, priority rank {rank[node]}")

    ring = str(ids[0])
    node = next_of[ids[0]]
    count = 0
    while node != ids[0] and count < len(ids):
        ring = ring + " -> " + str(node)
        node = next_of[node]
        count = count + 1
    print("Ring: " + ring + " -> " + str(ids[0]))
    print()

    coordinator = ids[0]
    for node in ids:
        if node > coordinator:
            coordinator = node

    if active[coordinator]:
        print("Coordinator Node " + str(coordinator) + " is active. No election needed.")
        return

    print("Coordinator Node " + str(coordinator) + " has failed (no reply) -> failure detected")

    initiator = -1
    for node in ids:
        if active[node] and (initiator == -1 or node < initiator):
            initiator = node
    if initiator == -1:
        print("No active node left in the ring")
        return

    token = [initiator]
    print(f"Node {initiator} starts the election, token = {token}")
    print()
    print("ELECTION token circulation:")
    current = initiator
    while True:
        nxt = next_active(current, next_of, active, len(ids))
        if nxt == initiator:
            print(f"  Node {current} -> Node {nxt} : token is back at initiator {token}")
            break
        token.append(nxt)
        print(f"  Node {current} -> Node {nxt} : ELECTION, Node {nxt} adds its ID -> {token}")
        current = nxt
    print()

    leader = token[0]
    for node in token:
        if node > leader:
            leader = node
    print(f"Highest ID in token {token} is {leader}")
    print(f"Node {leader} is elected as the new coordinator (priority rank {rank[leader]})")
    print()

    print("COORDINATOR message circulation:")
    current = initiator
    while True:
        nxt = next_active(current, next_of, active, len(ids))
        print(f"  Node {current} -> Node {nxt} : COORDINATOR({leader})")
        if nxt == initiator:
            break
        current = nxt
    print()
    print(f"All active nodes now know that Node {leader} is the coordinator")


main()
