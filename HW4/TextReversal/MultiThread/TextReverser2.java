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


class LineReverser implements Callable<String>
{
	private String unprocessedLine;
	private Stack<String> wordStack;
	private Scanner lineScanner;
	private StringBuilder processedLine;
	
	LineReverser(String line)
	{
		this.unprocessedLine = line;
		wordStack = new Stack<>();
		processedLine = new StringBuilder();
	}
	
	public String call()
	{
		unprocessedLine = unprocessedLine.replaceAll("[^A-Za-z0-9 ]", "");
		lineScanner = new Scanner(unprocessedLine);
		while (lineScanner.hasNext())
			wordStack.push(lineScanner.next());
		
		while(!wordStack.isEmpty()) 
		{
			processedLine.append(wordStack.pop() + ' ');			
		}
		processedLine.append('\n');
		return processedLine.toString();
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
			
			String line;
			System.out.println(getTimeStamp() + " Starting to read file");
			
			while (fileScan.hasNextLine()) 
			{
				line = fileScan.nextLine();
				futures.add(service.submit(new LineReverser(line)));
			}
			
			for (int y = futures.size() - 1; y > -1; y--)
			{	
				try 
				{
					writer.write(futures.get(y).get());
				} 
				catch (InterruptedException e) 
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
			System.out.println(getTimeStamp() + " Finished writing file\n\n");
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
