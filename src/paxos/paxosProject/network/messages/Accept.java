package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import paxosProject.Configuration;
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

    public void deserialize_accept(ByteBuf buf) {
        this.setSender(buf.readInt());
        this.setType(buf.readInt());
        this.proposalID= (buf.readInt());
        this.slot = buf.readInt();
        this.requestID = buf.readInt();
        this.key = buf.readInt();
        this.Value = buf.readInt();
    }

    public void serialize_accept(ByteBuf buf, Accept accept) {
        buf.writeInt(accept.getType());
        buf.writeInt(accept.hashCode());
        buf.writeInt(accept.getProposalID());
        buf.writeInt(accept.getSlot());
        buf.writeInt(accept.getRequestID());
        buf.writeInt(accept.getKey());
        buf.writeInt(accept.getValue());
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
        try {
            return ("Sender: " + getSender().toString() + " ||| Type: " + MSG_TYPE.values()[getType()] + " ||| ProposalID: "
                    + proposalID + " ||| Slot: " + slot + " ||| Key: " + key + " ||| Value: " + Value);
        } catch (Exception e){
            e.printStackTrace();
            return "Error ";
        }
    }

    public int getProposalID(){
        return this.proposalID;
    }
    public int getKey() { return this.key; }
    public int getRequestID() { return this.requestID; }
    public int getSlot() { return this.slot; }
    public int getvalue() { return this.Value;}

    public static  void main(String[] args){

        NodeIdentifier nodeIdentifier1 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,1);
        NodeIdentifier nodeIdentifier2 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,2);
        Accept accept1 = new Accept(nodeIdentifier1,1,2,3,4,5);
        Accept accept2 = new Accept(nodeIdentifier2,6,7,8,9,10);
        Accept accept3 = new Accept(nodeIdentifier2,10,11,12,13,14);


        ByteBuf bb;
        PooledByteBufAllocator allocator = new PooledByteBufAllocator();
        bb = allocator.buffer(Configuration.MAX_MSG_SIZE);

        System.out.println(" Before Serializing");
        System.out.println("Client Send: " + accept1.toString());
        System.out.println("Client Recv: " + accept2.toString());

        accept1.serialize(bb);
        System.out.println(bb.toString());

        System.out.println(" After de serializing ");
        accept2.deserialize(bb);
        System.out.println("Client Send: " + accept1.toString());
        System.out.print("Client Recv: " + accept2.toString());

    }
}


