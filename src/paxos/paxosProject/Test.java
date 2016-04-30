package paxosProject;

import paxosProject.network.*;
import paxosProject.network.messages.Accepted;
import paxosProject.network.messages.Message;
import paxosProject.network.messages.Request;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;

public class Test {
	NodeIdentifier acceptorID1 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR, 1);
	NodeIdentifier acceptorID2 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,2);
	NodeIdentifier acceptorID3 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,3);
	NodeIdentifier learnerID = new NodeIdentifier(NodeIdentifier.Role.LEARNER, 4);
	NodeIdentifier clientID = new NodeIdentifier(NodeIdentifier.Role.CLIENT, 0);

	public static void main(String[] args) throws Exception {
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
		Configuration.addNodeAddress(acceptorID1, new InetSocketAddress("localhost", 5001));
		Configuration.addNodeAddress(acceptorID2, new InetSocketAddress("localhost", 6001));
		Configuration.addNodeAddress(acceptorID3, new InetSocketAddress("localhost", 7001));

		//TestClient client = new TestClient(clientID);
		//TestServer server = new TestServer(serverID);

		// Initializing the Various players of Protocol

		Client client = new Client(clientID);
		//Proposer proposer = new Proposer(proposerID);
		Acceptor acceptor1 = new Acceptor(acceptorID1);
		Acceptor acceptor2 = new Acceptor(acceptorID2);
		Acceptor acceptor3 = new Acceptor(acceptorID3);

		Learner learner = new Learner(learnerID);
		client.sendRequest(new Request(clientID, 200), acceptorID1);

		//client.sendValue(serverID, 200);
		//server.sendValue(serverID, 100);
	}

	class TestClient implements EventHandler {
		NodeIdentifier myID = null;
		Network network = null;

		public TestClient(NodeIdentifier myID) {
			this.myID = myID;
			network = new NettyNetwork(myID, this);
		}

		public void sendValue(NodeIdentifier receiver, int value) {
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

		public TestServer(NodeIdentifier myID) {
			this.myID = myID;
			network = new NettyNetwork(myID, this);
		}

		public void sendValue(NodeIdentifier receiver, int value) {
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

}