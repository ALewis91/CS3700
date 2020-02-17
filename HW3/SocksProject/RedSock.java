import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class RedSock implements Runnable
{
	private LinkedBlockingQueue<String> MatchQueue;
	private int numSocksProduced;
	
	public RedSock(LinkedBlockingQueue<String> MQ)
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
			MatchQueue.add("red");
			System.out.println("Red Sock: " + (i + 1) + " of " + numSocksProduced + " red socks produced.");

		}
	}
}
