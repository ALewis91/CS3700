import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Callable;

public class Winner2 implements Callable<String>
{
	private LinkedList<Player2> players;
	private int minScore;
	private int numPlayers;
	private int playersMoved;
	private int playersWaiting;
	private Timestamp timeStamp;
	private boolean allTied;
	
	public Winner2(LinkedList<Player2> players, int numPlayers)
	{
		this.players = players;
		this.numPlayers = numPlayers;
		playersMoved = 0;
		playersWaiting = 0;
	}
	
	public String call() 
	{
		Player2 currentPlayer; // player whose score is being compared with minScore
		LinkedList<Player2> losers = new LinkedList<>(); // list of players with minScore
		
		while (players.size() > 1)
		{
			// Wait until all players have made their move
			waitPlayersMove();
			
			// Set minScore value to max int
			minScore = Integer.MAX_VALUE;

			// iterate over all players left in the list
			for (int x = 0; x < players.size(); x++)
			{
				// point currentPlayer to the next player in the list and wait until they've made their move
				currentPlayer = players.get(x);
				currentPlayer.waitToFinish();
				
				// if currentPlayer has a lower score than minScore
				if (currentPlayer.getScore() < minScore)
				{
					losers.clear();  // clear the list of players with a higher score from losers list
					losers.add(currentPlayer); // add currentPlayer to losers list
					minScore = currentPlayer.getScore(); // set minScore to currentPlayer's score
				}
				
				// else if currentPlayer has the same score as current loser
				else if (currentPlayer.getScore() == minScore)
				{
					losers.add(currentPlayer); // add currentPlayer to losers list
				}
			}
			
			// If not an n-way tie
			if (losers.size() < players.size())
			{
				Player2 loser;
				
				// If there is more than one loser
				if (losers.size() > 1)
				{
					// randomly select a loser from the list
					Random rand = new Random();
					loser = losers.get(rand.nextInt(losers.size()));
					System.out.println(getTimeStamp() + " " + loser.getName() + ": ELIMINATED (was randomly selected to be removed out of " + losers.size() + " lowest scoring players).");
				}
				else
				{
					loser = losers.getFirst();
					System.out.println(getTimeStamp() + " " + loser.getName() + ": ELIMINATED.");
				}
				// remove loser from the list of players
				players.remove(loser);
				allTied = false;
			}
			else
				allTied = true;

			losers.clear();
			waitForWinner();
			System.out.println("\n\n" + getTimeStamp() + " PLAYERS REMAINING: " + numPlayers + "\n\n");
		}
		System.out.println("\n\n" + getTimeStamp() + "GAME OVER\n");
		return players.getFirst().getName();
	}


	public synchronized void waitPlayersMove() 
	{
		playersMoved++;
		
		// Wait until all players and winner thread are ready to process player moves
		
		if (playersMoved <= numPlayers)
		{
			try 
			{
				wait();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			playersMoved = 0;
			notifyAll();
		}
	}

	
	public synchronized void waitForWinner()
	{
		playersWaiting++;
		
		// Wait until all players and winner thread are ready for next round
		if (playersWaiting <= numPlayers)
		{
			try 
			{
				wait();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			playersWaiting = 0;
			if (!allTied)
				numPlayers--;
			notifyAll();
		}
	}
	
	@SuppressWarnings("deprecation")
	private String getTimeStamp()
	{
		timeStamp = new Timestamp(System.currentTimeMillis());
		return "[" + timeStamp.getHours() + ":" 
				+ timeStamp.getMinutes() + ":" 
				+ timeStamp.getSeconds() + "." 
				+ timeStamp.getNanos()/1000000 + "]";
	}
}
