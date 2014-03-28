package com.visor.knight.converter;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;

import com.visor.knight.R;
import com.visor.knight.ScriptC_filter;

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
	
	private final RenderScript rs;
	private final ScriptC_filter mScript;
	private Allocation inAllocation;
	private Allocation outAllocation;
	private Bitmap outBitmap;
	
	public FilterscriptConverter(Context ctx){
		this.rs = RenderScript.create(ctx);
		mScript = new ScriptC_filter(rs, ctx.getResources(), R.raw.filter);
	}
	
	@Override
	protected void initialize(int width, int height) {;
		outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		inAllocation  = Allocation.createFromBitmap(rs, outBitmap);
		outAllocation = Allocation.createFromBitmap(rs, outBitmap);
		mScript.set_in(inAllocation);
		mScript.set_width(width);
		mScript.set_height(height);
	}
	
	public Bitmap convertFrame(byte[] yuvFrame) {
		/* potentially correct implementation */
		inAllocation.copy2DRangeFrom(0, 0, width, height, yuvFrame);
		mScript.forEach_root(outAllocation);
		outAllocation.copyTo(outBitmap);

		return outBitmap;
	}
}
