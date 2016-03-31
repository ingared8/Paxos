package paxosProject.network;

import paxosProject.network.messages.Accepted;
import paxosProject.network.messages.Message;
import paxosProject.network.messages.Response;

import java.nio.channels.ClosedChannelException;

/**
 * Created by ingared on 3/30/16.
 */
public class Learner implements EventHandler {

    private NodeIdentifier myID;
    private  Network network;

    public Learner(NodeIdentifier node){

        this.myID = node;
        this.network = new NettyNetwork(myID, this);

    }

    public void sendValue(NodeIdentifier receiver, int value){
        System.out.printf("server send %s => %s\n", new Accepted(myID, value), receiver);
        network.sendMessage(receiver, new Accepted(myID, value));
    }

    /*
     * Handle a message from another node
     * Client is expected to receive Responses from the Learners
     */

    @Override
    public void handleMessage(Message msg){

        if (msg instanceof Response) {
            System.out.printf("Response Received from : %s => %d\n", msg.getSender().toString(), msg.getValue());
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
