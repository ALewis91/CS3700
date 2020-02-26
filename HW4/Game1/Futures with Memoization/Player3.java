import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Callable;

public class Player3 implements Callable<String>
{
	private String name;
	private String move;
	private int score;
	private LinkedList<Player3> players;
	private Player3 opponent;
	private boolean turnOver;
	private Winner3 winner;
	private HashMap<String, Integer> memoizedMoves;
	private Timestamp timeStamp;

	public Player3(String name, LinkedList<Player3> players, Winner3 winner) 
	{
		score = 0;
		this.winner = winner;
		this.name = name;
		this.players = players;
		turnOver = false;
		memoizedMoves = new HashMap<>();
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
	
	public String call() 
	{
		while (players.size() > 1)
		{
			// Make player move
			makeMove();

			System.out.println(getTimeStamp() + " " + name + ": waiting for opponents.");
			winner.waitPlayersMove();
			
			System.out.println(getTimeStamp() + " " + name + ": " + move);
			
			String opponentMove;
			Integer tempScore;
			for (int x = 0; x < players.size(); x++)
			{
				opponent = players.get(x);
				if (opponent == this)
					continue;
				opponentMove = opponent.getMove();
				tempScore = memoizedMoves.get(move+opponentMove);
				if (tempScore != null)
					score += tempScore;
				else
				{
					if (this.move == "rock")
					{
						if (opponent.getMove() == "paper")
						{
							memoizedMoves.put(move+opponentMove, -1);
							score -= 1;
						}
						else if (opponent.getMove() == "scissors")
						{
							memoizedMoves.put(move+opponentMove, 1);
							score += 1;
						}
						else
						{
							memoizedMoves.put(move+opponentMove, 0);
						}
							
					}
					else if (this.move == "paper")
					{
						if (opponent.getMove() == "scissors")
						{
							memoizedMoves.put(move+opponentMove, -1);
							score -= 1;
						}
						else if (opponent.getMove() == "rock")
						{
							memoizedMoves.put(move+opponentMove, 1);
							score += 1;
						}
						else
						{
							memoizedMoves.put(move+opponentMove, 0);
						}
					}
					else
					{
						if (opponent.getMove() == "rock")
						{
							memoizedMoves.put(move+opponentMove, -1);
							score -= 1;
						}
						else if (opponent.getMove() == "paper")
						{
							memoizedMoves.put(move+opponentMove, 1);
							score += 1;
						}
						else
						{
							memoizedMoves.put(move+opponentMove, 0);
						}
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
		return (name + " is finished playing.");
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
