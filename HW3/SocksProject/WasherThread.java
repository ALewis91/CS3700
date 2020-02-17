import java.util.concurrent.LinkedBlockingQueue;

public class WasherThread implements Runnable 
{
	
	private LinkedBlockingQueue<String> DestroyQueue;
	private long lastSocksReceivedTime;
	private boolean finished;
	private String currentSocks;
	
	public WasherThread(LinkedBlockingQueue<String> D)
	{
		DestroyQueue = D;
		finished = false;
	}
	public void run() 
	{
		lastSocksReceivedTime = System.currentTimeMillis();
		while (!finished) 
		{
			if ((System.currentTimeMillis() - lastSocksReceivedTime)/1000 > 15)
			{
				System.out.println("WasherThread: No socks received in 15 seconds. Shutting down.");
				finished = true;
			}
			else
			{
				if (!DestroyQueue.isEmpty())
				{
					currentSocks = DestroyQueue.remove();
					System.out.println("WasherThread: Destroying " + currentSocks + " socks.");
				}
			}
		}
	}
	
	

}
