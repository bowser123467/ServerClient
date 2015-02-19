package me.tyler.server;

import java.nio.ByteBuffer;

public abstract class PacketHook {

	private final byte packetId;
	
	private boolean isHooked;
	
	public PacketHook(byte packetId){
		this.packetId = packetId;
	}
	
	public abstract void onReceive(Server server, User user, ByteBuffer buf);
	
	public byte getPacketId(){
		return packetId;
	}
	
	public void hook(){
		if(isHooked)
			return;
		isHooked = true;
		PacketFactory.hook(this);
	}
	
}
