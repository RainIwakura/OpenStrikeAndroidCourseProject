package com.example.wifiserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.wifiserver.ServerActivity.PrintRunnable;

public class ServerThread extends Thread {

	int port;
	private Handler handle;
	private String wifiInfo;
	private ServerActivity sActivity;
	private DatagramSocket serverSocket;
	private DatagramPacket packet;
	private Map<String, InetAddress> hashMap;
	private boolean keepRunning = true;
	
	public ServerThread(String wifiInfo, Integer port, Handler handle,
			PrintRunnable msg) {
		// process the command-line args
		System.out.println("constructor");
		this.port = port;
		this.handle = handle;
		this.wifiInfo = wifiInfo;
		this.hashMap = new HashMap<String, InetAddress>();
		try {
			this.serverSocket = new DatagramSocket(null);
			serverSocket.bind(new InetSocketAddress(InetAddress.getByName(wifiInfo), port));
			System.out.println("ss is created");
		} catch (SocketException | UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void run() {
		byte[] buffer = new byte[65000];
		String reply = null;
		packet = new DatagramPacket(buffer,buffer.length);
		
		System.out.println("in server 1");
		while (keepRunning) {
			System.out.println("in server 2");
			if (serverSocket == null) {
				System.out.println("ss is null");
			}
			if (packet == null) {
				System.out.println("pack is null");
			}
			try {
				serverSocket.receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String request = new String(packet.getData(), 0, packet.getLength());
			
			
			String message = "User request is: " + request;
			
			this.handle.sendMessage(this.createBundleMsg(message));
			
			if ( hashMap.containsKey(request.substring(5, request.length()))) {
				System.out.println("user exists");
			}
			
			if ( !hashMap.containsKey(request.substring(5, request.length())) && request.substring(0, 3) == "JOIN") {
				hashMap.put(request.substring(5, request.length() - 1), packet.getAddress());
				reply = "Welcome";
			} else if ( ( hashMap.containsKey(request.substring(5, request.length() - 1)) ) && (request.substring(0, 3) == "JOIN") ) {
				 reply = "You're already registered";
			} else {
				 reply = request;
			}
			System.out.println(reply);
			packet = new DatagramPacket (reply.getBytes(), reply.getBytes().length, packet.getAddress(), packet.getPort());
			
			try {
				serverSocket.send(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

		
		
		
	public void BroadcastMessage (String message) {
	}
	
	
	
	
	
	public DatagramSocket getSocket() {
		return this.serverSocket;
	}
	
	
	public Message createBundleMsg(String strMsg) {
		Bundle b = new Bundle();
		b.putString("msg", strMsg);
		Message msg = handle.obtainMessage();
		msg.setData(b);
		return msg;
	}
	
	
	public void closeSocket () {
		keepRunning = false;
		if (this.serverSocket.isBound()) {
			this.serverSocket.close();
		}
	}
	
	
	

	public void setHandler (Handler handler) {
		this.handle = handler;
	}
	
	public Handler getHandler () {
		return handle;
	}
	
	public void setActivity (ServerActivity sa) {
		this.sActivity = sa;
	}
	
	/*
	 * Handle all the clients
	 */

	class ClientInfo {

		private Long timeOfConnection;
		private Long timeOfLastRequest;
		private int id;
		private String ip;
		boolean beenServed = false;
		private String username;
		private UUID uid;

		public ClientInfo(Long connect, Long lrequest, int id, String ip) {
			timeOfConnection = connect;
			timeOfLastRequest = lrequest;
			this.id = id;
			this.ip = ip;
		}

		/**
		 * @return the timeOfConnection
		 */
		public Long getTimeOfConnection() {
			return timeOfConnection;
		}

		/**
		 * @return the timeOfLastRequest
		 */
		public Long getTimeOfLastRequest() {
			return timeOfLastRequest;
		}

		/**
		 * @param timeOfLastRequest
		 *            the timeOfLastRequest to set
		 */
		public void setTimeOfLastRequest(Long timeOfLastRequest) {
			this.timeOfLastRequest = timeOfLastRequest;
		}

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public boolean getStatus() {
			return this.beenServed;
		}

		public void setStatus(boolean status) {
			this.beenServed = status;
		}

		public String getUsername() {
			return this.username;
		}

		public void setUsername(String username) {
			this.username = username;
		}
	}

	
}