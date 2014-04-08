package com.visor.knight.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.visor.knight.R;

public class KnightVisorActivity extends SherlockFragmentActivity implements ActivityDotH {

    private SobelFragment sobelFragment;
   
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
	        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	            public void onStopTrackingTouch(SeekBar seekBar) {}
	            public void onStartTrackingTouch(SeekBar seekBar) {}
	            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	            }
	        });
        }

        /* set up window so we get full screen */
        final Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override public void onStopTrackingTouch(SeekBar seekBar) {}
		@Override public void onStartTrackingTouch(SeekBar seekBar) {}
		@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            sobelFragment.setSobelThresholdAsPercentage(progress);
		}
	};

	public SeekBar.OnSeekBarChangeListener getOnSeekBarChangeListener() {
		return onSeekBarChangeListener;
	}
}
