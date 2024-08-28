import mpi.*;

public class Main {
    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // Define the array to sum
        int[] array = null;
        int arraySize = 0;

        if (rank == 0) {
            array = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            arraySize = array.length;
        }

        // Broadcast the array size to all processes
        int[] sizeArray = new int[1];
        if (rank == 0) {
            sizeArray[0] = arraySize;
        }
        MPI.COMM_WORLD.Bcast(sizeArray, 0, 1, MPI.INT, 0);
        arraySize = sizeArray[0];

        // Broadcast the array to all processes
        int[] localArray = new int[arraySize];
        if (rank == 0) {
            MPI.COMM_WORLD.Bcast(array, 0, arraySize, MPI.INT, 0);
        } else {
            MPI.COMM_WORLD.Bcast(localArray, 0, arraySize, MPI.INT, 0);
        }

        // Calculate local chunk size
        int chunkSize = (arraySize + size - 1) / size;
        int start = rank * chunkSize;
        int end = Math.min(start + chunkSize, arraySize);

        // Calculate local sum
        int localSum = 0;
        for (int i = start; i < end; i++) {
            localSum += localArray[i];
        }

        // Reduce local sums to the root process
        int[] globalSumBuffer = new int[1];
        MPI.COMM_WORLD.Reduce(new int[]{localSum}, 0, globalSumBuffer, 0, 1, MPI.INT, MPI.SUM, 0);

        // Print the result in the root process
        if (rank == 0) {
            System.out.println("Global Sum: " + globalSumBuffer[0]);
            // Save the result to a file for JavaFX to read
            try {
                java.nio.file.Files.write(java.nio.file.Paths.get("result.txt"),
                        Integer.toString(globalSumBuffer[0]).getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MPI.Finalize();
    }
}