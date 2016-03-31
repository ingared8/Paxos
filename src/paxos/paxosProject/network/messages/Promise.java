package paxosProject.network.messages;

import paxosProject.network.NodeIdentifier;

public class Promise extends Message {

    public enum STATUS{
        PROMISE,REJECT
    }
    private int status;
    protected Promise() {}

    public Promise(NodeIdentifier sender, STATUS status){
        this.setSender(sender.hashCode());
        this.setType(1);
        this.status = status.ordinal();
    }

}

