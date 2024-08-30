import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Sequential {
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

        System.out.println("Starting sequential computation with depth: " + recursionDepth);

        // Compute fractal
        long startTime = System.currentTimeMillis();
        computeFractal(data, 0, 0, gridSize, recursionDepth);
        long endTime = System.currentTimeMillis();

        System.out.println("Sequential computation completed in " + (endTime - startTime) + " milliseconds.");

        // Write result to file
        writeToFile(data, gridSize);
    }

    private static void computeFractal(int[] data, int x, int y, int size, int depth) {
        System.out.println("Computing on thread: " + Thread.currentThread().getName());
        if (depth == 0) {
            return;
        }

        int newSize = size / 3;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (isInFractal(i, j, size)) {
                    data[(y + i) * size + (x + j)] = 1;
                } else {
                    data[(y + i) * size + (x + j)] = 0;
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
                    computeFractal(data, x + i * newSize, y + j * newSize, newSize, newDepth);
                }
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
