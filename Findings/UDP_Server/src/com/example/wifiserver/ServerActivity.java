package com.example.wifiserver;

import java.net.Socket;
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
import android.widget.TextView;
import android.widget.Toast;

public class ServerActivity extends Activity {
	
	Handler handle = new Handler() {	
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			String message = b.getString("msg_s");
			TextView v = (TextView) findViewById (R.id.server_status_field);
			v.setText(message);
		}
	};
	
	TextView ipView;
	private boolean showIpButtonStatus = false;
	private int port;
	TextView status;
	ServerThread server;
	private String username;

	private final ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 1,
			TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_srv);
		Intent intent = getIntent();
		this.username = intent.getStringExtra("username");
		ipView = (TextView) findViewById(R.id.showIp);
		ipView.setVisibility(View.VISIBLE);
		ipView.setText("IP " + getIpAddr());
		System.out.println("new server");
		
		int port = 5555;
	
		server = new ServerThread(getIpAddr(), port, handle, new PrintRunnable());
		pool.execute(server);
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
/*
	public void onPause() {
		super.onPause();
//		mHandler.removeCallbacksAndMessages(null);
	}

	public void onResume() {
		super.onResume();
	}

	public void onRestart() {
		super.onRestart();
	}

	public void onStop() {
		super.onStop();
	//	mHandler.removeCallbacks(null);
	}

	public void onDestroy() {
		super.onDestroy();
		//mHandler.removeCallbacks(null);
	}*/

	public void goBack(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
//		pool.remove(server);
	//	pool.shutdownNow();
		finish();
	}
/*
	public void updateTextField(String str) {
		System.out.println("updateTextField");
		TextView status = (TextView) findViewById(R.id.server_status_field);
		System.out.println("updater this hash: " + this.hashCode());
		status.setText(str);
		// System.out.println("updater hash " + status.hashCode());
	}

/*	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// outState.put
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}
*/
	
	
	

	class PrintRunnable implements Runnable {
		
		String text = null;
		
		public void setText (String str) {
			text = str;
		}
		
		public void run() {
			TextView v = (TextView) findViewById (R.id.server_status_field);
			v.setText(text);
		}
		
	};
}
