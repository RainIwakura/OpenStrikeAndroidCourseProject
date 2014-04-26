// Copyright 2007-2014 metaio GmbH. All rights reserved.
package com.metaio.Example;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.tools.io.AssetsManager;

public class TutorialTrackingSamples extends ARViewActivity {

	private IGeometry mImagePlane;
	private IGeometry mHealth;
	private IGeometry mDead;
	private IGeometry mWin;
	private IGeometry mShield;
	private IGeometry mSword;
	String trackingConfigFile;
	private boolean block = false;
	// timer
	private int check = 0;
	private Button startButton;
	// private Button pauseButton;
	private Button stopButton;
	private TextView timerValue;
	private long startTime = 0L;
	private Handler customHandler = new Handler();
	private Player player = new Player();

	long timeInMilliseconds = 0L;
	// long timeSwapBuff = 0L;
	// long updatedTime = 0L;

	boolean isTracking = false;
	//

	private MetaioSDKCallbackHandler mCallbackHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCallbackHandler = new MetaioSDKCallbackHandler();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCallbackHandler.delete();
		mCallbackHandler = null;
	}

	@Override
	protected int getGUILayout() {
		return R.layout.tutorial_trackingsamples3;
	}

	public synchronized void checkLife() {
		if (timeInMilliseconds > 1500L && !block) {
			block = true;
			stopTimer();
			check++;
			System.out.println(">> " + timeInMilliseconds + " " + check);
			System.out.println(player.getHealth());
			player.setHealth(player.getHealth() - 20);
			startTimer();
			mImagePlane.setVisible(false);
			mShield.setVisible(true);
		} else {
			mImagePlane.setVisible(true);
			mShield.setVisible(false);
		}
	}

	@Override
	public void onDrawFrame() {
		super.onDrawFrame();

		if (metaioSDK != null) {
			// get all detected poses/targets
			TrackingValuesVector poses = metaioSDK.getTrackingValues();
			// if we have detected one, attach our metaio man to this coordinate
			// system Id
			if (poses.size() != 0) {
				// Detect which picture is detected and assign appropriate asset
				for (int i = 0; i < poses.size(); i++) {
					if (poses.get(i).isTrackingState()) {
						if (poses.get(i).getCoordinateSystemID() == 5) {
							// start timer
							if (timeInMilliseconds == 0) {
								startTimer();
							}
							mHealth.setCoordinateSystemID(poses.get(i)
									.getCoordinateSystemID());
						} else if (poses.get(i).getCoordinateSystemID() == 2) {
							// start timer
							if (timeInMilliseconds == 0) {
								startTimer();
							}
							mSword.setCoordinateSystemID(poses.get(i)
									.getCoordinateSystemID());
							//mSword.setVisible(true); // ??????
						} else if (poses.get(i).getCoordinateSystemID() == 3) {
							// start timer
							if (timeInMilliseconds == 0) {
								startTimer();
							}

							mShield.setCoordinateSystemID(poses.get(i)
									.getCoordinateSystemID());
							//mShield.setVisible(true);
							if (timeInMilliseconds > 1000L) {
								mImagePlane.setCoordinateSystemID(poses.get(i)
										.getCoordinateSystemID());
								checkLife();
								if (!player.isAlive()) {
									mImagePlane.setVisible(false);
									mShield.setVisible(false);
									mDead.setCoordinateSystemID(poses.get(i)
											.getCoordinateSystemID());
								}
							} 
						}

						else if (poses.get(i).getCoordinateSystemID() == 4) {
							if (timeInMilliseconds == 0) {
								startTimer();
							}
							mWin.setCoordinateSystemID(poses.get(i)
									.getCoordinateSystemID());
						}
					} else {
						stopTimer();
					}
				}

			}

		}
	}

	public void onButtonClick(View v) {
		finish();
	}

	public void onIdButtonClick(View v) {
		trackingConfigFile = AssetsManager.getAssetPath(
				getApplicationContext(),
				"TutorialTrackingSamples/Assets/TrackingData_Marker.xml");

		MetaioDebug.log("Tracking Config path = " + trackingConfigFile);
		Log.v("!!!!", "Tracking Config path = " + trackingConfigFile);
		boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
		MetaioDebug.log("Id Marker tracking data loaded: " + result);
		// mMetaioMan.setScale(new Vector3d(2f, 2f, 2f));
	}

	public void onPictureButtonClick(View v) {
		trackingConfigFile = AssetsManager
				.getAssetPath(getApplicationContext(),

				"TutorialTrackingSamples/Assets/TrackingData_PictureMarker.xml");
		Log.v("!!!!", "Tracking Config path = " + trackingConfigFile);
		MetaioDebug.log("Tracking Config path = " + trackingConfigFile);

		boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
		MetaioDebug.log("Picture Marker tracking data loaded: " + result);

	}

	public void onMarkerlessButtonClick(View v) {
		trackingConfigFile = AssetsManager
				.getAssetPath(getApplicationContext(),

				"TutorialTrackingSamples/Assets/TrackingData_MarkerlessFast.xml");
		MetaioDebug.log("Tracking Config path = " + trackingConfigFile);

		boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
		MetaioDebug.log("Markerless tracking data loaded: " + result);

	}

	public void startTimer() {
		startTime = SystemClock.uptimeMillis();
		customHandler.postDelayed(updateTimerThread, 0);// Causes the Runnable r
														// to be added to the
														// message queue, to be
														// run after the
														// specified amount of
														// time elapses. The
														// runnable will be run
														// on the thread to
														// which this handler is
														// attached.

	}

	public void stopTimer() {
		timeInMilliseconds = 0;
		customHandler.removeCallbacks(updateTimerThread);
		block = false;
	}

	@Override
	protected void loadContents() {
		timerValue = (TextView) findViewById(R.id.timerValue);

		startButton = (Button) findViewById(R.id.startButton);

		startButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				startTimer();
			}
		});

		stopButton = (Button) findViewById(R.id.stopButton);
		stopButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				stopTimer();

			}
		});

		try {

			// Load desired tracking data for planar marker tracking
			trackingConfigFile = AssetsManager.getAssetPath(
					getApplicationContext(),
					"TutorialTrackingSamples/Assets/TrackingData_Marker.xml");
			metaioSDK.setTrackingConfiguration(trackingConfigFile);

			// Loading blood
			final String imagePath = AssetsManager.getAssetPath(
					getApplicationContext(),
					"TutorialTrackingSamples/Assets/health/blood.png");
			if (imagePath != null) {
				mImagePlane = metaioSDK.createGeometryFromImage(imagePath);
				if (mImagePlane != null) {
					mImagePlane.setScale(3.0f);
					MetaioDebug.log("Loaded geometry " + imagePath);
				} else {
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "
							+ imagePath);
				}
			}

			// Lost image
			final String deadPath = AssetsManager.getAssetPath(
					getApplicationContext(),
					"TutorialTrackingSamples/Assets/health/dead.jpg");

			if (deadPath != null) {
				mDead = metaioSDK.createGeometryFromImage(deadPath);
				if (mDead != null) {
					mDead.setScale(2.0f);
					MetaioDebug.log("Loaded geometry " + deadPath);
				} else {
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "
							+ deadPath);
				}
			}

			// Loading sword
			final String swordPath = AssetsManager.getAssetPath(
					getApplicationContext(),
					"TutorialTrackingSamples/Assets/sword.png");
			if (imagePath != null) {
				mSword = metaioSDK.createGeometryFromImage(swordPath);
				if (mSword != null) {
					mSword.setScale(1.0f);
					MetaioDebug.log("Loaded geometry " + swordPath);
				} else {
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "
							+ swordPath);
				}
			}

			// Loading win
			mWin = metaioSDK.createGeometryFromImage(AssetsManager
					.getAssetPath(getApplicationContext(),
							"TutorialTrackingSamples/Assets/win.jpg"));
			if (mWin != null) {
				mWin.setScale(1.0f);
				MetaioDebug.log("Loaded geometry ");
			}

			// Loading shield
			mShield = metaioSDK.createGeometryFromImage(AssetsManager
					.getAssetPath(getApplicationContext(),
							"TutorialTrackingSamples/Assets/shield.png"));
			if (mShield != null) {
				mShield.setScale(1.0f);
				MetaioDebug.log("Loaded geometry ");
			}

			// Health object
			final String modelPath = AssetsManager.getAssetPath(
					getApplicationContext(),
					"TutorialTrackingSamples/Assets/health/health.md2");
			if (modelPath != null) {
				mHealth = metaioSDK.createGeometry(modelPath);
				if (mHealth != null) {
					// Set geometry properties
					mHealth.setScale(0.05f);
					mHealth.setRotation(new Rotation(4f, 4f, 4f));

					MetaioDebug.log("Loaded geometry " + modelPath);
				} else
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "
							+ modelPath);
			}

		} catch (Exception e) {

		}
	}

	// timer thread
	private Runnable updateTimerThread = new Runnable() {

		public void run() {

			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			// updatedTime = timeSwapBuff + timeInMilliseconds;
			//
			// int secs = (int) (updatedTime / 1000);
			// int mins = secs / 60;
			// secs = secs % 60;
			// int milliseconds = (int) (updatedTime % 1000);
			// timerValue.setText("" + mins + ":"
			// + String.format("%02d", secs) + ":"
			// + String.format("%03d", milliseconds));
			// customHandler.postDelayed(this, 0);
			//

			// long timeInMilliseconds = System.currentTimeMillis() - startTime;
			int seconds = (int) (timeInMilliseconds / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;
			int milliseconds = (int) (timeInMilliseconds % 1000);
			timerValue.setText("" + minutes + ":"
					+ String.format("%02d", seconds) + ":"
					+ String.format("%03d", milliseconds));
			customHandler.postDelayed(this, 50);

			// customHandler.postDelayed(this, 0);//50

		}

	};

	//

	@Override
	protected void onGeometryTouched(IGeometry geometry) {
		// TODO Auto-generated method stub

	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
		return mCallbackHandler;
	}

	final class MetaioSDKCallbackHandler extends IMetaioSDKCallback {

		@Override
		public void onSDKReady() {
			// show GUI
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mGUIView.setVisibility(View.VISIBLE);
				}
			});
		}
	}

}