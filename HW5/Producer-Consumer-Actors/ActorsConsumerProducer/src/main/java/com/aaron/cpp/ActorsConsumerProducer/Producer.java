package com.aaron.cpp.ActorsConsumerProducer;

import akka.actor.typed.javadsl.*;

public class Producer extends AbstractBehavior<Producer.Command> {
	
	public Producer(ActorContext<Command> context)
	{
		super(context);
	}

	public interface Command{}
	
	public static final class Produce implements Command {
		
		public Produce()
	}
}

	@Override
	public Receive<Command> createReceive() {
		return null;
	}
