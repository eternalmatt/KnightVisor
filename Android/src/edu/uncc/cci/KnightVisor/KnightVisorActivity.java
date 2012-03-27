package edu.uncc.cci.KnightVisor;

import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

public class KnightVisorActivity extends Activity implements SurfaceHolder.Callback {
    
	private Camera camera;
	private SurfaceView cameraView;
	private FrameLayout frameLayout;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the entire screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Create the surface for the camera to draw its preview on
		cameraView = new SurfaceView(this);
		cameraView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		cameraView.getHolder().addCallback(this);

		// Setup the layout where the cameraView is completely obscured by the edgeView
		frameLayout = new FrameLayout(this);
		frameLayout.addView(cameraView);
		setContentView(frameLayout);
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopCameraPreview();
	}

	private void stopCameraPreview() {
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	private void startCameraPreview() {
		try {
			camera = Camera.open();
			camera.setPreviewDisplay(cameraView.getHolder());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
			Toast.makeText(this, "Camera is not available", Toast.LENGTH_LONG).show();
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (camera != null)
		{
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(width, height); // TODO: check that width, height are a valid camera preview size
			camera.setParameters(parameters);
			camera.startPreview();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		startCameraPreview();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		stopCameraPreview();
	}
}