// Copyright 2007-2014 metaio GmbH. All rights reserved.
package mobile.openstrike.game;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.metaio.Example.BuildConfig;
import com.metaio.Example.R;
import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

	WebView mWebView;

	/**
	 * Task that will extract all the assets
	 */
	AssetsExtracter mTask;

	/**
	 * Progress view
	 */
	View mProgress;

	/**
	 * True while launching a tutorial, used to prevent multiple launches of the
	 * tutorial
	 */
	boolean mLaunchingTutorial;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.welcome_screen);

		// Enable metaio SDK log messages based on build configuration
		MetaioDebug.enableLogging(BuildConfig.DEBUG);

		mProgress = findViewById(R.id.progress);
		// mWebView = (WebView) findViewById(R.id.webview);

		// extract all the assets
		mTask = new AssetsExtracter();
		mTask.execute(0);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	/**
	 * This task extracts all the assets to an external or internal location to
	 * make them accessible to metaio SDK
	 */
	private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			mProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			try {
				// Extract all assets and overwrite existing files if debug
				// build
				AssetsManager.extractAllAssets(getApplicationContext(),
						BuildConfig.DEBUG);
			} catch (IOException e) {
				MetaioDebug.printStackTrace(Log.ERROR, e);
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mProgress.setVisibility(View.GONE);
		}

	}

	public void startGame(View v) {

		Intent i = new Intent(this, TutorialTrackingSamples.class);
		startActivity(i);
		this.finish();

	}

}
