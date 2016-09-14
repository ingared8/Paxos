package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import paxosProject.Configuration;
import paxosProject.network.Functions;
import paxosProject.network.NodeIdentifier;

import java.util.*;

public class Accepted extends Message {

	// feel free to add other variables
	private int status;
	private int proposalID;
    private int slot;
    private int requestID;
    private int key;
    private int Value;
    private long index;

	public enum STATUS{
		REJECTED,ACCEPTED
	}

	protected Accepted(){}
	
	public Accepted(NodeIdentifier sender,Accept accept,STATUS status) {
		this.setSender(sender.hashCode());
		this.setType(MSG_TYPE.ACCEPTED.ordinal());
		this.status = status.ordinal();
		this.proposalID = accept.getProposalID();
        this.slot = accept.getSlot();
        this.requestID = accept.getRequestID();
        this.key = accept.getKey();
        this.Value = accept.getvalue();
        this.index = 100;
	}

    public long getIndex(){return this.index;}
    public int getProposalID(){ return this.proposalID; }
    public int getStatus(){ return this.status; }
    public int getSlot(){ return this.slot; }
    public int getKey(){ return this.key; }
    public int getRequestID(){ return this.requestID; }
    public int getvalue(){ return this.Value;}

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeInt(proposalID);
        buf.writeInt(status);
        buf.writeInt(slot);
        buf.writeInt(requestID);
        buf.writeInt(key);
        buf.writeInt(Value);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        proposalID = buf.readInt();
        status = buf.readInt();
        slot = buf.readInt();
        requestID = buf.readInt();
        key = buf.readInt();
        Value = buf.readInt();
    }

    public String toString() {
        return ("Sender: " + getSender().toString() + " ||| Type: " + MSG_TYPE.values()[getType()] + " ||| Slot: " + slot + " ||| ProposalID: "
                + getProposalID() + " ||| Request ID: " + getRequestID()   + " ||| Status: " + STATUS.values()[status]  + " ||| Key: " + key + " ||| Value: " + Value);
    }

    public static  void main(String[] args){

        NodeIdentifier nodeIdentifier1 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,1);
        NodeIdentifier nodeIdentifier2 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,2);
        Accept accept1 = new Accept(nodeIdentifier1,1,2,3,4,5);
        Accept accept2 = new Accept(nodeIdentifier2,6,2,1,9,10);
        Accept accept3 = new Accept(nodeIdentifier2,6,2,12,13,14);

        Accepted a1 = new Accepted(nodeIdentifier1,accept1,STATUS.ACCEPTED);
        Accepted a2 = new Accepted(nodeIdentifier2,accept2,STATUS.REJECTED);
        Accepted a3 = new Accepted(nodeIdentifier1,accept3,STATUS.ACCEPTED);

        ByteBuf bb;
        PooledByteBufAllocator allocator = new PooledByteBufAllocator();
        bb = allocator.buffer(Configuration.MAX_MSG_SIZE);

        System.out.println(" Before Serializing");
        System.out.println("Client Send: " + a1.toString());
        System.out.println("Client Recv: " + a2.toString());

        a1.serialize(bb);
        System.out.println(bb.toString());

        System.out.println(" After de serializing ");
        a2.deserialize(bb);
        System.out.println("Client Send: " + a1.toString());
        System.out.println("Client Recv: " + a2.toString());

        a2 = new Accepted(nodeIdentifier2,accept2,STATUS.REJECTED);

        Queue<Accepted> queue = new PriorityQueue<>(10, Functions.getAcceptedComparator());
        queue.add(a1);
        queue.add(a2);
        queue.add(a3);

        System.out.println(a1.getProposalID());
        System.out.println(a2.getProposalID());
        System.out.println(a3.getProposalID());

        System.out.println(" Using Deque Operation");
        while (!queue.isEmpty()){
            Accepted a= (Accepted)queue.poll();
            System.out.println(a.toString());
        }
        System.out.println();

        queue.add(a3);
        queue.add(a2);
        queue.add(a3);
        queue.add(a2);
        queue.add(a1);

        Iterator<Accepted> iter = queue.iterator();

        while (iter.hasNext()){
            Accepted a = (Accepted)iter.next();
            System.out.println(a.toString());
        }

        // Testing Set

        Set<Accepted> acceptedSet = new HashSet<>();
        NodeIdentifier nodeIdentifier3 = new NodeIdentifier(NodeIdentifier.Role.ACCEPTOR,2);
        Accepted a4 = new Accepted(nodeIdentifier3,accept2,STATUS.REJECTED);
        Accepted a5 = new Accepted(nodeIdentifier2,accept2,STATUS.REJECTED);

        acceptedSet.add(a1);
        acceptedSet.add(a2);
        acceptedSet.add(a3);
        acceptedSet.add(a4);
        acceptedSet.add(a5);

        Iterator<Accepted> iter1 = acceptedSet.iterator();
        System.out.println("Set ");
        while (iter1.hasNext()){
            Accepted a = (Accepted)iter1.next();
            System.out.println(a.toString());
        }
    }
}
