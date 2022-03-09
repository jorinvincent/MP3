package cmsc433.mp3.messages;

import akka.actor.ActorRef;
import cmsc433.mp3.enums.AccessRequestDenialReason;
import cmsc433.mp3.enums.ResourceStatus;
import cmsc433.mp3.util.AccessRelease;
import cmsc433.mp3.util.AccessRequest;

/**
 * Messages to be sent to logger.
 * 
 * @author Rance Cleaveland
 *
 */
public class LogMsg {

	public static enum EventType {
		/* Logged by a UserActor when it starts*/
		USER_START,  
		/* Logged by a UserActor when it terminates */
		USER_TERMINATE, 
		
		/* Logged by a ResourceManagerActor when new local resource is created 
		 * (this happens only during the configuration phase of the program) */
		LOCAL_RESOURCE_CREATED,
		/* Logged by a ResourceManagerActor when it discovers the manager of a 
		 * remote resource for the first time */
		REMOTE_RESOURCE_DISCOVERED,  
		
		/* Logged by a ResourceManagerActor when it receives an access request 
		 * (regardless of whether it came directly from a user or from another 
		 * ResourceManagerActor) */
		ACCESS_REQUEST_RECEIVED,
		/* Logged by a ResourceManagerActor when it forwards this request to 
		 * another ResourceManagerActor */
		ACCESS_REQUEST_FORWARDED,
		/* Logged by a ResourceManagerActor when it grants an access request */
		ACCESS_REQUEST_GRANTED,
		/* Logged by a ResourceManagerActor when it denies an access request */
		ACCESS_REQUEST_DENIED, 
		
		/* Logged by a ResourceManagerActor when it receives an access release 
		 * (regardless of whether it came directly from a user or from another 
		 * ResourceManagerActor) */
		ACCESS_RELEASE_RECEIVED, 
		/* Logged by a ResourceManagerActor when it forwards an access release 
		 * to another ResourceManagerActor */
		ACCESS_RELEASE_FORWARDED,
		/* Logged by a ResourceManagerActor when an access release has been
		 * processed for a resource it managers. Should only be logged if the
		 * access release was valid (i.e. the user held access prior to the
		 * release). */
		ACCESS_RELEASED,
		/* Logged by a ResourceManagerActor when an invalid access release has
		 * been ignored. */
		ACCESS_RELEASE_IGNORED,
	}
	
	// Static methods for constructing log messages

	/**
	 * 
	 * @param user The user actor that has started up.
	 * @return A LogMsg indicating the user has started.
	 */
	public static LogMsg makeUserStartLogMsg (ActorRef user) {
		return new LogMsg(EventType.USER_START, user, null, null, null, null, null, null, null);
	}
	
	/**
	 * 
	 * @param user The user actor that is terminating.
	 * @return A LogMsg indicating the user has terminated.
	 */
	public static LogMsg makeUserTerminateLogMsg (ActorRef user) {
		return new LogMsg(EventType.USER_TERMINATE, user, null, null, null, null, null, null, null);
	}
	
	/**
	 * 
	 * @param local_resource_manager The resource manager actor who owns this new resource.
	 * @param resource_name The name of the resource being created
	 * @return A LogMsg indicating that the resource has been created.
	 */
	public static LogMsg makeLocalResourceCreatedLogMsg (ActorRef local_resource_manager, String resource_name) {
		return new LogMsg(EventType.LOCAL_RESOURCE_CREATED, null, local_resource_manager, null, resource_name, null, null, null, null);
	}
	
	/**
	 * 
	 * @param local_resource_manager The resource manager that just discovered that a resource exists
	 * @param remote_resource_manager The owner of the resource that has just been discovered.
	 * @param resource_name The name of the resource that was discovered
	 * @return A LogMsg indicating that the resource was discovered.
	 */
	public static LogMsg makeRemoteResourceDiscoveredLogMsg (ActorRef local_resource_manager, ActorRef remote_resource_manager, String resource_name) {
		return new LogMsg(EventType.REMOTE_RESOURCE_DISCOVERED, null, local_resource_manager, remote_resource_manager, resource_name, null, null, null, null);
	}
	
	/**
	 * 
	 * @param user The user actor that originally sent this request
	 * @param local_resource_manager The resource manager that just received an access request
	 * @param access_request The AccessRequest object was received (must pass the original object, NOT A COPY)
	 * @return A LogMsg indicating that an access request has been received
	 */
	public static LogMsg makeAccessRequestReceivedLogMsg (ActorRef user, ActorRef local_resource_manager, AccessRequest access_request) {
		return new LogMsg(EventType.ACCESS_REQUEST_RECEIVED, user, local_resource_manager, null, access_request.getResourceName(), access_request, null, null, null);
	}
	
