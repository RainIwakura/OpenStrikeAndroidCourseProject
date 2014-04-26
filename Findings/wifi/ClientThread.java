package com.example.wifiserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
			sos.write(buffer);
		} catch (IOException e) {
			Log.d(TAG, "Exception during write", e);
		}
	}


	public void run() {
		br = new BufferedReader(new InputStreamReader(System.in));
		buf = new byte[512];
		System.out.println(serverIp);

		try {
			sock = new Socket(InetAddress.getByName(serverIp), 5555);
			sos = sock.getOutputStream();
			sis = sock.getInputStream();
		} catch (UnknownHostException uhe) {
			System.out.println("client: " + host + " cannot be resolved.");
		} catch (IOException ioe) {
			System.out.println("client: cannot initialize socket.");
			System.exit(-1);
		}
		write (this.clientIp.getBytes());
		
/*		try {
			synchronized (handle) {
				handle.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		for (;;) {
			try {
				n = sis.read(buf);

				if (n == -1) {
					System.out
							.println("The server has closed the connection - exiting...");
					sock.close();
					System.exit(-1);
				} else {
					System.out.println("The server says: " + new String(buf));
					handle.sendMessage(createMsg("The server says: " + new String(buf)));
				}
			} catch (IOException rwe) {
				System.out.println("Client: I/O error.");
				System.exit(-1);
			}
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
