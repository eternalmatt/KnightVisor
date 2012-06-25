package com.visor.knight;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;
import as.adamsmith.etherealdialpad.dsp.ISynthService;

public class KnightVisorActivity extends Activity {

    public static final String TAG = KnightVisorActivity.class.getSimpleName();

    private Camera camera = null;
    private EdgeView edgeView = null;
    private ISynthService synthService = null;
    private ServiceConnection synthServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            synthService = ISynthService.Stub.asInterface(service);
            if (edgeView != null) edgeView.synthService = synthService;
        }

        public void onServiceDisconnected(ComponentName name) {
            synthService = null;
            if (edgeView != null) edgeView.synthService = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open();
        Intent synthServiceIntent = new Intent(ISynthService.class.getName());
        bindService(synthServiceIntent, synthServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

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
        setContentView(R.layout.main);
        edgeView = (EdgeView)this.findViewById(R.id.edgeView);
        if (synthService != null) {
            edgeView.synthService = synthService;
        }

        /* set up window so we get full screen */
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /* set up a surfaceView where the camera display will be put */
        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
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

        SeekBar seekbar = (SeekBar)findViewById(R.id.seekBar);
        seekbar.setMax(150);
        seekbar.setProgress(75);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {}

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                edgeView.setThresholdManually(150 - progress);
            }
        });

        final Context ctx = this;
        ((CheckBox)findViewById(R.id.medianCheckBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                edgeView.setMedianFiltering(checked);
                if (checked) Toast.makeText(ctx, "Median Filtering", Toast.LENGTH_SHORT).show();
            }
        });

        ((CheckBox)findViewById(R.id.automaticCheckBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                edgeView.automaticThresholding(checked);
                if (checked) Toast.makeText(ctx, "Automatic Thresholding", Toast.LENGTH_SHORT).show();
            }
        });

        ((CheckBox)findViewById(R.id.logarithmicCheckBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                edgeView.logarithmicTransform(checked);
                if (checked) Toast.makeText(ctx, "Log Transform", Toast.LENGTH_SHORT).show();
            }
        });

        ((CheckBox)findViewById(R.id.grayscaleCheckBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                edgeView.grayscaleOnly(checked);
                if (checked) Toast.makeText(ctx, "Grayscale", Toast.LENGTH_SHORT).show();
            }
        });

        ((CheckBox)findViewById(R.id.edgeIntensityCheckBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                edgeView.setSoftEdges(checked);
                if (checked) Toast.makeText(ctx, "Soft Edges", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.takePictureButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (camera == null) return;
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    public void onPictureTaken(byte[] data, Camera camera) {

                        final File path = new File(Environment.getExternalStorageDirectory(), ctx.getPackageName());
                        if (false == path.exists()) path.mkdir();

                        File file = new File(path, "image.png");

                        OutputStream os = null;
                        try {
                            os = new FileOutputStream(file);
                        } catch (FileNotFoundException e) {
                            Toast.makeText(ctx, "File not written", Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        }

                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Log.d(TAG, "Bitmap decoded");
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                        Log.d(TAG, "Bitmap compressed");

                        camera.addCallbackBuffer(data);
                        Log.d(TAG, "camera::addCallbackBuffer");
                        camera.startPreview();
                        Log.d(TAG, "camera::startPreview");

                    }
                });
            }
        });

    }

    /* options menu */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

            case R.id.launchBrowser:

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
