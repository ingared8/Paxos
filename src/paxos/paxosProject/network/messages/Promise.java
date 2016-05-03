package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import paxosProject.Configuration;
import paxosProject.network.NodeIdentifier;

public class Promise extends Message {

    public enum STATUS{
        REJECT,ACCEPT
    }

    private int status;
    private int proposalID;

    protected Promise() {}

    public Promise(NodeIdentifier sender, STATUS statusmsg,int proposalId){
        this.setSender(sender.hashCode());
        this.setType(MSG_TYPE.PROMISE.ordinal());
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
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeInt(proposalID);
        buf.writeInt(status);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        proposalID = buf.readInt();
        status = buf.readInt();
    }

    public String toString() {
        return ("Sender: " + getSender().toString() + " ||| Type: " + MSG_TYPE.values()[getType()] + " ||| Status: " + STATUS.values()[status] + " for ProposalID " + proposalID);
    }

    public static void main(String[] args){

        NodeIdentifier nodeIdentifier1 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,1);
        NodeIdentifier nodeIdentifier2 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,2);
        Promise promise1 = new Promise(nodeIdentifier1,STATUS.ACCEPT,1);
        Promise promise2 = new Promise(nodeIdentifier2,STATUS.REJECT,2);

        ByteBuf bb;
        PooledByteBufAllocator allocator = new PooledByteBufAllocator();
        bb = allocator.buffer(Configuration.MAX_MSG_SIZE);

        System.out.println(" Before Serializing");
        System.out.println("Client Send: " + promise1.toString());
        System.out.println("Client Recv: " + promise2.toString());

        promise1.serialize(bb);
        System.out.println(bb.toString());

        System.out.println(" After de serializing ");
        promise2.deserialize(bb);
        System.out.println("Client Send: " + promise1.toString());
        System.out.print("Client Recv: " + promise2.toString());
    }
}

