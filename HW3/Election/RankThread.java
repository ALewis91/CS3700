import java.util.LinkedList;

public class RankThread implements Runnable 
{
	private LinkedList<String> leader;
	private int leaderRank;
	private int leaderIndex;
	private final LinkedList<ElectedOfficial> officials;
	private boolean newLeader;
	private volatile boolean terminate;
	public RankThread(LinkedList<ElectedOfficial> officials, LinkedList<String> leader, boolean terminate)
	{
		this.officials = officials;
		this.leader = leader;
		leaderRank = Integer.MIN_VALUE;
		this.terminate = terminate;
	}
	@Override
	public void run() 
	{
		while(!terminate)
		{
			try {
				synchronized(officials)
				{
					while(true)
						officials.wait();
				}
					
			}
			catch (InterruptedException ex)
			{
				synchronized(officials)
				{
					newLeader = false;
					for (int x = 0; x < officials.size(); x++)
					{
						int rank = officials.get(x).getRank();
						if (rank > leaderRank)
						{
							newLeader = true;
							leaderIndex = x;
							leaderRank = rank;
						}
					}
					if (newLeader)
					{
						newLeader = false;
						synchronized(leader)
						{
							leaderRank = officials.get(leaderIndex).getRank();
							if (leader.size() > 0)
								leader.pop();
							leader.push(officials.get(leaderIndex).getOfficial());
						}
						for (int x = 0; x < officials.size(); x++)
						{
							officials.get(x).interrupt();
						}
					}
				}
			}
		}
	}
}


