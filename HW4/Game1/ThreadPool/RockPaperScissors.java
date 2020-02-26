import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RockPaperScissors {

	public static void main(String[] args) throws InterruptedException 
	{	      
		Scanner input = new Scanner(System.in);
		int numberPlayers;

		System.out.println("How many players would you like to compete in this game?");
		numberPlayers = input.nextInt();
		input.close();
		
		LinkedList<Player> players = new LinkedList<>();
		Winner winner = new Winner(players, numberPlayers);
		ExecutorService service = Executors.newCachedThreadPool();
		
		for (int x = 0; x < numberPlayers; x++)
		{
			Player newPlayer = new Player(("Player" + x), players, winner);
			players.add(newPlayer);
			service.execute(newPlayer);
		}
		Thread.sleep(100);
		service.execute(winner);
	}

}
