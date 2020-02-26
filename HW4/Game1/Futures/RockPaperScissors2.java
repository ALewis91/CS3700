import java.util.ArrayList;
import java.util.LinkedList;
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
		int numberPlayers;

		System.out.println("How many players would you like to compete in this game?");
		numberPlayers = input.nextInt();
		input.close();
		
		LinkedList<Player2> players = new LinkedList<>();
		Winner2 winner = new Winner2(players, numberPlayers);
		ExecutorService service = Executors.newCachedThreadPool();
		ArrayList<Future> playerFutures = new ArrayList<>();
		for (int x = 0; x < numberPlayers; x++)
		{
			Player2 newPlayer = new Player2(("Player" + x), players, winner);
			players.add(newPlayer);
			Future<String> future = service.submit(newPlayer);
			playerFutures.add(future);
		}
		Thread.sleep(100);
		Future<String> result = service.submit(winner);
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
		
		// Print result
		
		try 
		{
			System.out.println(result.get() + " HAS WON!!");
		} 
		catch (ExecutionException e) 
		{
			e.printStackTrace();
		}
	}

}
