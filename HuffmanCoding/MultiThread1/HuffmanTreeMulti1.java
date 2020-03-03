import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class HuffmanTreeMulti1
{
	
	private int[] characterFrequencyTable;
	private byte[] compressedText;
	private byte[] buffer;
	private byte modByte;
	private byte bitBuffer;
	private int bufferSize;
	private static HuffmanNode root;
	private static byte[] text;
	private PriorityQueue<HuffmanNode> freqPQ;
	private static ConcurrentHashMap<Character, String> compressionMap;
	private final int N_ASCII = 256;
	private String input;
	private String output;
	private FileOutputStream fos;
	private final int maxBytes = 512;
	private int currentBytes;
	private int numBitsRead;
	
	private int leaves;
	private ForkJoinPool pool;
	private CountDownLatch latch;
		
	public HuffmanTreeMulti1(String in, String out)
	{		
		input = in;
		output = out;

		buffer = new byte[maxBytes];
		bitBuffer = 0;
		bufferSize = 0;
		currentBytes = 0;
		leaves = 0;
				
		characterFrequencyTable = new int[N_ASCII];
		freqPQ = new PriorityQueue<HuffmanNode>();
		compressionMap = new ConcurrentHashMap<>();
	}
	
	public void createTree() throws IOException
	{
		text = Files.readAllBytes(Paths.get(input));
		pool = ForkJoinPool.commonPool();
		characterFrequencyTable = pool.invoke(new RecursiveFrequencyGetter(0, text.length));
		
		int freq;
		for (int x = 0; x < N_ASCII; x++)
		{
			freq = characterFrequencyTable[x];
			if (freq > 0)
			{
				freqPQ.add(new HuffmanNode(freq, (char)x));	
			}
		}
		leaves = freqPQ.size();
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
		latch = new CountDownLatch(leaves);
		pool.execute(new RecursiveMapBuilder(root, ""));	
		try 
		{
			latch.await();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		pool.shutdown();

		fos = new FileOutputStream(output);
		writeTree(root);
		writeEncodedText();
		clear();
		fos.close();
	}
	

	protected class RecursiveMapBuilder extends RecursiveAction
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private HuffmanNode node;
		private String code;
		
		protected RecursiveMapBuilder(HuffmanNode node, String code)
		{	
			this.node = node;
			this.code = code;
		}
		
		@Override
		protected void compute()
		{
			if (node.isExternal())
			{
				compressionMap.put(node.getChar(), code.toString());
				latch.countDown();
			}
			else
			{
				RecursiveMapBuilder  left = new RecursiveMapBuilder(node.getLeft(), code + "0");
				left.fork();
				node = node.getRight();
				code += "1";
				compute();
			}	
			
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
		for (int x = 0; x < text.length; x++)
		{
			code = compressionMap.get((char)text[x]);
			//System.out.println(text[x] + ": " + code);
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
		root = readTree();
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
	
	
    private HuffmanNode readTree() 
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
    		HuffmanNode left = readTree();
    		HuffmanNode right = readTree();
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
    
    protected class RecursiveFrequencyGetter extends RecursiveTask<int[]>
    {
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int[] localFreqs;
    	int start;
    	int end;
    	
    	RecursiveFrequencyGetter(int start, int end)
    	{
    		this.start = start;
    		this.end = end;
    		localFreqs = new int[N_ASCII];
    	}
    	
		@Override
		protected int[] compute()
		{
			if(start - end < 12_000)
			{
				for (int x = start; x < end; x++)
				{
					localFreqs[text[x]]++;
				}
				return localFreqs;
			}
			else
			{
				int mid = (start + end)/2;
				RecursiveFrequencyGetter right = new RecursiveFrequencyGetter(mid, end);
				right.fork();
				end = mid;
				compute();
				int[] rightResult = right.join();
				for (int x = 0; x < N_ASCII; x++)
					localFreqs[x] += rightResult[x];
				return localFreqs;
			}
		}
    	
    }

}
