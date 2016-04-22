package paxosProject.network.messages;

import paxosProject.network.NodeIdentifier;

public class Accept extends Message {

    protected Accept(){}

    public int proposalID;

    public Accept(NodeIdentifier sender, int proposalId){
        this.setSender(sender.hashCode());
        this.setType(2);
        this.proposalID = proposalId;
    }

    public int getProposalID(){
        return this.proposalID;
    }

}
