import java.sql.Timestamp;
import java.util.LinkedList;



public class Producer5Consumer2Atomic
{
	static Timestamp timeStamp;
	
	public static void main(String[] args) 
	{
		long start, stop;
		start = System.currentTimeMillis();
		ArrayAtomicQueue<String> resource = new ArrayAtomicQueue<>(10);
		LinkedList<ProducerAtomic> producers = new LinkedList<>();
		LinkedList<ConsumerAtomic> consumers = new LinkedList<>();
		
		for (int x = 0; x < 5; x++)
		{
			ProducerAtomic producer = new ProducerAtomic(resource);
			producer.setName("Producer#"+x);
			producers.add(producer);
			producer.start();
		}
		
		for (int x = 0; x < 2; x++)
		{
			ConsumerAtomic consumer = new ConsumerAtomic(resource);
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
