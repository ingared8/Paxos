package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

public class Accept extends Message {

    private int key;
    private int Value;
    private int requestID;
    private int proposalID;
    private int slot;

    protected Accept(){}

    public Accept(NodeIdentifier sender, int proposalId, int slot, int id, int key, int value){
        this.setSender(sender.hashCode());
        this.setType(MSG_TYPE.ACCEPT.ordinal());
        this.proposalID = proposalId;
        this.slot = slot;
        this.requestID = id;
        this.key = key;
        this.Value = value;
    }

    public Accept(NodeIdentifier sender,int proposalID, int slot,Request request){
        this.setSender(sender.hashCode());
        this.setType(MSG_TYPE.ACCEPT.ordinal());
        this.proposalID = proposalID;
        this.slot = slot;
        this.requestID = request.getId();
        this.key = request.getKey();
        this.Value = request.getValue();
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeInt(proposalID);
        buf.writeInt(slot);
        buf.writeInt(requestID);
        buf.writeInt(key);
        buf.writeInt(Value);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        proposalID = buf.readInt();
        slot = buf.readInt();
        requestID = buf.readInt();
        key = buf.readInt();
        Value = buf.readInt();
    }

    public String toString() {
        return ("Sender: " + getSender().toString() + " ||| Type: " + MSG_TYPE.values()[getType()] + " ||| ProposalID: "
                + proposalID + " ||| Slot: " + slot + " ||| Key: " + key + " ||| Value: " + Value);
    }

    public int getProposalID(){
        return this.proposalID;
    }

    public int getKey() { return this.key; }
    public int getRequestID() { return this.requestID; }
    public int getSlot() { return this.slot; }
    public int getvalue() { return this.Value;}

}
