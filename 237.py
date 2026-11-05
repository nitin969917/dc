# Q237: MPI Matrix Multiplication (Parallel Scatter-Gather)
# Domain: OTC Derivative Pricing Platform
#
# CSV File: matrix_237.csv
# row_id,col_0,col_1,col_2,col_3
# R0,2,4,1,5
# R1,0,6,7,1
# R2,3,9,9,9
# R3,3,8,9,0
#
# The CSV holds one 4x4 matrix A. It is multiplied with itself (C = A x A).
# Master broadcasts B (= A) to every worker, scatters one row of A to each worker
# thread, every worker computes its row of C with dot products, master gathers them.

import csv
import threading

FILE_NAME = "matrix_237.csv"

print_lock = threading.Lock()


def log(msg):
    print_lock.acquire()
    print(msg)
    print_lock.release()


def worker(rank, row_id, row, B, C):
    log(f"[WORKER {rank}] Received row {row_id} = {row}")
    result = []
    for j in range(len(B[0])):
        total = 0
        expr = ""
        for k in range(len(row)):
            total = total + row[k] * B[k][j]
            if k > 0:
                expr = expr + " + "
            expr = expr + str(row[k]) + "*" + str(B[k][j])
        result.append(total)
        log(f"[WORKER {rank}] partial dot product C[{rank}][{j}] = {expr} = {total}")
    C[rank] = result
    log(f"[WORKER {rank}] Sending row {rank} of C back to master: {result}")


def print_matrix(name, M):
    print(name + ":")
    for row in M:
        line = ""
        for value in row:
            line = line + f"{value:6d}"
        print(line)


def main():
    print("MPI Matrix Multiplication (Scatter-Gather) - OTC Derivative Pricing Platform")
    print()

    A = []
    row_ids = []
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
        row_ids.append(row[0].strip())
        values = []
        for k in range(1, 5):
            values.append(int(row[k].strip()))
        A.append(values)
    f.close()

    n = len(A)
    if n != 4:
        print("Matrix must have 4 rows (4x4) to compute A x A, found " + str(n) + " rows")
        return

    B = []
    for row in A:
        B.append(list(row))

    print_matrix("Matrix A (from CSV)", A)
    print_matrix("Matrix B (= A)", B)
    print()

    print(f"[MASTER] Broadcasting matrix B to {n} workers")
    print(f"[MASTER] Scattering rows of A to {n} workers")
    for i in range(n):
        print(f"[MASTER] {row_ids[i]} -> Worker {i}")
    print()

    C = [None] * n
    threads = []
    for i in range(n):
        t = threading.Thread(target=worker, args=(i, row_ids[i], A[i], B, C))
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

    print()
    print("[MASTER] Gathering result rows from all workers")
    print_matrix("Final matrix C = A x B", C)
    print()

    # check the parallel answer with a normal sequential multiplication
    correct = True
    for i in range(n):
        for j in range(n):
            total = 0
            for k in range(n):
                total = total + A[i][k] * B[k][j]
            if total != C[i][j]:
                correct = False
    if correct:
        print("Verification with sequential multiplication: CORRECT")
    else:
        print("Verification with sequential multiplication: MISMATCH")


main()
