package paxosProject.network.messages;

import paxosProject.network.NodeIdentifier;

public class Accept extends Message {

    protected Accept(){}

    public Accept(NodeIdentifier sender){
        this.setSender(sender.hashCode());
        this.setType(2);
    }

}
