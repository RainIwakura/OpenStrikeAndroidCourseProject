package com.example.wifiserver;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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

public class ClientActivity extends Activity {
	private Handler handle = new Handler() {
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			TextView status = (TextView) findViewById(R.id.status2);
			status.setText(bundle.getString("msg"));
		}

	};
	private int serverPort;
	private String serverIP;
	EditText msg;
	TextView msgName;
	Button sndMsg;
	ClientThread cThread;

	private final ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 1,
			TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cln);

		msg = (EditText) findViewById(R.id.msg);
		msg.setVisibility(View.INVISIBLE);
		msgName = (TextView) findViewById(R.id.msgName);
		msgName.setVisibility(View.INVISIBLE);
		// Send Message button
		sndMsg = (Button) findViewById(R.id.sendMsg);
		sndMsg.setVisibility(View.INVISIBLE);
		
		cThread = new ClientThread(null, this.handle, getIpAddr());
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
		
		
		this.msg.setVisibility(View.VISIBLE);
		this.msgName.setVisibility(View.VISIBLE);
		this.sndMsg.setVisibility(View.VISIBLE);
		//cThread.write(getIpAddr().getBytes());
	}

	public void writeToServer(View v) {
		String msg = this.msg.getText().toString();
		Bundle bundle = new Bundle();
		Message info = new Message();
		bundle.putString("msgThread", msg);
		info.setData(bundle);
		handle.sendMessage(info);
		/*synchronized (handle) {
			handle.notify();
		}*/
		cThread.write(msg.getBytes());
	}

	public String getIpAddr() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format("%d.%d.%d.%d", (ip & 0xff),
				(ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

		return ipString;
	}
}