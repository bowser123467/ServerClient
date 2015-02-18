package me.tyler.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class User {

	private static int nextId = 0;
	
	private Socket socket;
	private boolean isTimedout;
	private String username;
	private int uniqueId;
	
	public User(Socket socket){
		this.socket = socket;
		isTimedout = false;
		uniqueId = nextId++;
	}
	
	public OutputStream getOutput(){
		if(!isConnected()) 
			return null;
		try {
			return socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public InputStream getInput(){
		if(!isConnected()) 
			return null;
		try {
			return socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			isTimedout = true;
		}
		
		return null;
	}
	
	public void sendData(byte[] bytes){
		if(!isConnected())
			return;
		try {
			getOutput().write(bytes);
		} catch(SocketTimeoutException e){
			isTimedout = true;
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
		}
	}
	
	public byte[] readData(){
		if(!isConnected()) 
			return null;
		
		try {
			byte size = (byte) getInput().read();
			if(size < 0) return new byte[0];
			byte[] data = new byte[size];
			data[0] = size;
			
			getInput().read(data, 1, size-1);
			
			return data;
			
		} catch(SocketTimeoutException e) {
			return new byte[0];
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
		}catch(NullPointerException e){
			System.err.println(e.getMessage());
			disconnect();
		}
		return new byte[0];
	}
	
	public boolean isConnected(){
		return socket.isConnected() && !isTimedout;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public String getUsername(){
		return username;
	}

	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		isTimedout = true;
	}

	public int getUniqueID() {
		return uniqueId;
	}
	
}
