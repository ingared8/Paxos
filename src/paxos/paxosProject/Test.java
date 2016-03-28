package paxosProject;

import paxosProject.network.EventHandler;
import paxosProject.network.NettyNetwork;
import paxosProject.network.Network;
import paxosProject.network.NodeIdentifier;
import paxosProject.network.messages.Accepted;
import paxosProject.network.messages.Message;

import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;

public class Test {
	NodeIdentifier clientID = new NodeIdentifier(NodeIdentifier.Role.CLIENT,  0);
	NodeIdentifier serverID = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,  1);

	public static void main(String []args) throws Exception {
		Test test = new Test();
		test.simpleTest(args[0]);
	}

	private void simpleTest(String configFile) throws Exception {
		Configuration.initConfiguration(configFile);
		Configuration.showNodeConfig();

		Configuration.addActiveLogger("NettyNetwork", SimpleLogger.INFO);
		Configuration.addNodeAddress(clientID, new InetSocketAddress("localhost", 2001));
		Configuration.addNodeAddress(serverID, new InetSocketAddress("localhost", 5001));
		TestClient client = new TestClient(clientID);
		TestServer server = new TestServer(serverID);
		client.sendValue(serverID, 200);
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
}
