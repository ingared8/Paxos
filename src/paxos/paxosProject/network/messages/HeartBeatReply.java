package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import paxosProject.Configuration;
import paxosProject.network.NodeIdentifier;

/**
 * Created by ingared on 5/3/16.
 */
public class HeartBeatReply extends  Message{


    protected HeartBeatReply(){}

    private int count;

    public HeartBeatReply(NodeIdentifier sender){
        this.setType(MSG_TYPE.HEARTBEATREPLY.ordinal());
        this.setSender(sender);
        count = 0;
    }

    public HeartBeatReply(NodeIdentifier sender, int heartbeatcount){
        this.setType(MSG_TYPE.HEARTBEATREPLY.ordinal());
        this.setSender(sender);
        this.count = heartbeatcount;
    }

    public HeartBeatReply(NodeIdentifier sender,HeartBeat heartBeat){
        this.setType(MSG_TYPE.HEARTBEATREPLY.ordinal());
        this.setSender(sender);
        this.count = heartBeat.getCount();
    }

    public int getCount(){
        return count;
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeInt(count);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        count = buf.readInt();
    }

    public String toString(){
        try {
            return ("Sender: " + getSender().toString() + " ||| Type: " + MSG_TYPE.values()[getType()] + " ||| Count: " + getCount());
        } catch (Exception e){
            return "Null";
        }
    }

    public static void main(String[] args){

        NodeIdentifier nodeIdentifier1 = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,1);
        NodeIdentifier nodeIdentifier2 = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,2);
        HeartBeatReply heartBeatMessage1 = new HeartBeatReply(nodeIdentifier1,100);
        HeartBeatReply heartBeatMessage2 = new HeartBeatReply(nodeIdentifier2,200);

        ByteBuf bb;
        PooledByteBufAllocator allocator = new PooledByteBufAllocator();
        bb = allocator.buffer(Configuration.MAX_MSG_SIZE);

        System.out.println(" Before Serializing");
        System.out.println("Client Send: " + heartBeatMessage1.toString());
        System.out.println("Client Recv: " + heartBeatMessage2.toString());

        heartBeatMessage1.serialize(bb);
        System.out.println(bb.toString());

        System.out.println(" After de serializing ");
        heartBeatMessage2.deserialize(bb);
        System.out.println("Client Send: " + heartBeatMessage1.toString());
        System.out.print("Client Recv: " + heartBeatMessage2.toString());
    }

}
