package paxosProject.network;

import paxosProject.Configuration;
import paxosProject.network.messages.Leadership;
import paxosProject.network.messages.Message;
import paxosProject.network.messages.Request;
import paxosProject.network.messages.Response;

import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.DelayQueue;

public class Client implements EventHandler {

    private NodeIdentifier myID;
    private Network network;
    private int requestID;
    private long starttime;
    private int numOfClients;
    NodeIdentifier leader;
    private DelayQueue<Request> requestQueue;
    private Map<Integer,Request> requestMap;
    private Map<Integer,Integer> receiveMap;

    public Client(NodeIdentifier node) {
        this.myID = node;
        this.network = new NettyNetwork(myID, this);
        this.requestID = node.getID();
        requestQueue = new DelayQueue<Request>();
        requestMap = new HashMap<>();
        leader = Configuration.proposerIDs.get(1);
    }

    public void sendRequest(Request request, NodeIdentifier receiver) {
        //System.out.printf(myID.toString() + " : Sending Request ( %s )  to  %s\n", request.toString(), receiver.toString());
        network.sendMessage(receiver, request);
        requestQueue.add(request);
        requestMap.put(request.getId(), request);
    }

    public synchronized void sendRequest(int key, int value, NodeIdentifier receiver) {
        Request request = new Request(myID,requestID, key, value);
        sendRequest(request, receiver);
        requestID += Configuration.numClients;
    }

    public synchronized void sendRequest(int key, int value) {
        Request request = new Request(myID,requestID, key, value);
        sendRequest(request,leader);
        requestID += Configuration.numClients;
    }

    public void sendRequest(Request request) {
        //System.out.printf(myID.toString() + " : Sending Request ( %s )  to  %s\n", request.toString(), leader.toString());
        network.sendMessage(leader, request);
        requestQueue.add(request);
        requestMap.put(request.getId(), request);
    }


    /*
     * Handle a message from another node
     * Client is expected to receive Responses from the Learners
     */

    @Override
    public void handleMessage(Message msg) {

        if (msg instanceof Response) {
            System.out.printf(myID.toString() + " : Response Received from : %s => %d for Request: %d \n", msg.getSender().toString(), msg.getValue(), ((Response) msg).getRequestID());
            Request req = (Request)requestMap.get(((Response) msg).getRequestID());
            if (requestQueue.contains(req)) {
                requestQueue.remove(req);
            }
        } else if (msg instanceof Request) {
            // Message needs to be directed towards the leader of proposer
            Request req = (Request) msg;
            NodeIdentifier proposalLeader = req.getSender();
            req.setSender(myID.hashCode());
            sendRequest(req, proposalLeader);
        } else if (msg instanceof Leadership) {
            leader = msg.getSender();
            System.out.println(myID.toString() + ": Updating my Leader  to " + leader.toString());
        } else {
            System.out.printf(myID.toString() + " : Response Received of Unknown type:  %s\n", msg.toString());
        }
    }

    /*
     * Handle a timer event. A timer event is triggered if
     * there is no other event in a given amount of time (100ms).
     */

    @Override
    public void handleTimer() {
        //System.out.print(requestQueue.size());
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

    public void reTransmitMessages(){

        Thread thread = new Thread(){
            @Override
            public void run(){
                while (true){
                    try {
                        Thread.sleep(Configuration.pingTimeout);
                        if (!requestQueue.isEmpty()){
                            Request request = requestQueue.poll();
                            Request req = new Request(myID,request.getId(),request.getKey(),request.getValue());
                            sendRequest(req);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }
}