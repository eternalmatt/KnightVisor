package edu.uncc.cci.KnightVisor;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.SeekBar;

public class KnightVisorActivity extends Activity {

    EdgeView edgeView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        /* set up window so we get full screen */
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        
        /* set up a surfaceView where the camera display will be put */
        SurfaceView   surfaceView   = new SurfaceView(this);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() 
        {
            private Camera camera = null;
            
            public void surfaceDestroyed(SurfaceHolder holder) 
            {
                if (camera == null) return;
                
                try { 
                    camera.setPreviewDisplay(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                camera.stopPreview();
                camera.setPreviewCallback(null);
                camera.release();
                camera = null;
            }
            
            public void surfaceCreated  (SurfaceHolder holder)
            {   
                camera = Camera.open();
                
                try {
                    camera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                camera.setPreviewCallback(edgeView);
                camera.startPreview();
            }
            
            public void surfaceChanged  (SurfaceHolder holder, int format, int width, int height) 
            {
                if (camera == null) 
                    return;

                Camera.Parameters parameters = camera.getParameters();
                Camera.Size       size       = getBestPreviewSize(width, height, parameters);

                if (size == null) 
                    return;
                
                parameters.setPreviewSize(size.width, size.height);
                parameters.setPreviewFormat(ImageFormat.NV21);
                camera.setParameters(parameters);
                camera.startPreview();
            }
        });
        
        
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.mainFrameLayout);
        frameLayout.addView(surfaceView);
        
        edgeView = new EdgeView(this);
        frameLayout.addView(edgeView);
        
        
        SeekBar seekbar = (SeekBar)findViewById(R.id.seekBar);
        seekbar.setMax(150); 
        seekbar.setProgress(75);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch (SeekBar seekBar) { }
            public void onProgressChanged   (SeekBar seekBar, int progress, boolean fromUser) {
                edgeView.setThresholdManually(150 - progress);
            } 
        });
        
        
        CheckBox checkbox = (CheckBox)findViewById(R.id.medianCheckBox);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                edgeView.setMedianFiltering(checked);
            }
        });
    }
    
    
    /* helper function to set up the display */
    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        /* trying to get largest possible size */
        for (Camera.Size size : parameters.getSupportedPreviewSizes())
            if (size.width <= width && size.height <= height)
                if (result == null || size.width * size.height > result.width * result.height)
                    result = size;
        
        return result;
    }
    
    
    /* options menu */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.red:
                edgeView.setColorSelected(Color.RED);
                return true;
            case R.id.green:
                edgeView.setColorSelected(Color.GREEN);
                return true;
            case R.id.blue:
                edgeView.setColorSelected(Color.BLUE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
