package com.visor.knight.converter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptGroup;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

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
	private final ScriptC_filter script;
	private final ScriptIntrinsicYuvToRGB yuv;
	private ScriptGroup scriptGroup;
	private Allocation in;
	private Allocation out;
	private Bitmap bitmap;
	
	public FilterscriptConverter(Context ctx){
		this.rs = RenderScript.create(ctx);
		script = new ScriptC_filter(rs, ctx.getResources(), R.raw.filter);
        yuv = ScriptIntrinsicYuvToRGB.create(rs, Element.RGBA_8888(rs));
	}
	
	@Override
	protected void initialize(int width, int height) {;
    	bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    	script.set_width(width);
    	script.set_height(height);

    	Type.Builder tb = new Type.Builder(rs, Element.RGBA_8888(rs));
	    tb.setX(width);
	    tb.setY(height);
	    Type t = tb.create();
	    out = Allocation.createTyped(rs, t, Allocation.USAGE_SCRIPT);// | Allocation.USAGE_IO_OUTPUT);
	    
	    tb = new Type.Builder(rs, Element.createPixel(rs, Element.DataType.UNSIGNED_8, Element.DataKind.PIXEL_YUV));
        tb.setX(width);
        tb.setY(height);
        tb.setYuvFormat(ImageFormat.NV21);
        in  = Allocation.createTyped(rs, tb.create(), Allocation.USAGE_SCRIPT);
	    
        ScriptGroup.Builder b = new ScriptGroup.Builder(rs);
        b.addKernel(script.getKernelID_sobel());
        b.addKernel(yuv.getKernelID());
        b.addConnection(t, yuv.getKernelID(), script.getFieldID_in());
        scriptGroup = b.create();
	}
	
	@Override
	public Bitmap convertFrame(final byte[] yuvFrame) {

	    in.copyFrom(yuvFrame);
        yuv.setInput(in);
	    scriptGroup.setOutput(script.getKernelID_sobel(), out);
	    scriptGroup.execute();
	    out.copyTo(bitmap);
	    
		/* potentially correct implementation 
		in.copyFrom(yuvFrame);
		script.forEach_sobel(out);
		out.copyTo(bitmap);
		*/
	    
		return bitmap;
	}
}
