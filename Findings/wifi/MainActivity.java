package com.example.wifiserver;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends Activity {

	public ArrayList<String> IPs;
	private Handler handle;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
	
	public void createServerActivity (View view){
		Intent intent = new Intent(this, ServerActivity.class);
/*		EditText editText = (EditText)findViewById(R.id.MainEdit);
		String email = editText.getText().toString();
		intent.putExtra(EMAIL, email);*/
		
	//	intent.putExtra("HANDLE", ha);
		startActivity(intent);
		finish();
	}
	public void createClientActivity (View view){
		Intent intent = new Intent(this, ClientActivity.class);
/*		EditText editText = (EditText)findViewById(R.id.MainEdit);
		String email = editText.getText().toString();
		intent.putExtra(EMAIL, email);*/
		
	//	intent.putExtra("HANDLE", ha);
		startActivity(intent);
		finish();
	}

}
