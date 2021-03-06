import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;

public class Player3 implements Callable<Boolean>
{
	private String name;
	private String move;
	private int score;
	private Player3 opponent;
	private MatchMaker3 matchMaker;
	private MatchSynchronizer matchSync;
	private String outcome;
	HashMap<String, Integer> memoizedMoveMap;
	
	private Timestamp timeStamp;
	
	Player3(String name, MatchMaker3 matchMaker)
	{
		this.name = name;
		this.matchMaker = matchMaker;
		memoizedMoveMap  = new HashMap<>();
	}
	
	@Override
	public Boolean call() 
	{
		matchMaker.gameReady(this);
		
		// Prime the loop control variable
		score = 1;
		
		// Play until there is a winner, or break if lose
		while (!matchMaker.gameOver() && score == 1)
		{
			
			// Wait to be assigned a match
			matchMaker.getMatch(this);
			
			// Prime the loop
			score = 0;
			
			// Loop until there is a winner
			while (score == 0)
			{				
				// After a match has been made, make your play
				makeMove();

				// Signal ready for evaluation and wait for other player to finish making their move
				matchSync.readyStart();
				
				// Check for winner/loser
				score = evaluate(opponent.getMove());
				outcome = score == 0 ? "TIE" : score > 0 ? "WIN" : "LOSE";
				// Wait until other player has evaluated the moves before moving on
				matchSync.readyStop();
				System.out.println(getTimeStamp() + " " + name + " vs " + opponent.getName() 
									+ ": " + move + " vs " + opponent.getMove() + ": " + outcome);
			}
			
			
			// If this thread lost, remove it from the list of players
			if (score != 1)
			{
				System.out.println(getTimeStamp() + " " + name + ": ELIMINATED.");
			}
						
			matchMaker.roundReady(this, score);				
		}
		if (matchMaker.getWinner() == this)
		{
			System.out.println("\n\n" + getTimeStamp() + " " +"GAME OVER\n\n" + name + ": WON THE TOURNAMENT.");
			return true;
		}
		else
			return false;
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
	
	private int evaluate(String oppMove)
	{
		if (memoizedMoveMap.containsKey(move + oppMove))
			return memoizedMoveMap.get(move+oppMove);
		
		if (move == "rock")
		{
			if (oppMove == "paper")
			{
				memoizedMoveMap.put(move+oppMove, -1);
				return -1;
			}
			else if (oppMove == "scissors")
			{
				memoizedMoveMap.put(move+oppMove, 1);
				return 1;
			}
			else
			{
				memoizedMoveMap.put(move+oppMove, 0);
				return 0;
			}
			
		}
		else if (move == "paper")
		{
			if (oppMove == "scissors")
			{
				memoizedMoveMap.put(move+oppMove, -1);
				return -1;
			}
			else if (oppMove == "rock")
			{
				memoizedMoveMap.put(move+oppMove, 1);
				return 1;
			}
			else 
			{
				memoizedMoveMap.put(move+oppMove, 0);
				return 0;
			}
		}
		else
		{
			if (oppMove == "rock")
			{
				memoizedMoveMap.put(move+oppMove, -1);
				return -1;
			}
			else if (oppMove == "paper")
			{
				memoizedMoveMap.put(move+oppMove, 1);
				return 1;
			}
			else 
			{
				memoizedMoveMap.put(move+oppMove, 0);
				return 0;
			}
		}
	}
	
	public void setOpponent(Player3 opponent)
	{
		this.opponent = opponent;
	}
	
	public Player3 getOpponent()
	{
		return opponent;
	}
	
	public void setMatchsync(MatchSynchronizer sync)
	{
		matchSync = sync;
	}
	
	public String getMove()
	{
		return move;
	}
	
	public String getName()
	{
		return name;
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
