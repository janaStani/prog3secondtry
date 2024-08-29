import mpi.*;
import java.io.*;
import java.util.Arrays;

public class Main {
    private static final int GRID_SIZE = 3;  // Grid size (81x81)
    private static final int MAX_DEPTH = 1;   // Recursion depth

    public static void main(String[] args) {
        try {
            MPI.Init(args);

            int rank = MPI.COMM_WORLD.Rank();
            int size = MPI.COMM_WORLD.Size();
            System.out.println("Hello from rank " + rank + " of " + size);

            int initialSize = GRID_SIZE;  // store the grid size, use later for the fractal
            int recursionDepth = MAX_DEPTH; // store the recursion depth, use later for the fractal

            // array to store the fractal data
            int[] data = new int[initialSize * initialSize]; // 81x81 = 6561

            if (rank == 0) {
                Arrays.fill(data, 0);  // set all values to 0, initially
            }

            // broadcast grid size and recursion depth from the root process to all other processes
            MPI.COMM_WORLD.Bcast(new int[]{initialSize}, 0, 1, MPI.INT, 0);
            MPI.COMM_WORLD.Bcast(new int[]{recursionDepth}, 0, 1, MPI.INT, 0);

            // calculate number of rows each process is responsible for
            int rowsPerProcess = (initialSize + size - 1) / size; // distribute rows
            int startRow = rank * rowsPerProcess; // each process starts from different row
            int endRow = Math.min(startRow + rowsPerProcess, initialSize); // calculate end row

            // compute the fractals for each process
            computeFractal(data, 0, 0, initialSize, recursionDepth, startRow, endRow);

            // array in the root process to store the complete grid data after gathering from all processes
            int[] globalData = new int[initialSize * initialSize];
            // gather the data from all processes to the root process
            MPI.COMM_WORLD.Gather(data, startRow * initialSize, rowsPerProcess * initialSize, MPI.INT, globalData, 0, rowsPerProcess * initialSize, MPI.INT, 0);

            // check if the current process is the root process, only the root process writes the result to the file
            if (rank == 0) {
                //open a file to write the result
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"))) {
                    for (int i = 0; i < initialSize; i++) { // go through each row
                        for (int j = 0; j < initialSize; j++) {  // go through each column
                            writer.write(globalData[i * initialSize + j] + " ");  // write value of each cell to a file
                        }
                        writer.newLine(); // new line after each row
                    }
                }
            }

            MPI.Finalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // fractal computation method
    private static void computeFractal(int[] data, int x, int y, int size, int depth, int startRow, int endRow) {
        if (depth == 0) {
            return;
        }

        int newSize = size / 3; // divide the grid in 9 sub-squares

        // iterate over each row within the range of current process
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < size; j++) { // iterate over each column
                if (isInFractal(i, j, size)) {
                    data[i * size + j] = 1; // set the value to 1, filled black square
                } else {
                    data[i * size + j] = 0; // set the value to 0, empty white square
                }
            }
        }

        // if zero no need to go further
        if (newSize > 0) {
            int newDepth = depth - 1; // calculate next recursion depth
            for (int i = 0; i < 3; i++) {  // iterate over the sub-squares row-wise
                for (int j = 0; j < 3; j++) { // iterate over the sub-squares column-wise
                    if (i == 1 && j == 1) {
                        continue; // skip the center square
                    }
                    computeFractal(data, x + i * newSize, y + j * newSize, newSize, newDepth, startRow, endRow);
                }
            }
        }
    }

    // checks if a cell (x, y) is in the fractal
    private static boolean isInFractal(int x, int y, int size) {
        while (size > 0) {
            if ((x % 3 == 1) && (y % 3 == 1)) {
                return false; // the cell is in the center square
            }
            x /= 3;   // move to the next sub-square
            y /= 3;
            size /= 3;
        }
        return true;
    }
}
