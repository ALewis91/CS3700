import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Random;

public class Player implements Runnable
{
	private String name;
	private String move;
	private int score;
	private LinkedList<Player> players;
	private Player opponent;
	private boolean turnOver;
	private Winner winner;
	Timestamp timeStamp;
	
	public Player(String name, LinkedList<Player> players, Winner winner) 
	{
		score = 0;
		this.winner = winner;
		this.name = name;
		this.players = players;
		turnOver = false;
	}
	
	public String getMove()
	{
		return move;
	}
	
	public int getScore()
	{
		return score;
	}
	
	public String getName()
	{
		return name;
	}
	
	public synchronized void waitToFinish()
	{
		while(!turnOver)
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
		turnOver = false;
	}
	
	public synchronized void finished()
	{
		turnOver = true;
		notify();
	}
	
	public void makeMove()
	{
		Random rand = new Random();
		int moveDigit = rand.nextInt(3);
		if (moveDigit == 0)
		{
			move = "paper";
		}
		else if (moveDigit == 1)
		{
			move = "rock";
		}
		else
		{
			move = "scissors";
		}
	}
	
	public void run() 
	{
		while (players.size() > 1)
		{
			// Make your move
			makeMove();
			
			System.out.println(getTimeStamp() + " " + name + ": waiting for opponents.");
			winner.waitPlayersMove();
			
			System.out.println(getTimeStamp() + " " + name + ": " + move);
			
			for (int x = 0; x < players.size(); x++)
			{
				opponent = players.get(x);
				if (opponent == this)
					continue;
				if (this.move == "rock")
				{
					if (opponent.getMove() == "paper")
					{
						score -= 1;
					}
					else if (opponent.getMove() == "scissors")
					{
						score += 1;
					}
						
				}
				else if (this.move == "paper")
				{
					if (opponent.getMove() == "scissors")
					{
						score -= 1;
					}
					else if (opponent.getMove() == "rock")
					{
						score += 1;
					}	
				}
				else
				{
					if (opponent.getMove() == "rock")
					{
						score -= 1;
					}
					else if (opponent.getMove() == "paper")
					{
						score += 1;
					}
				}
			}
			
			System.out.println(getTimeStamp() + " " + name + " score: " + score);
			
			// Alert winner thread player is finished processing score
			finished();
			
			// Wait until all threads are done processing this round
			winner.waitForWinner();
			
			// Stop if this player was eliminated
			if (!players.contains(this))
				break;
			
			// Reset score
			score = 0;
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
