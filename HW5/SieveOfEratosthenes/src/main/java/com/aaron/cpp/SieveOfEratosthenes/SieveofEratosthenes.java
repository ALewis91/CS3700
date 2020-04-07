package com.aaron.cpp.SieveOfEratosthenes;

import akka.actor.typed.ActorSystem;

/**
 * Hello world!
 *
 */
public class SieveofEratosthenes 
{
    public static void main( String[] args )
    {
       ActorSystem<NonMultipleSieve.Command> system = ActorSystem.create(NonMultipleSieve.create(), "sot-system");
       boolean[] primes = new boolean[1_000_001];
       primes[0] = false;
       primes[1] = false;
       int numPrimes = 0;
       for (int x = 2; x < primes.length; x++)
    	   primes[x] = true;
       
       long start, end;
       start = System.currentTimeMillis();
       for (int x = 0; x < Math.sqrt(primes.length); x++)
       {
    	   if (primes[x])
    		   system.tell(new NonMultipleSieve.Cut(x, primes));
       }
       for (int x = 2; x < primes.length; x++)
       {
    	   if (primes[x])
    	   {
    		   numPrimes++;
    		   System.out.println(x);
    	   }
       }
       end = System.currentTimeMillis();
       System.out.println("Total primes: " + numPrimes);
       System.out.println("Execution time: " + (end - start) + " ms");
       
    }
}
