package me.tyler.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class User
{

	private static int nextId = 0;

	private final Socket socket;

	private boolean isTimedout;

	private String username;

	private final int uniqueId;

	public User(Socket socket)
	{
		this.socket = socket;
		isTimedout = false;
		uniqueId = nextId++;
	}

	public OutputStream getOutput()
	{
		if (!isConnected())
			return null;
		try
		{
			return socket.getOutputStream();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public InputStream getInput()
	{
		if (!isConnected())
			return null;
		try
		{
			return socket.getInputStream();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			isTimedout = true;
		}

		return null;
	}

	/**
	 * Send data over a udp protocol
	 * @param bytes data to send
	 */
	public void sendData(byte[] bytes, Server server)
	{
		if (!isConnected())
			return;
		try
		{
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, socket.getInetAddress(), Constants.PORT);

			server.getDatagramSocket().send(packet);
		}
		catch (SocketTimeoutException e)
		{
			isTimedout = true;
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			disconnect();
		}
	}

	/**
	 * Send data over a tcp protocol
	 * @param bytes data to send
	 */
	public void sendReliableData(byte[] bytes)
	{
		if (!isConnected())
			return;
		try
		{
			getOutput().write(bytes);
		}
		catch (SocketTimeoutException e)
		{
			isTimedout = true;
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			disconnect();
		}
	}

	public byte[] readData(boolean reliable, Server server)
	{
		if (!isConnected())
			return null;
		byte[] bytes = null;

		if (reliable)
		{
			try
			{
				byte size = (byte) getInput().read();
				if (size < 0)
					return new byte[0];
				byte[] data = new byte[size];
				data[0] = size;

				getInput().read(data, 1, size - 1);

				return data;

			}
			catch (SocketTimeoutException e)
			{
				return new byte[0];
			}
			catch (IOException e)
			{
				System.err.println(e.getMessage());
				disconnect();
			}
			catch (NullPointerException e)
			{
				System.err.println(e.getMessage());
				disconnect();
			}
		}
		else
		{
			bytes = new byte[Constants.UDP_LENGTH];

			DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

			try
			{
				server.getDatagramSocket().receive(packet);
				bytes = packet.getData();
			}
			catch (SocketTimeoutException e)
			{
				return new byte[0];
			}
			catch (IOException e)
			{
				System.err.println(e.getMessage());
				disconnect();
			}
			catch (NullPointerException e)
			{
				System.err.println(e.getMessage());
				disconnect();
			}

		}

		return bytes;
	}

	public boolean isConnected()
	{
		return socket.isConnected() && !isTimedout;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getUsername()
	{
		return username;
	}

	public void disconnect()
	{
		try
		{
			socket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		isTimedout = true;
	}

	public int getUniqueID()
	{
		return uniqueId;
	}

}
