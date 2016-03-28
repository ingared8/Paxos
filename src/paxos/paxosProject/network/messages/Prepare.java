package paxosProject.network.messages;

import paxosProject.network.NodeIdentifier;

public class Prepare extends Message {

    protected Prepare(){}

    public Prepare(NodeIdentifier sender){
        this.setSender(sender.hashCode());
        this.setType(0);
    }

}
