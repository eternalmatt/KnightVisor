package com.visor.knight;

import android.graphics.Bitmap;

public abstract class EdgeConverter {
    
    protected final int width, height;
    
    public EdgeConverter(int width, int height){
        this.width = width;
        this.height = height;
    }
    
    public static EdgeConverter getDefaultConverter(int width, int height) {
        return new NativeConverter(width, height);
    }

    public abstract void setThreshold(int threshold);
    public abstract void setColor(int color);
    public abstract void setMedianFiltering(boolean medianFiltering);
    public abstract void setGrayscaleOnly(boolean grayscaleOnly);
    public abstract void setAutomaticThreshold(boolean automaticThreshold);
    public abstract void setLogarithmicTransform(boolean logarithmicTransform);
    public abstract void setSoftEdges(boolean softEdges);
    
    public abstract Bitmap convertFrame(final byte yuvFrame[]);
    
    
}
