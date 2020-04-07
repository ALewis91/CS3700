class ConsumerLock extends Thread
{
	private ArrayLockingQueue<String> products;
	private int consumed;
	String product;

	ConsumerLock(ArrayLockingQueue<String> products)
	{
		this.products = products;
		consumed = 0;
	}
	
	public void run()
	{
		while (ProducerLock.activeProducers() || !products.isEmpty())
		{
			product = products.poll();
			if (product == null)
				break;
			else
			{
				consumed++;
				try 
				{
					System.out.println(Thread.currentThread().getName() + " consumed: " + product);
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
		}
		System.out.println(Thread.currentThread().getName() + " consumed: " + consumed + " products.");
	}
}