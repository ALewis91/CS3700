import java.util.concurrent.LinkedBlockingQueue;

public class SocksThreads {

	public static void main(String[] args) 
	{
		LinkedBlockingQueue<String> DestroyQ = new LinkedBlockingQueue<>(400);
		LinkedBlockingQueue<String> MatchQ = new LinkedBlockingQueue<>(400);
		WasherThread WT = new WasherThread(DestroyQ);
		MatchingThread MT = new MatchingThread(MatchQ, DestroyQ);
		RedSock redSock = new RedSock(MatchQ);
		BlueSock blueSock = new BlueSock(MatchQ);
		GreenSock greenSock = new GreenSock(MatchQ);
		OrangeSock orangeSock = new OrangeSock(MatchQ);
		Thread T0 = new Thread(WT);
		Thread T1 = new Thread(MT);
		Thread T2 = new Thread(redSock);
		Thread T3 = new Thread(blueSock);
		Thread T4 = new Thread(greenSock);
		Thread T5 = new Thread(orangeSock);
		T0.start();
		T1.start();
		T2.start();
		T3.start();
		T4.start();
		T5.start();
	}
}
