import java.util.ArrayList;

class ArrayIsolatedQueue<K> 
{
	private ArrayList<K> queue;
	private int capacity;
	private int size;
	private int writePosition;
	private int readPosition;
	
	ArrayIsolatedQueue(int capacity)
	{
		queue = new ArrayList<K>(capacity);
		this.capacity = capacity;
		size = 0;
		writePosition = 0;
		readPosition = 0;
	}
	
	public void add(K element)
	{
		synchronized(ArrayIsolatedQueue.class)
		{
			while (size == capacity)
			{
				try
				{
					ArrayIsolatedQueue.class.wait();
				}
				catch (InterruptedException ex)
				{
					ex.printStackTrace();
				}
			}
			queue.add(writePosition, element);
			writePosition = (writePosition + 1) % capacity;
			size++;
			ArrayIsolatedQueue.class.notifyAll();
		}
	}
	
	public K poll()
	{
		synchronized(ArrayIsolatedQueue.class)
		{
			while (size == 0 && ProducerIsolated.activeProducers())
			{
				try
				{
					ArrayIsolatedQueue.class.wait();
				}
				catch (InterruptedException ex)
				{
					ex.printStackTrace();
				}
			}
			if (ProducerIsolated.activeProducers() || size > 0)
			{
				K elem = queue.get(readPosition);
				readPosition = (readPosition + 1) % capacity;
				size--;
				if (size == 0)
					ArrayIsolatedQueue.class.notifyAll();
				return elem;
			}
			else
			{
				return null;
			}
		}
	}
	
	public boolean isEmpty()
	{
		synchronized(ArrayIsolatedQueue.class)
		{		
			boolean isEmpty = size == 0;
			return isEmpty;
		}
	}
}