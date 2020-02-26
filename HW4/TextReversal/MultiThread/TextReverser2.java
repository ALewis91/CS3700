import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


class ProcessBlock implements Callable<String>
{
	private String unprocessedBlock;
	private StringBuilder block;
	private Stack<String> wordStack;
	private Scanner blockScanner;
	
	ProcessBlock(String block)
	{
		this.unprocessedBlock = block;
		wordStack = new Stack<>();
		this.block = new StringBuilder();
	}
	
	public String call()
	{
		unprocessedBlock = unprocessedBlock.replaceAll("[^A-Za-z0-9 ]", "");
		blockScanner = new Scanner(unprocessedBlock);
		while (blockScanner.hasNext())
			wordStack.push(blockScanner.next());
		wordStack.push("\n");
		
		while(!wordStack.isEmpty()) 
			block.append(wordStack.pop() + ' ');
		block.append('\n');
		
		return block.toString();
	}	
}


public class TextReverser2
{
	static Timestamp timeStamp;

	public static void main(String[] args) throws FileNotFoundException 
	{
		PrintWriter writer;
		LinkedList<Future<String>> futures;

		Scanner fileScan;
		
		for (int x = 0; x < 4; x++)
		{
			writer = new PrintWriter(new File("backwards.txt"));
			fileScan = new Scanner(new File("src/DeclarationIndependence.txt"));

			int numberThreads = (int)(Math.pow(2, x) + .5);
			futures = new LinkedList<>();
			ExecutorService service = Executors.newFixedThreadPool(numberThreads);

			System.out.println("Processing with " + numberThreads + " threads\n");
			
			StringBuilder block = new StringBuilder();
			System.out.println(getTimeStamp() + " Starting to read file");
			
			while (fileScan.hasNextLine()) 
			{
				block.append(fileScan.nextLine());
				for (int y = 0 ; y < 4; y++)
				{
					if (fileScan.hasNext())
						block.append(fileScan.nextLine());
				}
				futures.add(service.submit(new ProcessBlock(block.toString())));
				block = new StringBuilder();
			}
			
			for (Future<String> task : futures)
			{	
				try 
				{
					writer.write(task.get());
				} catch (InterruptedException e) 
				{
					e.printStackTrace();
				} 
				catch (ExecutionException e) 
				{
					e.printStackTrace();
				}
			}
			fileScan.close();
			writer.close();
			System.out.println(getTimeStamp() + " Finished reading file\n\n");
		}
	}
	
	@SuppressWarnings("deprecation")
	public static String getTimeStamp()
	{
		timeStamp = new Timestamp(System.currentTimeMillis());
		return "[" + timeStamp.getHours() + ":" 
				+ timeStamp.getMinutes() + ":" 
				+ timeStamp.getSeconds() + "." 
				+ timeStamp.getNanos()/1000000 + "]";
	}
}
