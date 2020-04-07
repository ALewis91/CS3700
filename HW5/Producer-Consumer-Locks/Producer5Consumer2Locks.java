import java.sql.Timestamp;
import java.util.LinkedList;

public class Producer5Consumer2Locks 
{
	static Timestamp timeStamp;
	
	public static void main(String[] args) 
	{
		long start, stop;
		start = System.currentTimeMillis();
		ArrayLockingQueue<String> resource = new ArrayLockingQueue<>(10);
		LinkedList<ProducerLock> producers = new LinkedList<>();
		LinkedList<ConsumerLock> consumers = new LinkedList<>();
		for (int x = 0; x < 5; x++)
		{
			ProducerLock producer = new ProducerLock(resource);
			producer.setName("Producer#"+x);
			producers.add(producer);
			producer.start();
		}
		
		for (int x = 0; x < 2; x++)
		{
			ConsumerLock consumer = new ConsumerLock(resource);
			consumer.setName("Consumer#"+x);
			consumers.add(consumer);
			consumer.start();
		}
		
		for (int x = 0; x < 5; x++)
		{
			try 
			{
				producers.get(x).join();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		for (int x = 0; x < 2; x++)
		{
			try 
			{
				consumers.get(x).join();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		stop = System.currentTimeMillis();
		
		System.out.println("Execution time: " + (stop-start) + " ms");
	}
	
	@SuppressWarnings("deprecation")
	public static String getTimeStamp()
	{
		timeStamp = new Timestamp(System.currentTimeMillis());
		return "[" + timeStamp.getHours() + ":" 
				+ timeStamp.getMinutes() + ":" 
				+ timeStamp.getSeconds() + "." 
				+ timeStamp.getNanos()/1_000_000 + "]";
	}
}
