import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


class FrequencyCounter
{
	private int[] frequencyTable;
	private boolean inUse;
	int value;
	
	FrequencyCounter()
	{
		frequencyTable = new int[256];
		inUse = false;
	}
	
	public synchronized void updateFrequencies(int[] freqs)
	{
		while (inUse)
		{
			try
			{
				wait();
			}
			catch (InterruptedException ex)
			{
				ex.printStackTrace();
			}
		}
		inUse = true;
		for (int x = 0; x < 256; x++)
		{
			value = frequencyTable[x];
			frequencyTable[x] = freqs[x] + value;
		}
		inUse = false;
		notifyAll();
	}
	
	public synchronized int[] getFrequencies()
	{
		return frequencyTable;
	}
}

class LineFreqProcessor extends Thread
{
	private int[] localFreqs;
	private FrequencyCounter counter;
	private StringBuilder block;
	private int begin;
	private int end;
	
	LineFreqProcessor(FrequencyCounter counter, StringBuilder text, int begin, int end)
	{
		block = text;
		this.counter = counter;
		localFreqs = new int[256];
		this.begin = begin;
		this.end = end;
	}
	
	public void run() 
	{
		for (int x = begin; x < end; x++)
			localFreqs[block.charAt(x)]++;
		counter.updateFrequencies(localFreqs);
	}
	
}

class LineEncodingProcessor implements Callable<StringBuilder>
{
	private HashMap<Character, String> map;
	private StringBuilder block;
	private StringBuilder encodedBlock;
	private int begin;
	private int end;
	
	LineEncodingProcessor(HashMap<Character, String> map, StringBuilder text, int begin, int end)
	{
		block = text;
		this.map = map;
		this.begin = begin;
		this.end = end;
		encodedBlock = new StringBuilder();
	}
	
	public StringBuilder call() 
	{
		for (int x = begin; x < end; x++)
			encodedBlock.append(map.get(block.charAt(x)));
		
		return encodedBlock;
	}
	
}


public class FileCompressorMulti1 {

	static Timestamp timeStamp;
	
	public static void main(String[] args) throws FileNotFoundException 
	{
		FrequencyCounter counter;
		long totalTreeBuildingTime = 0;
		long totalFileCompressionTime = 0;
		long startTime;
		long stopTime;
		int numberTrials = 1000;
		int numFreqThreads = 2;
		int numEncodeThreads = 8;
		int blockSize;
		int beginIndex;
		int endIndex;
			
		HuffmanTree tree;
		HashMap<Character, String> codeMap;
		
		Thread[] threads;
		LinkedList<Future<StringBuilder>> futures;
		ExecutorService service;
		LineEncodingProcessor task;
		LineFreqProcessor thread;

		StringBuilder text;
		StringBuilder compressedText;
		Scanner fileScanner;
		PrintWriter writer;
		
		String treeStr;
		
		for (int i = 0; i < numberTrials; i++)
		{
			fileScanner = new Scanner(new File("src/US_Constitution.txt"));
			writer = new PrintWriter(new File("US_Constitution_compressed.txt"));
			counter = new FrequencyCounter();
			threads = new Thread[numFreqThreads];
			text = new StringBuilder();
			compressedText = new StringBuilder();
			
			futures = new LinkedList<>();
			service = Executors.newFixedThreadPool(8);
			
			
			while (fileScanner.hasNextLine()) 
			{
				text.append(fileScanner.nextLine() + '\n');
			}

			blockSize = text.length()/numFreqThreads;
			beginIndex = 0;
			endIndex = blockSize;
			

			
			System.out.println("Trial #" + (i+1));
			System.out.println(getTimeStamp() + " Starting to build the tree...");
			startTime = System.nanoTime();
			

			for (int j = 0; j < numFreqThreads; j++)
			{
				thread = new LineFreqProcessor(counter, text, beginIndex, endIndex);
				thread.start();
				threads[j] = thread;;
				beginIndex += blockSize;
				endIndex = beginIndex + blockSize;
				if (j == numFreqThreads - 1)
					endIndex = text.length();
			}

			
			for (int j = 0; j < numFreqThreads; j++)
			{
				try 
				{
					threads[j].join();
				} 
				catch (InterruptedException e) 
				{

					e.printStackTrace();
				}
			}

			tree = new HuffmanTree(counter.getFrequencies());
			stopTime = System.nanoTime();
			totalTreeBuildingTime += (stopTime - startTime);
			
			System.out.println(getTimeStamp() + " Finished building the tree...");
	
			System.out.println(getTimeStamp() + " Starting compressed file...");
			startTime = System.nanoTime();

			codeMap = tree.getCodeMap();

			blockSize = text.length()/numEncodeThreads;
			beginIndex = 0;
			endIndex = blockSize;
			
			for (int j = 0; j < numEncodeThreads; j++)
			{
				task = new LineEncodingProcessor(codeMap, text, beginIndex, endIndex);
				futures.add(service.submit(task));
				beginIndex += blockSize;
				endIndex = beginIndex + blockSize;
				if (j == numEncodeThreads - 1)
					endIndex = text.length();
			}

			
			for (int j = 0; j < numEncodeThreads; j++)
			{
				try 
				{
					compressedText.append(futures.get(j).get());
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
			

			treeStr = tree.treeString().toString();
			
			stopTime = System.nanoTime();
			System.out.println(getTimeStamp() + " Finished compressing file...\n");
			
			totalFileCompressionTime += (stopTime - startTime);
			
			writer.write(treeStr);
			writer.write(compressedText.toString());
			fileScanner.close();
			writer.close();
			service.shutdownNow();
		}
		System.out.println("Average Tree Building Time: " + (totalTreeBuildingTime/numberTrials) + " ns");
		System.out.println("Average File Compression Time: " + (totalFileCompressionTime/numberTrials) + " ns");
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
