package me.tyler.server;

import java.nio.ByteBuffer;

public class PacketFactory {
	
	private static byte PACKET_HANDSHAKE = 1;
	private static byte PACKET_LOGIN_STATUS = 2;
	private static byte PACKET_CHAT_MESSAGE = 3;
	
	
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
		
		final int MAX_LENGTH = 125;
		
		ByteBuffer buf = getLazyBuffer(128);
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

	public static void process(User user, ByteBuffer buf, Server server) {
		byte size = buf.get();
		byte packetId = buf.get();
		
		System.out.println(size+ " "+packetId);
		
		if(packetId == PACKET_HANDSHAKE){
			
			int version = buf.getInt();
			
			if(version != Constants.VERSION){
				user.sendData(getLoginStatus((byte) 1));
				user.disconnect();
			}
			
		}else if(packetId == PACKET_CHAT_MESSAGE){
			
			byte length = buf.get();
			byte[] messageBytes = new byte[length];
			String message = null;
			
			buf.get(messageBytes);
			
			message = new String(messageBytes);
			
			System.out.println(user.getUsername()+": "+message);
			
		}
		
	}
	
}
