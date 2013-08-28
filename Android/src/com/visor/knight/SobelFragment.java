package com.visor.knight;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import as.adamsmith.etherealdialpad.dsp.ISynthService;

public class SobelFragment extends Fragment implements ServiceConnection {

	private final String TAG = getClass().getSimpleName();
	//@InjectView(R.id.edgeView)		
	EdgeView	edgeView;
	//@InjectView(R.id.surfaceView)	
	SurfaceView surfaceView;
	
	private SurfaceHolder surfaceHolder;
	private CameraHandler cameraHandler;
	private ISynthService synthService;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.main, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.edgeView = (EdgeView) view.findViewById(R.id.edgeView);
		this.surfaceView = (SurfaceView) view.findViewById(R.id.surfaceView);
		this.surfaceHolder = surfaceView.getHolder();
		this.cameraHandler = new CameraHandler(edgeView, surfaceHolder);
	}

    @Override
    public void onStart() {
    	Log.d(TAG, "onStart");
    	super.onStart();
        final Intent synthServiceIntent = new Intent(ISynthService.class.getName());
        getActivity().bindService(synthServiceIntent, this, Context.BIND_AUTO_CREATE);
        cameraHandler.openCamera();
    }

    
    @Override
    public void onStop() {
    	Log.d(TAG, "onStop");
    	super.onStop();
    	cameraHandler.releaseCamera();
    	getActivity().unbindService(this);
    }
	

    public void onServiceConnected(ComponentName name, IBinder service) {
        synthService = ISynthService.Stub.asInterface(service);
        if (edgeView != null) {
        	edgeView.setSynthService(synthService);
        	//TODO: update options menu
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        synthService = null;
        if (edgeView != null) {
        	edgeView.setSynthService(null);
        }
    }
    
    public boolean isEtherealDialpadInstalled() {
    	return synthService != null;
    }
    
    public void setSobelThresholdAsPercentage(int p) {
        edgeView.getEdgeConverter().setThreshold(150-p);
    }
    
    
    public void nextCameraClicked() {
    	cameraHandler.setToNextCamera();
    }
    
    public void shareImageClicked() {
    	edgeView.captureNextFrame();
    }
    
	public void enableVolume(boolean enableIt) {
		edgeView.setSynthService(enableIt ? synthService : null);
	}
    
}
