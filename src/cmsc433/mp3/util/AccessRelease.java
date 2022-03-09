package cmsc433.mp3.util;

import cmsc433.mp3.enums.AccessType;

/**
 * An access release which users can make.
 * 
 * @author Rance Cleaveland
 *
 */

public class AccessRelease {
	
	private final String resourceName;
	private final AccessType type;
	
	public AccessRelease (String resourceName, AccessType type) {
		this.resourceName = resourceName;
		this.type = type;
	}
	
	public String getResourceName () {
		return resourceName;
	}
	
	public AccessType getType () {
		return type;
	}
	
	public String toString () {
		return "Release " + type.toString() + " access to " + resourceName;
	}
}
