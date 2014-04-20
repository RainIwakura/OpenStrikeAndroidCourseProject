package com.example.camera1;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends Activity {
   private static final String TAG = "MyActivity";
   private Camera cameraObject;
   private MediaRecorder mMediaRecorder;
   private ShowCamera showCamera;
   public static Activity activity;
   
   public static Camera isCameraAvailiable(){
	   int cameraCount = 0;
	    Camera cam = null;
	    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
	    cameraCount = Camera.getNumberOfCameras();
	    for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
	        Camera.getCameraInfo( camIdx, cameraInfo );
	        if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK  ) {
	            try {
	                cam = Camera.open( camIdx );
	            } catch (RuntimeException e) {
	            }
	        }
	    }
	    return cam;
   }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onStop() {
    	super.onPause();
        releaseCamera();  
    }
    
    private void releaseCamera(){
        if (cameraObject != null){
            cameraObject.release();        // release the camera for other applications
            cameraObject = null;
        }
    }
    
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      activity = this;
      cameraObject = isCameraAvailiable();
      ImageView preview_right = (ImageView) findViewById(R.id.camera_preview_right);
      ImageView preview_left = (ImageView) findViewById(R.id.camera_preview_left);
      preview_right.setImageResource(R.drawable.ic_launcher);
      showCamera = new ShowCamera(this, cameraObject, preview_left, preview_right);
      FrameLayout camera_preview = (FrameLayout) findViewById(R.id.camera_preview);
      camera_preview.addView(showCamera);
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }
}
