public class MatchSynchronizer 
{
	private int playersReadyStart;
	private int playersReadyStop;

	MatchSynchronizer()
	{
		playersReadyStart = 0;
		playersReadyStop = 0;
	}
	
	public synchronized void readyStart()
	{
		playersReadyStart++;
		if (playersReadyStart < 2)
		{
			try
			{
				wait();
			}
			catch (InterruptedException ex)
			{
				ex.printStackTrace();
			}
		}
		else
		{
			playersReadyStart = 0;
			notify();
		}
	}
	
	public synchronized void readyStop()
	{
		playersReadyStop++;
		if (playersReadyStop < 2)
		{
			try
			{
				wait();
			}
			catch (InterruptedException ex)
			{
				ex.printStackTrace();
			}
		}
		else
		{
			playersReadyStop = 0;
			notify();
		}
	}
}
