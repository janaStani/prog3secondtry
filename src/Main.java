import mpi.*;
import java.io.*;              // for the file
import java.util.Arrays;

public class Main {

    // default settings, if no arguments are provided
    private static final int DEFAULT_GRID_SIZE = 81;  // 3^4
    private static final int DEFAULT_RECURSION_DEPTH = 4;

    public static void main(String[] args) {

        // try-catch block to handle exceptions
        try {
            MPI.Init(args);      // init MPI

            int rank = MPI.COMM_WORLD.Rank();        // get rank of current process
            int size = MPI.COMM_WORLD.Size();        // get total num of processes in MPI communicator

            int gridSize = DEFAULT_GRID_SIZE;               // default grid size
            int recursionDepth = DEFAULT_RECURSION_DEPTH;   // default depth

            // command line argument handling for the recursion depth
            if (args.length > 0) {                        // if we have arguments
                try {
                    recursionDepth = Integer.parseInt(args[args.length - 1]);    // get the last cmdln arg set it as recursion depth
                    if (rank == 0) {                                             // if root process
                        System.out.println("Starting distributed computation with depth: " + recursionDepth);
                        System.out.println("Total number of processes: " + size);
                    }
                } catch (NumberFormatException e) {
                    if (rank == 0) {
                        System.err.println("Invalid recursion depth. Using default value of " + DEFAULT_RECURSION_DEPTH + ".");
                    } // error handling
                }
            } else {
                if (rank == 0) {                         // if we don't have arguments
                    System.out.println("No recursion depth argument provided. Using default value of " + DEFAULT_RECURSION_DEPTH + ".");
                }
            }

            // calculate grid size based on recursion depth (3^depth)
            gridSize = (int) Math.pow(3, recursionDepth);

            // initialize 2d array for the fractal data, for each process
            int[] data = new int[gridSize * gridSize];

            if (rank == 0) {               // if root process
                Arrays.fill(data, 0);  // initialize array with 0s
            }

            // broadcast gridSize and recursionDepth from root process to all other processes in the communicator
            MPI.COMM_WORLD.Bcast(new int[]{gridSize}, 0, 1, MPI.INT, 0);            // creates a new array with gridSize and broadcasts it
            MPI.COMM_WORLD.Bcast(new int[]{recursionDepth}, 0, 1, MPI.INT, 0);      // creates a new array with recursionDepth and broadcasts it
            // 0 the starting index, since we are broadcasting only 1 value, integer will be sent, the rank of the root process

            // calculate number of rows each process is responsible for
            int rowsPerProcess = (gridSize + size - 1) / size;  // num of rows + num of processes - 1 because of 0 indexing, to round in uneven division then divide with num of processes
            int startRow = rank * rowsPerProcess;  // current process * rows per process = index of the starting row
            int endRow = Math.min(startRow + rowsPerProcess, gridSize);  // (index of starting row + number of rows per process), grid ensure that it doesn't go over the grid size

            long startTime = System.nanoTime();  // start time for computation of pattern

            computeFractal(data, 0, 0, gridSize, recursionDepth, startRow, endRow); // compute the fractal pattern for the current process, withing the range of rows
            // array to store the data of current process, leftmost coordinate, topmost coordinate, size of current section, level of fractal that should be computed, segment coordinates

            long endTime = System.nanoTime();    // End time

            double elapsedTime = (endTime - startTime) / 1_000_000.0; // time calculates in milliseconds, till 3 decimal places

            if (rank == 0) {
                System.out.printf("Distributed computation completed in %.3f milliseconds.%n", elapsedTime);
            }


            // array in the root process to store the complete grid data after gathering from all processes
            int[] globalData = new int[gridSize * gridSize];

            // collect the data from all processes and store it in the array in the root process
            MPI.COMM_WORLD.Gather(data, startRow * gridSize, rowsPerProcess * gridSize, MPI.INT, globalData, 0, rowsPerProcess * gridSize, MPI.INT, 0);
            // array from each process, starting index of the data array, number of element to send, int, array to store the data, starting index of the global array, number of elements to receive, int, root process


            // if root process, write the result to file
            if (rank == 0) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"))) {
                    for (int i = 0; i < gridSize; i++) {  // go through each row in the grid
                        for (int j = 0; j < gridSize; j++) {  // go through each column with current row
                            writer.write(globalData[i * gridSize + j] + " ");  // write value of each cell to the file
                        }// element at row i and column j in 2D grid is located at index i * gridSize + j in the 1D globalData array
                        writer.newLine();  // New line after each row
                    }
                }
            }

            MPI.Finalize();   // finalize MPI, release resources
        } catch (Exception e) {   // catch block for exceptions
            e.printStackTrace();
        }
    }

    // fractal computation method
    private static void computeFractal(int[] data, int x, int y, int size, int depth, int startRow, int endRow) {
        // 1d array to store, sub-grid coordinates, size of current section of grid, recursion level, range of rows to compute

        if (depth == 0) {   // base case, stop condition
            return;
        }

        int newSize = size / 3;       // divide current section of grid into sub-grids

        for (int i = startRow; i < endRow; i++) {   // rows of the grid, assigned to the current process
            for (int j = 0; j < size; j++) {        // current column for ith row
                if (isInFractal(i, j, size)) {      // call function to check cell
                    data[i * size + j] = 1;         // set cell to 1 (black)
                } else {
                    data[i * size + j] = 0;         // set cell to 0 (white)
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
                    computeFractal(data, x + i * newSize, y + j * newSize, newSize, newDepth, startRow, endRow);
                } // recursive call to compute fractal for each sub-grid
            }
        }
    }

    // checks if a cell (x, y) is in the fractal
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
}
