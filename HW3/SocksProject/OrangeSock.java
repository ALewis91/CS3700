import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class OrangeSock implements Runnable
{
	private LinkedBlockingQueue<String> MatchQueue;
	private int numSocksProduced;
	
	public OrangeSock(LinkedBlockingQueue<String> MQ)
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
			MatchQueue.add("orange");
			System.out.println("OrangeSock: " + (i + 1) + " of " + numSocksProduced + " orange socks produced.");

		}
	}
}
