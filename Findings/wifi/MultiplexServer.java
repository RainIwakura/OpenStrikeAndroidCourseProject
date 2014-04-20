package com.example.wifiserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class MultiplexServer extends Thread {

	int port;
	ByteBuffer readbuffer;
	Selector selector;
	ServerSocketChannel serverSock;
	Set<SelectionKey> readyKeys;
	private int numOfRequests = 0;
	private Handler handle;
	private Runnable message;
	private  String wifiInfo;
	public MultiplexServer(String wifiInfo, Integer port, Handler handle, Runnable msg) {
		// process the command-line args

		this.port = port;
		this.handle = handle;
		this.message = msg;
		this.wifiInfo = wifiInfo;
	}


	
	/*
	 * Handle all the clients
	 */
	@Override
	public void run() {
		SelectorProvider sp = SelectorProvider.provider();

		// initialize main buffer
		readbuffer = ByteBuffer.allocate(1024);
		// Create the selector
		// create and bind the server socket
		try {
			selector = sp.openSelector();
			serverSock = sp.openServerSocketChannel();
			serverSock.configureBlocking(false);
			serverSock.socket().bind(new InetSocketAddress(InetAddress.getByName(wifiInfo),port));
			serverSock.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException io) {
			io.printStackTrace();
		} 

	    int bytesRead;
		boolean closeConnection = false;
		Message msg = new Message();
		Bundle b = new Bundle();
			
		for (;;) {
			// Wait for something to happen on one of our sockets // If nothing
			// is happening then we can feel free to block
			try {
				selector.select();
			} catch (IOException ioe) {
				System.out.println("IOExcetion: line 126");
				ioe.printStackTrace();
			} // get the keys that have had some action
				// and loop through them all
			System.out.println("OK. Something happened.");
			readyKeys = selector.selectedKeys();

			Iterator<SelectionKey> it = readyKeys.iterator();
			System.out.println("There are " + readyKeys.size()
					+ " sockets ready to be checked.");
			
			// Wait so we can follow the output
			try {
				Thread.sleep(1000);

			} catch (InterruptedException ie) {
				System.out.println("Interrupted exception: line 138");
			}
			while (it.hasNext()) { 
				// Two possibilities: it's a new client	 knocking 
				// or it's an existing client with a request
				SelectionKey key = (SelectionKey) it.next();
				if (key.isAcceptable()) {

					System.out.println("Action on the listen socket."); 
					
					ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
					System.out.println("SOCKET " + ssc);
					
					// You won't block here, because we are guaranteed it is
					// ready
					try {
						if (ssc == null) {
							System.out.println("null 1");
							
						}
						ServerSocket sock = ssc.socket();
						System.out.println(sock.getLocalPort());
						System.out.println(sock.getInetAddress().toString());
			
						SocketChannel sc = ssc.accept();
						if (sc == null) {
							System.out.println("null 2");
						}
						System.out.println("Accepted a new client.");
						ClientInfo info = new ClientInfo(
						System.currentTimeMillis(), 0l, numOfRequests);

						// Now add the new client sock to set of what is being
						// listened to
						if (sc != null) {
							sc.configureBlocking(false);
							sc.register(selector, SelectionKey.OP_READ, info);
						}
					} catch (IOException ioe) {
						System.out.println("IOException: line 168");
						ioe.printStackTrace();
					}

					System.out.println("Registered a new client.");
				} else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
					System.out.println("Action on a client socket."); // So this
																		// socket
					// is a client socket
					SocketChannel sc = (SocketChannel) key.channel();
					System.out.println("SOCKET " + sc); // read a message from
														// the client
					bytesRead = 0;
					readbuffer.clear();
					boolean requestActive = false;
					try {
						bytesRead = sc.read(readbuffer);
					} catch (IOException ioe) {
						System.out.println("IOException: line 192");
						ioe.printStackTrace();
					}
					System.out.println("Read " + bytesRead
							+ " bytes from a client socket.");
					String response = new String();
					response = "";
					ClientInfo info = (ClientInfo) sc.keyFor(selector)
							.attachment();

					if (bytesRead == -1) {
						System.out.println("Client has disconnected.");
						key.cancel();
						try {
							sc.close();
						} catch (IOException ioe) {
							System.out.println("IOException: line 212");
							ioe.printStackTrace();
						}
					} else {
						String request = new String(readbuffer.array(), 0,
								bytesRead);
						System.out.println("The client said: " + request);

						switch (request) {
						case "ON_SINCE":
							response = "You've been connected for: "
									+ (int) ((System.currentTimeMillis() - info.timeOfConnection) / 1000)
									+ " s";
							break;
						case "IDLE_SINCE": {
							response = "You've been idle for: "
									+ (int) ((System.currentTimeMillis() - info.timeOfLastRequest) / 1000)
									+ " s";
							break;
						}
						case "REQUESTS":
							response = "Number of requests served: "
									+ numOfRequests;
							break;
						case "QUIT": {
							response = "Closing connection";
							closeConnection = true;
							break;
						}
						case "username": {
							response = "USERNAME";
							break;
						}
						default: {
							response = "BAD";
							break;
						}
						}
						info.setTimeOfLastRequest(System.currentTimeMillis());

						numOfRequests++;

						// switch the buffer so the input is heading out
						// readbuffer.clear();
						System.out.println(response); // clear buffer from
														// remaining chars
						response += "    ";
						readbuffer.put(response.getBytes());
						readbuffer.flip();
						try {
							sc.write(readbuffer);
						} catch (IOException ioe) {
							System.out.println("IOexception: line 239");
							ioe.printStackTrace();
						}

						readbuffer.clear();

						if (closeConnection) {
							closeConnection = false;
							System.out.println("Client has disconnected.");
							key.cancel();
							try {
								sc.close();
							} catch (IOException ioe) {
								System.out.println("IOException: line 212");
								ioe.printStackTrace();
							}
						}
					}
				} // remove this key from the set of ready channels it.remove();

			}

		}

	}

	class ClientInfo {

		private Long timeOfConnection;
		private Long timeOfLastRequest;
		private int id;

		public ClientInfo(Long connect, Long lrequest, int id) {
			timeOfConnection = connect;
			timeOfLastRequest = lrequest;
			this.id = id;
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
	}
}