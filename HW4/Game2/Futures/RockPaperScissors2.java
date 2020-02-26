import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class RockPaperScissors2 {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws InterruptedException 
	{	      
		Scanner input = new Scanner(System.in);
		int numberPlayers = 0;
		
		while (!powerOf2(numberPlayers) || numberPlayers < 2)
		{
			System.out.println("How many players would you like to compete in this game2?");
			numberPlayers = input.nextInt();
			if (!powerOf2(numberPlayers))
				System.out.println("Please enter a number which is a power of 2 so everyone has a match!");
			if (numberPlayers == 1)
				System.out.println("You need more than one player to have a match!");
		}
		input.close();
				
		ExecutorService service = Executors.newCachedThreadPool();
		MatchMaker2 matchMaker = new MatchMaker2(numberPlayers);
		
		ArrayList<Future> playerFutures = new ArrayList<>();
		for (int x = 0; x < numberPlayers; x++)
		{
			Player2 newPlayer = new Player2(("Player" + x), matchMaker);
			Future<Boolean> future = service.submit(newPlayer);
			playerFutures.add(future);
		}

		Future<String> currentPlayer;
		
		// Wait for all players to finish
		for (int x = 0; x < playerFutures.size(); x++)
		{
			currentPlayer = playerFutures.get(x);
			try 
			{
				currentPlayer.get();
			} 
			catch (ExecutionException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public static boolean powerOf2(int n)
	{
		int m = 1;
		while (m < n)
			m *= 2;
		return m == n;
	}

}
