package paxosProject.network;

import paxosProject.Configuration;
import paxosProject.network.messages.*;

import java.nio.channels.ClosedChannelException;
import java.util.Iterator;

public class Acceptor implements EventHandler {

    private NodeIdentifier myID;
    private Network network;
    private int maxProposalID;
    private int proposalID;
    private int quorum;

    public Acceptor(NodeIdentifier node) {
        this.myID = node;
        this.network = new NettyNetwork(myID, this);
        this.maxProposalID = 0;
        this.proposalID = 0;
        this.quorum = 0;
    }

    public void sendPromise(NodeIdentifier receiver, int proposalId) {
        Promise promiseMsg = new Promise(myID, Promise.STATUS.ACCEPT,proposalId);

        System.out.printf("Acceptor: Sending Promise (%s) to Proposer %s\n", promiseMsg, receiver);
        network.sendMessage(receiver, promiseMsg);
    }

		/*
		 * Handle a message from another node
		 * Client is expected to receive Responses from the Learners
		 */

		/*
        Proposer needs to send prepare message to every one
         */

    public void sendPrepare(NodeIdentifier receiver, Prepare prepareMsg){
        this.network.sendMessage(receiver,prepareMsg);
    }

    public void sendAccept(NodeIdentifier receiver,Accept acceptMsg) {
        this.network.sendMessage(receiver, acceptMsg);
    }

    private void sendAcceptMsg(int proposalID) {

        Iterator<NodeIdentifier> iter = Configuration.acceptorIDs.values().iterator();
        NodeIdentifier receiver;
        Accept accept;
        while ( iter.hasNext()){
            receiver = (NodeIdentifier)iter.next();
            accept = new Accept(myID,proposalID);
            sendAccept(receiver, accept);
        }
        proposalID += 1;
    }


    @Override
    public void handleMessage(Message msg) {

        if (msg instanceof Prepare) {
            // TODO Check proposal Id of the message
            Prepare prepareMsg = (Prepare)msg;
            int proposalId = prepareMsg.getProposalID();
            System.out.printf("Acceptor: Proposal:%s Received from : %s\n", proposalId, msg.getSender().toString());
            Promise promise;
            if (proposalId < maxProposalID){
                // Reject
                promise = new Promise(myID,Promise.STATUS.REJECT,proposalId);
            } else {
                // Accept
                promise = new Promise(myID,Promise.STATUS.ACCEPT, proposalId);
            }

            System.out.println(myID.toString() + " : " + promise.toString());
            network.sendMessage(prepareMsg.getSender(), promise);

        } else {
            System.out.printf("Response Received of Unknown type:  %s\n", msg.toString());
        }

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
            Promise promise = (Promise)msg;
            quorum += promise.getstatus();
            System.out.println("status " + promise.getstatus());
            System.out.println(promise.toString() + " Quorum :" + quorum + " Total votes " + Configuration.numAcceptors/2);
            if ( quorum > (Configuration.numAcceptors)/2) {
                // Quorum reached, send accept
                System.out.println("Quorum reached ");
                sendAcceptMsg(promise.getProposalID());
            } else {
                // Quorum not reached.
                System.out.println("Quorum not reached");
                int zz = 0;
            }
        } else if (msg instanceof Accepted) {
            System.out.println(" Accepted stage reached");
        } else {
            System.out.printf("Unknown msg type received : " + msg.toString() + "\n");
            System.out.printf(msg.getSender().toString() + " " + msg.getValue() );
            //throw new RuntimeException("Unknown msg type received : " + msg.toString() );
        }


    }

    /*
     * Handle a timer event. A timer event is triggered if
     * there is no other event in a given amount of time (100ms).
     */
    @Override
    public void handleTimer() {

    }

		/*
		 * Handle a failure event. A failure event is triggered
		 * if the corresponding connection is broken.
		 */
    // should catch ClosedChannelException for test purpose

    @Override
    public void handleFailure(NodeIdentifier node, Throwable cause) {

        if (cause instanceof ClosedChannelException) {
            System.out.printf("%s handleFailure get %s\n", myID, cause);
        }
    }
}

