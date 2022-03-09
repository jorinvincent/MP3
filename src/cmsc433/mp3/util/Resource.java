package cmsc433.mp3.util;

import cmsc433.mp3.enums.ResourceStatus;

/**
 * Class of resources.
 * 
 * @author Rance Cleaveland
 *
 */
public class Resource {
	public final String name;	// Resource name
	private volatile ResourceStatus status = ResourceStatus.ENABLED;
	
	/**
	 * Creates new resource with given name, and default status of ENABLED.
	 * @param name	Name of resource
	 */
	public Resource (String name) {
		this.name = name;
	}
	
	/**
	 * @return	Name of resource
	 */
	public String getName() {
		return name;
	}


	/**
	 * @return Status of resource
	 */
	public ResourceStatus getStatus() {
		return status;
	}
	
	/**
	 * Change resource status to ENABLED.
	 */
	public void enable() {
		status = ResourceStatus.ENABLED;
	}
	
	/**
	 * Change resource status to DISABLED.
	 */
	public void disable () {
		status = ResourceStatus.DISABLED;
	}
}
