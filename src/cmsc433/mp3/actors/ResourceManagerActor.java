package cmsc433.mp3.actors;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Queue;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.AbstractActor.Receive;
import cmsc433.mp3.enums.*;
import cmsc433.mp3.messages.*;
import cmsc433.mp3.util.*;
import akka.actor.AbstractActor;

public class ResourceManagerActor extends AbstractActor {
	
	private ActorRef logger;					// Actor to send logging messages to
	
	private Set<ActorRef> remoteManagers = new HashSet<ActorRef>();
	private Set<ActorRef> localUsers = new HashSet<ActorRef>();
	private Map<String, Resource> localResources = new HashMap<String, Resource>();
	private Map<String, Queue<AccessRequestMsg>> resourceQueue = new HashMap<String, Queue<AccessRequestMsg>>();
	private Map<String, List<ActorRef>> resourceReads = new HashMap<String, List<ActorRef>>();
	private Map<String, ActorRef> resourceWrites = new HashMap<String, ActorRef>();
	private Map<ActorRef, Map<String, AccessRequestMsg>> resourceRequestMessages = new HashMap<ActorRef, Map<String, AccessRequestMsg>>();
	private Map<String, ActorRef> remoteResourceList = new HashMap<String, ActorRef>();
	private Map<AccessRequestMsg, Integer> potentialRemoteResourceList = new HashMap<AccessRequestMsg, Integer>();

	/**
	 * Props structure-generator for this class.
	 * @return  Props structure
	 */
	static Props props (ActorRef logger) {
		return Props.create(ResourceManagerActor.class, logger);
	}
	
	/**
	 * Factory method for creating resource managers
	 * @param logger			Actor to send logging messages to
	 * @param system			Actor system in which manager will execute
	 * @return					Reference to new manager
	 */
	public static ActorRef makeResourceManager (ActorRef logger, ActorSystem system) {
		ActorRef newManager = system.actorOf(props(logger));
		return newManager;
	}
	
	/**
	 * Sends a message to the Logger Actor
	 * @param msg The message to be sent to the logger
	 */
	public void log (LogMsg msg) {
		logger.tell(msg, getSelf());
	}
	
	/**
	 * Constructor
	 * 
	 * @param logger			Actor to send logging messages to
	 */
	private ResourceManagerActor(ActorRef logger) {
		super();
		this.logger = logger;
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(Object.class, this::onReceive)
				.build();
	}

	// You may want to add data structures for managing local resources and users, storing
	// remote managers, etc.
	//
	// REMEMBER:  YOU ARE NOT ALLOWED TO CREATE MUTABLE DATA STRUCTURES THAT ARE SHARED BY
	// MULTIPLE ACTORS!
	
	/* (non-Javadoc)
	 * 
	 * You must provide an implementation of the onReceive() method below.
	 * 
	 * @see akka.actor.AbstractActor#createReceive
	 */
	
