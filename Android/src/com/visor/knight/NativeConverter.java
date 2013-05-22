package com.visor.knight;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import android.graphics.Bitmap;

public class NativeConverter extends EdgeConverter {

    /* everything native */
    static { System.loadLibrary("native"); }
    public native void nativeProcessing(byte[] f, int width, int height, IntBuffer output);
    public native void setThresholdManually(int threshold);
    public native void setColorSelected(int color);
    public native void setMedianFiltering(boolean on);
    public native void grayscaleOnly(boolean gray);
    public native void automaticThresholding(boolean automatic);
    public native void logarithmicTransform(boolean on);
    public native void setSoftEdges(boolean soft);
    
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
    
    @Override
    public void setEdgeOptions(EdgeOptions options) {
        setThresholdManually(options.getThreshold());
        setColorSelected(options.getColor());
        setMedianFiltering(options.isMedianFiltering());
        grayscaleOnly(options.isGrayscaleOnly());
        automaticThresholding(options.isAutomaticThreshold());
        logarithmicTransform(options.isLogarithmicTransform());
        setSoftEdges(options.isSoftEdges());
    }

}
