package paxosProject.network;

import paxosProject.Configuration;
import paxosProject.network.messages.*;

import java.nio.channels.ClosedChannelException;
import java.util.Iterator;

public class Proposer implements EventHandler {

    /*
    The functions of Proposer are

        a) Receive values from Clients
        b) Propose/Prepare the client value to other nodes ( Acceptors).
        c) Receive their Promises/Rejections
        d) If Majority of them promise send Accept
        e) Receive Accepted from the Acceptors.

     */

    private NodeIdentifier myID;
    private  Network network;
    private int proposalID;

    public Proposer(NodeIdentifier node){
        this.myID = node;
        this.network = new NettyNetwork(myID, this);
        this.proposalID = 0;
    }

    /*
    Proposer needs to send prepare message to every one
     */
    public void sendPrepare(NodeIdentifier receiver, Prepare prepareMsg){
        this.network.sendMessage(receiver,prepareMsg);
    }

    /*
     * Handle a message from another node
     */

    @Override
    public void handleMessage(Message msg){

        // Check for Clients requests
        if ( msg instanceof Request){

            Iterator<NodeIdentifier> iter = Configuration.acceptorIDs.values().iterator();
            NodeIdentifier receiver;
            Prepare prepareMsg;
            while ( iter.hasNext()){
                receiver = (NodeIdentifier)iter.next();
                prepareMsg = new Prepare(myID, proposalID);
                prepareMsg.setValue(msg.getValue());
                sendPrepare(receiver, prepareMsg);
            }
            proposalID += 1;

        } else if (msg instanceof Promise){
            System.out.printf("Proposer: Received Ack( %s) from %s\n", msg.toString(),msg.getSender() );

        } else if (msg instanceof Accepted) {

        } else {
            throw new RuntimeException("Unknown msg type received : " + msg.toString() );
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
