import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RockPaperScissors {

	public static void main(String[] args) throws InterruptedException 
	{	      
		Scanner input = new Scanner(System.in);
		int numberPlayers = 0;
		
		while (!powerOf2(numberPlayers) || numberPlayers < 2)
		{
			System.out.println("How many players would you like to compete in this game?");
			numberPlayers = input.nextInt();
			if (!powerOf2(numberPlayers))
				System.out.println("Please enter a number which is a power of 2 so everyone has a match!");
			if (numberPlayers == 1)
				System.out.println("You need more than one player to have a match!");
		}
		input.close();
				
		ExecutorService service = Executors.newCachedThreadPool();
		MatchMaker matchMaker = new MatchMaker(numberPlayers);
				
		for (int x = 0; x < numberPlayers; x++)
		{
			Player newPlayer = new Player(("Player" + x), matchMaker);
			service.execute(newPlayer);
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
