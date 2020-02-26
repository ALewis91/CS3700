import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class RockPaperScissors3 {
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws InterruptedException 
	{	      
		Scanner input = new Scanner(System.in);
		int numberPlayers;

		System.out.println("How many players would you like to compete in this game?");
		numberPlayers = input.nextInt();
		input.close();
		
		LinkedList<Player3> players = new LinkedList<>();
		Winner3 winner = new Winner3(players, numberPlayers);
		ExecutorService service = Executors.newCachedThreadPool();
		ArrayList<Future> playerFutures = new ArrayList<>();
		for (int x = 0; x < numberPlayers; x++)
		{
			Player3 newPlayer = new Player3(("Player" + x), players, winner);
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
