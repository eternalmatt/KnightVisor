package com.visor.knight.camera;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;

public class CameraHandler {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Camera.PreviewCallback previewCallback;
    private final SurfaceHolder surfaceHolder;
    private final int numberOfCameras = Camera.getNumberOfCameras();

    private int cameraId = 0;
    private CameraSurfaceCallback surfaceHolderCallback = null;
    
    
    @SuppressWarnings("deprecation")
    public CameraHandler(Camera.PreviewCallback previewCallback, SurfaceHolder surfaceHolder) {
        this.previewCallback = previewCallback;
        this.surfaceHolder = surfaceHolder;
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void increaseCameraId(int cameraId) {

        // reset cameraId if invalid
        cameraId %= numberOfCameras;
        
        // don't bother opening/closing camera if the same
        if (cameraId == this.cameraId) {
            return;
        } else {
            this.cameraId = cameraId;
            openCamera();
        }
    }

    public void setToNextCamera() {
        increaseCameraId(cameraId + 1);
    }

    /**
     * Whoever is opening the camera hopefully has a good reason to do so, and
     * the cameraId is already set
     */
    public void openCamera() {
        releaseCamera();
        
        CallableCamera callable = new CallableCamera(cameraId);
        Future<Camera> futureCamera = executor.submit(callable);
        surfaceHolderCallback = new CameraSurfaceCallback(futureCamera, previewCallback);
        
        surfaceHolder.addCallback(surfaceHolderCallback);
    }
    
    public void releaseCamera() {
        if (surfaceHolderCallback != null) {
            surfaceHolderCallback.releaseCamera();
            surfaceHolder.removeCallback(surfaceHolderCallback);
        }
        surfaceHolderCallback = null;
    }
     
}
