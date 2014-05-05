// Copyright 2007-2014 metaio GmbH. All rights reserved.
package com.metaio.Example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.tools.io.AssetsManager;

public class TutorialTrackingSamples extends ARViewActivity {

	//private IGeometry mMetaioMan;
	private IGeometry mImagePlane;
	private IGeometry mHealth;

	String trackingConfigFile;

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
		return R.layout.tutorial_tracking_samples;
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
				////////////////////////////////////////////
				//Detect which picture is detected and assign appropriate asset
				for (int i = 0; i < poses.size(); i++) {
					if (poses.get(i).isTrackingState()) {
						if (poses.get(i).getCoordinateSystemID() == 1) {
							mHealth.setCoordinateSystemID(poses.get(i)
									.getCoordinateSystemID());
						} else if (poses.get(i).getCoordinateSystemID() == 2) {
							mImagePlane.setCoordinateSystemID(poses.get(i)
									.getCoordinateSystemID());
						}
					}
				}
				// ////////////////////////////////////
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

		boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
		MetaioDebug.log("Id Marker tracking data loaded: " + result);
		//mMetaioMan.setScale(new Vector3d(2f, 2f, 2f));
	}

	public void onPictureButtonClick(View v) {
		trackingConfigFile = AssetsManager
				.getAssetPath(getApplicationContext(),
						"TutorialTrackingSamples/Assets/TrackingData_PictureMarker.xml");
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

	@Override
	protected void loadContents() {
		try {

			// Load desired tracking data for planar marker tracking
//			trackingConfigFile = AssetsManager
//					.getAssetPath(getApplicationContext(),
//							"TutorialTrackingSamples/Assets/TrackingData_MarkerLess.xml");

			// Loading image geometry
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
			
			
			final String modelPath = AssetsManager.getAssetPath(getApplicationContext(), "TutorialTrackingSamples/Assets/health/health.md2");			
			if (modelPath != null) 
			{
				mHealth = metaioSDK.createGeometry(modelPath);
				if (mHealth != null) 
				{
					// Set geometry properties
					mHealth.setScale(0.05f);
					mHealth.setRotation(new Rotation(4f, 4f,4f));
					
					MetaioDebug.log("Loaded geometry "+ modelPath);
				}
				else
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "+modelPath);
			}	

		} catch (Exception e) {

		}
	}

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
