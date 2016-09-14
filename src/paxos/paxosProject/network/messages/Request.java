package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import paxosProject.Configuration;
import paxosProject.network.NodeIdentifier;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class Request extends  Message implements Delayed {

    private int key;
    private int value;
    private int id;
    private long startTime;
    protected Request(){}

    private static int delay =  400;

    /*
    Any request is constructe
    d from the client and  desired value it requests
    */

    public Request(NodeIdentifier sender, int id,int key,int value){
        this.setSender(sender.hashCode());
        this.setType(Message.MSG_TYPE.REQUEST.ordinal());
        this.id = id;
        this.key = key;
        this.value = value;
        this.startTime = System.currentTimeMillis() + 400;
    }

    public int getId(){
        return id;
    }

    public int getKey(){
        return key;
    }

    public int getValue(){
        return value;
    }


    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeInt(this.id);
        buf.writeInt(this.key);
        buf.writeInt(this.value);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        this.id = buf.readInt();
        this.key = buf.readInt();
        this.value = buf.readInt();
    }

    public String toString(){

        return (": " + getSender().toString() + " ||| Type: " + Message.MSG_TYPE.values()[getType()] + " ||| id: " + id +  " ||| Key: "+ key + " ||| Value: " + value);
    }

    @Override
    public long getDelay(TimeUnit timeUnit) {
        return timeUnit.convert((this.startTime - System.currentTimeMillis()), TimeUnit.MILLISECONDS );
    }

    @Override
    public int compareTo(Delayed delayed) {
        if ( this.getDelay(TimeUnit.MILLISECONDS) <= delayed.getDelay(TimeUnit.MILLISECONDS)){
            return -1;
        } else {
            return 1;
        }
    }


    // test the code
    public static void main(String[] args){

        NodeIdentifier nodeIdentifier = new NodeIdentifier(NodeIdentifier.Role.CLIENT,1);
        NodeIdentifier nodeIdentifier1 = new NodeIdentifier(NodeIdentifier.Role.CLIENT,2);
        Request req = new Request(nodeIdentifier,1,10, 20);
        Request req2 = new Request(nodeIdentifier1,2,20,30);
        ByteBuf bb;
        PooledByteBufAllocator allocator = new PooledByteBufAllocator();
        bb = allocator.buffer(Configuration.MAX_MSG_SIZE);

        System.out.println(" Before Serializing");
        System.out.println("Client Send: " + req.toString());
        System.out.println("Client Recv: " + req2.toString());

        req.serialize(bb);
        System.out.println(bb.toString());

        System.out.println(" After de serializing ");
        req2.deserialize(bb);
        System.out.println("Client Send: " + req.toString());
        System.out.print("Client Recv: " + req2.toString());
    }
}

