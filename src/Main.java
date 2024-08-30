import mpi.*;
import java.io.*;
import java.util.Arrays;

public class Main {

    // Default grid size and recursion depth
    private static final int DEFAULT_GRID_SIZE = 9;  // 3^2 for depth 2
    private static final int DEFAULT_RECURSION_DEPTH = 2;

    public static void main(String[] args) {
        try {
            MPI.Init(args);

            int rank = MPI.COMM_WORLD.Rank();
            int size = MPI.COMM_WORLD.Size();

            int gridSize = DEFAULT_GRID_SIZE;
            int recursionDepth = DEFAULT_RECURSION_DEPTH;  // default depth

            if (args.length > 0) {
                try {
                    recursionDepth = Integer.parseInt(args[args.length - 1]);
                    if (rank == 0) {
                        System.out.println("Recursion Depth set to: " + recursionDepth);
                    }
                } catch (NumberFormatException e) {
                    if (rank == 0) {
                        System.err.println("Invalid recursion depth. Using default value of " + DEFAULT_RECURSION_DEPTH + ".");
                    }
                }
            } else {
                if (rank == 0) {
                    System.out.println("No recursion depth argument provided. Using default value of " + DEFAULT_RECURSION_DEPTH + ".");
                }
            }

            // Calculate grid size based on recursion depth
            gridSize = (int) Math.pow(3, recursionDepth);

            // Initialize array to store fractal data
            int[] data = new int[gridSize * gridSize];

            if (rank == 0) {
                Arrays.fill(data, 0);  // Initialize with all 0s
            }

            // Broadcast grid size and recursion depth from the root process to all other processes
            MPI.COMM_WORLD.Bcast(new int[]{gridSize}, 0, 1, MPI.INT, 0);
            MPI.COMM_WORLD.Bcast(new int[]{recursionDepth}, 0, 1, MPI.INT, 0);

            // Calculate number of rows each process is responsible for
            int rowsPerProcess = (gridSize + size - 1) / size;  // Distribute rows
            int startRow = rank * rowsPerProcess;  // Each process starts from a different row
            int endRow = Math.min(startRow + rowsPerProcess, gridSize);  // Calculate end row

            // Compute the fractal for each process
            computeFractal(data, 0, 0, gridSize, recursionDepth, startRow, endRow);

            // Array in the root process to store the complete grid data after gathering from all processes
            int[] globalData = new int[gridSize * gridSize];

            // Gather the data from all processes to the root process
            MPI.COMM_WORLD.Gather(data, startRow * gridSize, rowsPerProcess * gridSize, MPI.INT, globalData, 0, rowsPerProcess * gridSize, MPI.INT, 0);

            // If the current process is the root process, write the result to the file
            if (rank == 0) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"))) {
                    for (int i = 0; i < gridSize; i++) {  // Go through each row
                        for (int j = 0; j < gridSize; j++) {  // Go through each column
                            writer.write(globalData[i * gridSize + j] + " ");  // Write value of each cell to the file
                        }
                        writer.newLine();  // New line after each row
                    }
                }
            }

            MPI.Finalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fractal computation method
    private static void computeFractal(int[] data, int x, int y, int size, int depth, int startRow, int endRow) {
        if (depth == 0) {
            return;
        }

        int newSize = size / 3;

        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < size; j++) {
                if (isInFractal(i, j, size)) {
                    data[i * size + j] = 1;
                } else {
                    data[i * size + j] = 0;
                }
            }
        }

        if (newSize > 0) {
            int newDepth = depth - 1;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (i == 1 && j == 1) {
                        continue;  // Center block is empty
                    }
                    computeFractal(data, x + i * newSize, y + j * newSize, newSize, newDepth, startRow, endRow);
                }
            }
        }
    }

    // Checks if a cell (x, y) is in the fractal
    private static boolean isInFractal(int x, int y, int size) {
        while (size > 0) {
            if ((x % 3 == 1) && (y % 3 == 1)) {
                return false;  // Inside center block
            }
            x /= 3;
            y /= 3;
            size /= 3;
        }
        return true;
    }
}
