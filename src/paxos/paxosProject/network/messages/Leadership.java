package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import paxosProject.Configuration;
import paxosProject.network.NodeIdentifier;

/**
 * Created by ingared on 5/3/16.
 */
public class Leadership extends  Message {

    private int count;

    protected Leadership(){}

    public Leadership(NodeIdentifier sender){
        this.setType(MSG_TYPE.LEADERSHIP.ordinal());
        this.setSender(sender);
    }



    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
    }

    public String toString(){
        try {
            return ("Sender: " + getSender().toString() + " ||| Type: " + Message.MSG_TYPE.values()[getType()] );
        } catch (Exception e){
            return "Null";
        }
    }

    public static void main(String[] args){

        NodeIdentifier nodeIdentifier1 = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,1);
        NodeIdentifier nodeIdentifier2 = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,2);

        Leadership l1 = new Leadership(nodeIdentifier1);
        Leadership l2 = new Leadership(nodeIdentifier2);

        ByteBuf bb;
        PooledByteBufAllocator allocator = new PooledByteBufAllocator();
        bb = allocator.buffer(Configuration.MAX_MSG_SIZE);

        System.out.println(" Before Serializing");
        System.out.println("Client Send: " + l1.toString());
        System.out.println("Client Recv: " + l2.toString());

        l1.serialize(bb);
        System.out.println(bb.toString());

        System.out.println(" After de serializing ");
        l2.deserialize(bb);
        System.out.println("Client Send: " + l1.toString());
        System.out.print("Client Recv: " + l2.toString());
    }
}
