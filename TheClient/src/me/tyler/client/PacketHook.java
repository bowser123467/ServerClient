package me.tyler.client;

import java.nio.ByteBuffer;

public abstract class PacketHook {

	private byte packetId;
	
	PacketHook() {
		PacketFactory.hook(this);
	}
	
	public PacketHook(byte packetId){
		this.packetId = packetId;
	}
	
	public abstract void onReceive(Client client, ByteBuffer buf);
	
	public byte getPacketId(){
		return packetId;
	}
	
}
