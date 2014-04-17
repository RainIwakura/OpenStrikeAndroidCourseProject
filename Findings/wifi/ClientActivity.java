package com.example.wifiserver;

import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class ClientActivity extends Activity {
	Socket socket;
	TextView ipView;
	private boolean showIpButtonStatus  = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client);
		
		ipView = (TextView) findViewById (R.id.showIp);
		ipView.setVisibility(View.INVISIBLE);
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

	public void showIp (View v) {
		if (!this.showIpButtonStatus) {
			String host = this.getIpAddr();
			ipView.setVisibility(View.VISIBLE);
			ipView.setText("IP " + host);
			this.showIpButtonStatus = true;
		} else {
			ipView.setVisibility(View.INVISIBLE);
			this.showIpButtonStatus = false;
		}
	}
	
	public String getIpAddr() {
		   WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		   WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		   int ip = wifiInfo.getIpAddress();

		   String ipString = String.format(
		   "%d.%d.%d.%d",
		   (ip & 0xff),
		   (ip >> 8 & 0xff),
		   (ip >> 16 & 0xff),
		   (ip >> 24 & 0xff));

		   return ipString;
		}
	
	public void onPause () {
		super.onPause();
	}
	
	public void goBack(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
