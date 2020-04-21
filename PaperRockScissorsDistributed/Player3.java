import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class Player3 {

	public static void main(String[] args) throws IOException, InterruptedException 
	{
		int moveObjectSize = 0, numberPlayers = 3;
		int[] roundScore = new int[numberPlayers];
		int[] totalScore = new int[numberPlayers];
		File[] files = new File[numberPlayers];
		MappedByteBuffer[] mappedMemoryFiles = new MappedByteBuffer[numberPlayers];
		RandomAccessFile[] randomAccessFiles = new RandomAccessFile[numberPlayers];
		long bufferSize = 100;

		for (int x = 0; x < numberPlayers; x++)
		{
			roundScore[x] = totalScore[x] = 0;
			files[x] = new File("player" + (x+1) + "moves.txt");
			randomAccessFiles[x] = new RandomAccessFile(files[x], "rw");
			mappedMemoryFiles[x] = randomAccessFiles[x].getChannel().map(FileChannel.MapMode.READ_WRITE, 0, bufferSize);
		}
		
		String move;
		int playerIndex = 2;
		byte[] serializedMove = new byte[100];
		RPSMove[] moves = new RPSMove[numberPlayers];
		LinkedList<Integer> playersNotPlayed;

		System.out.println("How many games would you like to play?");
		Scanner scan = new Scanner(System.in);
		int numGames = scan.nextInt();
		scan.close();
		
		mappedMemoryFiles[0].position(95).putInt(numGames);
		
		for(int gameNum = 1; gameNum <= numGames; gameNum++)
		{
			for (int y = 0; y < numberPlayers; y++)
			{
				mappedMemoryFiles[playerIndex].position(y*4).putInt(0);
				roundScore[y] = 0;
			}

			move = generateMove();
			moves[playerIndex] = new RPSMove("Player " + (playerIndex+1), move);
						
			mappedMemoryFiles[playerIndex].position(numberPlayers*4);
			serializedMove = moves[playerIndex].toByteArray();
			mappedMemoryFiles[playerIndex].put(serializedMove);
			for (int y = 0; y < numberPlayers; y++)
				mappedMemoryFiles[playerIndex].position(y*4).putInt(1);

			int listIndex = 0;
			int opponentIndex;
			playersNotPlayed = new LinkedList<>();
			for (int x = 1; x < numberPlayers; x++)
				playersNotPlayed.add((playerIndex+x)%numberPlayers);
			

			while (playersNotPlayed.size() > 0)
			{
				listIndex %= playersNotPlayed.size();
				opponentIndex = playersNotPlayed.get(listIndex);
				if (mappedMemoryFiles[opponentIndex].position(playerIndex*4).getInt() == 1)
				{
					playersNotPlayed.remove(listIndex);
					mappedMemoryFiles[opponentIndex].position(numberPlayers*4);
					moveObjectSize = mappedMemoryFiles[opponentIndex].getInt();
					serializedMove = new byte[moveObjectSize];
					mappedMemoryFiles[opponentIndex].get(serializedMove, 0, moveObjectSize);

					mappedMemoryFiles[opponentIndex].position(4*playerIndex).putInt(0);
					moves[opponentIndex] = new RPSMove(serializedMove);
				}
				listIndex++;
			}

			LinkedList<Integer> playersNeedMove = new LinkedList<>();
			for (int x = 0; x < numberPlayers; x++)
				playersNeedMove.add(x);
			
			listIndex = 0;
			while (playersNeedMove.size() > 1)
			{
				listIndex %= playersNeedMove.size();
				if(mappedMemoryFiles[playerIndex].getInt(playersNeedMove.get(listIndex)*4) == 0)
					playersNeedMove.remove(listIndex);
				listIndex++;
			}
			
			System.out.println("Game Number " + (gameNum));
			roundScore = scoresCalculator(moves);
			for (int x = 0; x < numberPlayers; x++)
			{
				System.out.println(moves[x].getPlayer() + ": " + moves[x].getMove() + " Round Score: " + roundScore[x]);
				totalScore[x] += roundScore[x];
			}
		
			System.out.println();	
		}
		System.out.println("Final Results for Player 3:");
		for (int x = 0; x < numberPlayers; x++)
			System.out.println("Player" + (x+1) + ": " + totalScore[x]);
		
		Thread.sleep(5000);
		for(int x = 0; x < numberPlayers; x++)
			files[x].delete();
		
	}
	
	public static String generateMove()
	{
		Random rand = new Random();
		int moveNumber = rand.nextInt(3);
		if (moveNumber == 0)
			return "Paper";
		else if (moveNumber == 1)
			return "Rock";
		else
			return "Scissors";
	}
	
	public static int[] scoresCalculator(RPSMove[] moves)
	{
		int[] scores = new int[moves.length];
		for (int x = 0; x < moves.length; x++)
			scores[x] = 0;
		for (int x = 0; x < moves.length; x++)
		{
			for (int y = 0; y < moves.length; y++)
			{
				scores[x] += calcScore(moves[x].getMove(), moves[y].getMove());
			}
		}
		return scores;
	}
	public static int calcScore(String m1, String m2)
	{
		if (m1.equalsIgnoreCase("Paper"))
		{
			if (m2.equalsIgnoreCase("Rock"))
				return 1;
			else
				return 0;
		}
		else if (m1.equalsIgnoreCase("Rock"))
		{
			if (m2.equalsIgnoreCase("Scissors"))
				return 1;
			else
				return 0;
		}
		else
		{
			if (m2.equalsIgnoreCase("Paper"))
				return 1;
			else
				return 0;
		}
	}

}
