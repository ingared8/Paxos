package paxosProject.network;

import paxosProject.network.messages.Message;
import paxosProject.network.messages.Request;
import paxosProject.network.messages.Response;

import java.nio.channels.ClosedChannelException;

public class Client implements EventHandler   {

    private NodeIdentifier myID;
    private  Network network;

    public Client(NodeIdentifier node){
        this.myID = node;
        this.network = new NettyNetwork(myID, this);
    }

    public void sendRequest(Request request, NodeIdentifier receiver){
        System.out.printf("Client: Sending Request ( %s )  to  %s\n", request.toString(),receiver.toString() );
        network.sendMessage(receiver, request);
    }

    public void sendRequest(int value, NodeIdentifier receiver){
        Request request = new Request(myID,value);
        System.out.printf("Client: Sending Request ( %s )  to  %s\n", request.toString(),receiver.toString() );
        network.sendMessage(receiver, request);
    }

    /*
     * Handle a message from another node
     * Client is expected to receive Responses from the Learners
     */

    @Override
    public void handleMessage(Message msg){

        if (msg instanceof Response) {
            System.out.printf("Client: Response Received from : %s => %d\n", msg.getSender().toString(), msg.getValue());
        } else {
            System.out.printf("Client: Response Received of Unknown type:  %s\n", msg.toString());
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

    public void main() {

        // Test the functionality of the Client

        NodeIdentifier clientID = new NodeIdentifier(NodeIdentifier.Role.CLIENT,1);
        NodeIdentifier proposerID = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,1);
        Client client = new Client(clientID);


        System.out.println("Client Test: Sending message");
        client.sendRequest(25,proposerID);

    }

}