	public void onReceive(Object msg) throws Exception {		
		if (msg instanceof AddRemoteManagersRequestMsg) {
			AddRemoteManagersRequestMsg message = (AddRemoteManagersRequestMsg) msg;
		}
		else if (msg instanceof AddLocalUsersRequestMsg) {
			AddLocalUsersRequestMsg message = (AddLocalUsersRequestMsg) msg;
		}
		else if (msg instanceof AddInitialLocalResourcesRequestMsg) {
			AddInitialLocalResourcesRequestMsg message = (AddInitialLocalResourcesRequestMsg) msg;
		}
		else if (msg instanceof AccessRequestMsg) {
			AccessRequestMsg message = (AccessRequestMsg) msg;
			AccessRequest request = message.getAccessRequest();
			ActorRef requestSender = message.getReplyTo();
			String resourceName = request.getResourceName();
			AccessRequestType requestType = request.getType();

			Queue<AccessRequestMsg> accessRequests;

			log(LogMsg.makeAccessRequestReceivedLogMsg(requestSender, getSelf(), request));

			if (localResources.containsKey(resourceName)) {
				if (requestType == AccessRequestType.CONCURRENT_READ_BLOCKING || requestType == AccessRequestType.EXCLUSIVE_WRITE_BLOCKING) {
					accessRequests = resourceQueue.get(resourceName);
					if (accessRequests == null) {
						accessRequests = new LinkedList<AccessRequestMsg>();
					}
					accessRequests.add(message);
					resourceQueue.put(resourceName, accessRequests);

					handleBlockingAccessRequests(resourceName);
				}
				else {
					if (resourceQueue.get(resourceName) == null || resourceQueue.get(resourceName).isEmpty()) {
						handleNonBlockingAccessRequests(message);
					} else {
						log(LogMsg.makeAccessRequestDeniedLogMsg(requestSender, getSelf(), request, AccessRequestDenialReason.RESOURCE_BUSY));
						requestSender.tell(new AccessRequestDeniedMsg(request, AccessRequestDenialReason.RESOURCE_BUSY), getSelf());
					}
				}
			}
			else if (remoteResourceList.containsKey(resourceName)) {
				ActorRef remoteManager = remoteResourceList.get(resourceName);

				log(LogMsg.makeAccessRequestForwardedLogMsg(getSelf(), remoteManager, request));
				remoteManager.tell(message, getSelf());
			}
			else {
				WhoHasResourceRequestMsg whoHasResourceRequestMessage = new WhoHasResourceRequestMsg(resourceName);

				potentialRemoteResourceList.put(message, remoteManagers.size());
				for (ActorRef manager : remoteManagers) {
					if(!resourceRequestMessages.containsKey(manager)) {
						resourceRequestMessages.put(manager, new HashMap<String, AccessRequestMsg>());
					}
					resourceRequestMessages.get(manager).put(resourceName, message);
					manager.tell(whoHasResourceRequestMessage, getSelf());
				}
			}
		}
		else if (msg instanceof AccessReleaseMsg) {
			AccessReleaseMsg message = (AccessReleaseMsg) msg;
		}
		else if (msg instanceof WhoHasResourceRequestMsg) {
			WhoHasResourceRequestMsg message = (WhoHasResourceRequestMsg) msg;
			String resourceName = message.getResourceName();

			if (localResources.containsKey(resourceName)) {
				WhoHasResourceResponseMsg responseMessage = new WhoHasResourceResponseMsg(resourceName, true, getSelf());
				getSender().tell(responseMessage, getSelf());
			}
			else {
				WhoHasResourceResponseMsg responseMessage = new WhoHasResourceResponseMsg(resourceName, false, getSelf());
				getSender().tell(responseMessage, getSelf());
			}
		}
		else if (msg instanceof WhoHasResourceResponseMsg) {
			WhoHasResourceResponseMsg message = (WhoHasResourceResponseMsg) msg;
			String resourceName = message.getResourceName();
			ActorRef sender = message.getSender();
			Boolean result = message.getResult();

			AccessRequestMsg requestMessage = resourceRequestMessages.get(sender).get(resourceName);
			AccessRequest request = requestMessage.getAccessRequest();
			ActorRef requestSender = requestMessage.getReplyTo();

			if (result) {
				log(LogMsg.makeRemoteResourceDiscoveredLogMsg(getSelf(), sender, resourceName));
				remoteResourceList.put(resourceName, sender);

				log(LogMsg.makeAccessRequestForwardedLogMsg(getSelf(), sender, request));
				potentialRemoteResourceList.remove(requestMessage);
				resourceRequestMessages.get(sender).remove(resourceName);
				sender.tell(requestMessage, getSelf());
			}
			else {
				Integer count = potentialRemoteResourceList.get(requestMessage);

				if (count != 0) {
					count--;
					potentialRemoteResourceList.put(requestMessage, count);
				}
				if (count == 0) {
					log(LogMsg.makeAccessRequestDeniedLogMsg(requestSender, getSelf(), request, AccessRequestDenialReason.RESOURCE_NOT_FOUND));
					requestSender.tell(new AccessRequestDeniedMsg(request, AccessRequestDenialReason.RESOURCE_NOT_FOUND), getSelf());
				}
			}
		}
	}



