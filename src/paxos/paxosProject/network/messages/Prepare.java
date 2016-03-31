package paxosProject.network.messages;

import paxosProject.network.NodeIdentifier;

public class Prepare extends Message {

    private int proposalID;

    protected Prepare(){}

    public Prepare(NodeIdentifier sender,int proposalID){
        this.setSender(sender.hashCode());
        this.setType(0);
        this.proposalID = proposalID;
    }

    private int getProposalID(){
        return proposalID;
    }

    private void setProposalID(int newProposalId){
        this.proposalID = newProposalId;
    }

}
