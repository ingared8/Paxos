package paxosProject.network;

import paxosProject.Configuration;
import paxosProject.network.messages.*;

import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    private int numOfProposers;
    private int leaderStatus;
    private NodeIdentifier leaderNode;
    private int slot;
    private int heartbeatCount = 0;
    private int waitCount = 0;
    private boolean ping_option = true;

    // Variables to store the intermediate state of the program
    private Map<Integer,Integer> quorumMap = new HashMap<>();
    private Map<Integer,Integer> promiseMap = new HashMap<>();

    public enum STATUS{
        NON_LEADER,LEADER
    }

    protected Proposer(){}

    public void setLeaderNode(){

        if ( leaderNode == null) {
            leaderNode = Configuration.proposerIDs.get(1);
            if (myID.getID() == 1) {
                leaderStatus = STATUS.LEADER.ordinal();
            } else {
                leaderStatus = STATUS.NON_LEADER.ordinal();
            }
            System.out.println(myID.toString() + " is " + STATUS.values()[leaderStatus]);
            return;
        }


        int i=leaderNode.getID();
        System.out.println(myID.toString() + " : Calling Leader function and Current LeaderId is " + i );
        boolean active = true;
        NodeIdentifier newLeader = leaderNode;
        i += 1;

        while (active ) {
            try {
                newLeader = Configuration.proposerIDs.get(Math.max(1,i%(1+Configuration.numProposers)));
                active = false;
            } catch (Exception e){
                i+=1;
            }
        }

        if (newLeader.hashCode() == myID.hashCode()){
            System.out.println(myID.toString() + "Gaining my Leadeship");
            leaderStatus = STATUS.LEADER.ordinal();
            leaderNode = newLeader;
            broadcastLeadership();
        } else if (myID.hashCode() == leaderNode.hashCode()){
            System.out.println(myID.toString() + "Dropping my Leadeship");
            leaderStatus = STATUS.NON_LEADER.ordinal();
            ping_option = false;
            waitCount = 0;
        } else {
            System.out.println(myID.toString() + "waiting for message from Leadeship");
            leaderStatus = STATUS.NON_LEADER.ordinal();
            ping_option = false;
            waitCount = 0;
        }

    }

    private void broadcastLeadership() {

        System.out.println(myID.toString() + "Broadcasting my leadership");
        Iterator<NodeIdentifier> iter = Configuration.proposerIDs.values().iterator();
        Leadership leadership = new Leadership(myID);
        while(iter.hasNext()){
            NodeIdentifier receiver = (NodeIdentifier)iter.next();
            network.sendMessage(receiver,leadership);
        }
    }

    public void setLeaderNode1(NodeIdentifier node){
        leaderNode = myID;
        leaderStatus = STATUS.LEADER.ordinal();
    }

    public Proposer(NodeIdentifier node){
        this.myID = node;
        this.network = new NettyNetwork(myID, this);
        this.proposalID = node.getID();
        this.leaderStatus = STATUS.NON_LEADER.ordinal();
        numOfProposers = Configuration.numProposers;
        setLeaderNode();
        heartbeat();
    }

    private void heartbeat() {
        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (leaderStatus == STATUS.NON_LEADER.ordinal() && ping_option){
                    //System.out.println(myID.toString() + " Pinging leader *** " + leaderNode.toString());
                    try {
                        network.sendMessage(leaderNode, new HeartBeat(myID, heartbeatCount));
                        heartbeatCount += 1;
                        Thread.sleep(1l);
                        if (heartbeatCount%500 == 0){
                            System.out.println(myID.toString() + " Pinging leader " + leaderNode.toString());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
        System.out.println(myID.toString() + "HearBeat Thread starting ");
        thread.start();
        //thread.run();
    }

    /*
    Proposer needs to send prepare message to every one
    */

    public void leaderElection(){
        Prepare prepare = new Prepare(myID,proposalID);
        Iterator<NodeIdentifier> iter = Configuration.acceptorIDs.values().iterator();
        NodeIdentifier receiver;
        Accept accept;
        while ( iter.hasNext()){
            receiver = (NodeIdentifier)iter.next();
            sendPrepare(receiver, prepare);
            promiseMap.put(proposalID,0);
        }
        proposalID += numOfProposers;
    }

    public void sendPrepare(NodeIdentifier receiver, Prepare prepareMsg){
        this.network.sendMessage(receiver, prepareMsg);
    }


    /*
     * Handle a message from another node
     */

    @Override
    public void handleMessage(Message msg){
        if (leaderStatus == STATUS.LEADER.ordinal()) {
            if (msg instanceof Promise) {
                Promise promise = (Promise)msg;
                System.out.println(myID + " received " + ((Promise) msg).toString());
                if (promise.getstatus() != Promise.STATUS.REJECT.ordinal() && (promiseMap.containsKey(promise.getProposalID()))) {
                    int value = promiseMap.get(promise.getProposalID()) + 1;
                    promiseMap.remove(promise.getProposalID());
                    promiseMap.put(promise.getProposalID(), value);
                    if (value >= (1 + Configuration.numAcceptors / 2)) {
                        this.proposalID += numOfProposers;
                    }
                }
                if (promise.getstatus() == Promise.STATUS.REJECT.ordinal() && (promiseMap.containsKey(promise.getProposalID()))) {
                        promiseMap.remove(promise.getProposalID());
                }

                if (promise.getstatus() == Promise.STATUS.REJECT.ordinal()){
                    setLeaderNode();
                }
            } else if (msg instanceof Request){
                Iterator<NodeIdentifier> iter = Configuration.acceptorIDs.values().iterator();
                NodeIdentifier receiver;
                Accept accept = new Accept(myID,proposalID,slot,(Request)msg);
                slot += 1;
                quorumMap.put(accept.getProposalID(),0);
                while ( iter.hasNext()){
                    receiver = (NodeIdentifier)iter.next();
                    this.network.sendMessage(receiver, accept);
                }
            } else if (msg instanceof HeartBeat) {
                HeartBeat heartBeatMessage = (HeartBeat)msg;
                NodeIdentifier sender = heartBeatMessage.getSender();
                this.network.sendMessage(sender, new HeartBeatReply(sender,heartBeatMessage));
                heartbeatCount += 1;
                if (heartbeatCount%1000 == 0){
                    System.out.println(myID.toString() + " replying to ping from " + sender.toString() + " for HeartBeat " + heartBeatMessage.getCount());
                }
            } else if (msg instanceof Accepted) {
                Accepted accepted = (Accepted)msg;
                System.out.println(myID.toString() + " received " + accepted.toString());
                if (accepted.getStatus() == Accepted.STATUS.ACCEPTED.ordinal() && (quorumMap.containsKey(accepted.getProposalID()))) {
                    System.out.println("PromiseMap " + promiseMap.toString());
                    int value = quorumMap.get(accepted.getProposalID()) + 1;
                    quorumMap.remove(accepted.getProposalID());
                    quorumMap.put(accepted.getProposalID(), value);
                    if (value >= (1 + Configuration.numAcceptors/2)) {
                        System.out.println(myID.toString()+ " Sending Learn to learners for " + accepted.toString());
                        quorumMap.remove(accepted.getProposalID());
                        this.sendLearn(accepted);
                    }
                }
                if (accepted.getStatus() == Accepted.STATUS.REJECTED.ordinal() && (quorumMap.containsKey(accepted.getProposalID()))) {
                    quorumMap.remove(accepted.getProposalID());
                }
            } else {
                System.out.printf("Unknown msg type received : " + msg.toString() + "\n");
                //throw new RuntimeException("Unknown msg type received : " + msg.toString() );
            }
        } else {

            if (msg instanceof Request){
                Request request = (Request)msg;
                NodeIdentifier sender = request.getSender();
                request.setSender(leaderNode);
                this.network.sendMessage(sender,request);
            } else if (msg instanceof HeartBeatReply) {
                if (((HeartBeatReply) msg).getCount()%500 == 0){
                    System.out.println(myID.toString() + " recieved a reply for a ping from " + msg.getSender().toString() + " for hearbeat: " + ((HeartBeatReply) msg).getCount());
                    waitCount = 0;
                }
            } else if (msg instanceof HeartBeat) {
                waitCount = -100;
            } else if (msg instanceof Leadership){
                leaderStatus = STATUS.NON_LEADER.ordinal();
                leaderNode = msg.getSender();
                this.waitCount = 0;
                System.out.printf(myID.toString() + "Leadership message received : " + msg.toString() + "\n");
            } else {
                System.out.printf(myID.toString() + "Unknown msg type received : " + msg.toString() + "\n");
            }
        }
    }

    private void sendLearn(Accepted accepted) {
        Learn learn = new Learn(myID,accepted);
        Iterator<NodeIdentifier> iter = Configuration.learnerIDs.values().iterator();
        NodeIdentifier receiver;
        while ( iter.hasNext()){
            receiver = (NodeIdentifier)iter.next();
            System.out.println(myID.toString() + ": Sending Learn to  " + receiver.toString() + " " + learn.toString());
            network.sendMessage(receiver,learn);
        }
    }

    /*
	 * Handle a timer event. A timer event is triggered if
	 * there is no other event in a given amount of time (100ms).
	 */

    @Override
    public void handleTimer() throws InterruptedException {

        // Check for whether is active
        if (leaderStatus == STATUS.NON_LEADER.ordinal() && ping_option) {
            waitCount += 1;
            if (leaderStatus != STATUS.LEADER.ordinal() && waitCount >= 100) {
                System.out.println(myID.toString() + " WaitCount" + waitCount + ": Not received a ping from leader");
                setLeaderNode();
                waitCount = 0;
            }
        }
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

