package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import paxosProject.Configuration;
import paxosProject.network.NodeIdentifier;

/**
 * Created by ingared on 5/1/16.
 */
public class HeartBeat extends  Message{


    protected HeartBeat(){}

    private int count;

    public HeartBeat(NodeIdentifier sender){
        this.setType(MSG_TYPE.HEARTBEAT.ordinal());
        this.setSender(sender);
        count = 0;
    }

    public HeartBeat(NodeIdentifier sender, int heartbeatcount){
        this.setType(MSG_TYPE.HEARTBEAT.ordinal());
        this.setSender(sender);
        this.count = heartbeatcount;
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
            return ("Sender: " + getSender().toString() + " ||| Type: " + MSG_TYPE.values()[getType()]);
        } catch (Exception e){
            return "Null";
        }
    }

    public static void main(String[] args){

        NodeIdentifier nodeIdentifier1 = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,1);
        NodeIdentifier nodeIdentifier2 = new NodeIdentifier(NodeIdentifier.Role.PROPOSER,2);
        HeartBeat heartBeatMessage1 = new HeartBeat(nodeIdentifier1);
        HeartBeat heartBeatMessage2 = new HeartBeat();

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
