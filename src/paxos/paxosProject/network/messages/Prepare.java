package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import paxosProject.Configuration;
import paxosProject.network.NodeIdentifier;

public class Prepare extends Message {

    private int proposalID;

    protected Prepare(){}

    public Prepare(NodeIdentifier sender,int proposalID){
        super(MSG_TYPE.PREPARE,sender);
        this.proposalID = proposalID;
    }

    public int getProposalID(){
        return proposalID;
    }

    private void setProposalID(int newProposalId){
        this.proposalID = newProposalId;
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeInt(proposalID);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        proposalID = buf.readInt();
    }

    public String toString(){
        return ("Sender: " + getSender().toString() + " ||| Type: " + getSender().toString() + " ||| Proposal ID: " + proposalID);
    }

    public static void main(String[] args){

        NodeIdentifier nodeIdentifier1 = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,1);
        NodeIdentifier nodeIdentifier2 = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,2);
        Prepare prepare1 = new Prepare(nodeIdentifier1,1);
        Prepare prepare2 = new Prepare(nodeIdentifier2,2);

        ByteBuf bb;
        PooledByteBufAllocator allocator = new PooledByteBufAllocator();
        bb = allocator.buffer(Configuration.MAX_MSG_SIZE);

        System.out.println(" Before Serializing");
        System.out.println("Client Send: " + prepare1.toString());
        System.out.println("Client Recv: " + prepare2.toString());

        prepare1.serialize(bb);
        System.out.println(bb.toString());

        System.out.println(" After de serializing ");
        prepare2.deserialize(bb);
        System.out.println("Client Send: " + prepare1.toString());
        System.out.print("Client Recv: " + prepare2.toString());
    }
}
