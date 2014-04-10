package com.visor.knight.camera;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;

public final class CameraSurfaceCallback implements SurfaceHolder.Callback {

    private final String TAG = getClass().getSimpleName();
    private final Future<Camera> cameraFuture;
    private final Camera.PreviewCallback previewCallback;
    
    private Camera camera;
    private boolean released = false;

    public CameraSurfaceCallback(Future<Camera> cameraFuture, PreviewCallback previewCallback) {
        this.cameraFuture = cameraFuture;
        this.previewCallback = previewCallback;
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        try {
            camera = cameraFuture.get();
            if (camera == null) {
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return;
        }

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Camera.Parameters parameters = camera.getParameters();
        final Camera.Size size = getBestPreviewSize(width, height, parameters);

        /* if we can't get a proper size, don't display */
        if (size == null) {
            Log.e(TAG, "Could not get a valid camera size");
            return;
        }

        parameters.setPreviewSize(size.width, size.height);
        parameters.setPreviewFormat(ImageFormat.NV21);

        /* set up the camera and start preview */
        camera.setParameters(parameters);
        camera.addCallbackBuffer(new byte[size.width * size.height * 4]);
        camera.setPreviewCallbackWithBuffer(previewCallback);
        camera.startPreview();
        Log.d(TAG, "Camera acquired");
    }

    @Override public void surfaceCreated(SurfaceHolder holder) {}
    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    /* helper function to set up the display */
    private static Camera.Size getBestPreviewSize(int width, int height,
            Camera.Parameters parameters) {

        Camera.Size result = null;

        /* trying to get largest possible size */
        for (final Camera.Size size : parameters.getSupportedPreviewSizes())
            if (size.width <= width && size.height <= height) {
                if (result == null || size.width * size.height > result.width
                                * result.height) {
                    result = size;
                }
            }

        return result;
    }

    /** necessary, absurd null checking in this method */
    public void releaseCamera() {
        if (released){
            return;
        }
        
        try {
            if (camera == null) {
                camera = cameraFuture.get();// attempt to get camera out of Future
                if (camera == null){
                    return; 
                }
            }
            
            camera.setPreviewDisplay(null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            /* this is the sequence given on android.com */
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            released = true;
            Log.d(TAG, "Camera has been released");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        releaseCamera();
    }
}
