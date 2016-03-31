package paxosProject.network;

import paxosProject.network.messages.Message;
import paxosProject.network.messages.Prepare;
import paxosProject.network.messages.Promise;

import java.nio.channels.ClosedChannelException;

public class Acceptor implements EventHandler {

    private NodeIdentifier myID;
    private Network network;
    private int proposalID;

    public Acceptor(NodeIdentifier node){
        this.myID = node;
        this.network = new NettyNetwork(myID, this);
        this.proposalID = 0;
    }

    public void sendPromise(NodeIdentifier receiver){
        Promise promiseMsg = new Promise(myID,Promise.STATUS.PROMISE);

        System.out.printf("Acceptor: Sending Promise (%s) to Proposer %s\n", promiseMsg, receiver);
        network.sendMessage(receiver,promiseMsg);
    }

    /*
     * Handle a message from another node
     * Client is expected to receive Responses from the Learners
     */

    @Override
    public void handleMessage(Message msg){

        if (msg instanceof Prepare) {
            // TODO Check proposal Id of the message
            System.out.printf("Acceptor: Proposal ( %s) Received from : %s\n", msg.getSender().toString(), msg.toString());

        } else {
            System.out.printf("Response Received of Unknown type:  %s\n", msg.toString());
        }
    }

    /*
	 * Handle a timer event. A timer event is triggered if
	 * there is no other event in a given amount of time (100ms).
	 */
    @Override
    public void handleTimer(){

    }

    /*
     * Handle a failure event. A failure event is triggered
     * if the corresponding connection is broken.
     */
    // should catch ClosedChannelException for test purpose

    @Override
    public void handleFailure(NodeIdentifier node, Throwable cause){

        if (cause instanceof ClosedChannelException){
            System.out.printf("%s handleFailure get %s\n", myID, cause);
        }
    }

}
