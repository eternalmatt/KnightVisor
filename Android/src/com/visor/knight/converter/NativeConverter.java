package com.visor.knight.converter;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import android.graphics.Bitmap;
import android.os.Build;

public class NativeConverter extends EdgeConverter {

    /* everything native */
    static { System.loadLibrary("native"); }
    public native void nativeProcessing(byte[] f, int width, int height, IntBuffer output);
    public native void setThreshold(int threshold);
    public native void setColor(int color);
    public native void setMedianFiltering(boolean medianFiltering);
    public native void setGrayscaleOnly(boolean grayscaleOnly);
    public native void setAutomaticThreshold(boolean automaticThreshold);
    public native void setLogarithmicTransform(boolean logarithmicTransform);
    public native void setSoftEdges(boolean softEdges);
    
    private IntBuffer intBuffer;
    private Bitmap bitmap;
    
    @Override
	protected void initialize(int width, int height) {
        intBuffer = ByteBuffer.allocateDirect(width * height * 4).asIntBuffer();
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    @Override
    public Bitmap convertFrame(final byte[] yuvFrame) {

        nativeProcessing(yuvFrame, width, height, intBuffer); //the really important step.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
        	//bug that seems localized to 4.2+
        	intBuffer.rewind();
        }
        bitmap.copyPixelsFromBuffer(intBuffer);
        return bitmap;
    }
    
}
