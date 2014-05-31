package mobile.openstrike.game;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.StringTokenizer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ClientThread extends Thread {
	private String host = "server";
	private int port = 5555;
	private String line;
	private Socket sock = null;
	private byte buf[];
	private int n;
	private String serverIp;
	private String clientIp;
	private Handler handle;
	private String TAG = "ClientThread";
	DatagramSocket socket;
	private String username, team;

	public ClientThread(DatagramSocket socket, String sIp, Handler handle,
			String cIp) {
		this.socket = socket;
		this.serverIp = sIp;
		this.handle = handle;
		this.clientIp = cIp;
	}

	public void setIpAddres(String ip) {
		this.serverIp = ip;
	}

	public void setUsername(String u) {
		this.username = u;
	}

	public void setTeam(String u) {
		this.team = u;
	}

	public void write(byte[] buffer) {
		try {
			System.out.println(new String(buffer, 0, buffer.length));
			socket.send(new DatagramPacket(buffer, buffer.length, InetAddress
					.getByName(serverIp), 5555));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {

		System.out.println(this.username);

		byte[] buffer = new byte[1024];

		/*
		 * if (socket.equals(null)) {
		 * System.out.println("client socket is null"); }
		 */

		this.write(("JOIN " + this.username + " " + this.team).getBytes());
		Player ourPlayer = new Player(this.username, this.team);
		GameData.INSTANCE.getPlayerList().add(ourPlayer);
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
			StringTokenizer st = new StringTokenizer(reply);
			String reqType = st.nextToken();
			if (reqType.equals("GROUP")) {
				while (st.hasMoreTokens()) {
					String name = st.nextToken();
					String team = st.nextToken();
					Player player = new Player(name, team);
					if (team.equals("1")) {
						player.setType(Integer
								.toString(GameData.INSTANCE.t1 + 2));
						GameData.INSTANCE.t1++;
					} else {
						player.setType(Integer
								.toString(GameData.INSTANCE.t2 + 4));
						GameData.INSTANCE.t2++;
					}
					GameData.INSTANCE.getPlayerList().add(player);
				}
				if (ourPlayer.getTeam().equals("1")) {
					GameData.INSTANCE
							.getPlayerList()
							.get(GameData.INSTANCE.getPlayerList().indexOf(
									ourPlayer))
							.setType(Integer.toString(GameData.INSTANCE.t1 + 2));
					GameData.INSTANCE.t1++;
				} else {
					GameData.INSTANCE
							.getPlayerList()
							.get(GameData.INSTANCE.getPlayerList().indexOf(
									ourPlayer))
							.setType(Integer.toString(GameData.INSTANCE.t2 + 4));
					GameData.INSTANCE.t2++;
				}

			} else if (reqType.equals("JOIN")) {
				GameData.INSTANCE.getPlayerList().add(
						new Player(st.nextToken(), st.nextToken()));
			} else if (reqType.equals("SHOT")) {
				String name = st.nextToken();
				for (Player p : GameData.INSTANCE.getPlayerList())
					if (p.getName().equals(name)) {
						p.setHealth(p.getHealth() - 20);
					}
			} else if (reqType.equals("GOAL")) {

			} else if (reqType.equals("HEALTH")) {
				String name = st.nextToken();
				for (Player p : GameData.INSTANCE.getPlayerList())
					if (p.getName().equals(name)) {
						p.setHealth(p.getHealth() + 20);
					}
			}
			// handle.sendMessage(createMsg(reply));
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
