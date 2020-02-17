import java.util.LinkedList;
import java.util.Random;

public class ElectedOfficial extends Thread 
{
	private int rank;
	private String name;
	private final LinkedList<String> leader;
	private String localLeader;
	private Thread rankThread;
	private volatile boolean terminate;
	public ElectedOfficial(Thread rt, String name, LinkedList<String> leader, boolean terminate)
	{
		Random rand = new Random();
		rank = rand.nextInt();
		if (rand.nextInt(2) != 0)
			rank *= -1;
		rankThread = rt;
		this.leader = leader;
		localLeader = name;
		this.name = name;
		this.terminate = terminate;
		identify();
		rankThread.interrupt();
	}
	
	public String getOfficial()
	{
		return name;
	}
	
	public int getRank()
	{
		return rank;
	}

	@Override
	public void run() 
	{
		while (!terminate)
		{
			try {
				synchronized(leader) 
				{
					while (true)
						leader.wait();
				}
			}
			catch (InterruptedException ex)
			{
				synchronized(leader)
				{
					localLeader = leader.getFirst();
				}
			}
		}
	}
	
	public void identify()
	{
		System.out.println("Name: " + name + " Rank: " + rank + " Leader: " + localLeader);
	}
}
