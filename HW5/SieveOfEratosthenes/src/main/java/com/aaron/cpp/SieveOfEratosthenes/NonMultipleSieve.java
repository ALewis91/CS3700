package com.aaron.cpp.SieveOfEratosthenes;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class NonMultipleSieve extends AbstractBehavior<NonMultipleSieve.Command>
{

	interface Command{}
	
	
	public static class Cut implements Command
	{
		public Cut(int currentPrime, boolean[] primes)
		{	
			for (int x = currentPrime*currentPrime; x < primes.length; x+=currentPrime)
				primes[x] = false;
		}
	}
	
	public static Behavior<Command> create() {
		return Behaviors.setup(context -> new NonMultipleSieve(context));
	}
	
	private NonMultipleSieve(ActorContext<Command> context)
	{
		super(context);
	}
	
	@Override
	public akka.actor.typed.javadsl.Receive<Command> createReceive() 
	{
		return newReceiveBuilder()
				.onMessage(Cut.class, this::onCut).build();
	}
	
	private Behavior<Command> onCut(Cut command)
	{
		return this;
	}
}

