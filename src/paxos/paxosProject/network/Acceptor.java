package paxosProject.network;

import paxosProject.Configuration;
import paxosProject.network.messages.*;

import java.nio.channels.ClosedChannelException;

public class Acceptor implements EventHandler {

    public enum STATUS{
        NONLEADER,LEADER
    }

    private NodeIdentifier myID;
    private Network network;
    private int maxProposalID;
    private int proposalID;
    private int quorum;
    private int leader;
    private int numOfAcceptors;

    public Network getNetwork(){
        return  this.network;
    }

    public Acceptor(NodeIdentifier node) {
        this.myID = node;
        this.network = new NettyNetwork(myID, this);
        this.maxProposalID = 0;
        this.proposalID = node.getID();
        this.quorum = 0;
        this.leader = STATUS.NONLEADER.ordinal();
        numOfAcceptors = Configuration.numAcceptors;
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




    @Override
    public void handleMessage(Message msg) {


        if (msg instanceof Prepare) {

            Prepare prepareMsg = (Prepare)msg;
            int proposalId = prepareMsg.getProposalID();
            System.out.printf("%s : %s Received from : %s\n", myID.toString(), proposalId, msg.getSender().toString());
            Promise promise;
            if (proposalId < maxProposalID){
                // Reject
                promise = new Promise(myID,Promise.STATUS.REJECT,proposalId);
            } else {
                // Accept
                promise = new Promise(myID,Promise.STATUS.ACCEPT, proposalId);
                maxProposalID = proposalId;
            }

            System.out.println(myID.toString() + " : " + promise.toString());
            network.sendMessage(prepareMsg.getSender(), promise);

        } else if (msg instanceof Accept) {
            Accept accept= (Accept)msg;
            int proposalId = accept.getProposalID();
            System.out.printf("%s : %s Received from : %s\n", myID.toString(),msg.toString(), msg.getSender().toString());
            Accepted accepted;
            if (proposalId < maxProposalID){
                // Reject
                accepted = new Accepted(myID,accept, Accepted.STATUS.REJECTED);
            } else {
                // Accept
                accepted = new Accepted(myID, accept, Accepted.STATUS.ACCEPTED);
                maxProposalID = proposalId;
            }

            System.out.println(myID.toString() + " Sending Accepted  : " + accepted.toString() );
            network.sendMessage(accept.getSender(), accepted);

        } else {
            System.out.println(myID.toString() + ": Unknown message type " + msg.toString());
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

