public class HuffmanNode 
{
	//private HuffmanNode par;
	private HuffmanNode left;
	private HuffmanNode right;
	private int frequency;
	private char character;
	
	HuffmanNode()
	{
		frequency = -1;
		character = (char)127;
		//par = null;
		left = null;
		right = null;
	}
	
	HuffmanNode(int freq, char c)
	{
		frequency = freq;
		character = c;
		//par = null;
		left = null;
		right = null;
	}
	
	public void setFreq(int f)
	{
		frequency = f;
	}
	
	public int getFreq()
	{
		return frequency;
	}
	
	public void setChar(char c)
	{
		character = c;
	}
	
	public char getChar()
	{
		return character;
	}
	
	public boolean isExternal()
	{
		return (left == null && right == null);
	}
	
	public void setLeft(HuffmanNode l)
	{
		left = l;
	}
	
	public void setRight(HuffmanNode r)
	{
		right = r;
	}
	
	//public void setPar(HuffmanNode p)
	//{
	//	par = p;
	//}
	
	public HuffmanNode getLeft()
	{
		return left;
	}
	
	public HuffmanNode getRight()
	{
		return right;
	}
	
	//public HuffmanNode getPar()
	//{
	//	return par;
	//}
}