import java.util.concurrent.atomic.AtomicInteger;

class ProducerAtomic extends Thread
{
	private ArrayAtomicQueue<String> products;
	private static AtomicInteger numActiveProducers = new AtomicInteger(0);
	private int produced;
	private String product;
	
	ProducerAtomic(ArrayAtomicQueue<String> products)
	{
		this.products = products;
		numActiveProducers.getAndIncrement();
		produced = 0;
	}
	
	public void run()
	{
		while (produced < 100)
		{
			product = Thread.currentThread().getName() + "'s product # " + (++produced);
			products.add(product);
			System.out.println(Thread.currentThread().getName() + " added " + product);
		}
		numActiveProducers.getAndDecrement();
	}
	
	public static boolean activeProducers()
	{
		return numActiveProducers.get() > 0;
	}
}