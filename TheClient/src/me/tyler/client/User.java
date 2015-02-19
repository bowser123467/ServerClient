package me.tyler.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class User
{

	private final Socket socketTcp;

	private DatagramSocket socketUdp;

	private boolean isTimedout;

	private String username;

	public User(Socket socket)
	{
		this.socketTcp = socket;
		try
		{
			this.socketUdp = new DatagramSocket();
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		isTimedout = false;
	}

	public OutputStream getOutput()
	{
		if (!isConnected())
			return null;
		try
		{
			return socketTcp.getOutputStream();
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
			disconnect();
		}
		return null;
	}

	public InputStream getInput()
	{
		if (!isConnected())
			return null;
		try
		{
			return socketTcp.getInputStream();
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
			disconnect();
		}

		return null;
	}

	public void sendData(byte[] bytes, Client client)
	{
		try
		{
			socketUdp.send(new DatagramPacket(bytes, bytes.length, socketTcp
					.getInetAddress(), socketTcp.getPort()));
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
			disconnect();
		}
	}

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
			System.err.println(e.getMessage());
			disconnect();
		}
	}

	public byte[] readData(boolean reliable)
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
				socketUdp.receive(packet);
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
		return socketTcp.isConnected() && !isTimedout;
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
			socketTcp.close();
			socketUdp.close();
		}
		catch (IOException e)
		{
			isTimedout = true;
			e.printStackTrace();
		}
	}

}
