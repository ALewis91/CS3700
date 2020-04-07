import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

class ArrayAtomicQueue<K> 
{
	private ArrayList<K> queue;
	private int capacity;
	private int writePosition;
	private int readPosition;
	private AtomicInteger productsLeft;
	private AtomicInteger emptySpacesLeft;
	
	ArrayAtomicQueue(int capacity)
	{
		queue = new ArrayList<K>(capacity);
		this.capacity = capacity;
		writePosition = 0;
		readPosition = 0;
		productsLeft = new AtomicInteger(0);
		emptySpacesLeft = new AtomicInteger(capacity);
	}
	
	public void add(K element)
	{
		synchronized(ArrayAtomicQueue.class)
		{
			while (emptySpacesLeft.get() == 0)
			{
				try
				{
					ArrayAtomicQueue.class.wait();
				}
				catch (InterruptedException ex)
				{
					ex.printStackTrace();
				}
			}
			queue.add(writePosition, element);
			writePosition = (writePosition + 1) % capacity;
			emptySpacesLeft.decrementAndGet();
			productsLeft.incrementAndGet();
			ArrayAtomicQueue.class.notifyAll();
		}
	}
	
	public K poll()
	{
		synchronized(ArrayAtomicQueue.class)
		{
			while (productsLeft.get() == 0 && ProducerIsolated.activeProducers())
			{
				try
				{
					ArrayAtomicQueue.class.wait();
				}
				catch (InterruptedException ex)
				{
					ex.printStackTrace();
				}
			}
			if (ProducerIsolated.activeProducers() || productsLeft.get() > 0)
			{
				K elem = queue.get(readPosition);
				readPosition = (readPosition + 1) % capacity;
				productsLeft.decrementAndGet();
				emptySpacesLeft.incrementAndGet();
				if (productsLeft.get() == 0)
					ArrayAtomicQueue.class.notifyAll();
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
		return productsLeft.get() == 0;
	}
}