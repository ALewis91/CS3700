package com.aaron.cpp.ActorsConsumerProducer;

import java.util.LinkedList;
import java.util.Queue;

import com.aaron.cpp.SieveOfEratosthenes.NonMultipleSieve.Command;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;

public class Buffer extends AbstractBehavior<Buffer.Command>
{
	interface Command{}

	public Buffer(ActorContext<Command> context) 
	{
		super(context);
	}

	public static Behavior<Command> create() {
		return Behaviors.setup(Buffer::new);
	}
	
	private final int CAPACITY = 10;
	private String[] buffer = new String[CAPACITY];
	private Queue<ActorRef<Command>> readyProducers = new LinkedList<>();
	private Queue<ActorRef<Command>> readyConsumers = new LinkedList<>();
	
	@Override
	public Receive<Command> createReceive() 
	{
		return newReceiveBuilder()
				.onMessage("reqAdd", this::reqAdd).build();
	}

}

