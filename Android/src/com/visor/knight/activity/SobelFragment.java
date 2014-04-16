package com.visor.knight.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import as.adamsmith.etherealdialpad.dsp.ISynthService;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.visor.knight.R;
import com.visor.knight.camera.CameraHandler;
import com.visor.knight.view.EdgeView;

public class SobelFragment extends SherlockFragment implements ServiceConnection {

	private final String TAG = getClass().getSimpleName();
	private EdgeView edgeView;
	private SurfaceView surfaceView;
	
	private SurfaceHolder surfaceHolder;
	private CameraHandler cameraHandler;
	private ISynthService synthService;
	private MenuItem soundMenuItem, cameraMenuItem, shareMenuItem, edgesMenuItem;
    private boolean volume_enabled, softEdges=true;
    
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
        enableVolume(true);
    }
    
    public void onServiceDisconnected(ComponentName name) {
        synthService = null;
        enableVolume(false);
    }
    
    public void setSobelThresholdAsPercentage(int p) {
        edgeView.getEdgeConverter().setThreshold(150-p);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final int with_text_if_room = MenuItem.SHOW_AS_ACTION_IF_ROOM 
                                    | MenuItem.SHOW_AS_ACTION_WITH_TEXT;
        
        soundMenuItem = menu.add(R.string.ethereal_dialpad_menu_item);
        soundMenuItem.setShowAsAction(with_text_if_room);
        
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
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item == soundMenuItem){
            if (isEtherealDialpadInstalled()){
                switchVolumeState();
            } else {
                showEtherealDialpadMarketAlert();
            }
        } else if (item == cameraMenuItem){
            nextCameraClicked();
        } else if (item == shareMenuItem){
            shareImageClicked();
        } else if (item == edgesMenuItem){
            edgesMenuItemClicked();
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void edgesMenuItemClicked() {
        softEdges = !softEdges;
        edgesMenuItem.setTitle(softEdges ? R.string.menu_softedges_true : R.string.menu_softedges_false);
        edgeView.setSoftEdges(softEdges);
    }

    private void switchVolumeState(){
        volume_enabled = !volume_enabled;
        enableVolume(volume_enabled);
    }
    
    private void showEtherealDialpadMarketAlert(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.alertTitle);
        builder.setMessage(R.string.alertMessage);
        builder.setPositiveButton(R.string.alertPositiveButton,
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Uri url = Uri.parse(getString(R.string.etherealDialpadURL));
                    Intent intent = new Intent(Intent.ACTION_VIEW, url);
                    startActivity(intent);
                }
            });
        builder.setNegativeButton(R.string.alertNegativeButton, null);
        builder.show();
    }
    
    private boolean isEtherealDialpadInstalled() {
        return synthService != null;
    }
    
    private void nextCameraClicked() {
        cameraHandler.setToNextCamera();
    }
    
    private void shareImageClicked() {
        edgeView.captureNextFrame();
    }
    
    private void enableVolume(boolean enableIt) {
        if (edgeView != null){
            edgeView.setSynthService(enableIt ? synthService : null);
        }
        
        soundMenuItem.setIcon(enableIt ? R.drawable.ic_volume : R.drawable.ic_volume_off);
    }

	public void setKernel(int[][] nums) {
		//todo lol
	}
}
