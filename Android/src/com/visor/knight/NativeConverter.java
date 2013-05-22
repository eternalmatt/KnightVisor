package com.visor.knight;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import android.graphics.Bitmap;

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
    
    private final IntBuffer intBuffer;
    private final Bitmap bitmap;
    
    public NativeConverter(int width, int height) {
        super(width, height);
        
        final int length = width * height;
        intBuffer = ByteBuffer.allocateDirect(length * 4).asIntBuffer();
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    @Override
    public Bitmap convertFrame(byte[] yuvFrame) {

        nativeProcessing(yuvFrame, width, height, intBuffer);
        bitmap.copyPixelsFromBuffer(intBuffer);
        return bitmap;
    }
    
}