	/**
	 * 
	 * @param local_resource_manager The resource manager that is forwarding this request
	 * @param remote_resource_manager The resource manager the request is being forwarded to
	 * @param access_request The AccessRequest object corresponding to the request being forwarded (must pass the original object, NOT A COPY)
	 * @return A LogMsg indicating that an access request has been forwarded.
	 */
	public static LogMsg makeAccessRequestForwardedLogMsg (ActorRef local_resource_manager, ActorRef remote_resource_manager, AccessRequest access_request) {
		return new LogMsg(EventType.ACCESS_REQUEST_FORWARDED, null, local_resource_manager, remote_resource_manager, access_request.getResourceName(), access_request, null, null, null);
	}
	
	/**
	 * 
	 * @param user The user who is being granted access
	 * @param local_resource_manager The resource manager that is granting access 
	 * @param access_request The AccessRequest object corresponding to the request being granted (must pass the original object, NOT A COPY)
	 * @return A LogMsg indicating that an access request has been granted.
	 */
	public static LogMsg makeAccessRequestGrantedLogMsg (ActorRef user, ActorRef local_resource_manager, AccessRequest access_request) {
		return new LogMsg(EventType.ACCESS_REQUEST_GRANTED, user, local_resource_manager, null, access_request.getResourceName(), access_request, null, null, null);
	}
	
	/**
	 * 
	 * @param user The user who is being denied access
	 * @param local_resource_manager The resource manager that is denying access
	 * @param access_request The AccessRequest object corresponding to the request being denied (must pass the original object, NOT A COPY)
	 * @param access_request_denial_reason The reason this request is being denied
	 * @return A LogMsg indicating that an access request has been denied
	 */
	public static LogMsg makeAccessRequestDeniedLogMsg (ActorRef user, ActorRef local_resource_manager, AccessRequest access_request, AccessRequestDenialReason access_request_denial_reason) {
		return new LogMsg(EventType.ACCESS_REQUEST_DENIED, user, local_resource_manager, null, access_request.getResourceName(), access_request, access_request_denial_reason, null, null);
	}
	
	/**
	 * 
	 * @param sender The user actor that originally sent this request
	 * @param local_resource_manager The resource manager that just received an access release
	 * @param access_release The AccessRelease object corresponding to the access release received (must pass the original object, NOT A COPY)
	 * @return A LogMsg indicating that an access release was received
	 */
	public static LogMsg makeAccessReleaseReceivedLogMsg (ActorRef user, ActorRef local_resource_manager, AccessRelease access_release) {
		return new LogMsg(EventType.ACCESS_RELEASE_RECEIVED, user, local_resource_manager, null, access_release.getResourceName(), null, null, access_release,  null);
	}
	
	/**
	 * 
	 * @param local_resource_manager The resource manager that is forwarding this access release
	 * @param remote_resource_manager The resource manager that the access release is being forwarded to
	 * @param access_release The AccessRelesae object corresponding to the access release being forwarded (must pass the original object, NOT A COPY)
	 * @return A LogMsg indicating that an access release was forwarded
	 */
	public static LogMsg makeAccessReleaseForwardedLogMsg (ActorRef local_resource_manager, ActorRef remote_resource_manager, AccessRelease access_release) {
		return new LogMsg(EventType.ACCESS_RELEASE_FORWARDED, null, local_resource_manager, remote_resource_manager, access_release.getResourceName(), null, null, access_release, null);
	}
	
	/**
	 * Note: this event should only be logged if access is actually released; don't log this event if the user did
	 * not have access in the first place.
	 * 
	 * @param user The user who is releasing access
	 * @param local_resource_manager The resource manager who owns the resource on which access is being released
	 * @param access_release The AccessRelease object corresponding to the access release that is occurring (must pass the original object, NOT A COPY)
	 * @return A LogMsg indicating that a user has released access on a resource
	 */
	public static LogMsg makeAccessReleasedLogMsg (ActorRef user, ActorRef local_resource_manager, AccessRelease access_release) {
		return new LogMsg(EventType.ACCESS_RELEASED, user, local_resource_manager, null, access_release.getResourceName(), null, null, access_release, null);
	}

	/**
	 * 
	 * @param user The user who sent the invalid access release
	 * @param local_resource_manager The resource manager who owns the resource on which the invalid access release was attempted.
	 * @param access_release The AccessRelease object corresponding to the access release that is being ignored (must pass the original object, NOT A COPY)
	 * @return A LogMsg indicating a resource manager has ignored an access release on a resource
	 */
	public static LogMsg makeAccessReleaseIgnoredLogMsg (ActorRef user, ActorRef local_resource_manager, AccessRelease access_release) {
		return new LogMsg(EventType.ACCESS_RELEASE_IGNORED, user, local_resource_manager, null, access_release.getResourceName(), null, null, access_release, null);
	}
		
