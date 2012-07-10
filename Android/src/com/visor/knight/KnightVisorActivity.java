package com.visor.knight;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import as.adamsmith.etherealdialpad.dsp.ISynthService;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class KnightVisorActivity extends Activity {

    public static final String TAG = KnightVisorActivity.class.getSimpleName();

    private ActionBar actionBar = null;
    private Camera camera = null;
    private EdgeView edgeView = null;
    private SynthServiceConnection synthServiceConnection = new SynthServiceConnection();

    @Override
    protected void onStart() {
        super.onStart();
        camera = Camera.open();
        Intent synthServiceIntent = new Intent(ISynthService.class.getName());
        bindService(synthServiceIntent, synthServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(synthServiceConnection);

        if (camera == null) return;

        try {
            camera.setPreviewDisplay(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* this is the sequence given on android.com */
        camera.stopPreview();
        camera.setPreviewCallback(null);
        camera.release();
        camera = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_with_actionbar);
        edgeView = (EdgeView)this.findViewById(R.id.edgeView);
        synthServiceConnection.addViewToBeNotified(edgeView);

        actionBar = (ActionBar)findViewById(R.id.actionbar);
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.setHomeAction(new ActionBar.Action() {
            public void performAction(View view) {
                edgeView.setColorSelected(Color.GREEN);
            }

            public int getDrawable() {
                return R.drawable.ic_launcher;
            }
        });
        actionBar.addAction(new Action() {
            public void performAction(View view) {
                edgeView.captureNextFrame();
            }

            public int getDrawable() {
                return R.drawable.ic_menu_camera;
            }
        });
        actionBar.addAction(new Action() {
            public void performAction(View view) {
                KnightVisorActivity.this.openOptionsMenu();
            }

            public int getDrawable() {
                return R.drawable.ic_menu_moreoverflow_normal_holo_dark;
            }
        });

        SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setMax(150);
        seekBar.setProgress(75);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {}

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                edgeView.setThresholdManually(150 - progress);
            }
        });

        /* set up window so we get full screen */
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /* set up a surfaceView where the camera display will be put */
        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceDestroyed(SurfaceHolder holder) {} /* doesn't matter */

            public void surfaceCreated(SurfaceHolder holder) {} /* doesn't matter */

            public void surfaceChanged(SurfaceHolder holder, int format, final int width, final int height) {
                try {
                    camera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height, parameters);

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
                camera.setPreviewCallbackWithBuffer(edgeView);
                camera.startPreview();
            }

            /* helper function to set up the display */
            private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
                Camera.Size result = null;

                /* trying to get largest possible size */
                for (Camera.Size size : parameters.getSupportedPreviewSizes())
                    if (size.width <= width && size.height <= height) {
                        if (result == null || size.width * size.height > result.width * result.height) {
                            result = size;
                        }
                    }

                return result;
            }
        }); // end SurfaceHolder.Callback

    }

    /* options menu */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.red) {
            edgeView.setColorSelected(Color.RED);
            return true;
        } else if (item.getItemId() == R.id.green) {
            edgeView.setColorSelected(Color.GREEN);
            return true;
        } else if (item.getItemId() == R.id.blue) {
            edgeView.setColorSelected(Color.BLUE);
            return true;
        } else if (item.getItemId() == R.id.launchBrowser) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.alertTitle);
            builder.setMessage(R.string.alertMessage);
            builder.setPositiveButton(R.string.alertPositiveButton, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Uri url = Uri.parse(getString(R.string.etherealDialpadURL));
                    Intent intent = new Intent(Intent.ACTION_VIEW, url);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(R.string.alertNegativeButton, null);
            builder.show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
