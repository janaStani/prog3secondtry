import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;

public class Parallel {
    private static final int DEFAULT_RECURSION_DEPTH = 4;  // default depth
    private static final ForkJoinPool pool = new ForkJoinPool();  // ForkJoinPool for parallel tasks

    public static void main(String[] args) {

        // set default recursion depth
        int recursionDepth = DEFAULT_RECURSION_DEPTH;

        // command line argument handling for the recursion depth
        if (args.length > 0) {
            try {
                recursionDepth = Integer.parseInt(args[0]);  // read recursion depth from the first argument
            } catch (NumberFormatException e) {
                System.err.println("Invalid recursion depth. Using default value of " + DEFAULT_RECURSION_DEPTH + ".");
            }
        } else {
            System.out.println("No recursion depth argument provided. Using default value of " + DEFAULT_RECURSION_DEPTH + ".");
        }

        // calculate grid size based on recursion depth
        long gridSize = (long) Math.pow(3, recursionDepth);

        // array for storing fractal data
        int[] data = new int[(int) (gridSize * gridSize)];

        System.out.println("Starting parallel computation with depth: " + recursionDepth);

        // print the number of threads in the ForkJoinPool
        System.out.println("Number of threads in ForkJoinPool: " + pool.getParallelism());

        // track time for computing
        long startTime = System.nanoTime();

        // start parallel computation from (0, 0) with size of current grid and depth of recursion and store in data array
        pool.invoke(new ComputeTask(data, 0, 0, (int) gridSize, recursionDepth));

        long endTime = System.nanoTime();

        // calculate time in ms with three decimal places
        double elapsedTime = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Parallel computation completed in %.3f milliseconds.%n", elapsedTime);

        // write result to file
        writeToFile(data, (int)gridSize);
    }

    // RecursiveAction because we don't need to return a result
    private static class ComputeTask extends RecursiveAction {
        private final int[] data;               // store fractal data
        private final int x, y, size, depth;

        // constructor
        ComputeTask(int[] data, int x, int y, int size, int depth) {
            this.data = data;
            this.x = x;
            this.y = y;
            this.size = size;
            this.depth = depth;
        }

        // compute method
        @Override
        protected void compute() {

            if (depth == 0) {    // base case, stop condition
                return;
            }

            int newSize = size / 3;   // next subgrid size

            // ensure that the x and y indices are within the grid bounds
            if (x < 0 || y < 0 || x + size > (int) Math.pow(3, depth) || y + size > (int) Math.pow(3, depth)) {
                return;
            }

            for (int i = 0; i < size; i++) {                                             // go through current grid
                for (int j = 0; j < size; j++) {
                    if (isInFractal(i, j, size)) {                                       // call helper method
                        data[(y + i) * (int) Math.pow(3, depth) + (x + j)] = 1;          // set to 1 (black)
                    } else {
                        data[(y + i) * (int) Math.pow(3, depth) + (x + j)] = 0;          // set to 0 (white)
                    }
                }
            }

            if (newSize > 0) {
                int newDepth = depth - 1;                     // decrease depth
                ComputeTask[] tasks = new ComputeTask[8];     // array to store 8 sub-grids to work on, excluding center block
                int taskIndex = 0;
                for (int i = 0; i < 3; i++) {                 // iterate through sub-grids
                    for (int j = 0; j < 3; j++) {
                        if (i == 1 && j == 1) {
                            continue;  // skip the center block
                        }
                        tasks[taskIndex++] = new ComputeTask(data, x + i * newSize, y + j * newSize, newSize, newDepth);
                        // recursive call to compute fractal for each sub-grid, increment task index for next task
                    }
                }
                invokeAll(tasks); // takes an array of tasks and invokes them in parallel
                // each task will execute compute method independently on its respective sub-grid
            }
        }
    }

    private static boolean isInFractal(int x, int y, int size) {
        while (size > 0) {
            if ((x % 3 == 1) && (y % 3 == 1)) {
                return false;  // center block of current sub-grid
            }
            x /= 3;   // go to the next sub-grid
            y /= 3;
            size /= 3;    // decrease size
        }
        return true;   // cell is in the fractal, if not center block
    }

    // write result to file
    private static void writeToFile(int[] data, int gridSize) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"))) {
            for (int i = 0; i < gridSize; i++) {                      // go through the result array
                for (int j = 0; j < gridSize; j++) {
                    writer.write(data[i * gridSize + j] + " ");
                }
                writer.newLine();
            }
        } catch (IOException e) {  // handle exception
            e.printStackTrace();
        }
    }
}
