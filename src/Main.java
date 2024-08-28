import mpi.*;
import java.io.*;
import java.util.*;

public class Main {
    private static final int GRID_SIZE = 27;  // Size of the grid
    private static final int MAX_DEPTH = 3;   // Recursion depth

    public static void main(String[] args) {
        try {
            MPI.Init(args);

            int rank = MPI.COMM_WORLD.Rank();
            int size = MPI.COMM_WORLD.Size();

            // Define the grid size and depth
            int initialSize = GRID_SIZE;
            int recursionDepth = MAX_DEPTH;

            // Initialize the grid
            int[] data = new int[initialSize * initialSize];
            if (rank == 0) {
                Arrays.fill(data, 1);  // Start with all cells as '1'
            }

            // Broadcast the size and recursion depth
            MPI.COMM_WORLD.Bcast(new int[]{initialSize}, 0, 1, MPI.INT, 0);
            MPI.COMM_WORLD.Bcast(new int[]{recursionDepth}, 0, 1, MPI.INT, 0);

            // Broadcast the initial grid data
            MPI.COMM_WORLD.Bcast(data, 0, data.length, MPI.INT, 0);

            // Compute the fractal
            computeFractal(data, 0, 0, initialSize, recursionDepth, rank, size);

            // Gather the results to the root process
            if (rank == 0) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"))) {
                    for (int i = 0; i < initialSize; i++) {
                        for (int j = 0; j < initialSize; j++) {
                            writer.write(data[i * initialSize + j] + " ");
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

    private static void computeFractal(int[] data, int x, int y, int size, int depth, int rank, int numProcesses) {
        if (depth == 0) {
            return;
        }

        int newSize = size / 3;
        if (size <= 1) return;

        // Calculate the range of rows for this process
        int startRow = (rank * size) / numProcesses;
        int endRow = ((rank + 1) * size) / numProcesses;

        // Perform computation
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < size; j++) {
                data[i * size + j] = 1;
            }
        }

        // Recursively apply the fractal pattern
        if (size > 1) {
            int newDepth = depth - 1;
            int subSize = size / 3;

            for (int i = 0; i < 9; i++) {
                int subX = x + (i % 3) * subSize;
                int subY = y + (i / 3) * subSize;

                if (i != 4) {  // Skip the center square
                    computeFractal(data, subX, subY, subSize, newDepth, rank, numProcesses);
                }
            }
        }
    }
}
