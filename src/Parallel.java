import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Parallel {
    private static final int DEFAULT_RECURSION_DEPTH = 2;  // Default depth

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
        int gridSize = (int) Math.pow(3, recursionDepth);
        int[] data = new int[gridSize * gridSize];

        System.out.println("Starting parallel computation with depth: " + recursionDepth);

        // Compute fractal in parallel
        try (ForkJoinPool pool = new ForkJoinPool()) {
            long startTime = System.currentTimeMillis();
            pool.invoke(new FractalTask(data, 0, 0, gridSize, recursionDepth));
            long endTime = System.currentTimeMillis();

            System.out.println("Parallel computation completed in " + (endTime - startTime) + " milliseconds.");
        }

        // Write result to file
        writeToFile(data, gridSize);
    }

    private static class FractalTask extends RecursiveAction {
        private final int[] data;
        private final int x, y, size, depth;

        FractalTask(int[] data, int x, int y, int size, int depth) {
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

            // Fill the area for the current level of recursion
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (isInFractal(x + i, y + j, size)) {
                        data[(y + i) * size + (x + j)] = 1;
                    } else {
                        data[(y + i) * size + (x + j)] = 0;
                    }
                }
            }

            // Create and invoke child tasks
            if (newSize > 0) {
                int newDepth = depth - 1;
                FractalTask[] tasks = new FractalTask[8];
                int index = 0;

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (i == 1 && j == 1) {
                            continue;  // Skip the center block
                        }
                        tasks[index++] = new FractalTask(data, x + i * newSize, y + j * newSize, newSize, newDepth);
                    }
                }

                invokeAll(tasks);  // Execute all tasks in parallel
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
