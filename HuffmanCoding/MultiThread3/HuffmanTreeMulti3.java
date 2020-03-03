import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;


public class HuffmanTreeMulti3
{
	private int[] characterFrequencyTable;
	private byte[] compressedText;
	private byte[] buffer;
	private byte modByte;
	private byte bitBuffer;
	private int bufferSize;
	private HuffmanNode root;
	private byte[] text;
	private PriorityQueue<HuffmanNode> freqPQ;
	private HashMap<Character, String> compressionMap;
	private final int N_ASCII = 256;
	private String input;
	private String output;
	private FileOutputStream fos;
	private final int maxBytes = 512;
	private int currentBytes;
	private int readIndex;
	private ForkJoinPool pool;
	private byte sentinel;
	private CountDownLatch latch;
	
	private int numProcessors;
		
	public HuffmanTreeMulti3(String in, String out)
	{	
		
		input = in;
		output = out;

		buffer = new byte[maxBytes];
		bitBuffer = 0;
		bufferSize = 0;
		currentBytes = 0;
		sentinel = 7;
		
		characterFrequencyTable = new int[N_ASCII];
		freqPQ = new PriorityQueue<>(new HuffmanNodeComparator());
		compressionMap = new HashMap<>();
		
		numProcessors = Runtime.getRuntime().availableProcessors();
	}
	
	protected class RecursiveFrequencyGetter extends RecursiveTask<int[]>
    {
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int[] localFreqs;
    	byte[] bytes;
    	int start;
    	int end;
    	
    	RecursiveFrequencyGetter(int start, int end, byte[] bytes)
    	{
    		this.start = start;
    		this.end = end;
    		this.bytes = bytes;
    		localFreqs = new int[N_ASCII];
    	}
    	
