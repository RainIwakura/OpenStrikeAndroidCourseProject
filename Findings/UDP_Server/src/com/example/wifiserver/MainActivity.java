package com.example.wifiserver;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	public ArrayList<String> IPs;
	private Handler handle;
	private String username;
	private EditText unameField;
	private boolean hasEnteredUname;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.unameField = (EditText) findViewById(R.id.username_field);
		this.hasEnteredUname = false;
	}

	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

	public void createServerActivity(View view) {
		saveUsername();
		if (this.hasEnteredUname) {
			Intent intent = new Intent(this, ServerActivity.class);
			intent.putExtra("username", this.username);
			startActivity(intent);
			Log.d("main activity", "creating server");
			finish();
		} else {
			Toast.makeText(getApplicationContext(),
					"Please enter username first", Toast.LENGTH_SHORT).show();
		}
	}

	public void createClientActivity(View view) {
		saveUsername();
		if (this.hasEnteredUname) {
			Intent intent = new Intent(this, ClientActivity.class);
			intent.putExtra("username", this.username);
			startActivity(intent);
			Log.d("main activity", "creating client");
			finish();
		} else {
			Toast.makeText(getApplicationContext(),
					"Please enter username first", Toast.LENGTH_SHORT).show();
		}
	}

	public void saveUsername() {
		this.username = this.unameField.getText().toString();
		if (!username.isEmpty()) {
			this.hasEnteredUname = true;
		}
	}

}
