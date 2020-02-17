import java.util.concurrent.LinkedBlockingQueue;

public class MatchingThread implements Runnable {
	private boolean redSockInHand;
	private boolean blueSockInHand;
	private boolean greenSockInHand;
	private boolean orangeSockInHand;
	private String currentSock;
	private LinkedBlockingQueue<String> MatchQueue;
	private LinkedBlockingQueue<String> DestroyQueue;
	private long previousSockReceivedTime;
	private boolean finished;
	private int totalWashed;
	private int inHandOffset;
	public MatchingThread (LinkedBlockingQueue<String> MQ,
			LinkedBlockingQueue<String> DQ)
	{
		redSockInHand = false;
		blueSockInHand = false;
		greenSockInHand = false;
		orangeSockInHand = false;
		inHandOffset = 0;
		finished = false;
		MatchQueue = MQ;
		DestroyQueue = DQ;
		totalWashed = 0;
	}
	public void run() {
		previousSockReceivedTime = System.currentTimeMillis();
		while (!finished)
		{
			if ((System.currentTimeMillis()-previousSockReceivedTime)/1000 > 15) {
				System.out.println("MatchingThread: No socks received in 15 seconds. Shutting down with " 
			+ (MatchQueue.size() + inHandOffset) + " socks in queue.");
				finished = true;
			}
			else
			{
				if (!MatchQueue.isEmpty()) 
				{
					currentSock = MatchQueue.remove();
					previousSockReceivedTime = System.currentTimeMillis();
					
					if (currentSock == "red")
					{
						if (redSockInHand)
						{
							DestroyQueue.add("red");
							inHandOffset--;
							totalWashed+=2;
							System.out.println("MatchigThread: Matching red socks. " + totalWashed 
									+ " total washed. " + (MatchQueue.size() + inHandOffset) + " in queue.");
							redSockInHand = false;
						}
						else
						{
							redSockInHand = true;
							inHandOffset++;
						}
					}
					else if (currentSock == "blue")
					{
						if (blueSockInHand)
						{							
							DestroyQueue.add("blue");
							totalWashed+=2;
							inHandOffset--;
							System.out.println("MatchigThread: Matching blue socks. " + totalWashed 
									+ " total washed. " + (MatchQueue.size() + inHandOffset) + " in queue.");							blueSockInHand = false;
						}
						else
						{
							blueSockInHand = true;
							inHandOffset++;
						}
					}
					else if (currentSock == "green")
					{
						if (greenSockInHand)
						{
							DestroyQueue.add("green");
							totalWashed+=2;
							inHandOffset--;
							System.out.println("MatchigThread: Matching green socks. " + totalWashed 
									+ " total washed. " + (MatchQueue.size() + inHandOffset) + " in queue.");							greenSockInHand = false;
						}
						else
						{
							greenSockInHand = true;
							inHandOffset++;
						}
					}
					else
					{
						if (orangeSockInHand)
						{
							DestroyQueue.add("orange");
							totalWashed+=2;
							inHandOffset--;
							System.out.println("MatchigThread: Matching orange socks. " + totalWashed 
									+ " total washed. " + (MatchQueue.size() + inHandOffset) + " in queue.");							orangeSockInHand = false;
						}
						else
						{
							orangeSockInHand = true;
							inHandOffset++;
						}
					}
				}
			}
		}
	}
}