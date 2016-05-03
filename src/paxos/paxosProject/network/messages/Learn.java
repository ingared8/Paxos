package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import paxosProject.Configuration;
import paxosProject.network.NodeIdentifier;

/**
 * Created by ingared on 5/1/16.
 */

public class Learn extends Message{

    private int status;
    private int proposalID;
    private int slot;
    private int requestID;
    private int key;
    private int value;

    protected Learn(){}

    public Learn(NodeIdentifier sender,Accepted accepted) {
        this.setSender(sender.hashCode());
        this.setType(MSG_TYPE.LEARN.ordinal());
        this.status = accepted.getStatus();
        this.proposalID = accepted.getProposalID();
        this.slot = accepted.getSlot();
        this.requestID = accepted.getRequestID();
        this.key = accepted.getKey();
        this.value = accepted.getvalue();
    }

    public int getProposalID(){return proposalID;}
    public int getRequestID(){return this.requestID;}
    public int getSlot(){ return this.slot;}

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeInt(proposalID);
        buf.writeInt(status);
        buf.writeInt(slot);
        buf.writeInt(requestID);
        buf.writeInt(value);
        buf.writeInt(key);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        proposalID = buf.readInt();
        status = buf.readInt();
        slot = buf.readInt();
        requestID = buf.readInt();
        value = buf.readInt();
        key = buf.readInt();
    }

    public String toString() {
        return ("Sender: " + getSender().toString() + " ||| Type: " + MSG_TYPE.values()[getType()] + " ||| ProposalID: "
                + proposalID + " ||| Status: " + Accepted.STATUS.values()[status] +" ||| Slot: " + slot + " ||| Key: " + key + " ||| Value: " + value);
    }

    public static void main(String[] args){

        NodeIdentifier nodeIdentifier1 = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,1);
        NodeIdentifier nodeIdentifier2 = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,2);

        Accept accept1 = new Accept(nodeIdentifier1,1,2,3,10,20);
        Accept accept2 = new Accept(nodeIdentifier2,4,5,6,30,40);

        Accepted accepted1 = new Accepted(nodeIdentifier1,accept1, Accepted.STATUS.ACCEPTED);
        Accepted accepted2 = new Accepted(nodeIdentifier2,accept2, Accepted.STATUS.REJECTED);

        Learn learn1 = new Learn(nodeIdentifier1,accepted1);
        Learn learn2 = new Learn(nodeIdentifier2,accepted2);

        ByteBuf bb;
        PooledByteBufAllocator allocator = new PooledByteBufAllocator();
        bb = allocator.buffer(Configuration.MAX_MSG_SIZE);

        System.out.println(" Before Serializing");
        System.out.println("Client Send: " + learn1.toString());
        System.out.println("Client Recv: " + learn2.toString());

        learn1.serialize(bb);
        System.out.println(bb.toString());

        System.out.println(" After de serializing ");
        learn2.deserialize(bb);
        System.out.println("Client Send: " + learn1.toString());
        System.out.print("Client Recv: " + learn2.toString());
    }

}


