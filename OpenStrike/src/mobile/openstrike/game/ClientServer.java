package mobile.openstrike.game;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.metaio.Example.R;

public class ClientServer extends Activity {

	public ArrayList<String> IPs;
	private Handler handle;
	private String username;
	private String team;
	private EditText unameField;
	private boolean hasEnteredUname;
	private RadioGroup	teamGroup;
	private RadioButton	teamButton;
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
			teamGroup = (RadioGroup) findViewById(R.id.radioGroup1);
			int selectedId = teamGroup.getCheckedRadioButtonId();
			teamButton = (RadioButton) findViewById(selectedId);
			if (teamButton.getText().equals("Team 1")) {
				intent.putExtra("team", "1");
			} else {
				intent.putExtra("team", "2");
			}
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
