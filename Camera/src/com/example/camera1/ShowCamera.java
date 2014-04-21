package com.example.camera1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {
   private static final String TAG = "MyActivity";
   private SurfaceHolder holdMe;
   private Camera theCamera;
   private ImageView showLeft, showRight;
   private Bitmap bitmapLeft, bitmapRight;
   
   public ShowCamera(Context context,Camera camera, ImageView left, ImageView right) {
      super(context);
      theCamera = camera;
      holdMe = getHolder();
      showLeft = left;
      showRight = right;
      holdMe.addCallback(this);
   }

   @Override
   public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
   }

   @Override
   public void surfaceCreated(SurfaceHolder holder) {
      try   {
         theCamera.setPreviewDisplay(holder);
         theCamera.setPreviewCallback( new PreviewCallback() {
             public void onPreviewFrame( byte[] data, Camera camera ) {
                 if ( camera != null )
                 {
                     Camera.Parameters parameters = camera.getParameters();
                     int imageFormat = parameters.getPreviewFormat();
                     Bitmap bitmap = null;
                     
                     if ( imageFormat == ImageFormat.NV21 )
                     {
                         int w = parameters.getPreviewSize().width;
                         int h = parameters.getPreviewSize().height;
                         YuvImage yuvImage = new YuvImage( data, imageFormat, w, h, null );
                         Rect rect = new Rect( 0, 0, w, h );
                         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                         yuvImage.compressToJpeg( rect, 100, outputStream );

                         bitmap = BitmapFactory.decodeByteArray( outputStream.toByteArray(), 0, outputStream.size() );
                         bitmapLeft = BitmapFactory.decodeByteArray( outputStream.toByteArray(), 0, outputStream.size() );
                         bitmapRight = BitmapFactory.decodeByteArray( outputStream.toByteArray(), 0, outputStream.size() );
                     }
                     else if ( imageFormat == ImageFormat.JPEG || imageFormat == ImageFormat.RGB_565 )
                     {
                         bitmap = BitmapFactory.decodeByteArray( data, 0, data.length );
                         bitmapLeft = BitmapFactory.decodeByteArray( data, 0, data.length );
                         bitmapRight = BitmapFactory.decodeByteArray( data, 0, data.length );
                     }

                     if ( bitmap != null )
                     {
                    	 bitmap.recycle();
                         bitmap = null;
//                    	 FileOutputStream fileStream;
//                         try {
//                             String filePath = "/sdcard/pictures/image_preview.jpg";
//                             File imageFile = new File( filePath );
//                             fileStream = new FileOutputStream( imageFile );
//                             bitmapLeft.compress(Bitmap.CompressFormat.JPEG, 80, fileStream);
//                             fileStream.flush();
//                             fileStream.close();
//                         } catch (FileNotFoundException e) {
//                             Log.e( TAG, e.toString() );
//                         } catch (IOException e) {
//                             Log.e(TAG, e.toString() );
//                         }
                    	 //Log.i(TAG, "cloning bitmap");
                    	 showLeft.setImageBitmap(bitmapLeft);
                    	 showRight.setImageBitmap(bitmapRight);
                     }
                     if ( bitmap == null )
                     {
//                    	 Log.i(TAG, "bitmap is null");
                     }
//                     Log.e(TAG, "onPreviewFrame" );
                 }
                 else
                 {
//                     Log.e(TAG, "Camera is null" );
                 }
             }
         });
         theCamera.startPreview(); 
      } catch (IOException e) {
      }
   }

   @Override
   public void surfaceDestroyed(SurfaceHolder arg0) {
   }

}