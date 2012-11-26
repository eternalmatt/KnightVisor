
package com.visor.knight;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraHandler {
    private static final String TAG = CameraHandler.class.getSimpleName();

    private Camera.PreviewCallback previewCallback;
    private SurfaceHolder surfaceHolder;
    private final int numberOfCameras = Camera.getNumberOfCameras();

    private int cameraId = 0;
    private Camera camera = null;

    private AsyncTask<Integer, Void, Camera> cameraOpener = null;
    private SurfaceHolder.Callback surfaceHolderCallback = null;

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {

        // reset cameraId if invalid
        if (cameraId >= numberOfCameras)
            cameraId = 0;

        // don't bother opening/closing camera if the same
        if (cameraId == this.cameraId)
            return;

        this.cameraId = cameraId;
        openCamera();
    }

    public void setToNextCamera() {
        setCameraId(cameraId + 1);
    }

    public int getNumberOfCameras() {
        return numberOfCameras;
    }
   
    public Camera.PreviewCallback getPreviewCallback() {
		return previewCallback;
	}

	public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
		this.previewCallback = previewCallback;
	}

	public SurfaceHolder getSurfaceHolder() {
		return surfaceHolder;
	}

	public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
		this.surfaceHolder = surfaceHolder;
	}

	@SuppressWarnings("deprecation")
	public CameraHandler(Camera.PreviewCallback previewCallback, SurfaceHolder surfaceHolder) {
        this.previewCallback = previewCallback;
        this.surfaceHolder = surfaceHolder;
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Whoever is opening the camera hopefully has a good reason to do so, and
     * the cameraId is already set
     */
    public void openCamera() {
        surfaceHolder.removeCallback(surfaceHolderCallback);

        cameraOpener = new AsyncTask<Integer, Void, Camera>() {
            protected void onPreExecute() {
                releaseCamera();
            }

            protected Camera doInBackground(Integer... params) {
            	try {
            		return Camera.open(params[0]);
            	} catch (RuntimeException e) {
            		e.printStackTrace();
            		return null;
            	}            	
            }

            protected void onPostExecute(Camera result) {
            	Log.d("Camera", "Camera has been opened");
                camera = result;
            }
        };
        cameraOpener.execute(cameraId);

        this.surfaceHolderCallback = getNewSurfaceHolderCallback();

        surfaceHolder.addCallback(surfaceHolderCallback);
    }

    public boolean releaseCamera() {
        if (camera == null)
            return true;

        try {
            camera.setPreviewDisplay(null);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {

            /* this is the sequence given on android.com */
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            Log.d("Camera", "Camera has been released");
        }
        return true;
    }

    protected SurfaceHolder.Callback getNewSurfaceHolderCallback() {
        return new SurfaceHolder.Callback() {
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                try {
                    if (camera == null)
                        camera = cameraOpener.get();
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

            /* helper function to set up the display */
            private Camera.Size getBestPreviewSize(int width, int height,
                    Camera.Parameters parameters) {

                Camera.Size result = null;

                /* trying to get largest possible size */
                for (final Camera.Size size : parameters.getSupportedPreviewSizes())
                    if (size.width <= width && size.height <= height) {
                        if (result == null
                                || size.width * size.height > result.width * result.height) {
                            result = size;
                        }
                    }

                return result;
            }

            public void surfaceCreated(SurfaceHolder holder) {
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        };
    }
}
