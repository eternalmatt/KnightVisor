package com.visor.knight;

import android.graphics.Bitmap;

public abstract class EdgeConverter {
    
    protected final int width, height;
    
    public EdgeConverter(int width, int height){
        this.width = width;
        this.height = height;
    }
    
    public abstract Bitmap convertFrame(final byte yuvFrame[]);
    
    public abstract void setEdgeOptions(EdgeOptions options);
    
}
