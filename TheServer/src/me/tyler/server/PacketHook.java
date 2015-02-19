package me.tyler.server;

import java.nio.ByteBuffer;

public abstract class PacketHook {

	private byte packetId;
	
	PacketHook() {
		PacketFactory.hook(this);
	}
	
	public PacketHook(byte packetId){
		this.packetId = packetId;
	}
	
	public abstract void onReceive(Server server, User user, ByteBuffer buf);
	
	public byte getPacketId(){
		return packetId;
	}
	
}
