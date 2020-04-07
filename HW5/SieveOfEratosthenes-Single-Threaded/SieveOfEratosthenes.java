public class SieveOfEratosthenes 
{

	public static void main(String[] args) 
	{
		final int MAX = 1_000_000;
		int numPrimes = 0;
		boolean[] primes = new boolean[MAX+1];
		for (int x = 0; x < MAX+1; x++)
			primes[x] = true;
		
		long start, end;
		start = System.currentTimeMillis();
		for (int x = 2; x < Math.sqrt(MAX+1); x++)
		{
			if (primes[x])
			{
				for (int y = x*x; y < MAX+1; y+=x)
					primes[y] = false;
			}
		}
		for (int x = 2; x < MAX+1; x++)
		{
			if (primes[x])
			{
				numPrimes++;
				System.out.println(x);
			}
		}
		end = System.currentTimeMillis();
		System.out.println("Total primes: " + numPrimes);
		System.out.println("Execution time: " + (end-start) + " ms");
	}
	
}
