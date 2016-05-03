package paxosProject.network;

import paxosProject.Configuration;
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
    private int numOfClients;
    private DelayQueue<Request> requestQueue;
    private Map<Integer,Request> requestMap;

    public Client(NodeIdentifier node) {
        this.myID = node;
        this.network = new NettyNetwork(myID, this);
        this.requestID = node.getID();
        requestQueue = new DelayQueue<Request>();
        requestMap = new HashMap<>();
    }

    public void sendRequest(Request request, NodeIdentifier receiver) {
        System.out.printf(myID.toString() + " : Sending Request ( %s )  to  %s\n", request.toString(), receiver.toString());
        network.sendMessage(receiver, request);
        requestQueue.add(request);
        requestMap.put(request.getId(), request);
    }

    public void sendRequest(int key, int value, NodeIdentifier receiver) {
        Request request = new Request(myID,requestID, key, value);
        sendRequest(request,receiver);
        requestID += Configuration.numClients;
    }

    /*
     * Handle a message from another node
     * Client is expected to receive Responses from the Learners
     */

    @Override
    public void handleMessage(Message msg) {

        if (msg instanceof Response) {
            System.out.printf(myID.toString() + " : Response Received from : %s => %d\n", msg.getSender().toString(), msg.getValue());
            Request req = (Request)requestMap.get(((Response) msg).getRequestID());
            if (requestQueue.contains(req)) {
                requestQueue.remove(req);
            }
        } else if (msg instanceof Request) {
            // Message needs to be directed towards the leader of proposer
            Request req = (Request)msg;
            NodeIdentifier proposalLeader = req.getSender();
            req.setSender(myID.hashCode());
            sendRequest(req, proposalLeader);

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
}