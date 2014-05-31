package mobile.openstrike.game;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ServerThread extends Thread {

	int port;
	private Handler handle;
	private String wifiInfo;
	private ServerActivity sActivity;
	private DatagramSocket serverSocket;
	private DatagramPacket packet;
	private Map<String, InetSocketAddress> treeMap;
	private Map<String, String> playerTeamMap;
	static boolean keepRunning = true;

	public ServerThread(String wifiInfo, Integer port, Handler handle,
			DatagramSocket socket) {
		// process the command-line args
		System.out.println("constructor");
		this.port = port;
		this.handle = handle;
		this.wifiInfo = wifiInfo;
		this.treeMap = new TreeMap<String, InetSocketAddress>();
		this.playerTeamMap = new TreeMap<String, String>();
		this.serverSocket = socket;
		// run loop again, once we've recreated activity with thread
		keepRunning = true;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[65000];
		String reply = null;
		packet = new DatagramPacket(buffer, buffer.length);

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
			if (!keepRunning)
				break;
			String request = new String(packet.getData(), 0, packet.getLength());

			String message = "User request is: " + request;
			

			InetSocketAddress address = new InetSocketAddress(
					packet.getAddress(), packet.getPort());
			parseRequest(request,address);
			reply = "first";
			System.out.println(reply);

			// boolean result2 = this.handle.sendMessage(this
			// .createBundleMsg(reply));

			// writeToSocket(reply, address);

		}
	}

	public void broadcastMessage(String message, String user) {
		Set<Map.Entry<String, InetSocketAddress>> cur = treeMap.entrySet();
		for (Map.Entry<String, InetSocketAddress> e : cur)
			if (!e.getKey().equals(user)) {
				System.out.println(e.getKey()+" <|> "+user);
				writeToSocket(message, e.getValue());
			}
	}

	public void writeToSocket(String message, InetSocketAddress address) {
		DatagramPacket packetToSend = new DatagramPacket(message.getBytes(),
				message.getBytes().length, address.getAddress(),
				address.getPort());
		try {
			this.serverSocket.send(packetToSend);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public DatagramSocket getSocket() {
		return this.serverSocket;
	}

	public Message createBundleMsg(String strMsg) {
		Bundle b = new Bundle();
		b.putString("msg_s", strMsg);
		Message msg = handle.obtainMessage();
		msg.setData(b);
		return msg;
	}

	public String addPlayer(String request, InetSocketAddress address) {
		String reply = null;
		if (!treeMap.containsKey(request.substring(5))) {
			treeMap.put(request.substring(5), address);
			reply = "Welcome";
		} else
			reply = "This name is already taken!";

		return reply;
	}

	public void setHandler(Handler handler) {
		this.handle = handler;
	}

	public Handler getHandler() {
		return handle;
	}

	public void setRunning(boolean running) {
		this.keepRunning = running;
	}

	public void parseRequest(String request,InetSocketAddress address) {
		StringTokenizer st = new StringTokenizer(request);
		String mes = st.nextToken();
		if (mes.equals("JOIN")) {
			mes = st.nextToken();
			String message = new String("GROUP");
			String team = st.nextToken();
			System.out.println(mes+"<- FUCK ->"+address);
			treeMap.put(mes, address);
			playerTeamMap.put(mes, team);
			Set<Map.Entry<String, InetSocketAddress>> cur = treeMap.entrySet();
			for (Map.Entry<String, InetSocketAddress> e : cur)
				if (!e.getKey().equals(mes)) {
					System.out.println(e.getKey()+" <<>> "+mes);
					message = message + " " + e.getKey()+" "+playerTeamMap.get(e.getKey());
				}
			message=message+"\n";
			System.out.println(message);
			writeToSocket(message, treeMap.get(mes));
			boolean result = this.handle.sendMessage(this.createBundleMsg(mes+" "+team));
		} else
			mes = st.nextToken();
		broadcastMessage(request, mes);
	}
}