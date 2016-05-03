package paxosProject.network;

import paxosProject.network.messages.Message;

public interface EventHandler {
	/*
	 * Handle a message from another node
	 */
	public void handleMessage(Message msg);
	
	/*
	 * Handle a timer event. A timer event is triggered if
	 * there is no other event in a given amount of time (100ms).
	 */
	public void handleTimer() throws InterruptedException;
	
	/*
	 * Handle a failure event. A failure event is triggered
	 * if the corresponding connection is broken.
	 */
	// should catch ClosedChannelException for test purpose
	public void handleFailure(NodeIdentifier node, Throwable cause);
}
