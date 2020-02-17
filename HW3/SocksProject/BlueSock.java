import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class BlueSock implements Runnable
{
	private LinkedBlockingQueue<String> MatchQueue;
	private int numSocksProduced;
	
	public BlueSock(LinkedBlockingQueue<String> MQ)
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
			MatchQueue.add("blue");
			System.out.println("BlueSock: " + (i + 1) + " of " + numSocksProduced + " blue socks produced.");

		}
	}
}
