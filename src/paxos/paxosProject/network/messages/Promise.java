package paxosProject.network.messages;

import paxosProject.network.NodeIdentifier;

public class Promise extends Message {

    protected Promise() {}

    public Promise(NodeIdentifier sender){
        this.setSender(sender.hashCode());
        this.setType(1);
    }

}

