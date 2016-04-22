package paxosProject;

import paxosProject.network.*;
import paxosProject.network.messages.*;

import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Iterator;

public class Test {
	NodeIdentifier clientID = new NodeIdentifier(NodeIdentifier.Role.CLIENT,  0);
    NodeIdentifier proposerID = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,1);
	NodeIdentifier acceptorID = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,  2);
    NodeIdentifier learnerID = new NodeIdentifier(NodeIdentifier.Role.LEARNER,3);
    NodeIdentifier serverID = acceptorID;

	public static void main(String []args) throws Exception {
		Test test = new Test();
		test.simpleTest(args[0]);
	}

	private void simpleTest(String configFile) throws Exception {
		Configuration.initConfiguration(configFile);
		Configuration.showNodeConfig();

		Configuration.addActiveLogger("NettyNetwork", SimpleLogger.INFO);
		Configuration.addNodeAddress(clientID, new InetSocketAddress("localhost", 2001));
        //Configuration.addNodeAddress(proposerID, new InetSocketAddress("localhost", 3001));
        Configuration.addNodeAddress(learnerID, new InetSocketAddress("localhost", 4001));
        Configuration.addNodeAddress(acceptorID, new InetSocketAddress("localhost", 5001));

		//TestClient client = new TestClient(clientID);
		//TestServer server = new TestServer(serverID);

        // Initializing the Various players of Protocol

        Client client = new Client(clientID);
        //Proposer proposer = new Proposer(proposerID);
        Acceptor acceptor = new Acceptor(acceptorID);
        Learner learner = new Learner(learnerID);
        client.sendRequest(new Request(clientID, 200), proposerID);

		//client.sendValue(serverID, 200);
		//server.sendValue(serverID, 100);
	}
	
	class TestClient implements EventHandler {
		NodeIdentifier myID = null;
		Network network = null;
		public TestClient(NodeIdentifier myID){
			this.myID = myID;
			network = new NettyNetwork(myID, this);
		}

		public void sendValue(NodeIdentifier receiver, int value){
			System.out.printf("server send %s => %s\n", new Accepted(myID, value), receiver);
			network.sendMessage(receiver, new Accepted(myID, value));
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg instanceof Accepted) {
				System.out.printf("%s receive %s\n", myID, (Accepted) msg);
			}
		}

		@Override
		public void handleTimer() {
		}

		@Override
		public void handleFailure(NodeIdentifier node, Throwable cause) {
			if (cause instanceof ClosedChannelException) {
				System.out.printf("%s handleFailure get %s\n", myID, cause);
			}
		}
	}

	class TestServer implements EventHandler {
		NodeIdentifier myID = null;
		Network network = null;
		public TestServer(NodeIdentifier myID){
			this.myID = myID;
			network = new NettyNetwork(myID, this);
		}
		
		public void sendValue(NodeIdentifier receiver, int value){
			System.out.printf("server send %s => %s\n", new Accepted(myID, value), receiver);
			network.sendMessage(receiver, new Accepted(myID, value));
		}
		
		@Override
		public void handleMessage(Message msg) {
			if (msg instanceof Accepted) {
				System.out.printf("%s receive %s\n", myID, (Accepted) msg);
			}
		}

		@Override
		public void handleTimer() {

		}

		@Override
		public void handleFailure(NodeIdentifier node, Throwable cause) {
			if (cause instanceof ClosedChannelException) {
				System.out.printf("%s handleFailure get %s\n", myID, cause);
			}
		}
	}

    public class Client implements EventHandler {

        private NodeIdentifier myID;
        private Network network;

        public Client(NodeIdentifier node) {
            this.myID = node;
            this.network = new NettyNetwork(myID, this);
        }

        public void sendRequest(Request request, NodeIdentifier receiver) {
            System.out.printf(myID.toString() + " : Sending Request ( %s )  to  %s\n", request.toString(), receiver.toString());
            network.sendMessage(receiver, request);
        }

        public void sendRequest(int value, NodeIdentifier receiver) {
            Request request = new Request(myID, value);
            System.out.printf(myID.toString() + " : Sending Request ( %s )  to  %s\n", request.toString(), receiver.toString());
            network.sendMessage(receiver, request);
        }

    /*
     * Handle a message from another node
     * Client is expected to receive Responses from the Learners
     */

        @Override
        public void handleMessage(Message msg) {

            if (msg instanceof Response) {
                System.out.printf(myID.toString() +" : Response Received from : %s => %d\n", msg.getSender().toString(), msg.getValue());
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

    public class Acceptor implements EventHandler {

        private NodeIdentifier myID;
        private Network network;
        private int maxProposalID;

        public Acceptor(NodeIdentifier node) {
            this.myID = node;
            this.network = new NettyNetwork(myID, this);
            this.maxProposalID = 0;
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
        private int quorum = 0;

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

        public void sendAccept(NodeIdentifier receiver,Accept acceptMsg) {
            this.network.sendMessage(receiver, acceptMsg);
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

        } else {
            System.out.printf("Unknown msg type received : " + msg.toString() + "\n");
            System.out.printf(msg.getSender().toString() + " " + msg.getValue() );
            //throw new RuntimeException("Unknown msg type received : " + msg.toString() );
        }
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

}
