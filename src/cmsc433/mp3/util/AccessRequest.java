package cmsc433.mp3.util;

import cmsc433.mp3.enums.AccessRequestType;

/**
 * Type of access requests that users can make.
 * 
 * @author Rance Cleaveland
 *
 */
public class AccessRequest {
	
	private final String resourceName;
	private final AccessRequestType type;
	
	public AccessRequest (String resourceName, AccessRequestType type) {
		this.resourceName = resourceName;
		this.type = type;
	}

	public String getResourceName() {
		return resourceName;
	}

	public AccessRequestType getType() {
		return type;
	}
	
	@Override
	public String toString () {
		return type.toString() + " request for " + resourceName;
	}

}
