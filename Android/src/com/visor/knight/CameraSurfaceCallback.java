package com.visor.knight;

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

    public CameraSurfaceCallback(Future<Camera> cameraFuture, PreviewCallback previewCallback) {
        this.cameraFuture = cameraFuture;
        this.previewCallback = previewCallback;
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        try {
            camera = cameraFuture.get();
            if (camera == null)
                return;
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
    }

    public void surfaceCreated(SurfaceHolder holder) {}
    public void surfaceDestroyed(SurfaceHolder holder) {}

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

    public void releaseCamera() {
        if (camera == null)
            return;

        try {
            camera.setPreviewDisplay(null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            /* this is the sequence given on android.com */
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            Log.d("Camera", "Camera has been released");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        releaseCamera();
    }
}
