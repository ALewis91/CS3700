import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffmanTree 
{
	private int[] characterFrequencyTable;
	private HuffmanNode root;
	private StringBuilder tree;
	private PriorityQueue<HuffmanNode> freqPQ;
	private HashMap<Character, String> compressionMap;
	
	public HuffmanTree(int[] freqTable)
	{
		characterFrequencyTable = freqTable;
		compressionMap = new HashMap<>();
		freqPQ = new PriorityQueue<>(256, new HuffmanNodeComparator());
		root = createTree();
		mapBuildTraversal(root, "", compressionMap);
		tree = new StringBuilder();
	}
	
	public void printCharCount()
	{
		System.out.println(root.getFreq());
	}
	
	private HuffmanNode createTree()
	{
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
			//left.setPar(par);
			//right.setPar(par);
			freqPQ.add(par);
		}
		return freqPQ.poll();
	}	
	
	private void mapBuildTraversal(HuffmanNode root, String code, HashMap<Character, String> map)
	{	
		if (!root.isExternal())
		{
			mapBuildTraversal(root.getLeft(), code + "0", map);
			mapBuildTraversal(root.getRight(), code + "1", map);
		}
		else
			map.put(root.getChar(), code);	
	}
	
	public HashMap<Character, String> getCodeMap()
	{
		return compressionMap;
	}
	
	private void getTree(HuffmanNode node)
	{
		String code = Integer.toBinaryString(node.getChar());
		if (node.isExternal())
		{
			tree.append('1');
			for (int x = 8 - code.length(); x > 0; x--)
				tree.append('0');
			tree.append(code);
			return;
		}
		tree.append('0');
		getTree(node.getLeft());
		getTree(node.getRight());
	}
	
	public StringBuilder treeString()
	{
		getTree(root);
		StringBuilder mapSize = new StringBuilder();
		String binarySize = Integer.toBinaryString(compressionMap.size());
		for (int x = binarySize.length() - 8; x > 0; x--)
			mapSize.append('0');
		mapSize.append(binarySize);
		mapSize.append(tree);
		return mapSize;
	}

	/*
	public static void expand(File file, PrintWriter out) throws FileNotFoundException
	{
		Scanner scan = new Scanner(file);
		StringBuilder binary = new StringBuilder();
		StringBuilder charCode = new StringBuilder();
		HuffmanNode decodeTree;
		
		for (int x = 0; x < 8; x++)
			binary.append(scan.next());
		int numChars = Integer.parseInt(binary.toString(), 2);
		binary = new StringBuilder();
		for (int x = 0; x < numChars; x++)
		{
			while (scan.nextByte() == '0')
			{
				
			}
		}
	}*/
}
