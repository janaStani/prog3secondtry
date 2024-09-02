import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;

public class Parallel {
    private static final int DEFAULT_RECURSION_DEPTH = 4;  // Default depth
    private static final int MAX_SAFE_GRID_SIZE = 46340;   // Approximate square root of Integer.MAX_VALUE for grid size
    private static final ForkJoinPool pool = new ForkJoinPool();  // ForkJoinPool for parallel tasks

    public static void main(String[] args) {
        // Set default recursion depth
        int recursionDepth = DEFAULT_RECURSION_DEPTH;

        // Check if argument for depth is provided
        if (args.length > 0) {
            try {
                recursionDepth = Integer.parseInt(args[0]);  // Read recursion depth from the first argument
            } catch (NumberFormatException e) {
                System.err.println("Invalid recursion depth. Using default value of " + DEFAULT_RECURSION_DEPTH + ".");
            }
        } else {
            System.out.println("No recursion depth argument provided. Using default value of " + DEFAULT_RECURSION_DEPTH + ".");
        }

        // Calculate grid size based on recursion depth
        long gridSize = (long) Math.pow(3, recursionDepth);

        // Check for overflow and adjust recursion depth if needed
        while (gridSize > MAX_SAFE_GRID_SIZE) {
            System.err.println("Grid size too large (" + gridSize + "), reducing recursion depth...");
            recursionDepth--;
            gridSize = (long) Math.pow(3, recursionDepth);
        }

        // Now gridSize is safe to cast to int
        int safeGridSize = (int) gridSize;
        int[] data = new int[safeGridSize * safeGridSize];

        System.out.println("Starting parallel computation with depth: " + recursionDepth);

        // Print the number of threads in the ForkJoinPool
        System.out.println("Number of threads in ForkJoinPool: " + pool.getParallelism());

        // Compute fractal
        long startTime = System.nanoTime();
        pool.invoke(new ComputeTask(data, 0, 0, safeGridSize, recursionDepth));
        long endTime = System.nanoTime();

        // Calculate elapsed time in milliseconds with three decimal places
        double elapsedTime = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Parallel computation completed in %.3f milliseconds.%n", elapsedTime);

        // Write result to file
        writeToFile(data, safeGridSize);
    }

    private static class ComputeTask extends RecursiveAction {
        private final int[] data;
        private final int x, y, size, depth;

        ComputeTask(int[] data, int x, int y, int size, int depth) {
            this.data = data;
            this.x = x;
            this.y = y;
            this.size = size;
            this.depth = depth;
        }

        @Override
        protected void compute() {
            if (depth == 0) {
                return;
            }

            int newSize = size / 3;

            // Ensure that the x and y indices are within the grid bounds
            if (x < 0 || y < 0 || x + size > (int) Math.pow(3, depth) || y + size > (int) Math.pow(3, depth)) {
                return;
            }

            // Set the fractal pattern
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (isInFractal(i, j, size)) {
                        data[(y + i) * (int) Math.pow(3, depth) + (x + j)] = 1;
                    } else {
                        data[(y + i) * (int) Math.pow(3, depth) + (x + j)] = 0;
                    }
                }
            }

            if (newSize > 0) {
                int newDepth = depth - 1;
                ComputeTask[] tasks = new ComputeTask[8];
                int taskIndex = 0;
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (i == 1 && j == 1) {
                            continue;  // Skip the center block
                        }
                        tasks[taskIndex++] = new ComputeTask(data, x + i * newSize, y + j * newSize, newSize, newDepth);
                    }
                }
                invokeAll(tasks);
            }
        }
    }

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

    private static void writeToFile(int[] data, int gridSize) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"))) {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    writer.write(data[i * gridSize + j] + " ");
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
