package paxosProject.network;

import paxosProject.Configuration;
import paxosProject.network.messages.*;

import java.nio.channels.ClosedChannelException;
import java.util.*;

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
    private int slot;
    private int maxSlot;
    private List<Accepted> acceptedList;
    private List<Prepare> prepareList;
    private static  int max_capacity = 1000;

    public Network getNetwork(){
        return  this.network;
    }

    public static Comparator<Accept> getComparator(){
        Comparator<Accept> acceptComparator = new Comparator<Accept>() {
            @Override
            public int compare(Accept a1, Accept a2) {
                if ( a2.getSlot() > a1.getSlot()){
                    return -1;
                } else if (a1.getSlot() > a2.getSlot()) {
                    return 1;
                } else {
                    if ( a2.getProposalID() > a1.getProposalID() ){
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        };
        return acceptComparator;
    }


    public Acceptor(NodeIdentifier node) {
        this.myID = node;
        this.network = new NettyNetwork(myID, this);
        this.maxProposalID = 0;
        this.proposalID = node.getID();
        this.quorum = 0;
        this.leader = STATUS.NONLEADER.ordinal();
        numOfAcceptors = Configuration.numAcceptors;
        slot = -1;
        maxSlot = -1;
        Comparator<Accept> acceptComparator = getComparator();
        acceptedList = new ArrayList<>(max_capacity);
        prepareList = new ArrayList<>(max_capacity);
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
                promise = new Promise(myID,Promise.STATUS.ACCEPT, proposalId,acceptedList);
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
                maxSlot = Math.max(accept.getSlot(),maxSlot);
            } else {
                // Accept
                accepted = new Accepted(myID, accept, Accepted.STATUS.ACCEPTED);
                acceptedList.add(accepted);
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

