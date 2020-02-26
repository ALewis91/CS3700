import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class MatchMaker3
{
	private int playersReady;
	private int numPlayers;
	private boolean gameOver;
	
	private Player3 opponent;
	private Player3 winner;
	
	private LinkedBlockingQueue<Player3> playerQ;
	LinkedList<Player3> players;
	HashMap<Player3, String> rematchMap;
		
	MatchMaker3(int startingPlayers)
	{
		playersReady = 0;
		numPlayers = startingPlayers;
		playerQ = new LinkedBlockingQueue<>();
		players = new LinkedList<Player3>();
		gameOver = startingPlayers > 1 ? false : true;
	}
		
	public synchronized void getMatch(Player3 self)
	{				
		// If player has already been matched with another, return
		if (!playerQ.contains(self))
			return;
		else
		{
			// Remove self from match making queue
			playerQ.remove(self);
			//System.out.println(self.getName() + ": removing self from matching queue."); 
			
			// Set current player's opponent to first in the queue
			opponent = playerQ.poll();
			self.setOpponent(opponent);
			
			// Set chosen opponent's opponent to self
			opponent.setOpponent(self);
			
			// Synchronize the two players using the match synchronizer
			MatchSynchronizer matchSync = new MatchSynchronizer();
			self.setMatchsync(matchSync);
			opponent.setMatchsync(matchSync);
			
			System.out.println(getTimeStamp() + " " + self.getName() + ": setting opponent to " + self.getOpponent().getName() + ".");
		}
	}
	
	
	
	public synchronized void gameReady(Player3 self)
	{
		players.add(self);
		
		 System.out.println(getTimeStamp() + " " + self.getName() + ": " + players.size() + "/" + numPlayers + " added to the players list.");
		
		if (players.size() < numPlayers)
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
			Collections.shuffle(players);
			playerQ.addAll(players);
			System.out.println("\n\n" + getTimeStamp() + " " + "All players ready to begin the tournament.");
			System.out.println(getTimeStamp() + " " + "Players remaining: " + players.size() + ".");
			System.out.println(getTimeStamp() + " " + "Players waiting for a match: " + playerQ.size() + ".\n\n");
			
			notifyAll();
		}
		System.out.println(getTimeStamp() + " " + self.getName() + ": starting the game.");
	}
	
	public synchronized void roundReady(Player3 p, int score)
	{
		playersReady++;
		if (score != 1)
		{
			remove(p);
		}
		if (playersReady < numPlayers)
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
			numPlayers = players.size();
			playersReady = 0;
			
			if (numPlayers > 1)
			{
				Collections.shuffle(players);
				playerQ.addAll(players);
				
				System.out.println("\n\n" + getTimeStamp() + "Players all finished the round.");
				System.out.println(getTimeStamp() + " " + "Players remaining: " + players.size() + ".");
				System.out.println(getTimeStamp() + " " + "Players waiting for a match: " + playerQ.size() + ".\n\n");
			}
			
			notifyAll();
		}
	}	
	
	public synchronized boolean gameOver()
	{
		return gameOver;
	}
	
	public synchronized Player3 getWinner()
	{
		return winner;
	}
	
	public synchronized void remove(Player3 p)
	{
		players.remove(p);
		if (players.size() == 1)
		{
			gameOver = true;
			winner = players.getFirst();
		}
	}
	
	@SuppressWarnings("deprecation")
	private String getTimeStamp()
	{
		Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
		return "[" + timeStamp.getHours() + ":" 
				+ timeStamp.getMinutes() + ":" 
				+ timeStamp.getSeconds() + "." 
				+ timeStamp.getNanos()/1000000 + "]";
	}
}
	