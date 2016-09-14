package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import paxosProject.Configuration;
import paxosProject.network.NodeIdentifier;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Promise extends Message {

    public enum STATUS{
        REJECT,ACCEPT
    }

    private int status;
    private int proposalID;
    private List<Accepted> acceptedList;

    protected Promise() {}

    public Promise(NodeIdentifier sender, STATUS statusmsg,int proposalId){
        this.setSender(sender.hashCode());
        this.setType(MSG_TYPE.PROMISE.ordinal());
        this.status = statusmsg.ordinal();
        this.proposalID = proposalId;
        this.acceptedList = new LinkedList<>();
    }

    public Promise(NodeIdentifier sender, STATUS statusmsg,int proposalId,List<Accepted> acceptedList){
        this.setSender(sender.hashCode());
        this.setType(MSG_TYPE.PROMISE.ordinal());
        this.status = statusmsg.ordinal();
        this.proposalID = proposalId;
        this.acceptedList = acceptedList;
    }

    public List<Accepted> getAcceptedList(){
        return this.acceptedList;
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
        int size = acceptedList.size();
        buf.writeInt(size);
        Iterator<Accepted> iter = acceptedList.iterator();
        while (iter.hasNext()){
            Accepted accepted = iter.next();
            accepted.serialize(buf);
        }
    }

    private void serialize_accept(ByteBuf buf, Accepted accept) {
        buf.writeInt(accept.getType());
        buf.writeInt(accept.hashCode());
        buf.writeInt(accept.getProposalID());
        buf.writeInt(accept.getSlot());
        buf.writeInt(accept.getRequestID());
        buf.writeInt(accept.getKey());
        buf.writeInt(accept.getValue());
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        proposalID = buf.readInt();
        status = buf.readInt();
        int size = buf.readInt();
        acceptedList = new LinkedList<>();
        for (int i=0; i<size;i++){
            NodeIdentifier nodeIdentifier = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,1);
            Accept accept = new Accept(nodeIdentifier,6,7,8,9,10);
            Accepted accepted = new Accepted(nodeIdentifier,accept, Accepted.STATUS.ACCEPTED);
            accepted.deserialize(buf);
            acceptedList.add(accepted);
        }
    }

    private Accept deserialize_accept1(ByteBuf buf) {
        NodeIdentifier n1 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,1);
        int type = buf.readInt();
        int hashcode =buf.readInt();
        int propId = buf.readInt();
        int slot = buf.readInt();
        int reqId = buf.readInt();
        int key = buf.readInt();
        int value = buf.readInt();
        Accept accept = new Accept(n1,propId,slot,reqId,key,value);
        return accept;
    }

    private Accept deserialize_accept(ByteBuf buf,Accept accept1) {
        NodeIdentifier n1 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,1);
        int type = buf.readInt();
        int hashcode =buf.readInt();
        int propId = buf.readInt();
        int slot = buf.readInt();
        int reqId = buf.readInt();
        int key = buf.readInt();
        int value = buf.readInt();
        Accept accept = new Accept(n1,propId,slot,reqId,key,value);
        return accept;
    }

    public String toString() {
        return ("Sender: " + getSender().toString() + " ||| Type: " + MSG_TYPE.values()[getType()] + " ||| Status: " + STATUS.values()[status] + " for ProposalID " + proposalID + " list:" + acceptedList.toString() );
    }

    public static void main(String[] args){

        NodeIdentifier nodeIdentifier1 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,1);
        NodeIdentifier nodeIdentifier2 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,2);
        Accepted accept1 = new Accepted(nodeIdentifier1, new Accept(nodeIdentifier1,1,2,3,4,5), Accepted.STATUS.ACCEPTED);
        Accepted accept2 = new Accepted(nodeIdentifier2,new Accept(nodeIdentifier2,6,7,8,9,10), Accepted.STATUS.REJECTED);
        Accepted accept3 = new Accepted(nodeIdentifier2,new Accept(nodeIdentifier1,11,12,13,14,15), Accepted.STATUS.REJECTED);


        List<Accepted> l1 = new LinkedList<>();
        List<Accepted> l2 = new LinkedList<>();

        l1.add(accept1);
        l2.add(accept2);

        l1.add(accept3);

        Promise promise1 = new Promise(nodeIdentifier1,STATUS.ACCEPT,1,l1);
        Promise promise2 = new Promise(nodeIdentifier2,STATUS.REJECT,2, l2);

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

