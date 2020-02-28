import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Scanner;

public class FileCompressor {

	static Timestamp timeStamp;
	
	public static void main(String[] args) throws FileNotFoundException 
	{
		int[] freqTable;
		long totalTreeBuildingTime = 0;
		long totalFileCompressionTime = 0;
		long startTime;
		long stopTime;
		int numberTrials = 1000;
		
		Scanner fileScanner;
		StringBuilder text;
		StringBuilder compressedText;
		PrintWriter writer;
		
		HuffmanTree tree;
		HashMap<Character, String> codeMap;
		
		String treeStr;
		
		for (int i = 0; i < numberTrials; i++)
		{
			fileScanner = new Scanner(new File("src/US_Constitution.txt"));
			text = new StringBuilder();
			compressedText = new StringBuilder();
			writer = new PrintWriter(new File("US_Constitution_compressed.txt"));
			freqTable = new int[256];
			
			while (fileScanner.hasNextLine()) 
			{
				text.append(fileScanner.nextLine());
				if (fileScanner.hasNextLine())
					text.append('\n');
			}
			
			System.out.println("Trial #" + (i+1));
			System.out.println(getTimeStamp() + " Starting to build the tree...");
			startTime = System.nanoTime();
			
			for (int x = 0; x < text.length(); x++)
				freqTable[text.charAt(x)]++;

			tree = new HuffmanTree(freqTable);
			
			stopTime = System.nanoTime();
			totalTreeBuildingTime += (stopTime - startTime);		
			System.out.println(getTimeStamp() + " Finished building the tree...");
	
			
			System.out.println(getTimeStamp() + " Starting encoding file...");
			startTime = System.nanoTime();

			codeMap = tree.getCodeMap();

			for (int x = 0; x < text.length(); x++)
			{
				compressedText.append(codeMap.get(text.charAt(x)));
			}
			
			treeStr = tree.treeString().toString();
			
			stopTime = System.nanoTime();
			System.out.println(getTimeStamp() + " Finished encoding file...\n");
			totalFileCompressionTime += (stopTime - startTime);

			writer.write(treeStr);
			writer.write(compressedText.toString());
			
			fileScanner.close();		
			writer.close();
			
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