	private void handleBlockingAccessRequests(String resourceName) {
		Queue<AccessRequestMsg> accessRequests = resourceQueue.get(resourceName);

		List<ActorRef> users;

		while (!accessRequests.isEmpty()) {
			AccessRequestMsg message = accessRequests.peek();
			AccessRequest request = message.getAccessRequest();
			ActorRef requestSender = message.getReplyTo();
			AccessRequestType requestType = request.getType();

			if (requestType == AccessRequestType.CONCURRENT_READ_BLOCKING) {
				if (exclusiveWriteAvailable(resourceName, requestSender)) {
					users = resourceReads.get(resourceName);
					if (users == null) {
						users = new LinkedList<ActorRef>();
					}
					users.add(requestSender);
					resourceReads.put(resourceName, users);
					accessRequests.poll();
					
					log(LogMsg.makeAccessRequestGrantedLogMsg(requestSender, self(), request));
					requestSender.tell(new AccessRequestGrantedMsg(message), getSelf());
				}
			}
			else if (requestType == AccessRequestType.EXCLUSIVE_WRITE_BLOCKING) {
				if (exclusiveWriteAvailable(resourceName, requestSender) && concurrentReadAvailable(resourceName, requestSender)) {
					resourceWrites.put(resourceName, requestSender);
					accessRequests.poll();

					log(LogMsg.makeAccessRequestGrantedLogMsg(requestSender, getSelf(), request));
					requestSender.tell(new AccessRequestGrantedMsg(message), getSelf());
				}
			}
		}
	}

	private void handleNonBlockingAccessRequests(AccessRequestMsg message) {
		AccessRequest request = message.getAccessRequest();
		ActorRef requestSender = message.getReplyTo();
		String resourceName = request.getResourceName();
		AccessRequestType requestType = request.getType();

		List<ActorRef> users;

		if (requestType == AccessRequestType.CONCURRENT_READ_NONBLOCKING) {
			if (exclusiveWriteAvailable(resourceName, requestSender)) {
				users = resourceReads.get(resourceName);
				if (users == null) {
					users = new LinkedList<ActorRef>();
				}
				users.add(requestSender);
				resourceReads.put(resourceName, users);

				log(LogMsg.makeAccessRequestGrantedLogMsg(requestSender, getSelf(), request));
				requestSender.tell(new AccessRequestGrantedMsg(message), getSelf());
			} else {
				log(LogMsg.makeAccessRequestDeniedLogMsg(requestSender, getSelf(), request, AccessRequestDenialReason.RESOURCE_BUSY));
				requestSender.tell(new AccessRequestDeniedMsg(request, AccessRequestDenialReason.RESOURCE_BUSY), getSelf());
			}
		}
		else if (requestType == AccessRequestType.EXCLUSIVE_WRITE_NONBLOCKING) {
			if (exclusiveWriteAvailable(resourceName, requestSender) && concurrentReadAvailable(resourceName, requestSender)) {
				resourceWrites.put(resourceName, requestSender);

				log(LogMsg.makeAccessRequestGrantedLogMsg(requestSender, getSelf(), request));
				requestSender.tell(new AccessRequestGrantedMsg(message), getSelf());
			} else {
				log(LogMsg.makeAccessRequestDeniedLogMsg(requestSender, getSelf(), request, AccessRequestDenialReason.RESOURCE_BUSY));
				requestSender.tell(new AccessRequestDeniedMsg(request, AccessRequestDenialReason.RESOURCE_BUSY), getSelf());
			}
		}
	}

	private boolean concurrentReadAvailable(String resourceName, ActorRef user) {
		if (resourceReads.containsKey(resourceName)) {
			for (ActorRef actor : resourceReads.get(resourceName)) {
				if (!actor.equals(user))
					return false;
			}
		}
		return true;
	}

	private boolean exclusiveWriteAvailable(String resourceName, ActorRef user) {
		if (resourceWrites.containsKey(resourceName)) {
			if (!resourceWrites.get(resourceName).equals(user)) {
				return false;
			}
		}
		return true;
	}

	

}





