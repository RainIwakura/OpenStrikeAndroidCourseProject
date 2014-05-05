// Copyright 2007-2014 metaio GmbH. All rights reserved.
package com.metaio.Example;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.CameraVector;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.tools.io.AssetsManager;

public class TutorialTrackingSamples extends ARViewActivity {
	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final String TAG = "TutorialTrackingSamples";
	// private IGeometry mMetaioMan;
	private IGeometry mImagePlane;
	private IGeometry mHealth;
	private IGeometry mDead;
	private IGeometry mWin;
	private IGeometry mShield;
	private IGeometry mSword;
	private boolean block = false;
	// timer
	private int check = 0;

	Bitmap bitmapFromCamera;
	String trackingConfigFile;

	// timer
	private Button startButton;
	// private Button pauseButton;
	private Button stopButton;
	private TextView timerValue;
	private long startTime = 0L;
	private Handler customHandler = new Handler();
	long timeInMilliseconds = 0L;
	private Player player = new Player();
	// long timeSwapBuff = 0L;
	// long updatedTime = 0L;

	Handler cameraHandler = new Handler();
	private ImageView left, right;

	boolean isTracking = false;
	//

	private MetaioSDKCallbackHandler mCallbackHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mCallbackHandler = new MetaioSDKCallbackHandler();
		metaioSDK.requestScreenshot();

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
						} else if (poses.get(i).getCoordinateSystemID() == 3) {
							// start timer
							if (timeInMilliseconds == 0) {
								startTimer();
							}

							mShield.setCoordinateSystemID(poses.get(i)
									.getCoordinateSystemID());
							 mShield.setVisible(true);
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
		left = (ImageView) findViewById(R.id.left);
		right = (ImageView) findViewById(R.id.right);
		// right = (ImageView) findViewById(R.id.camera_preview_right);
		timerValue = (TextView) findViewById(R.id.timerValue);
		try {

			// Load desired tracking data for planar marker tracking
			trackingConfigFile = AssetsManager.getAssetPath(
					getApplicationContext(),
					"TutorialTrackingSamples/Assets/TrackingData_Marker.xml");
			metaioSDK.setTrackingConfiguration(trackingConfigFile);

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

	// public void clickTest() {
	// //left.setImageBitmap(bitmapLeft);
	// new Thread(
	// new Runnable() {
	// public void run() {
	// left.setImageBitmap(bitmapLeft);
	// }
	// }
	// ).start();
	// }

	public void clickTest(View v) {
		// left.setImageBitmap(bitmapLeft);
		startTime = SystemClock.uptimeMillis();
		cameraHandler.postDelayed(updateCameraViewThread, 0);// Causes the
																// Runnable r
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

	// timer thread
	private Runnable updateTimerThread = new Runnable() {

		public void run() {

			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

			// long timeInMilliseconds = System.currentTimeMillis() - startTime;
			int seconds = (int) (timeInMilliseconds / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;
			int milliseconds = (int) (timeInMilliseconds % 1000);
			timerValue.setText("" + minutes + ":"
					+ String.format("%02d", seconds) + ":"
					+ String.format("%03d", milliseconds));
			customHandler.postDelayed(this, 50);

		}

	};

	//

	// camer thread
	private Runnable updateCameraViewThread = new Runnable() {

		public void run() {

			left.setImageBitmap(bitmapFromCamera);
			right.setImageBitmap(bitmapFromCamera);
			cameraHandler.postDelayed(this, 0);

		}

	};

	@Override
	protected void onGeometryTouched(IGeometry geometry) {
		// TODO Auto-generated method stub

	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
		return mCallbackHandler;
	}

	private static File getOutputMediaFile(int type) {
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyCameraApp");
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".png");
		} else {
			return null;
		}

		return mediaFile;
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

		public void onScreenshotImage(ImageStruct cameraFrame) {
			// saving image
			// File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			// if (cameraFrame == null) {
			// Log.i(TAG, "no data");
			// } else {
			// try {
			// FileOutputStream fos = new FileOutputStream(pictureFile);
			// Bitmap bmp = cameraFrame.getBitmap();
			// bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
			// } catch (FileNotFoundException e) {
			// Log.d(TAG, "File not found: " + e.getMessage());
			// } catch (IOException e) {
			// Log.d(TAG, "Error accessing file: " + e.getMessage());
			// }
			// Log.i(TAG, "taken");
			// }

			bitmapFromCamera = cameraFrame.getBitmap();
			metaioSDK.requestScreenshot();

		}
	}

	@Override
	protected void startCamera() {
		// Start the first camera found by default
		final CameraVector cameras = metaioSDK.getCameraList();
		if (cameras.size() > 0) {
			com.metaio.sdk.jni.Camera camera = cameras.get(0);
			// cameras.get(0).
			metaioSDK.startCamera(camera);
		} else {
			MetaioDebug.log(Log.WARN, "No camera found on the device!");
		}
	}

}
