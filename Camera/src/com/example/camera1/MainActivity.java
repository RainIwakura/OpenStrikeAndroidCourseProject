package com.example.camera1;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private static final String TAG = "MyActivity";
	private Camera cameraObject;
	private MediaRecorder mMediaRecorder;
	private ShowCamera showCamera;
	public static Activity activity;

	public static Camera isCameraAvailiable() {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				try {
					cam = Camera.open(camIdx);
				} catch (RuntimeException e) {
				}
			}
		}
		return cam;
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera(); // release the camera immediately on pause event
	}

	@Override
	protected void onStop() {
		super.onPause();
		releaseCamera();
	}

	private void releaseCamera() {
		if (cameraObject != null) {
			cameraObject.release(); // release the camera for other applications
			cameraObject = null;
		}
	}

	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			System.out.println("!!!WHAHAHA  "+size.width + " " + size.height);
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}

		return (result);
	} 


	private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			if (result == null) {
				result = size;
			} else {
				int resultArea = result.width * result.height;
				int newArea = size.width * size.height;

				if (newArea < resultArea) {
					result = size;
				}
			}
		}

		return (result);
	}

	final String PreviewSize = "PreviewSize";
	final String PictureSize = "PictureSize";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		activity = this;
		cameraObject = isCameraAvailiable();
		Camera.Parameters parameters = cameraObject.getParameters();
		Camera.Size size = getBestPreviewSize(3000, 2000, parameters);
		Camera.Size pictureSize = getSmallestPictureSize(parameters);
		size.width = 320;
		size.height = 240;
//		pictureSize.width = 640;
//		pictureSize.height = 480;
		parameters.setPreviewSize(size.width, size.height);
		parameters.setPictureSize(pictureSize.width, pictureSize.height);
		
		System.out.println("WHAHAHA  "+pictureSize.width + " " + pictureSize.height);
		parameters.setPictureFormat(ImageFormat.JPEG);
		parameters.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);
		
		cameraObject.setParameters(parameters);
		ImageView preview_right = (ImageView) findViewById(R.id.camera_preview_right);
		ImageView preview_left = (ImageView) findViewById(R.id.camera_preview_left);
		preview_right.setImageResource(R.drawable.ic_launcher);
		showCamera = new ShowCamera(this, cameraObject, preview_left,
				preview_right);
		FrameLayout camera_preview = (FrameLayout) findViewById(R.id.camera_preview);
		camera_preview.addView(showCamera);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
