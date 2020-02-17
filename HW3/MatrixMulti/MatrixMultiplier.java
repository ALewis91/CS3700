import java.util.Random;

public class MatrixMultiplier {
	
	public static void main(String[] args) throws InterruptedException 
	{
		int[] threadsUsed = {1, 2, 4, 8};
		int[] sizes = {10, 50, 100, 250, 500, 1000, 2500, 5000, 10000};
		Thread[] threads;
		int numThreads;
		int currentSizeIndex = 0;
		int size;
		int[][] matrixA;
		int[][] matrixB;
		int[][] matrixC;
		
		//Loop for increasing matrix sizes
		for (currentSizeIndex = 0; currentSizeIndex < sizes.length; currentSizeIndex++)
		{
			//Get next matrix length
			size = sizes[currentSizeIndex];
			
			System.out.println("Matrix Multiplication of " + size + " by " + size + " matrices");
			
			//Loop for increasing number of threads used for multiplication
			for (int t = 0; t < threadsUsed.length; t++)
			{	
				
				//Create matrix A and B by filling them with 1's
				matrixA = createMatrix(size,size);
				matrixB = createMatrix(size,size);
				
				//Init matrix C
				matrixC = new int[size][size];
				
				//Create a new array of threads to use for matrix multiplication
				numThreads = threadsUsed[t];
				threads = new Thread[numThreads];
				
				long startTime = System.nanoTime();
				for (int i = 0; i < threads.length; i++)
				{
					threads[i] = new Thread(new MatrixMultiplyingThread(matrixA, matrixB, matrixC, i, threads.length));
					threads[i].start();
				}
				for (int i = 0; i < threads.length; i++)
					threads[i].join();
				System.out.println("Threads: " + numThreads + "\nTime: " + (System.nanoTime()-startTime)*1.0/1000000000 + " seconds"); 
			}
		}
	}
	
	public static int[][] createMatrix(int m, int n)
	{
		int[][] matrix = new int[m][n];
		Random rand = new Random();
		for (int x = 0; x < m; x++)
		{
			for (int y = 0; y < n; y++)
			{
				matrix[x][y] = rand.nextInt(10);
			}
		}
		return matrix;
	}
	
	public static void printMatrix(int[][] matrix)
	{
		System.out.println("Matrix C (" + matrix.length + " x " + matrix[0].length + ')');
		for (int x = 0; x < matrix.length; x++)
		{
			for (int y = 0; y < matrix[0].length; y++)
				System.out.print(matrix[x][y] + " ");
			System.out.println();
		}
	}

}
