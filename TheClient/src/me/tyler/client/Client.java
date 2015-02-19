package me.tyler.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Client {

	private User me;

	public Client(String ip, int port) {
		try {
			me = new User(new Socket(InetAddress.getByName(ip), port));
			System.out.println("Client connected to " + ip + ":" + port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public User getMe() {
		return me;
	}

	private void run() {
		while (me.isConnected()) {
			runOnce();
		}
	}

	public void runOnce() {
		byte[] data = me.readData(true);
		byte[] dataUdp = me.readData(false);
		
		if (data != null) {
			if (data.length > 0) {
				ByteBuffer buf = ByteBuffer.wrap(data);
				PacketFactory.process(buf, this);
			}
		}
		if(dataUdp != null){
			if (dataUdp.length > 0) {
				ByteBuffer buf = ByteBuffer.wrap(dataUdp);
				PacketFactory.process(buf, this);
			}
		}
		me.sendReliableData(PacketFactory.getChatMessagePacket("Hello!"));
	}

	public static void main(String[] args) {
		Client client = new Client(Constants.IP_ADDRESS, Constants.PORT);
		if (client.getMe() != null)
			client.run();
		else
			System.err.println("Could not connect to server!");
	}

}
