package paxosProject.network.messages;

import paxosProject.network.NodeIdentifier;

public class Promise extends Message {

    public enum STATUS{
        ACCEPT,REJECT
    }

    private int status;
    private int proposalID;

    protected Promise() {}

    public Promise(NodeIdentifier sender, STATUS statusmsg,int proposalId){
        this.setSender(sender.hashCode());
        this.setType(1);
        this.status = statusmsg.ordinal();

        this.proposalID = proposalId;
    }

    public int getstatus(){
        return this.status;
    }

    public int getProposalID(){
        return this.proposalID;
    }

    @Override
    public String toString(){
        return (" Promise from " + getSender().toString() +" as " + STATUS.values()[status]  + " for ProposalID " + proposalID );
    }

}

