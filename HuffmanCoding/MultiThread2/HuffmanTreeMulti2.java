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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HuffmanTreeMulti2
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
	private int numBitsRead;
	
	private ExecutorService service;
	private LinkedList<Future> futures;
	private int numProcessors;
		
	public HuffmanTreeMulti2(String in, String out)
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
		
		numProcessors = Runtime.getRuntime().availableProcessors();
		futures = new LinkedList<>();
		
	}
	
	class FrequencyUpdater implements Callable<int[]>
	{
		private int lo, hi;
		private int[] table;
		
		FrequencyUpdater(int lo, int hi)
		{
			this.lo = lo;
			this.hi = hi;
			table = new int[N_ASCII];
		}
		
		public int[] call()
		{
			for (int x = lo; x < hi; x++)
				table[text[x]]++;
			return table;
		}
	}
	

	
	public void createTree() throws IOException
	{
		service = Executors.newFixedThreadPool(numProcessors);
		text = Files.readAllBytes(Paths.get(input));
		int chunks = (int) (Math.ceil((double)text.length / 25_000) > numProcessors ? numProcessors : Math.ceil((double)text.length / 25_000));
		int chunkSize = text.length/chunks;
		int lo, hi;
		lo = 0;
		hi = chunkSize;
		for (int x = 0; x < chunks; x++)
		{
			futures.add(service.submit(new FrequencyUpdater(lo, hi)));
			lo += chunkSize;
			hi = Math.min(hi + chunkSize, text.length);
		}
		int[][] freqs = new int[chunks][];
		for (int x = 0; x < chunks; x++)
		{
			try {
				freqs[x] = ((int[]) futures.get(x).get());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		futures.clear();
		
		for (int x = 0; x < freqs.length; x++)
		{
			for (int y = 0; y < N_ASCII; y++)
				characterFrequencyTable[y] += freqs[x][y];
		}
				

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
		int chunkSize = text.length/(numProcessors/2) < 25_000 ? 25_000 : text.length/(numProcessors/2);
		int start = chunkSize;
		int end = Math.min(start + chunkSize, text.length);
		String code;
		for (int x = 0; x < text.length/chunkSize; x++)
		{
			futures.add(service.submit(new BlockEncoder(start, end)));
			
			start = end;
			end = Math.min(start + chunkSize, text.length);
		}
		for (int x = 0; x < chunkSize; x++)
		{
			code = compressionMap.get((char)text[x]);
			for (int y = 0; y < code.length(); y++)
			{
				if (code.charAt(y) == '1')
					writeBit(true);
				else
					writeBit(false);
			}
		}
		Queue<String> wordStack;
		for (Future<Queue<String>> future : futures)
		{
			try 
			{
				wordStack = future.get();
				while(!wordStack.isEmpty())
				{
					code = wordStack.poll();
					for (int y = 0; y < code.length(); y++)
					{
						if (code.charAt(y) == '1')
							writeBit(true);
						else
							writeBit(false);
					}
				}
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
		service.shutdown();
	}
	
	public void expand() throws IOException
	{
		bitBuffer = 0;
		bufferSize = 0;
		currentBytes = 0;
		numBitsRead = 0;
		compressedText = Files.readAllBytes(Paths.get(input));
		int numBits = compressedText.length * 8;
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
    
    public void printBits()
    {
    	while (currentBytes < compressedText.length || bufferSize > 0)
    	{
    		if (getBit())
    			System.out.print(1);
    		else
    			System.out.print(0);
    	}
    }

}
