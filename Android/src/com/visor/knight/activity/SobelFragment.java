package com.visor.knight.activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.visor.knight.R;
import com.visor.knight.camera.CameraHandler;
import com.visor.knight.view.EdgeView;

public class SobelFragment extends SherlockFragment {

	private final String TAG = getClass().getSimpleName();
	private EdgeView edgeView;
	private SurfaceView surfaceView;
	
	private SurfaceHolder surfaceHolder;
	private CameraHandler cameraHandler;
	private MenuItem cameraMenuItem;
	private MenuItem edgesMenuItem;
    private MenuItem shareMenuItem;
    private boolean softEdges;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }    
	
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
        cameraHandler.openCamera();
    }
    
    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
    	cameraHandler.releaseCamera();
    	super.onStop();
    }
    
    public void setSobelThresholdAsPercentage(int p) {
        edgeView.getEdgeConverter().setThreshold(150-p);
    }
    
    /* MENU CODE BELOW */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final int with_text_if_room = MenuItem.SHOW_AS_ACTION_IF_ROOM 
                                    | MenuItem.SHOW_AS_ACTION_WITH_TEXT;
        
        if (Camera.getNumberOfCameras() > 1) {
            cameraMenuItem = menu.add(R.string.camera_menu_item);
            cameraMenuItem.setIcon(R.drawable.ic_menu_camera);
            cameraMenuItem.setShowAsAction(with_text_if_room);
        }
        
        shareMenuItem = menu.add(R.string.share_menu_item);
        shareMenuItem.setIcon(R.drawable.ic_menu_share);
        shareMenuItem.setShowAsAction(with_text_if_room);
        
        edgesMenuItem = menu.add(R.string.menu_softedges_true);
        edgesMenuItem.setShowAsAction(with_text_if_room);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == shareMenuItem){
            shareImageClicked();
        } else if (item == edgesMenuItem){
            edgesMenuItemClicked();
        } else if (item == cameraMenuItem){
            nextCameraClicked();
        }
        return super.onOptionsItemSelected(item);
    }
    
    protected void edgesMenuItemClicked() {
        softEdges = !softEdges;
        edgesMenuItem.setTitle(softEdges ? R.string.menu_softedges_true : R.string.menu_softedges_false);
        edgeView.setSoftEdges(softEdges);
    }
    
    private void nextCameraClicked() {
        cameraHandler.setToNextCamera();
    }
    
    private void shareImageClicked() {
        edgeView.captureNextFrame();
    }

	public void setKernel(int[][] nums) {
		//todo lol
	}
}
