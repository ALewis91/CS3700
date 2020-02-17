import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class GreenSock implements Runnable
{
	private LinkedBlockingQueue<String> MatchQueue;
	private int numSocksProduced;
	
	public GreenSock(LinkedBlockingQueue<String> MQ)
	{
		MatchQueue = MQ;
		Random rand = new Random();
		numSocksProduced = rand.nextInt(100) + 1;
	}

	@Override
	public void run() 
	{
		for (int i = 0; i < numSocksProduced; i++)
		{
			MatchQueue.add("green");
			System.out.println("GreenSock: " + (i + 1) + " of " + numSocksProduced + " green socks produced.");

		}
	}
}
