import mpi.*;
import java.io.*; // for the result.txt file
import java.util.Arrays;

public class Main {
    private static final int GRID_SIZE = 28;  // Updated grid size (28x28)
    private static final int MAX_DEPTH = 3;   // Recursion depth

    public static void main(String[] args) {
        try {
            MPI.Init(args);

            int rank = MPI.COMM_WORLD.Rank();
            int size = MPI.COMM_WORLD.Size();
            System.out.println("Hello from rank " + rank + " of " + size);


            int initialSize = GRID_SIZE;  // store the grid size, use later for the fractal
            int recursionDepth = MAX_DEPTH; // store the recursion depth, use later for the fractal

            // array to store the fractal data
            int[] data = new int[initialSize * initialSize]; //28x28 = 784

            if (rank == 0) {
                Arrays.fill(data, 0);  // set all values to 0, initially
            }

            // broadcast grid size and recursion depth from the root process to all other processes
            MPI.COMM_WORLD.Bcast(new int[]{initialSize}, 0, 1, MPI.INT, 0);
            MPI.COMM_WORLD.Bcast(new int[]{recursionDepth}, 0, 1, MPI.INT, 0);

            // calculate number of rows each process is responsible for
            int rowsPerProcess = (initialSize + size - 1) / size; // 28/4 = 7 each process 7 rows
            int startRow = rank * rowsPerProcess; // each process starts from 0, 7, 14, 21
            int endRow = Math.min(startRow + rowsPerProcess, initialSize); // ending row index 7, 14, 21, 28

            // compute the fractals for each process, takes the array to store the fractal,
            // starting coordinates of the grid, size of grid, recursion depth, range of rows to compute
            computeFractal(data, 0, 0, initialSize, recursionDepth, startRow, endRow);

            // array in the root process to store the complete grid data after gathering from all processes
            int[] globalData = new int[initialSize * initialSize];
            // gather the data from all processes to the root process
            // local array to store the fractal, offset in the local data array,
            // number of elements to gather from each process, datatype being gathered,array in root process,
            // offset in global array, number of elements to receive from each process, rank of root process
            MPI.COMM_WORLD.Gather(data, startRow * initialSize, rowsPerProcess * initialSize, MPI.INT, globalData, startRow * initialSize, rowsPerProcess * initialSize, MPI.INT, 0);

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
    //takes the array to store the fractal, (x,y) of top-left corner of current sub-grid, size of current grid,
    // recursion depth, row range to compute
    private static void computeFractal(int[] data, int x, int y, int size, int depth, int startRow, int endRow) {
        if (depth == 0) { // base case
            return;
        }

        int newSize = size / 3; // divide the grid in 9 sub-squares

        // iterate over each row within the range of current process
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < size; j++) { // iterate over each column
                if (isInFractal(i, j, size)) { // check current cell state, call helper method
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
                    if (i == 1 && j == 1) {   // center square
                        continue; // skip the center square
                    }
                    computeFractal(data, x + i * newSize, y + j * newSize, newSize, newDepth, startRow, endRow);
                    // recursively call the method for each sub-square
                    // takes array that holds grid values, (x,y) of top-left corner of current sub-grid, size of sub-grid,
                    // the new depth, same row range because the same process is responsible for the sub-grid
                }
            }
        }
    }

    //takes x (row index), y (column index), size of the grid at the current recursion level
    private static boolean isInFractal(int x, int y, int size) {
        while (size > 0) {   // until it checks all
            if ((x % 3 == 1) && (y % 3 == 1)) {  // if both are true means middle col and row, center square
                return false;   // remain white
            }
            x /= 3;   // move to the next sub-square
            y /= 3;
            size /= 3;
        }
        return true; // remain black
    }
}
