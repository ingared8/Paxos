package paxosProject.network;

import paxosProject.Configuration;
import paxosProject.network.messages.Learn;
import paxosProject.network.messages.Message;
import paxosProject.network.messages.Response;

import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ingared on 3/30/16.
 */

public class Learner implements EventHandler {

    private NodeIdentifier myID;
    private Network network;
    private List<Learn> learnedValues;
    private int index;

    public Learner(NodeIdentifier node){
        this.myID = node;
        this.network = new NettyNetwork(myID, this);
        learnedValues = new LinkedList<>();
        index = -1;
    }

    /*
     * Handle a message from another node
     * Client is expected to receive Responses from the Learners
     */

    @Override
    public void handleMessage(Message msg){

        if (msg instanceof Learn) {
            Learn learn = (Learn)msg;
            Response response = new Response(myID,learn);
            learnedValues.add(learn.getSlot(),learn);
            network.sendMessage(Configuration.clientIDs.get(Math.max(1,(learn.getRequestID()%Configuration.numClients))),response);

        } else if (msg instanceof Response) {
            System.out.printf("%s: Response Received from : %s => %d\n", myID.toString(), msg.getSender().toString(), msg.getValue());
        } else {
            System.out.printf(" %s : Response Received of Unknown type:  %s\n",myID.toString(), msg.toString());
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
