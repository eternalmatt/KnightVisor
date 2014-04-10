package com.visor.knight.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import as.adamsmith.etherealdialpad.dsp.ISynthService;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.visor.knight.R;
import com.visor.knight.extra.EtherealDialpadMarketAlert;

public class KnightVisorActivity extends SherlockFragmentActivity implements ActivityDotH, ServiceConnection {

    private SobelFragment sobelFragment;
    
    private ISynthService synthService;
    private MenuItem soundMenuItem;
    private boolean volumeEnabled, isEtherealDialpadInstalled;
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        sobelFragment = (SobelFragment) this.getSupportFragmentManager().findFragmentById(R.id.sobel_fragment);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
	        getSupportActionBar().setCustomView(R.layout.seekbar);
	        getSupportActionBar().setDisplayShowHomeEnabled(false);
	        getSupportActionBar().setDisplayShowCustomEnabled(true);
	
	        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
	        seekBar.setMax(150);
	        seekBar.setProgress(75);
	        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        }
        
        /* set up window so we get full screen */
        final Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        final Intent synthServiceIntent = new Intent(ISynthService.class.getName());
        bindService(synthServiceIntent, this, Context.BIND_AUTO_CREATE);
        enableVolume(false); //default false. true when connected.
    }
    
    @Override
    protected void onStop() {
        unbindService(this);
        super.onStop();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (synthService == null){
            return false;
        }
        
        final float w = sobelFragment.getView().getWidth();
        final float h = sobelFragment.getView().getWidth();
        final float u = Math.min(1, Math.max(0, event.getX() / w));
        final float v = Math.min(1, Math.max(0, 1.0f - event.getY() / h));

        try {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    synthService.primaryOff();
                    synthService.primaryXY(u, v);
                    break;
                case MotionEvent.ACTION_DOWN:
                    synthService.primaryOn();
                case MotionEvent.ACTION_MOVE:
                    synthService.primaryXY(u, v);
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    /* MENU CODE BELOW */
    
    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override public void onStopTrackingTouch(SeekBar seekBar) {}
		@Override public void onStartTrackingTouch(SeekBar seekBar) {}
		@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            sobelFragment.setSobelThresholdAsPercentage(progress);
		}
	};

	@Override
    public SeekBar.OnSeekBarChangeListener getOnSeekBarChangeListener() {
		return onSeekBarChangeListener;
	}

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        synthService = ISynthService.Stub.asInterface(service);
        enableVolume(true);
        isEtherealDialpadInstalled = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        synthService = null;
        enableVolume(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final int with_text_if_room = MenuItem.SHOW_AS_ACTION_IF_ROOM 
                                    | MenuItem.SHOW_AS_ACTION_WITH_TEXT;

        soundMenuItem = menu.add(R.string.ethereal_dialpad_menu_item);
        soundMenuItem.setShowAsAction(with_text_if_room);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item == soundMenuItem){
            if (isEtherealDialpadInstalled()){
                switchVolumeState();
            } else {
                showEtherealDialpadMarketAlert();
            }
        } 
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showEtherealDialpadMarketAlert(){
        EtherealDialpadMarketAlert.show(this);
    }
    
    private boolean isEtherealDialpadInstalled() {
        return isEtherealDialpadInstalled;
    }
    
    private void switchVolumeState(){
        volumeEnabled = !volumeEnabled;
        enableVolume(volumeEnabled);
    }
    
    private void enableVolume(boolean enableIt) {
        soundMenuItem.setIcon(enableIt ? R.drawable.ic_volume : R.drawable.ic_volume_off);
    }
}
