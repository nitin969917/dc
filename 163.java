// Q163: MPI Matrix Multiplication (Parallel Scatter-Gather)
// Domain: Stock Market Exchange Order Matching
//
// CSV File: matrix_163.csv
// row_id,col_0,col_1,col_2,col_3
// R0,2,4,1,5
// R1,9,2,3,2
// R2,4,9,7,0
// R3,1,9,4,7
//
// The CSV holds one 4x4 matrix A. It is multiplied with itself (C = A x A).
// Master broadcasts B (= A) to every worker, scatters one row of A to each worker
// thread, every worker computes its row of C with dot products, master gathers them.

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

class Worker extends Thread {
    int rank;
    String rowId;
    int[] row;
    int[][] B;
    int[][] C;

    Worker(int rank, String rowId, int[] row, int[][] B, int[][] C) {
        this.rank = rank;
        this.rowId = rowId;
        this.row = row;
        this.B = B;
        this.C = C;
    }

    public void run() {
        System.out.println("[WORKER " + rank + "] Received row " + rowId + " = " + Arrays.toString(row));
        int[] result = new int[B[0].length];
        for (int j = 0; j < B[0].length; j++) {
            int total = 0;
            String expr = "";
            for (int k = 0; k < row.length; k++) {
                total = total + row[k] * B[k][j];
                if (k > 0) {
                    expr = expr + " + ";
                }
                expr = expr + row[k] + "*" + B[k][j];
            }
            result[j] = total;
            System.out.println("[WORKER " + rank + "] partial dot product C[" + rank + "][" + j + "] = " + expr + " = " + total);
        }
        C[rank] = result;
        System.out.println("[WORKER " + rank + "] Sending row " + rank + " of C back to master: " + Arrays.toString(result));
    }
}

class Main {
    static final String FILE_NAME = "matrix_163.csv";

    static void printMatrix(String name, int[][] M) {
        System.out.println(name + ":");
        for (int[] row : M) {
            String line = "";
            for (int value : row) {
                line = line + String.format("%6d", value);
            }
            System.out.println(line);
        }
    }

    public static void main(String[] args) {
        System.out.println("MPI Matrix Multiplication (Scatter-Gather) - Stock Market Exchange Order Matching");
        System.out.println();

        ArrayList<int[]> rows = new ArrayList<>();
        ArrayList<String> rowIds = new ArrayList<>();
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
                rowIds.add(row[0].trim());
                int[] values = new int[4];
                for (int k = 1; k < 5; k++) {
                    values[k - 1] = Integer.parseInt(row[k].trim());
                }
                rows.add(values);
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Could not open " + FILE_NAME);
            return;
        }

        int n = rows.size();
        if (n != 4) {
            System.out.println("Matrix must have 4 rows (4x4) to compute A x A, found " + n + " rows");
            return;
        }

        int[][] A = new int[n][];
        int[][] B = new int[n][];
        for (int i = 0; i < n; i++) {
            A[i] = rows.get(i);
            B[i] = rows.get(i).clone();
        }

        printMatrix("Matrix A (from CSV)", A);
        printMatrix("Matrix B (= A)", B);
        System.out.println();

        System.out.println("[MASTER] Broadcasting matrix B to " + n + " workers");
        System.out.println("[MASTER] Scattering rows of A to " + n + " workers");
        for (int i = 0; i < n; i++) {
            System.out.println("[MASTER] " + rowIds.get(i) + " -> Worker " + i);
        }
        System.out.println();

        int[][] C = new int[n][];
        Worker[] workers = new Worker[n];
        for (int i = 0; i < n; i++) {
            workers[i] = new Worker(i, rowIds.get(i), A[i], B, C);
            workers[i].start();
        }

        for (int i = 0; i < n; i++) {
            try {
                workers[i].join();
            } catch (InterruptedException e) {
                System.out.println("Master interrupted");
            }
        }

        System.out.println();
        System.out.println("[MASTER] Gathering result rows from all workers");
        printMatrix("Final matrix C = A x B", C);
        System.out.println();

        // check the parallel answer with a normal sequential multiplication
        boolean correct = true;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int total = 0;
                for (int k = 0; k < n; k++) {
                    total = total + A[i][k] * B[k][j];
                }
                if (total != C[i][j]) {
                    correct = false;
                }
            }
        }
        if (correct) {
            System.out.println("Verification with sequential multiplication: CORRECT");
        } else {
            System.out.println("Verification with sequential multiplication: MISMATCH");
        }
    }
}
