package com.visor.knight;

import android.content.Context;
import android.graphics.Bitmap;

public abstract class EdgeConverter {
    
    public static EdgeConverter getDefaultConverter(Context ctx, int width, int height) {
        EdgeConverter converter = new NativeConverter();
        //EdgeConverter converter = new FilterscriptConverter(ctx);
        converter.setSize(width, height);
        return converter;
    }
    
    protected int width, height;
    
    public void setSize(int width, int height) {
    	if (this.width != width || this.height != height) {
    		this.width = width;
    		this.height = height;
    		initialize(width, height);
    	}
    }
    
    protected void initialize(int width, int height){
    	//for sub classes
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
