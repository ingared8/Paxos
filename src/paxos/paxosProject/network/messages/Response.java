package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import paxosProject.Configuration;
import paxosProject.network.NodeIdentifier;

/**
 * Created by ingared on 3/30/16.
 */

public class Response extends Message{

    protected Response(){}

    public int getRequestID() {
        return requestID;
    }

    private int requestID;

    public Response(NodeIdentifier sender, Learn learn){
        this.setSender(sender.hashCode());
        this.setType(MSG_TYPE.RESPOSNE.ordinal());
        this.requestID = learn.getRequestID();
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeInt(requestID);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        requestID = buf.readInt();
    }

    @Override
    public String toString(){
        return ("Sender: " + getSender().toString() + " ||| Type: " + MSG_TYPE.values()[getType()] + " ||| requestID: " + requestID);
    }

    public static void main(String[] args) {

        NodeIdentifier nodeIdentifier1 = new NodeIdentifier(NodeIdentifier.Role.LEARNER, 1);
        NodeIdentifier nodeIdentifier2 = new NodeIdentifier(NodeIdentifier.Role.LEARNER, 2);

        Accept accept1 = new Accept(nodeIdentifier1, 1, 2, 3, 10, 20);
        Accept accept2 = new Accept(nodeIdentifier2, 4, 5, 6, 30, 40);

        Accepted accepted1 = new Accepted(nodeIdentifier1, accept1, Accepted.STATUS.ACCEPTED);
        Accepted accepted2 = new Accepted(nodeIdentifier2, accept2, Accepted.STATUS.REJECTED);

        Learn learn1 = new Learn(nodeIdentifier1, accepted1);
        Learn learn2 = new Learn(nodeIdentifier2, accepted2);

        Response response1 = new Response(nodeIdentifier1,learn1);
        Response response2 = new Response(nodeIdentifier2, learn2);

        ByteBuf bb;
        PooledByteBufAllocator allocator = new PooledByteBufAllocator();
        bb = allocator.buffer(Configuration.MAX_MSG_SIZE);

        System.out.println(" Before Serializing");
        System.out.println("Client Send: " + response1.toString());
        System.out.println("Client Recv: " + response2.toString());

        response1.serialize(bb);
        System.out.println(bb.toString());

        System.out.println(" After de serializing ");
        response2.deserialize(bb);
        System.out.println("Client Send: " + response1.toString());
        System.out.println("Client Recv: " + response2.toString());
    }
}
