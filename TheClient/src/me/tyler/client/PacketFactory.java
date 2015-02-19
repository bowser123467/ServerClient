package me.tyler.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PacketFactory {
	
	private static List<PacketHook> hooks = new ArrayList<PacketHook>();
	
	private static byte PACKET_HANDSHAKE = 1;
	private static byte PACKET_LOGIN_STATUS = 2;
	private static byte PACKET_CHAT_MESSAGE = 3;
	private static byte PACKET_QUIT = 4;
	private static byte PACKET_CHANGE_NAME = 5;
	
	public static byte[] getHandshakePacket(){
		ByteBuffer buf = getLazyBuffer(8);
		
		buf.put(PACKET_HANDSHAKE);
		buf.putInt(Constants.VERSION);
		
		return buf.array();		
	}
	
	/**
	 * 
	 * @param status 0 is success, 1 is fail, 2 is server full
	 * 
	 */
	public static byte[] getLoginStatus(byte status){
		ByteBuffer buf = getLazyBuffer(4);
		
		buf.put(PACKET_LOGIN_STATUS);
		buf.put(status);
		
		return buf.array();
	}
	
	public static byte[] getChatMessagePacket(String message){
		
		final int MAX_LENGTH = 124;
		
		ByteBuffer buf = getLazyBuffer(127);
		byte[] bytes = message.getBytes();
		
		if(bytes.length > MAX_LENGTH){
			byte[] shortBytes = new byte[MAX_LENGTH];
			
			for(int i = 0; i < shortBytes.length;i++){
				shortBytes[i] = bytes[i];
			}
			
			bytes = shortBytes;
			
		}
		
		buf.put(PACKET_CHAT_MESSAGE);
		buf.put((byte) bytes.length);
		buf.put(bytes);
		
		return buf.array();
	}
	
	public static byte[] getQuitPacket(){
		ByteBuffer buf = getLazyBuffer(4);
		
		buf.put(PACKET_QUIT);
		
		return buf.array();
	}
	
	public static byte[] getNameChangePacket(String name){
		byte[] byteName = name.getBytes();
		ByteBuffer buf = getLazyBuffer(20);
		
		buf.put(PACKET_CHANGE_NAME);
		
		if(byteName.length > Constants.MAX_NAME_LENGTH){
			byte[] shortBytes = new byte[Constants.MAX_NAME_LENGTH];
			
			for(int i = 0; i < shortBytes.length;i++){
				shortBytes[i] = byteName[i];
			}
			
			byteName = shortBytes;
		}
		
		buf.put(byteName);
		
		return buf.array();
	}
	
	private static ByteBuffer getLazyBuffer(int size){
		
		if(size <= 0){
			throw new IllegalArgumentException("size must be positive");
		}
		if(size > Byte.MAX_VALUE){
			throw new IllegalArgumentException("size cannot be larger than "+Byte.MAX_VALUE);
		}
		
		ByteBuffer buf = ByteBuffer.allocate(size);
		
		buf.put((byte) size);
		
		return buf;
	}
	

	public static void process(ByteBuffer buf, Client client) {
		byte size = buf.get();
		byte packetId = buf.get();
		
		System.out.println(size+ " "+packetId);
		
		if(packetId == PACKET_HANDSHAKE){
			
			int version = buf.getInt();
			
			if(version != Constants.VERSION){
				client.getMe().disconnect();
			}else{
				client.getMe().sendReliableData(getHandshakePacket());
			}
			
		}else if(packetId == PACKET_CHAT_MESSAGE){
			
			byte length = buf.get();
			byte[] messageBytes = new byte[length];
			String message = null;
			
			buf.get(messageBytes);
			
			message = new String(messageBytes);
			
			System.out.println("Me : "+message);
			
		}else if(packetId == PACKET_CHANGE_NAME){
			byte[] nameBytes = new byte[Constants.MAX_NAME_LENGTH];
			String name = null;
			
			buf.get(nameBytes);
			name = new String(nameBytes).trim();
			
			client.getMe().setUsername(name);
			System.out.println("Username set to "+name);
		}else{
			for(PacketHook hook : hooks){
				if(hook.getPacketId() == packetId){
					hook.onReceive(client, buf);
				}
			}
		}
		
	}

	public static void hook(PacketHook packetHook) {
		hooks.add(packetHook);
	}
	
}
