package com.visor.knight;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;

public class FilterscriptConverter extends EdgeConverter {

	public void setThreshold(int threshold) {
	}
	public void setColor(int color) {
	}
	public void setMedianFiltering(boolean medianFiltering) {
	}
	public void setGrayscaleOnly(boolean grayscaleOnly) {
	}
	public void setAutomaticThreshold(boolean automaticThreshold) {
	}
	public void setLogarithmicTransform(boolean logarithmicTransform) {
	}
	public void setSoftEdges(boolean softEdges) {
	}
	
	private final ScriptC_filter mScript = null;
	private final Allocation inAllocation;
	private final Allocation outAllocation;
	private final Bitmap outBitmap;
	
	public FilterscriptConverter(Context ctx, int width, int height) {
		super(width, height);
		/* potentially correct declarations */
		RenderScript rs = RenderScript.create(ctx);
		inAllocation  = Allocation.createSized(rs, Element.RGBA_8888(rs), width*height, Allocation.USAGE_SCRIPT);
		outAllocation = Allocation.createSized(rs, Element.RGBA_8888(rs), width*height, Allocation.USAGE_SCRIPT);
		mScript.set_in(inAllocation);
		mScript.set_width(width);
		mScript.set_height(height);
		outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

	}
	
	public Bitmap convertFrame(byte[] yuvFrame) {
		/* potentially correct implementation */
		inAllocation.copy2DRangeFrom(0, 0, width, height, yuvFrame);
		mScript.forEach_root(outAllocation);
		outAllocation.copyTo(outBitmap);

		return outBitmap;
	}
}
