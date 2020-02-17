import java.util.LinkedList;

public class DemocraticPrimaries {

	public static void main(String[] args) throws InterruptedException 
	{
		LinkedList<String> leader = new LinkedList<String>();
		boolean terminate = false;
		LinkedList<ElectedOfficial> officials = new LinkedList<>();
		RankThread rankKeeper = new RankThread(officials, leader, terminate);
		//Start rank thread
		Thread ranker = new Thread(rankKeeper);
		ranker.start();
		// Add up to 100 officials
		for (int x = 0; x < 100; x++)
		{
			ElectedOfficial eo = new ElectedOfficial(ranker, "Official" + x, leader, terminate);
			eo.start();
			officials.add(eo);
			Thread.sleep(3000);
			for (int y = 0; y < officials.size(); y++)
				officials.get(y).identify();
		}
	}

}
