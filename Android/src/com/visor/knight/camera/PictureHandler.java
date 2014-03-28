package com.visor.knight.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.visor.knight.R;

public class PictureHandler {

    public static final String TAG = PictureHandler.class.getSimpleName();

    public static void savePicture(final Context context, final Bitmap bitmap) {

        if (bitmap == null) {
            Log.e(TAG, "No bitmap in PictureHandler::savePicture");
            Toast.makeText(context, "No bitmap in PictureHandler::savePicture", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Log.d(TAG, "Getting file path and creating file.");
        Toast.makeText(context, "Please wait...", Toast.LENGTH_SHORT).show();
        File path = new File(Environment.getExternalStorageDirectory(),
                context.getString(R.string.app_name));
        if (false == path.exists())
            path.mkdir();

        final File file = new File(path, "image.jpg");

        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));

        } catch (FileNotFoundException e) {
            Toast.makeText(context, "File not written", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        Log.d(TAG, "Bitmap compressed (file written)");

        MediaScannerConnection.scanFile(context, new String[] {
                file.getAbsolutePath()
        }, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType(context.getString(R.string.jpeg_mime_type));
                Log.d(TAG, "Intent created. Launching chooser.");
                context.startActivity(Intent.createChooser(shareIntent, "Share image?"));
            }
        });
    }
}
