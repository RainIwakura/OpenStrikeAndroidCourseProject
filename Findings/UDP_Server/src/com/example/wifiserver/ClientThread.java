package com.example.wifiserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ClientThread extends Thread {
	private String host = "server";
	private int port = 5555;
	private String line;
	private BufferedReader br;
	private OutputStream sos = null;
	private InputStream sis = null;
	private Socket sock = null;
	private byte buf[];
	private int n;
	private String serverIp;
	private String clientIp;
	private Handler handle;
	private String TAG = "ClientThread";
	DatagramSocket socket;

	public ClientThread(String sIp, Handler handle, String cIp) {
		this.serverIp = sIp;
		this.handle = handle;
		this.clientIp = cIp;
	}

	public void setIpAddres(String ip) {
		this.serverIp = ip;
	}

	public void write(byte[] buffer) {
		try {
			System.out.println(new String (buffer, 0, buffer.length));
			socket.send(new DatagramPacket(buffer,buffer.length,InetAddress.getByName(serverIp), 5555));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void run() {
		br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println(serverIp);

		byte[] buffer = new byte[1024];
		
		try {
			socket = new DatagramSocket();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.write("JOIN username".getBytes());
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		System.out.println("here");
		while (true) {
				try {
					socket.receive(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("here 2");
				String reply = new String(packet.getData(), 0, packet.getLength());
				System.out.println("The server says: " + reply);
				handle.sendMessage(createMsg("The server says: "+ reply));
		}
		
		}

	public Message createMsg(String strMsg) {
		Bundle b = new Bundle();
		b.putString("msg", strMsg);
		Message msg = handle.obtainMessage();
		msg.setData(b);
		return msg;
	}
	
}
