package me.tyler.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

public class Server
{

	private ServerSocket serverSocketTcp;

	private DatagramSocket serverSocketUdp;

	private boolean isRunning;

	private User[] users;

	public Server(int port)
	{
		try
		{
			serverSocketTcp = new ServerSocket(port);
			serverSocketTcp.setSoTimeout(10);

			serverSocketUdp = new DatagramSocket(port);
			serverSocketUdp.setSoTimeout(10);

			users = new User[Constants.MAX_USERS];
			System.out.println("Binded!");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void start()
	{
		isRunning = true;
		System.out.println("Starting server");
		while (isRunning)
		{
			try
			{
				Socket socket = serverSocketTcp.accept();

				socket.setSoTimeout(10);

				User user = new User(socket);

				System.out.println(socket.getInetAddress().toString() + " connected!");

				if (isServerFull())
				{
					user.sendReliableData(PacketFactory.getLoginStatus((byte) 2));
				}
				else
				{
					user.sendReliableData(PacketFactory.getHandshakePacket());
					addUser(user);
				}

			}
			catch (SocketTimeoutException e)
			{
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			for (int i = 0; i < users.length; i++)
			{
				User user = users[i];
				if (user != null)
				{
					if (user.isConnected())
					{
						byte[] data = user.readData(true, this);
						byte[] dataUdp = user.readData(false, this);
						if (data != null && data.length > 0)
						{
							ByteBuffer buf = ByteBuffer.wrap(data);
							PacketFactory.process(user, buf, this);
						}
						if (dataUdp != null && dataUdp.length > 0)
						{
							ByteBuffer buf = ByteBuffer.wrap(dataUdp);
							PacketFactory.process(user, buf, this);
						}

					}
				}
			}

		}
	}

	public void addUser(User user)
	{
		if (isServerFull())
		{
			return;
		}

		for (int i = 0; i < users.length; i++)
		{
			User u = users[i];
			if (u == null)
			{
				users[i] = user;
				return;
			}

			if (!u.isConnected())
			{
				users[i] = user;
				return;
			}

		}
	}

	public boolean isServerFull()
	{
		for (int i = 0; i < users.length; i++)
		{
			User user = users[i];
			if (user == null)
				return false;
			if (!user.isConnected())
			{
				return false;
			}
		}
		return true;
	}

	//TODO: Remove this and replace it with a better way of getting the datagram socket to other objects
	public DatagramSocket getDatagramSocket()
	{
		return serverSocketUdp;
	}

	public static void main(String[] args)
	{

		Server server = new Server(Constants.PORT);

		server.start();

	}

}
