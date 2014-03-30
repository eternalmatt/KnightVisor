package com.visor.knight.converter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Script.FieldID;
import android.renderscript.Script.KernelID;
import android.renderscript.ScriptGroup;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Short4;
import android.renderscript.Type;
import android.util.Pair;

import com.visor.knight.R;
import com.visor.knight.ScriptC_filter;

public class FilterscriptConverter extends EdgeConverter {
    
	private final RenderScript rs;
    private final ScriptC_filter script;
    private final ScriptC_filter medianScript;
	private final ScriptIntrinsicYuvToRGB yuv;
	private ScriptGroup scriptGroup;
	private Allocation in;
	private Allocation out;
	private Bitmap bitmap;
	private boolean median = true;
	
	public FilterscriptConverter(Context ctx){
		this.rs = RenderScript.create(ctx);
		script = new ScriptC_filter(rs, ctx.getResources(), R.raw.filter);
		medianScript = new ScriptC_filter(rs, ctx.getResources(), R.raw.filter);
        yuv = ScriptIntrinsicYuvToRGB.create(rs, Element.RGBA_8888(rs));
	}
	
	@Override
	protected void initialize(int width, int height) {;
    	bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        script.set_width(width);
        script.set_height(height);
        medianScript.set_width(width);
        medianScript.set_height(height);

    	createIn(width, height);
        createOut(width, height);
        createScriptGroup();
	}

    @Override
    public Bitmap convertFrame(final byte[] yuvFrame) {
        in.copyFrom(yuvFrame);
        yuv.setInput(in);
        scriptGroup.setOutput(script.getKernelID_sobel(), out);
        scriptGroup.execute();
        
        out.copyTo(bitmap);
        return bitmap;
    }

    private void createIn(final int width, final int height) {
        final Type.Builder tb = new Type.Builder(rs, Element.createPixel(rs, Element.DataType.UNSIGNED_8, Element.DataKind.PIXEL_YUV));
        tb.setX(width);
        tb.setY(height);
        tb.setYuvFormat(ImageFormat.NV21);
        in = Allocation.createTyped(rs, tb.create(), Allocation.USAGE_SCRIPT);
    }

    private void createOut(final int width, final int height) {
        final Type.Builder tb = new Type.Builder(rs, Element.RGBA_8888(rs));
	    tb.setX(width);
	    tb.setY(height);
	    out = Allocation.createTyped(rs, tb.create(), Allocation.USAGE_SCRIPT);
    }
	
	private void createScriptGroup(){
        ScriptGroup.Builder b = new ScriptGroup.Builder(rs);
        
        List<Pair<KernelID, FieldID>> pairs = new ArrayList<Pair<KernelID,FieldID>>();
        if (median){
            pairs.add(Pair.create(yuv.getKernelID(), medianScript.getFieldID_in()));
            pairs.add(Pair.create(medianScript.getKernelID_median(), script.getFieldID_in()));
        } else {
            pairs.add(Pair.create(yuv.getKernelID(), script.getFieldID_in()));
        }

        b.addKernel(script.getKernelID_sobel());
        for(Pair<KernelID, FieldID> pair : pairs){
            b.addKernel(pair.first);
        }
        for(Pair<KernelID, FieldID> pair : pairs){
            b.addConnection(out.getType(), pair.first, pair.second);
        }
        
        scriptGroup = b.create();
	}

    @Override 
    public void setThreshold(int threshold) {
        script.set_threshold(threshold);
    }
    
    @Override 
    public void setColor(int color) {
        final short r = (short) ((color & 0xFF000000) >>> 24);
        final short g = (short) ((color & 0x00FF0000) >>> 16);
        final short b = (short) ((color & 0x0000FF00) >>> 8);
        script.set_color(new Short4(r, g, b, (short)255));
    }
    
    @Override 
    public void setMedianFiltering(boolean medianFiltering) {
        this.median = medianFiltering;
        createScriptGroup();
    }
    
    @Override 
    public void setSoftEdges(boolean softEdges) {
        script.set_soft(softEdges);
    }
    
    @Override public void setGrayscaleOnly(boolean grayscaleOnly) {}
    @Override public void setAutomaticThreshold(boolean automaticThreshold) {}
    @Override public void setLogarithmicTransform(boolean logarithmicTransform) {}
}
