import mpi.*;
import java.io.*;
import java.util.Arrays;

public class Main {
    private static final int GRID_SIZE = 28;  // Updated grid size (28x28)
    private static final int MAX_DEPTH = 3;   // Recursion depth

    public static void main(String[] args) {
        try {
            MPI.Init(args);

            int rank = MPI.COMM_WORLD.Rank();
            int size = MPI.COMM_WORLD.Size();

            int initialSize = GRID_SIZE;
            int recursionDepth = MAX_DEPTH;

            // Initialize the grid
            int[] data = new int[initialSize * initialSize];

            if (rank == 0) {
                Arrays.fill(data, 0);  // Start with all cells as '0'
            }

            // Broadcast grid size and recursion depth
            MPI.COMM_WORLD.Bcast(new int[]{initialSize}, 0, 1, MPI.INT, 0);
            MPI.COMM_WORLD.Bcast(new int[]{recursionDepth}, 0, 1, MPI.INT, 0);

            // Calculate the range of rows for this process
            int rowsPerProcess = (initialSize + size - 1) / size;
            int startRow = rank * rowsPerProcess;
            int endRow = Math.min(startRow + rowsPerProcess, initialSize);

            // Compute fractal for the assigned rows
            computeFractal(data, 0, 0, initialSize, recursionDepth, startRow, endRow);

            // Gather all parts to the root process
            int[] globalData = new int[initialSize * initialSize];
            MPI.COMM_WORLD.Gather(data, startRow * initialSize, rowsPerProcess * initialSize, MPI.INT, globalData, startRow * initialSize, rowsPerProcess * initialSize, MPI.INT, 0);

            // Only rank 0 writes the result to the file
            if (rank == 0) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"))) {
                    for (int i = 0; i < initialSize; i++) {
                        for (int j = 0; j < initialSize; j++) {
                            writer.write(globalData[i * initialSize + j] + " ");
                        }
                        writer.newLine();
                    }
                }
            }

            MPI.Finalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                        continue; // Skip the center square
                    }
                    computeFractal(data, x + i * newSize, y + j * newSize, newSize, newDepth, startRow, endRow);
                }
            }
        }
    }

    private static boolean isInFractal(int x, int y, int size) {
        while (size > 0) {
            if ((x % 3 == 1) && (y % 3 == 1)) {
                return false;
            }
            x /= 3;
            y /= 3;
            size /= 3;
        }
        return true;
    }
}