		@Override
		protected int[] compute()
		{
			if(start - end < 8000)
			{
				for (int x = start; x < end; x++)
				{
					localFreqs[(char)bytes[x]]++;
				}
				return localFreqs;
			}
			else
			{
				int mid = (start + end)/2;
				RecursiveFrequencyGetter left = new RecursiveFrequencyGetter(start, mid, bytes);
				RecursiveFrequencyGetter right = new RecursiveFrequencyGetter(mid, end, bytes);
				left.fork();
				localFreqs = right.compute();
				int[] leftResult = left.join();
				for (int x = 0; x < N_ASCII; x++)
					localFreqs[x] += leftResult[x];
				return localFreqs;
			}
		}
    	
    }

	
	public void createTree() throws IOException
	{

		text = Files.readAllBytes(Paths.get(input));
		
		pool = ForkJoinPool.commonPool();
		characterFrequencyTable = pool.invoke(new RecursiveFrequencyGetter(0, text.length, text));
		
		// Add sentinel character to table/tree
		characterFrequencyTable[sentinel]++;
		
		int freq;
		for (int x = 0; x < N_ASCII; x++)
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
		String code = compressionMap.get((char)sentinel);
		for (int y = 0; y < code.length(); y++)
		{
			if (code.charAt(y) == '1')
			{
				writeBit(true);
			}
			else
			{
				writeBit(false);
			}
		}
		while (bufferSize > 0)
		{
			writeBit(true);
		}
		clear();
		writeEncodedText();
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
	
	private void writeEncodedText()
	{
		latch = new CountDownLatch(1);
		pool.invoke(new BlockWriter(0, text.length, new CountDownLatch(0)));
		try 
		{
			latch.await();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		pool.shutdown();
	}
	
	protected class BlockWriter extends RecursiveAction
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private CountDownLatch iCanWrite;
		private CountDownLatch youCanWrite;
		private int start, end;
		private byte[] bytes;
		private byte currentByte;
		private int currentBytesSize;
		private int byteBufferSize;
		private String code;
		
		protected BlockWriter(int lo, int hi, CountDownLatch countDown)
		{
			iCanWrite = countDown;
			youCanWrite = new CountDownLatch(1);
			start = lo;
			end = hi;
			bytes = new byte[10_000];
			currentByte = 0;
			byteBufferSize = 0;
			currentBytesSize = 0;
		}
		
		protected void compute()
		{
			if (end - start < 15_000)
			{

				for (int x = start; x < end; x++)
				{

					code = compressionMap.get((char)text[x]);

					for (int y = 0; y < code.length(); y++)
					{
						if (code.charAt(y) == '1')
						{
							writeBit(true);
						}
						else
						{
							writeBit(false);
						}
					}
				}
				cap();
				try 
				{
					iCanWrite.await();
				} 
				catch (InterruptedException e1) 
				{
					e1.printStackTrace();
				}
				try 
				{
					fos.write(bytes, 0, currentBytesSize);
					 if (end == text.length)
						 latch.countDown();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}

			}
			else
			{
				int mid = (start + end)/2;
				BlockWriter right = new BlockWriter(mid, end, youCanWrite);
				right.fork();
				end = mid;
				compute();
				youCanWrite.countDown();
				right.join();
			}
		}
		protected void writeBit(boolean bit)
		{
			if (byteBufferSize == 7)
			{
				currentByte <<= 1;
				if (bit)
					currentByte |= 1;
				bytes[currentBytesSize] = currentByte;
				currentBytesSize++;
				byteBufferSize = 0;
				currentByte = 0;
			}
			else
			{
				currentByte <<= 1;
				if (bit)
					currentByte |= 1;
				byteBufferSize++;
			}
		}

		
		
		protected void cap() 
		{
			code = compressionMap.get((char)sentinel);
			for (int y = 0; y < code.length(); y++)
			{
				if (code.charAt(y) == '1')
				{
					writeBit(true);
				}
				else
				{
					writeBit(false);
				}
			}
			while(byteBufferSize > 0)
			{
				writeBit(true);
			}
		}
	}
	
	public void expand() throws IOException
	{
		bitBuffer = 0;
		bufferSize = 0;
		currentBytes = 0;
		readIndex = 0;
		compressedText = Files.readAllBytes(Paths.get(input));
		int numBits = compressedText.length * 8;
		
		root = readTree();
		
		PrintWriter writer = new PrintWriter(new File(output));
		HuffmanNode node = root;
		while (readIndex < compressedText.length * 8)
		{
			while (!node.isExternal() && readIndex < numBits)
			{
				if (getBit())
				{
					node = node.getRight();
				}
				else
				{
					node = node.getLeft();
				}
			}
			if (node.getChar() == (char)sentinel)
			{
				if (numBits - readIndex > 8)
					skipByte();
				else
					break;
			}
			else
			{
				writer.write(node.getChar());
			}
			node = root;
		}
		writer.close();
	}
	
	
	class BlockEncoder implements Callable<Queue<String>>
	{
		private int lo, hi;
		private Queue<String> codes;
		BlockEncoder(int lo, int hi)
		{
			this.lo = lo;
			this.hi = hi;
			codes = new LinkedList<String>();
		}

		@Override
		public Queue<String> call()
		{
			for (int x = lo; x < hi; x++)
				codes.add(compressionMap.get((char)text[x]));
			return codes;
		}
		
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
    	if (bufferSize == 0 )
    	{
    		bitBuffer = compressedText[currentBytes];
    		currentBytes++;
    		bufferSize = 8;
    	}

    	modByte = bitBuffer;
    	bitBuffer <<= 1;
    	bufferSize--;
    	readIndex++;
    	return (modByte & 128) == 128;
    }
    
    private void skipByte()
    {	
    	if(bufferSize != 0)
    	{
	    	bufferSize = 0;
	    	readIndex = currentBytes * 8;
    	}
    }
    
    
    public void traverse(HuffmanNode node, String code)
    {
    	if (node.isExternal())
    	{
    		System.out.println(node.getChar() + ": " + code);
    		return;
    	}
    	traverse(node.getLeft(), code + "0");
    	traverse(node.getRight(), code + "1");
    }
    

}
