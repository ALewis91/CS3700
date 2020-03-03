import java.io.IOException;
import java.sql.Timestamp;

public class FileCompressor4 {

	static Timestamp timeStamp;
	
	public static void main(String[] args) throws IOException, InterruptedException 
	{
		long totalTreeBuildingTime = 0;
		long totalFileCompressionTime = 0;
		long startTime;
		long stopTime;
		int numberTrials = 10_000;
		HuffmanTreeMulti3 tree;
		
		// Warmup
		System.out.println("Warming up...");
		for (int i = 0; i < 1000; i++)
		{
			tree = new HuffmanTreeMulti3("src/US_Constitution.txt", "US_Constitution_compressed.txt");
			tree.createTree();
			tree.encode();
		}

		// Obtain average run time from n number of trials
		for (int i = 0; i < numberTrials; i++)
		{

			System.out.println("Trial #" + (i+1));
			System.out.println(getTimeStamp() + " Starting to build the tree...");
			startTime = System.nanoTime();
			
			tree = new HuffmanTreeMulti3("src/US_Constitution.txt", "US_Constitution_compressed.txt");
			tree.createTree();

			stopTime = System.nanoTime();
			totalTreeBuildingTime += (stopTime - startTime);		
			
			System.out.println(getTimeStamp() + " Finished building the tree...");
	
			
			System.out.println(getTimeStamp() + " Starting encoding file...");
			startTime = System.nanoTime();

			tree.encode();
			
			
			stopTime = System.nanoTime();
			System.out.println(getTimeStamp() + " Finished encoding file...\n");
			totalFileCompressionTime += (stopTime - startTime);
			
		}
		System.out.println("Average Tree Building Time: " + (totalTreeBuildingTime/numberTrials) + " ns");
		System.out.println("Average File Compression Time: " + (totalFileCompressionTime/numberTrials) + " ns");
		
		//tree = new HuffmanTreeMulti3("US_Constitution_compressed.txt", "US_Constitution_decompressed.txt");
		//tree.expand();
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
