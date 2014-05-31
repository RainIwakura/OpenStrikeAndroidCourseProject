package mobile.openstrike.game;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.metaio.Example.R;

public class ServerActivity extends Activity {
	ListView team1LV, team2LV;
	ArrayAdapter<String> adapter1;
	ArrayAdapter<String> adapter2;
	List<String> team1, team2;

	Handler handle = new Handler() {
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			String message = b.getString("msg_s");
			StringTokenizer st = new StringTokenizer(message);
			String mes = st.nextToken();
			TextView v = (TextView) findViewById(R.id.server_status_field);
			v.setText(message);
			if (st.nextToken().equals("1")) {
				team1.add(mes);
				adapter1.notifyDataSetChanged();
			} else {
				team2.add(mes);
				adapter2.notifyDataSetChanged();
			}
		}
	};

	TextView ipView;
	private boolean showIpButtonStatus = false;
	private int port;
	TextView status;
	ServerThread server;
	private String username;
	DatagramSocket socket;

	private final ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 1,
			TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_srv);
		team1LV = (ListView) findViewById(R.id.listTeam1);
		team2LV = (ListView) findViewById(R.id.listTeam2);
		team1 = new ArrayList<String>();
		team2 = new ArrayList<String>();
		Intent intent = getIntent();
		this.username = intent.getStringExtra("username");
		ipView = (TextView) findViewById(R.id.showIp);
		ipView.setText("IP " + getIpAddr());
		int port = 5555;
		// NOTE: socket is now binded in activity not thread
		try {
			socket = new DatagramSocket(null);
			socket.bind(new InetSocketAddress(InetAddress
					.getByName(getIpAddr()), port));
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		System.out.println("ss is created");

		if (server == null) {
			server = new ServerThread(getIpAddr(), port, handle, socket);
		} else {
			System.out.println("already exists");
		}
		pool.execute(server);
		adapter1 = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, team1);
		team1LV.setAdapter(adapter1);
		adapter1.notifyDataSetChanged();

		adapter2 = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, team2);
		team2LV.setAdapter(adapter2);
		adapter2.notifyDataSetChanged();
	}

	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.client, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public String getIpAddr() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format("%d.%d.%d.%d", (ip & 0xff),
				(ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

		return ipString;
	}

	public void onRestart() {
		super.onRestart();
		new CloseSocketTask().execute();

	}

	public void onStop() {
		super.onStop();
		System.out.println("stopped");
		new CloseSocketTask().execute();
		pool.remove(server);
		pool.shutdownNow();

	}

	public void onDestroy() {
		super.onDestroy();
		System.out.println("destroyed");
		new CloseSocketTask().execute();
		pool.remove(server);
		pool.shutdownNow();
	}

	public void goBack(View v) {
		// GO BACK SHOULD WORK NOW
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		server.setRunning(false);
		new CloseSocketTask().execute();
		pool.shutdownNow();
		finish();
	}

	// closes socket
	class CloseSocketTask extends AsyncTask {

		String message = null;

		@SuppressWarnings("resource")
		@Override
		protected String doInBackground(Object... params) {
			if (socket != null) {
				if (socket.isBound() && !socket.isClosed()) {
					socket.close();
					System.out.println("socket closed");
				}
			}
			return "message sent";
		}
	}

}
