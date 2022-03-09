package cmsc433.mp3.messages;

public class WhoHasResourceRequestMsg {	
	private final String resource_name;
	
	public WhoHasResourceRequestMsg (String resource) {
		this.resource_name = resource;
	}
	
	public String getResourceName () {
		return resource_name;
	}
	
	@Override 
	public String toString () {
		return "Who has " + resource_name + "?";
	}
}