	private final EventType type;						// Type of event
	private final ActorRef user;						// User generating or involved in this event.
	private final ActorRef local_resource_manager; 		// The resource manager generating this event.
	private final ActorRef remote_resource_manager;		
	private final String resource_name;					// The name of the resource involved in this event.
	private final AccessRequest access_request;
	private final AccessRequestDenialReason access_request_denial_reason;
	private final AccessRelease access_release;
	private final ResourceStatus new_resource_status;
	
	
	
	private LogMsg(EventType type, ActorRef user, ActorRef local_resource_manager, 
			ActorRef remote_resource_manager, String resource_name, AccessRequest access_request,
			AccessRequestDenialReason access_request_denial_reason, AccessRelease access_release,
			ResourceStatus new_resource_status) {
		this.type = type;
		this.user = user;
		this.local_resource_manager = local_resource_manager;
		this.remote_resource_manager = remote_resource_manager;
		this.resource_name = resource_name;
		this.access_request = access_request;
		this.access_request_denial_reason = access_request_denial_reason;
		this.access_release = access_release;
		this.new_resource_status = new_resource_status;
	}
	
	public EventType getType() {
		return type;
	}

	public ActorRef getUser() {
		return user;
	}
	
	public ActorRef getLocalResourceManager () {
		return local_resource_manager;
	}
	
	public ActorRef getRemoteResourceManager () {
		return remote_resource_manager;
	}
	
	public String getResourceName () {
		return resource_name;
	}
	
	public AccessRequest getAccessRequest () {
		return access_request;
	}
	
	public AccessRequestDenialReason getAccessRequestDenialReason () {
		return access_request_denial_reason;
	}
	
	public AccessRelease getAccessRelease () {
		return access_release;
	}
	
	public ResourceStatus getNewResourceStatus () {
		return new_resource_status;
	}
	
	
	@Override public String toString() {
		if (type == EventType.USER_START) {
			return "User Starting: " + actorRefToString(user);
		} else if (type == EventType.USER_TERMINATE) {
			return "User Terminating: " + actorRefToString(user);
		} else if (type == EventType.LOCAL_RESOURCE_CREATED) {
			return resource_name + " was added to " + actorRefToString(local_resource_manager);
		} else if (type == EventType.REMOTE_RESOURCE_DISCOVERED) {
			return actorRefToString(local_resource_manager) + " discovered that " + resource_name + " is managed by " + actorRefToString(remote_resource_manager);
		} else if (type == EventType.ACCESS_REQUEST_RECEIVED) {
			return actorRefToString(local_resource_manager) + " received " + access_request.toString() + " from " + actorRefToString(user);
		} else if (type == EventType.ACCESS_REQUEST_FORWARDED) {
			return actorRefToString(local_resource_manager) + " forwarded " + access_request.toString() + " to " + actorRefToString(remote_resource_manager);
		} else if (type == EventType.ACCESS_REQUEST_GRANTED) {
			return actorRefToString(local_resource_manager) + " granted " + access_request.toString() + " to " + actorRefToString(user);
		} else if (type == EventType.ACCESS_REQUEST_DENIED) {
			return actorRefToString(local_resource_manager) + " denied " + access_request.toString() + " to " + actorRefToString(user) + " because " + access_request_denial_reason.toString();
		} else if (type == EventType.ACCESS_RELEASE_RECEIVED) {
			return actorRefToString(local_resource_manager) + " received " + access_release.toString()  + " from " + actorRefToString(user);
		} else if (type == EventType.ACCESS_RELEASE_FORWARDED) {
			return actorRefToString(local_resource_manager) + " forwarded " + access_release.toString() + " to " + actorRefToString(remote_resource_manager);
		} else if (type == EventType.ACCESS_RELEASED) {
			return actorRefToString(user) + " released " + access_release.getType().toString() + " access to " + resource_name + " (managed by " + actorRefToString(local_resource_manager) + ")"; 
		} else if (type == EventType.ACCESS_RELEASE_IGNORED) {
			return actorRefToString(user) + "'s release of " + access_release.getType().toString() + " access to" + resource_name + " (managed by " + actorRefToString(local_resource_manager) + ") was ignored";
		} else {
			throw new AssertionError ("Unrecognized Event Type: " + type);
		}
		
	}
	
	private static String actorRefToString (ActorRef actorRef) {
		return actorRef.path().name();
	}
}
