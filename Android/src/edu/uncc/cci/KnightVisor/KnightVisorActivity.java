package edu.uncc.cci.KnightVisor;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

public class KnightVisorActivity extends Activity {

    /* lol, alphabetical order */
    private Camera        camera            = null;
    private EdgeView      edgeView          = null;
    private SurfaceHolder surfaceHolder     = null;

    private boolean cameraConfigured        = false;
    private boolean inPreview               = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /* set up window so we get full screen */
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        edgeView = new EdgeView(this);
        
        /* set up a surfaceView where the camera display will be put */
        SurfaceView surfaceView = new SurfaceView(this);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceDestroyed(SurfaceHolder holder) { }
            public void surfaceCreated  (SurfaceHolder holder) { }
            public void surfaceChanged  (SurfaceHolder holder, int format, int width, int height) {
                initializeCameraDimensions(width, height);
                startCameraPreview();
            }
        });
        
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(surfaceView); //still not entirely sure why this is necessary.
        frameLayout.addView(edgeView);
        setContentView(frameLayout);
    }    
    
    /* set up camera and configure with proper width/height */
    private void initializeCameraDimensions(int width, int height) {
        if (cameraConfigured || camera == null) 
            return;
        
        try {
            /* we're going to have the camera below whatever EdgeView doesn't draw */
            camera.setPreviewDisplay(surfaceHolder);
        } catch (Throwable t) {
            Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
            Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
        }

        Camera.Parameters parameters = camera.getParameters();
        Camera.Size       size       = getBestPreviewSize(width, height, parameters);

        if (size == null) 
            return;
        
        parameters.setPreviewSize(size.width, size.height);
        parameters.setPreviewFormat(ImageFormat.NV21);
        camera.setParameters(parameters);
        cameraConfigured = true;
    }
    
    /* start preview as long as the camera is configured */
    private void startCameraPreview() {
        if (cameraConfigured && camera != null) {
            camera.startPreview();
            inPreview = true;
        }
    }

    
    /* life cycle functions taking care of camera */
    
    @Override
    public void onResume() {
        super.onResume();

        camera = Camera.open();
        camera.setPreviewCallback(edgeView);
        startCameraPreview();
    }

    @Override
    public void onPause() {
        if (inPreview) {
            camera.stopPreview();
            inPreview = false;
        }
        
        if (camera != null)
        {
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }

        super.onPause();
    }

    
    /* helper function to set up the display */
    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        /* trying to get largest possible size */
        for (Camera.Size size : parameters.getSupportedPreviewSizes())
            if (size.width <= width && size.height <= height)
                if (result == null || size.width * size.height > result.width * result.height)
                    result = size;
        
        return result;
    }
}
