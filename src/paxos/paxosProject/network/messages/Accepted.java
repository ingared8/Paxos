package paxosProject.network.messages;

import paxosProject.network.NodeIdentifier;
import io.netty.buffer.ByteBuf;

public class Accepted extends Message {

	// feel free to add other variables
	private long index;

	protected Accepted(){}
	
	public Accepted(NodeIdentifier sender, long index) {
		super(MSG_TYPE.ACCEPTED, sender);
		this.index = index;
	}
	
	public long getIndex(){
		return index;
	}
	
	@Override
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		buf.writeLong(index);
	}
	
	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
        index = buf.readLong();
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Accepted<src=").append(super.getSender())
			.append(" index=").append(index).append(">");

		return sb.toString();
	}
}
