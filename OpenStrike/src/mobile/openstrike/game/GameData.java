package mobile.openstrike.game;

import java.util.ArrayList;
import java.util.List;

public class GameData {
	public static final GameData INSTANCE = new GameData();
	private List<Player> playerList = new ArrayList<Player>();
	private Player server;
	private ClientThread cThread;
	int t1=0,t2=0;
	public ClientThread getcThread() {
		return cThread;
	}
	public void setcThread(ClientThread cThread) {
		this.cThread = cThread;
	}
	public
	GameData() {
	}
	public List<Player> getPlayerList() {
		return playerList;
	}
	public Player getServer() {
		return server;
	}
	public void setServer(Player server) {
		this.server = server;
	}	
}
