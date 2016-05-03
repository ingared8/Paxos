package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

public class Accepted extends Message {

	// feel free to add other variables
	private int status;
	private int proposalID;
    private int slot;
    private int requestID;
    private int key;
    private int Value;
    private long index;

	public enum STATUS{
		REJECTED,ACCEPTED
	}

	protected Accepted(){}
	
	public Accepted(NodeIdentifier sender,Accept accept,STATUS status) {
		this.setSender(sender.hashCode());
		this.setType(MSG_TYPE.ACCEPTED.ordinal());
		this.status = status.ordinal();
		this.proposalID = accept.getProposalID();
        this.slot = accept.getSlot();
        this.requestID = accept.getRequestID();
        this.key = accept.getKey();
        this.Value = accept.getvalue();
        this.index = 100;
	}


    public long getIndex(){return index;}
    public int getProposalID(){ return proposalID; }
    public int getStatus(){ return this.status; }
    public int getSlot(){ return this.slot; }
    public int getKey(){ return this.key; }
    public int getRequestID(){ return this.requestID; }
    public int getvalue(){ return this.Value;}

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeInt(proposalID);
        buf.writeInt(status);
        buf.writeInt(slot);
        buf.writeInt(requestID);
        buf.writeInt(key);
        buf.writeInt(Value);

    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        proposalID = buf.readInt();
        status = buf.readInt();
        slot = buf.readInt();
        requestID = buf.readInt();
        key = buf.readInt();
        Value = buf.readInt();
    }

    public String toString() {
        return ("Sender: " + getSender().toString() + " ||| Type: " + MSG_TYPE.values()[getType()] + " ||| ProposalID: "
                + proposalID + " ||| Status: " + STATUS.values()[status] +" ||| Slot: " + slot + " ||| Key: " + key + " ||| Value: " + Value);
    }

}
