
public class MatrixMultiplyingThread implements Runnable
{
	private int skip, start;
	private int[][] A, B, C;
	private int sum;
	public MatrixMultiplyingThread(int[][] A, int[][] B, int[][] C, int start, int skip)
	{
		this.skip = skip;
		this.start = start;
		this.A = A;
		this.B = B;
		this.C = C;
	}
	@Override
	public void run() 
	{
		for (int m = 0; m < A.length; m++)
		{
			for (int p = start; p < B[0].length; p+=skip)
			{
				sum = 0;
				for (int n = 0; n < B.length; n++)
					sum+= A[m][n]*B[n][p];
				C[m][p] = sum;
			}
		}
	}

}
