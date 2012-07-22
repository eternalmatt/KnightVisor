
package com.visor.knight;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import as.adamsmith.etherealdialpad.dsp.ISynthService;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

public class KnightVisorActivity extends SherlockActivity implements ServiceConnection {

    public static final String TAG = KnightVisorActivity.class.getSimpleName();

    private boolean ethereal_diaplad_installed = false;
    private boolean volume_enabled = false;
    private EdgeView edgeView = null;
    private ISynthService synthService = null;
    private CameraHandler cameraHandler = null;

    /* ServiceConnection methods to work with Adam Smith's ISynthService */

    public void onServiceConnected(ComponentName name, IBinder service) {
        synthService = ISynthService.Stub.asInterface(service);
        edgeView.setSynthService(synthService);
        ethereal_diaplad_installed = synthService != null;
        volume_enabled = true;
        invalidateOptionsMenu();
    }

    public void onServiceDisconnected(ComponentName name) {
        synthService = null;
        edgeView.setSynthService(null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Intent synthServiceIntent = new Intent(ISynthService.class.getName());
        bindService(synthServiceIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        cameraHandler.releaseCamera();
        unbindService(this);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        edgeView = (EdgeView) this.findViewById(R.id.edgeView);
        edgeView.setSynthService(synthService);

        cameraHandler = new CameraHandler(edgeView);

        getSupportActionBar().setCustomView(R.layout.seekbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(150);
        seekBar.setProgress(75);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                edgeView.setThresholdManually(150 - progress);
            }
        });

        /* set up window so we get full screen */
        final Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /* set up a surfaceView where the camera display will be put */
        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        final SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(cameraHandler);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final int with_text_if_room = MenuItem.SHOW_AS_ACTION_IF_ROOM
                // | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT;

        if (cameraHandler.getNumberOfCameras() > 1) {
            MenuItem camera = menu.add("Camera");
            camera.setOnMenuItemClickListener(cameraMenuItemClickListener);
            camera.setShowAsAction(with_text_if_room);
        }

        MenuItem share = menu.add("Share");
        share.setIcon(R.drawable.ic_menu_share);
        share.setOnMenuItemClickListener(shareMenuItemClickListener);
        share.setShowAsAction(with_text_if_room);

        MenuItem sound = menu.add("Sound");
        sound.setIcon(ethereal_diaplad_installed ? R.drawable.ic_volume : R.drawable.ic_volume_off);
        sound.setOnMenuItemClickListener(soundMenuItemClickListener);
        sound.setShowAsAction(with_text_if_room);

        SubMenu colorSub = menu.addSubMenu("Color");
        colorSub.getItem().setShowAsAction(with_text_if_room);
        colorSub.add("Red");
        colorSub.add("Green");
        colorSub.add("Blue");

        return true;
    }

    final MenuItem.OnMenuItemClickListener cameraMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            cameraHandler.setToNextCamera();
            return true;
        }
    };

    final MenuItem.OnMenuItemClickListener shareMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            edgeView.captureNextFrame();
            return true;
        }
    };

    final MenuItem.OnMenuItemClickListener soundMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            if (ethereal_diaplad_installed) {

                /* flip volume_enabled */
                volume_enabled = !volume_enabled;

                /* flip the icon and set the service on/off */
                if (volume_enabled) {
                    item.setIcon(R.drawable.ic_volume);
                    edgeView.setSynthService(synthService);
                } else {
                    item.setIcon(R.drawable.ic_volume_off);
                    edgeView.setSynthService(null);
                }

            } else {

                /* create an intent to send the user to the Play Store */
                final AlertDialog.Builder builder = new AlertDialog.Builder(
                        KnightVisorActivity.this);
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
            return true;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "Options menu item selected: " + item.getTitle());
        if (item.getTitle() == "Red") {
            edgeView.setColorSelected(Color.RED);
            return true;
        } else if (item.getTitle() == "Green") {
            edgeView.setColorSelected(Color.GREEN);
            return true;
        } else if (item.getTitle() == "Blue") {
            edgeView.setColorSelected(Color.BLUE);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
