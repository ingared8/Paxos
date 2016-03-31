package paxosProject.network.messages;

import paxosProject.network.NodeIdentifier;

public class Request extends  Message {

    protected Request(){}

    /*
    Any request is constructed from the client and  desired value it requests
    */

    public Request(NodeIdentifier sender, int value){
        this.setSender(sender.hashCode());
        this.setType(2);
        this.setValue(value);
    }
}

