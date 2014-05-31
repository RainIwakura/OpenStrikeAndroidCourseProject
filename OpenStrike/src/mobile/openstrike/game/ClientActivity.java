package mobile.openstrike.game;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.metaio.Example.R;

public class ClientActivity extends Activity {
	private Handler handle = new Handler() {
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			TextView status = (TextView) findViewById(R.id.status2);
			status.setText(bundle.getString("msg"));
			System.out.println("GOT MESSAGE FROM SERVER!");
		}

	};
	private int serverPort;
	private String serverIP;
	EditText msg;
	TextView msgName;
	Button sndMsg;
	ClientThread cThread;
	DatagramSocket socket;
	private String username, team;

	private final ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 1,
			TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cln);
		Intent intent = getIntent();
		this.username = intent.getStringExtra("username");
		this.team = intent.getStringExtra("team");
		try {
			socket = new DatagramSocket(null);
			socket.bind(new InetSocketAddress(InetAddress
					.getByName(getIpAddr()), 5555));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cThread = new ClientThread(socket, null, this.handle, getIpAddr());
		cThread.setUsername(username);
		cThread.setTeam(team);
		GameData.INSTANCE.setcThread(cThread);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.server, menu);
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

	public void goBackClient(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	public void saveIPandPort(View v) {
		EditText IP = (EditText) findViewById(R.id.getIpField);
		this.serverIP = IP.getText().toString();
		Toast.makeText(getApplicationContext(), this.serverIP, 1000);
		cThread.setIpAddres(this.serverIP);
		pool.execute(cThread);
		Intent i = new Intent(this, TutorialTrackingSamples.class);
		i.putExtra("team", this.team);
		i.putExtra("username", this.username);
		startActivity(i);
	}

	public void writeToServer(View v) {
		String msg = this.msg.getText().toString();

		SendMessageTask task = new SendMessageTask();
		task.execute(msg, this.serverIP);
	}

	public String getIpAddr() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format("%d.%d.%d.%d", (ip & 0xff),
				(ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

		return ipString;
	}

	class SendMessageTask extends AsyncTask<String, Void, String> {

		String message = null;

		@Override
		protected String doInBackground(String... params) {

			try {
				socket.send(new DatagramPacket(params[0].getBytes(), params[0]
						.getBytes().length, InetAddress.getByName(params[1]),
						5555));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return "message sent";
		}

	}

}