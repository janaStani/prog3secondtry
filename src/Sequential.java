import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Sequential {
    private static final int DEFAULT_RECURSION_DEPTH = 4;  // default depth
    private static final int MAX_RECURSION_DEPTH = 10;     // recursion limit

    public static void main(String[] args) {
        // set default recursion depth
        int recursionDepth = DEFAULT_RECURSION_DEPTH;

        // command line argument handling for the recursion depth
        if (args.length > 0) {
            try {
                recursionDepth = Integer.parseInt(args[0]);
                if (recursionDepth > MAX_RECURSION_DEPTH) {
                    System.err.println("Recursion depth is too large. Using maximum value of " + MAX_RECURSION_DEPTH + ".");
                    recursionDepth = MAX_RECURSION_DEPTH;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid recursion depth. Using default value of " + DEFAULT_RECURSION_DEPTH + ".");
            }
        } else {
            System.out.println("No recursion depth argument provided. Using default value of " + DEFAULT_RECURSION_DEPTH + ".");
        }

        long gridSize = (long) Math.pow(3, recursionDepth);  // calc grid based on rec

        int[] data = new int[(int) (gridSize * gridSize)];  // array to store fractal data

        Thread currentThread = Thread.currentThread();
        System.out.printf("Running on thread: %s (ID: %d)%n", currentThread.getName(), currentThread.threadId());
        System.out.println("Starting sequential computation with depth: " + recursionDepth);

        // track computation time for the fractal
        long startTime = System.nanoTime();

        // start from (0, 0), size of current grid, and depth of recursion
        computeFractal(data, 0, 0, (int) gridSize, recursionDepth);

        long endTime = System.nanoTime();

        // calculate time in ms with three decimal places
        double elapsedTime = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Sequential computation completed in %.3f milliseconds.%n", elapsedTime);

        // write result to file
        writeToFile(data, (int) gridSize);
    }

    private static void computeFractal(int[] data, int x, int y, int size, int depth) {
        // array to store the data of current process, leftmost, topmost coordinate, size of current section, fractal lvl

        if (depth == 0) {   // base case
            return;
        }

        int newSize = size / 3;  // next subgrid size

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

        if (newSize > 0) {                     // check new size
            int newDepth = depth - 1;          // decrease depth
            for (int i = 0; i < 3; i++) {      // iterate through sub-grids
                for (int j = 0; j < 3; j++) {
                    if (i == 1 && j == 1) {
                        continue;  // center block is empty
                    }
                    computeFractal(data, x + i * newSize, y + j * newSize, newSize, newDepth);
                } // recursive call to compute fractal for each sub-grid
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
