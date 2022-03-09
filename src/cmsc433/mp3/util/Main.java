package cmsc433.mp3.util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import cmsc433.mp3.actors.SimulationManagerActor;
import cmsc433.mp3.enums.*;
import cmsc433.mp3.messages.SimulationFinishMsg;
import cmsc433.mp3.messages.SimulationStartMsg;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

/**
 * Sample class for setting up and running a resource-manager system.
 * 
 * Feel free to modify as you wish, but do not include any code that the rest of
 * your implementation depends on.
 * 
 * @author Rance Cleaveland
 *
 */
public class Main {

	ActorSystem system = ActorSystem.create("Resource manager system");

	public static void main(String[] args) throws FileNotFoundException {
		// Create actor system and instantiate a simulation manager.
				
		ActorSystem system = ActorSystem.create("Simulation");
		ArrayList<NodeSpecification> nodes = setupTest1();
		ActorRef simulationManager = SimulationManagerActor.makeSimulationManager(nodes, system);
		
		// Start simulation manager and retrieve result
		
		long futureDelay = 1000L;  // milliseconds
		Duration awaitDelay = Duration.Inf();

		Future<Object> fmsg = Patterns.ask(simulationManager, new SimulationStartMsg(), futureDelay);
		SimulationFinishMsg msg = null;
		try {
			msg = (SimulationFinishMsg)Await.result(fmsg, awaitDelay);
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
		// When each users has finished, terminate
		system.terminate();
		
		// Causes this thread to block until the shutdown is complete.
//		try {
//			Await.ready(system.whenTerminated(), Duration.Inf());
//		} catch (TimeoutException | InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// It is critical not to examine the log until after the actor system has shutdown. Otherwise, the log
		// may still be being modified as ResourceManagers send messages to the LoggerActor.
		for (Object o : msg.getLog())
			System.out.println(o);
	}

	private static ArrayList<NodeSpecification> setupTest1 () throws FileNotFoundException {
		// Create initial resources
		
		ArrayList<Resource> printers = Systems.makeResources("Printer", 2);
		ArrayList<Resource> scanners = Systems.makeResources("Scanner", 1);
		
		// Create user scripts
		UserScript script1 = UserScript.fromFile("test1script1.txt");
		UserScript script2 = UserScript.fromFile("test1script2.txt");
		
		// Create node specifications
		
		ArrayList<UserScript> scriptList1 = new ArrayList<UserScript>();
		scriptList1.add(script1);
		NodeSpecification node1 = new NodeSpecification(printers, scriptList1);
		
		ArrayList<UserScript> scriptList2 = new ArrayList<UserScript>();
		scriptList2.add(script2);
		NodeSpecification node2 = new NodeSpecification(scanners, scriptList2);
		
		// Return list of nodes
		ArrayList<NodeSpecification> list = new ArrayList<NodeSpecification> ();
		list.add(node1);
		list.add(node2);
		return list;
	}
	
	private static ArrayList<NodeSpecification> setupTest2 () throws FileNotFoundException {
		// Create initial resources
		ArrayList<Resource> printers = Systems.makeResources("Printer", 1);
		
		ArrayList<UserScript> scriptList1 = new ArrayList<UserScript> ();
		scriptList1.add(UserScript.fromFile("test2script.txt"));
		NodeSpecification node1 = new NodeSpecification(printers, scriptList1);
		NodeSpecification node2 = new NodeSpecification(new ArrayList<Resource>(), new ArrayList<UserScript>());
		
		ArrayList<NodeSpecification> list = new ArrayList<NodeSpecification> ();
		list.add(node1);
		list.add(node2);
		return list;
	}
	
	private static ArrayList<NodeSpecification> setupTest3 () throws FileNotFoundException {
		ArrayList<Resource> printers = Systems.makeResources("Printer", 1);
		ArrayList<Resource> scanners = Systems.makeResources("Scanner", 1);
		
		ArrayList<UserScript> scriptList1 = new ArrayList<UserScript> ();
		scriptList1.add(UserScript.fromFile("test3script1.txt"));
		
		ArrayList<UserScript> scriptList2 = new ArrayList<UserScript> ();
		scriptList2.add(UserScript.fromFile("test3script2.txt"));
		
		NodeSpecification node1 = new NodeSpecification(printers, scriptList1);
		NodeSpecification node2 = new NodeSpecification(scanners, scriptList2);
		
		ArrayList<NodeSpecification> list = new ArrayList<NodeSpecification> ();
		list.add(node1);
		list.add(node2);
		return list;
	}
}
