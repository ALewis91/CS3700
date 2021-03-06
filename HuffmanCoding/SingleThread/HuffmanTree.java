import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffmanTree 
{
	private int[] characterFrequencyTable;
	private byte[] compressedText;
	private byte[] buffer;
	private byte modByte;
	private byte bitBuffer;
	private int bufferSize;
	private HuffmanNode root;
	private String text;
	private PriorityQueue<HuffmanNode> freqPQ;
	private HashMap<Character, String> compressionMap;
	private final int N_ASCII = 256;
	private String input;
	private String output;
	private FileOutputStream fos;
	private final int maxBytes = 512;
	private int currentBytes;
	private int numBitsRead;
		
	public HuffmanTree(String in, String out)
	{	
		
		input = in;
		output = out;

		buffer = new byte[maxBytes];
		bitBuffer = 0;
		bufferSize = 0;
		currentBytes = 0;
				
		characterFrequencyTable = new int[N_ASCII];
		freqPQ = new PriorityQueue<>(new HuffmanNodeComparator());
		compressionMap = new HashMap<>();
		
	}
	
	public void createTree() throws IOException
	{

		text = new String(Files.readAllBytes(Paths.get(input)));
		
		for (int x = 0; x < text.length(); x++)
			characterFrequencyTable[text.charAt(x)]++;
		
		
		int freq;
		for (int x = 0; x < characterFrequencyTable.length; x++)
		{
			freq = characterFrequencyTable[x];
			if (freq > 0)
			{
				freqPQ.add(new HuffmanNode(freq, (char)x));	
			}
		}
		
		while (freqPQ.size() > 1)
		{
			HuffmanNode left = freqPQ.poll();
			HuffmanNode right = freqPQ.poll();

			HuffmanNode par = new HuffmanNode();
			par.setFreq(left.getFreq() + right.getFreq());
			par.setLeft(left);
			par.setRight(right);
			
			freqPQ.add(par);
		}
		root = freqPQ.poll();

	}	
	
	public void encode() throws IOException
	{
		mapBuildTraversal(root, "", compressionMap, 0);
		fos = new FileOutputStream(output);
		writeTree(root);
		writeEncodedText();
		clear();
		fos.close();
	}
	
	private void mapBuildTraversal(HuffmanNode node, String code, HashMap<Character, String> map, int lvl)
	{	
		if (!node.isExternal())
		{
			mapBuildTraversal(node.getLeft(), code + "0", map, (lvl+1));
			mapBuildTraversal(node.getRight(), code + "1", map, (lvl+1));
		}
		else
		{
			map.put(node.getChar(), code);
		}
	}
	
	private void writeTree(HuffmanNode node) throws IOException
	{
		if (node.isExternal())
		{
			writeBit(true);
			String code = Integer.toBinaryString(node.getChar());
			for (int x = 8 - code.length(); x > 0; x--)
			{
				writeBit(false);
			}
			for (int x = 0; x < code.length(); x++)
			{
				if (code.charAt(x) == '1')
				{
					writeBit(true);
				}
				else
				{
					writeBit(false);
				}
			}
			return;
		}
		writeBit(false);
		writeTree(node.getLeft());
		writeTree(node.getRight());
	}
	
	private void writeBit(boolean bit) throws IOException
	{
		if (bufferSize == 7)
		{
			bitBuffer <<= 1;
			if (bit)
				bitBuffer |= 1;

			if (currentBytes == maxBytes)
			{
				fos.write(buffer);
				currentBytes = 0;
			}
			buffer[currentBytes] = bitBuffer;
			currentBytes++;
			bufferSize = 0;
			bitBuffer = 0;
		}
		else
		{
			bitBuffer <<= 1;
			if (bit)
				bitBuffer |= 1;
			bufferSize++;
		}
	}
	
	private void clear() throws IOException
	{
		while (bufferSize != 0)
			writeBit(true);
		for (int x = 0; x < currentBytes; x++)
			fos.write(buffer[x]);
	}
	
	private void writeEncodedText() throws IOException
	{
		String code;
		for (int x = 0; x < text.length(); x++)
		{
			code = compressionMap.get(text.charAt(x));
			for (int y = 0; y < code.length(); y++)
			{
				if (code.charAt(y) == '1')
					writeBit(true);
				else
					writeBit(false);
			}
		}
	}
	
	public void expand() throws IOException
	{
		bitBuffer = 0;
		bufferSize = 0;
		currentBytes = 0;
		numBitsRead = 0;
		compressedText = Files.readAllBytes(Paths.get(input));
		int numBits = compressedText.length * 8;
		numBitsRead = 0;
		root = readTree(0);
		//traverse(root);		
		PrintWriter writer = new PrintWriter(new File(output));
		HuffmanNode node = root;
		while (numBitsRead < numBits)
		{
			while (!node.isExternal() && numBitsRead < numBits)
			{
				if (getBit())
					node = node.getRight();
				else
					node = node.getLeft();
			}
			writer.write(node.getChar());
			node = root;
		}
		writer.close();
	}
	
    private HuffmanNode readTree(int lvl) 
    {
    	byte character = 0;
    	if (getBit())
    	{
    		for (int x = 0; x < 8; x++)
    		{
    			if (getBit())
    			{
    				character <<= 1; 
    				character |= 1;
    			}
    			else
    			{
    				character <<= 1; 
    				character |= 0;
    			}
    		}
    		return new HuffmanNode(-1, (char)character);
    	}
    	else
    	{
    		HuffmanNode left = readTree(lvl+1);
    		HuffmanNode right = readTree(lvl+1);
    		HuffmanNode node = new HuffmanNode('\0', left, right);
    		return node;
    		//return new HuffmanNode('\0', left, right);
    	}
    	
    }
    
    private boolean getBit()
    {
    	if (bufferSize == 0)
    	{
    		bitBuffer = compressedText[currentBytes];
    		currentBytes++;
    		bufferSize = 8;
    	}

    	modByte = bitBuffer;
    	bitBuffer <<= 1;
    	bufferSize--;
    	numBitsRead++;
    	return (modByte & 128) == 128;
    }
    

}
