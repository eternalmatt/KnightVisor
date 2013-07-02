
package com.visor.knight;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

public class KnightVisorActivity extends SherlockFragmentActivity {

    public static final String TAG = KnightVisorActivity.class.getSimpleName();

    //@InjectFragment(R.id.sobel_fragment) 
    SobelFragment sobelFragment;
    
    boolean volume_enabled = false;
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        sobelFragment = (SobelFragment) this.getSupportFragmentManager().findFragmentById(R.id.sobel_fragment);

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
                sobelFragment.setSobelThresholdAsPercentage(progress);
            }
        });

        /* set up window so we get full screen */
        final Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final int with_text_if_room = MenuItem.SHOW_AS_ACTION_IF_ROOM
                // | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT;

        if (Camera.getNumberOfCameras() > 1) {
            MenuItem camera = menu.add("Camera");
            camera.setOnMenuItemClickListener(cameraMenuItemClickListener);
            camera.setShowAsAction(with_text_if_room);
        }

        MenuItem share = menu.add("Share");
        share.setIcon(R.drawable.ic_menu_share);
        share.setOnMenuItemClickListener(shareMenuItemClickListener);
        share.setShowAsAction(with_text_if_room);

        MenuItem sound = menu.add("Sound");
        //sound.setIcon(ethereal_diaplad_installed ? R.drawable.ic_volume : R.drawable.ic_volume_off);
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
            sobelFragment.nextCameraClicked();
            return true;
        }
    };

    final MenuItem.OnMenuItemClickListener shareMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            sobelFragment.shareImageClicked();
        	return true;
        }
    };

    final MenuItem.OnMenuItemClickListener soundMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            if (sobelFragment.isEtherealDialpadInstalled()) {

                /* flip volume_enabled */
                volume_enabled = !volume_enabled;

                /* flip the icon and set the service on/off */
                if (volume_enabled) {
                    item.setIcon(R.drawable.ic_volume);
                } else {
                    item.setIcon(R.drawable.ic_volume_off);
                }
                sobelFragment.enableVolume(volume_enabled);

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
    
}
