import java.util.Comparator;

public class HuffmanNodeComparator implements Comparator<HuffmanNode>
{

	public int compare(HuffmanNode o1, HuffmanNode o2) 
	{
		if (o1.getFreq() > o2.getFreq())
			return 1;
		else if (o1.getFreq() == o2.getFreq())
			return 0;
		else
			return -1;
	}
	
}