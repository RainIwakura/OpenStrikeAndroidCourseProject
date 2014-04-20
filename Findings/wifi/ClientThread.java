package com.example.wifiserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


public class ClientThread extends Thread
{
	private String host = "server";
	private int port = 5555;
	private String line;
	private BufferedReader br;
	private OutputStream sos = null;
	private InputStream sis = null;
	private Socket sock = null;
	private byte buf[];
	private int n;
	private String ip;
	private Handler handle;
	private String TAG = "ClientThread";
	
	private Handler ownHandler;
	
	public ClientThread (String ip, Handler handle) {
		this.ip = ip;
		this.handle = handle;
		
	}
	
	public void setIpAddres (String ip) {
		this.ip = ip;
	}
	
	
	
	public void write(byte[] buffer) {
        try {                                                                                                                                             
            sos.write(buffer);
        } catch (IOException e) {
            Log.d(TAG, "Exception during write", e);
        }
    }
	
	/*
	*/
	
	public void run() {
		br = new BufferedReader( new InputStreamReader(System.in) );
		buf = new byte[512];
		System.out.println(ip);
		
		
		try
		{
			sock = new Socket(InetAddress.getByName(ip), 5555);
			sos = sock.getOutputStream();
			sis = sock.getInputStream();
		}
		catch ( UnknownHostException uhe )
		{
			System.out.println( "client: " + host + " cannot be resolved." );
		}
		
		
		catch ( IOException ioe )
		{
			System.out.println( "client: cannot initialize socket." );
			System.exit(-1);
		}
		try {
			synchronized (handle) {
				handle.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String name = null;
	
		for (;;)
		{
			try
			{
		/*		Message msg = handle.obtainMessage();
				Bundle bundle = msg.getData();
				String str = bundle.getString("msgThread");
				
				sos.write(str.getBytes());*/
				n = sis.read( buf );
			
				if ( n == -1 )
				{
					System.out.println( "The server has closed the connection - exiting..." );
					sock.close();
					System.exit(-1);
				}
				else
					System.out.println( "The server says: " + new String( buf ) );
			}
			catch ( IOException rwe )
			{
				System.out.println( "Client: I/O error." );
				System.exit(-1);
			}
		}
	}
	}

