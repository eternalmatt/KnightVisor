package com.visor.knight;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class PictureHandler {

    public static final String TAG = PictureHandler.class.getSimpleName();

    public static void savePicture(final Context context, final Bitmap bitmap) {
        Log.d(TAG, "Getting file path and creating file.");
        Toast.makeText(context, "Please wait...", Toast.LENGTH_SHORT).show();
        File path = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.app_name));
        if (false == path.exists()) path.mkdir();

        final File file = new File(path, "image.png");

        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "File not written", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        Log.d(TAG, "Bitmap compressed (file written)");

        MediaScannerConnection.scanFile(context, new String[]{ file.getAbsolutePath() }, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("image/png");
                Log.d(TAG, "Intent created. Launching chooser.");
                context.startActivity(Intent.createChooser(shareIntent, "Share image?"));
            }
        });
    }

}
