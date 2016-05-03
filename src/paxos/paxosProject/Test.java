package paxosProject;

import paxosProject.network.*;

import java.util.HashMap;
import java.util.Map;

public class Test {

	//NodeIdentifier acceptorID1 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR, 1);
	//NodeIdentifier acceptorID2 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,2);
	//NodeIdentifier acceptorID3 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,3);
	//NodeIdentifier learnerID = new NodeIdentifier(NodeIdentifier.Role.LEARNER, 4);

	public static void main(String[] args) throws Exception {
		Test test = new Test();
		test.simpleTest(args[0]);
	}

	private void simpleTest(String configFile) throws Exception {
		Configuration.initConfiguration(configFile);
		Configuration.showNodeConfig();

        NodeIdentifier proposerID = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,1);
        NodeIdentifier clientID = Configuration.clientIDs.get(1);

		Configuration.addActiveLogger("NettyNetwork", SimpleLogger.INFO);

        //Configuration.addNodeAddress(clientID, new InetSocketAddress("localhost", 4001));
        //Configuration.addNodeAddress(proposerID, new InetSocketAddress("localhost", 5001));

        //System.out.println(Configuration.getNodeAddress(clientID));
        //System.out.println(Configuration.getNodeAddress(proposerID));

        //Configuration.addNodeAddress(proposerID, new InetSocketAddress("localhost", 3001));
		//Configuration.addNodeAddress(learnerID, new InetSocketAddress("localhost", 4001));
		//Configuration.addNodeAddress(acceptorID1, new InetSocketAddress("localhost", 5001));
		//Configuration.addNodeAddress(acceptorID2, new InetSocketAddress("localhost", 6001));
		//Configuration.addNodeAddress(acceptorID3, new InetSocketAddress("localhost", 7001));

		//TestClient client = new TestClient(clientID);
		//TestServer server = new TestServer(serverID);

		// Initializing the Various players of Protocol

        Map<Integer,Client> clientMap = new HashMap<>();

        for (Map.Entry<Integer, NodeIdentifier> entry: Configuration.clientIDs.entrySet()){
            clientMap.put(entry.getKey(),new Client(entry.getValue()));
        }

        Map<Integer,Proposer> proposerMap = new HashMap<>();
        for (Map.Entry<Integer, NodeIdentifier> entry: Configuration.proposerIDs.entrySet()){
            proposerMap.put(entry.getKey(),new Proposer(entry.getValue()));
        }

        Map<Integer,Acceptor> acceptorMap =new HashMap<>();
        for (Map.Entry<Integer, NodeIdentifier> entry: Configuration.acceptorIDs.entrySet()){
            acceptorMap.put((Integer)entry.getKey(),new Acceptor((NodeIdentifier)entry.getValue()));
        }

        Map<Integer,Learner> learnerMap = new HashMap<>();
        for(Map.Entry<Integer, NodeIdentifier> entry: Configuration.learnerIDs.entrySet()){
            learnerMap.put((Integer)entry.getKey(),new Learner((NodeIdentifier)entry.getValue()));
        }

        Client client = clientMap.get(1);
        //Thread.sleep(200);
        //proposerMap.get(1).setLeaderNode();

        //proposerMap.get(1).setLeaderNode();

        //proposerMap.get(1).setLeaderNode();

        /*
        Simple Test
         */
        //client.sendRequest(10,20,Configuration.proposerIDs.get(1));

		/*
		To Test whether a non Leader can act can entertain any clients requests
		 */
		//client.sendRequest(30,40,Configuration.proposerIDs.get(2));
	}

}