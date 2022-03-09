package cmsc433.mp3.messages;

import akka.actor.ActorRef;

public class WhoHasResourceResponseMsg {
	private final String resource_name;
	private final boolean result;
	private final ActorRef sender; // The actor who sends this response message.
	
	public WhoHasResourceResponseMsg (String resource_name, boolean result, ActorRef sender) {
		this.resource_name = resource_name;
		this.result = result;
		this.sender = sender;
	}
	
	public WhoHasResourceResponseMsg (WhoHasResourceRequestMsg request, boolean result, ActorRef sender) {
		this.resource_name = request.getResourceName();
		this.result = result;
		this.sender = sender;
	}
	
	public String getResourceName () {
		return resource_name;
	}
	
	public boolean getResult () {
		return result;
	}
	
	public ActorRef getSender () {
		return sender;
	}
	
	@Override public String toString () {
		return "I" + (result ? " have " : " do not have ") + resource_name;
	}
}
