import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class ArrayLockingQueue<K> 
{
	private ArrayList<K> queue;
	private final Lock lock = new ReentrantLock();
	private final Condition notFull = lock.newCondition();
	private final Condition notEmpty = lock.newCondition();
	private int capacity;
	private int size;
	private int writePosition;
	private int readPosition;
	
	ArrayLockingQueue(int capacity)
	{
		queue = new ArrayList<K>(capacity);
		this.capacity = capacity;
		size = 0;
		writePosition = 0;
		readPosition = 0;
	}
	
	public void add(K element)
	{
		lock.lock();
		try
		{
			while (size == capacity)
			{
				try
				{
					notFull.await();
				}
				catch(InterruptedException ex)
				{
					ex.printStackTrace();
				}
			}
			queue.add(writePosition, element);
			writePosition = (writePosition + 1) % capacity;
			size++;
			notEmpty.signal();
		}
		finally 
		{
			lock.unlock();
		}
	}
	
	public K poll()
	{
		lock.lock();
		try
		{
			K elem = null;
			while (size == 0 && ProducerLock.activeProducers())
			{	
				try
				{
					notEmpty.await();

				}
				catch(InterruptedException ex)
				{
					ex.printStackTrace();
				}
			}
			elem = queue.get(readPosition);
			readPosition = (readPosition + 1) % capacity;
			size--;
			notFull.signal();
			return elem;
		}
		finally
		{
			lock.unlock();	
		}
	}
		
	
	public boolean isEmpty()
	{
		lock.lock();
		boolean isEmpty = size == 0;
		lock.unlock();
		return isEmpty;
	}
}