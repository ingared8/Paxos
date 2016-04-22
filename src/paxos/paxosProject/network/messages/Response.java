package paxosProject.network.messages;

import paxosProject.network.NodeIdentifier;

/**
 * Created by ingared on 3/30/16.
 */

public class Response extends Message{

    protected Response(){}

    public Response(NodeIdentifier sender, int value){
        this.setSender(sender.hashCode());
        this.setType(5);
        this.setValue(value);
    }
}