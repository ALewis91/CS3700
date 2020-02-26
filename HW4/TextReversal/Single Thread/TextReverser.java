import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Scanner;
import java.util.Stack;

public class TextReverser 
{
	static Timestamp timeStamp;

	public static void main(String[] args) throws FileNotFoundException 
	{
		Scanner fileScan = new Scanner(new File("src/DeclarationIndependence.txt"));
		PrintWriter writer = new PrintWriter(new File("backwards.txt"));
		Stack<String> wordStack = new Stack<>();
		String line;
		Scanner lineScanner;
		
		System.out.println("Single threaded word reversal application\n\n");
		System.out.println(getTimeStamp() + " Starting to read file");
		while (fileScan.hasNextLine()) 
		{
			line = fileScan.nextLine().replaceAll("[^A-Za-z0-9 ]", "");
			lineScanner = new Scanner(line);
			while(lineScanner.hasNext())
				wordStack.push(lineScanner.next() + " ");
			if (fileScan.hasNextLine())
				wordStack.push("\n");
		}
		
		while (!wordStack.isEmpty())
			writer.write(wordStack.pop());
		fileScan.close();
		writer.close();
		System.out.println(getTimeStamp() + " Finished writing file");
		

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
