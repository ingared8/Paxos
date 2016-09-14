package paxosProject.network;

import paxosProject.Configuration;
import paxosProject.network.messages.*;

import java.nio.channels.ClosedChannelException;
import java.util.*;

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
    private  boolean ping_option = true;
    private int lock=1;

    // Variables to store the intermediate state of the program
    private Map<Integer,Integer> quorumMap;
    private Map<Integer,Integer> promiseMap;
    Set<Accepted> acceptedSet;

    public enum STATUS{
        NON_LEADER,LEADER
    }

    protected Proposer(){}

    public Proposer(NodeIdentifier node){
        this.myID = node;
        this.network = new NettyNetwork(myID, this);
        this.proposalID = node.getID();
        this.leaderStatus = STATUS.NON_LEADER.ordinal();
        numOfProposers = Configuration.numProposers;
        quorumMap = new HashMap<>();
        promiseMap = new HashMap<>();
        acceptedSet = new HashSet<>();
        setLeaderNode();
        heartbeat();
    }

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

         {
            if (newLeader.hashCode() == myID.hashCode()) {
                System.out.println(myID.toString() + "Gaining my Leadeship");
                leaderStatus = STATUS.LEADER.ordinal();
                leaderNode = newLeader;
                broadcastLeadership();
                broadcastPrepare();
            } else if (myID.hashCode() == leaderNode.hashCode()) {
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

    private void broadcastPrepare() {

        System.out.println(myID.toString() + "Broadcasting my Promise");
        Iterator<NodeIdentifier> iter = Configuration.acceptorIDs.values().iterator();
        Prepare prepare = new Prepare(myID,proposalID);
        acceptedSet = new HashSet<>();
        promiseMap.put(proposalID,0);
        while(iter.hasNext()){
            NodeIdentifier receiver = (NodeIdentifier)iter.next();
            network.sendMessage(receiver, prepare);
        }
        proposalID += numOfProposers;
    }

    public void setLeaderNode1(NodeIdentifier node){
        leaderNode = myID;
        leaderStatus = STATUS.LEADER.ordinal();
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
                        //if (heartbeatCount%500 == 0){
                        //    System.out.println(myID.toString() + " Pinging leader " + leaderNode.toString());
                        //}
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
                    addToset(acceptedSet,promise);
                    promiseMap.remove(promise.getProposalID());
                    promiseMap.put(promise.getProposalID(), value);
                    if (value >= (1 + Configuration.numAcceptors / 2)) {
                        this.proposalID += numOfProposers;
                        mergeSetData();
                        promiseMap.remove(promise.getProposalID());
                    }
                }
                if (promise.getstatus() == Promise.STATUS.REJECT.ordinal() && (promiseMap.containsKey(promise.getProposalID()))) {
                        promiseMap.remove(promise.getProposalID());
                        acceptedSet = new HashSet<>();
                }

                if (promise.getstatus() == Promise.STATUS.REJECT.ordinal()){
                    setLeaderNode();
                }
            } else if (msg instanceof Request){
                broadcastRequest(msg);
            } else if (msg instanceof HeartBeat) {
                HeartBeat heartBeatMessage = (HeartBeat)msg;
                NodeIdentifier sender = heartBeatMessage.getSender();
                this.network.sendMessage(sender, new HeartBeatReply(myID,heartBeatMessage));
                heartbeatCount += 1;
                //if (heartbeatCount%1000 == 0){
                //    System.out.println(myID.toString() + " replying to ping from " + sender.toString() + " for HeartBeat " + heartBeatMessage.getCount());
                //}
            } else if (msg instanceof Accepted) {
                Accepted accepted = (Accepted)msg;
                System.out.println(myID.toString() + " received " + accepted.toString() + " for Request " + accepted.getRequestID());
                if (accepted.getStatus() == Accepted.STATUS.ACCEPTED.ordinal() && (quorumMap.containsKey(accepted.getRequestID()))) {
                    System.out.println("PromiseMap " + promiseMap.toString());
                    int value = quorumMap.get(accepted.getRequestID()) + 1;
                    quorumMap.remove(accepted.getRequestID());
                    quorumMap.put(accepted.getRequestID(), value);
                    if (value >= (1 + Configuration.numAcceptors/2)) {
                        System.out.println(myID.toString()+ " Sending Learn to learners for " + accepted.toString());
                        quorumMap.remove(accepted.getRequestID());
                        this.sendLearn(accepted);
                    }
                }
                if (accepted.getStatus() == Accepted.STATUS.REJECTED.ordinal() && (quorumMap.containsKey(accepted.getRequestID()))) {
                    quorumMap.remove(accepted.getRequestID());
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
                HeartBeatReply reply = (HeartBeatReply)msg;
                waitCount = 0;
                //if (((HeartBeatReply) msg).getCount()%500 == 0){
                //    System.out.println(myID.toString() + " recieved a reply for a ping from " + reply.getSender().toString() + " for hearbeat: " + ((HeartBeatReply) msg).getCount());
                //    waitCount = 0;
                //}
            } else if (msg instanceof HeartBeat) {
                waitCount = -100;
            } else if (msg instanceof Leadership){
                leaderStatus = STATUS.NON_LEADER.ordinal();
                leaderNode = msg.getSender();
                this.waitCount = 0;
                ping_option = true;
                System.out.printf(myID.toString() + "Leadership message received : " + msg.toString() + "\n");
                heartbeat();
            } else {
                System.out.printf(myID.toString() + "Unknown msg type received : " + msg.toString() + "\n");
            }
        }
    }

    private void broadcastRequest(Message msg) {
        Iterator<NodeIdentifier> iter = Configuration.acceptorIDs.values().iterator();
        NodeIdentifier receiver;
        Accept accept = new Accept(myID,proposalID,slot,(Request) msg);
        slot += 1;
        quorumMap.put(accept.getRequestID(),0);
        while ( iter.hasNext()){
            receiver = (NodeIdentifier)iter.next();
            this.network.sendMessage(receiver, accept);
        }
    }

    private synchronized  void updateMap(Map<Integer,Integer> map, int key, int value ){
        map.put(key,value);
    }

    private synchronized int[] getMaxProposalIdAndMaxSlotId(Set<Accepted> acceptedSet){
        Iterator<Accepted> iter = acceptedSet.iterator();
        int slotMax = 0;
        int propMax = 0;
        while (iter.hasNext()){
            Accepted accepted = (Accepted)iter.next();
            slotMax = (accepted.getSlot() > slotMax ? accepted.getSlot():slotMax );
            propMax = (accepted.getProposalID() > propMax ? accepted.getProposalID():propMax );
        }

        int proposlId = myID.getID();
        System.out.println(myID.toString() + " : PropMax " + propMax + " Slot Max is " + slotMax);
        while (propMax >= proposlId){
            proposlId += numOfProposers;
        }
        slotMax += 1;
        return (new int[] {slotMax, proposlId});
    }

    private synchronized void mergeSetData() {
        Queue<Accepted> acceptedQueue = new PriorityQueue<>(1000,Functions.getAcceptedComparator());
        Iterator<Accepted> iter =acceptedSet.iterator();
        while (iter.hasNext()){
            Accepted accepted = (Accepted)iter.next();
            acceptedQueue.add(accepted);
        }

        int[] res = getMaxProposalIdAndMaxSlotId(acceptedSet);
        System.out.println(myID.toString() + " Slot is " + res[0] + " proposalId " + res[1]);
        slot = res[0];
        proposalID = res[1];
    }

    private synchronized void addToset(Set<Accepted> acceptSet, Promise promise) {

        Iterator<Accepted> iter = promise.getAcceptedList().iterator();
        while (iter.hasNext()){
            acceptedSet.add((Accepted)iter.next());
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
            if (leaderStatus != STATUS.LEADER.ordinal() && waitCount >= 40) {
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

