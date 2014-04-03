package com.visor.knight.converter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptGroup;
import android.support.v8.renderscript.ScriptIntrinsicConvolve3x3;
import android.support.v8.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v8.renderscript.Short4;
import android.support.v8.renderscript.Type;

import com.visor.knight.R;
import com.visor.knight.ScriptC_color;
import com.visor.knight.ScriptC_threshold;

public class IntrinsicConverter extends EdgeConverter {

    private static final float sobel[] = new float[]{-1,0,1,-2,0,2,-1,0,1};
    private static final Short4 green = new Short4((short)0,(short)255,(short)0,(short)255);
    
    private final Context ctx;
    private final RenderScript rs;
    private final Element inElement;
    private final Element outElement;
    private Allocation in;
    private Allocation out;
    private Bitmap bitmap;
    private ScriptGroup scriptGroup;
    private ScriptC_threshold threshold;
    
    public IntrinsicConverter(Context ctx) {
        this.rs = RenderScript.create(this.ctx = ctx);
        outElement = Element.RGBA_8888(this.rs);
        inElement = Element.createPixel(rs, Element.DataType.UNSIGNED_8, Element.DataKind.PIXEL_YUV);
    }
    
    @Override
    protected void initialize(int width, int height) {;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        createOut(width, height);
        createIn(width, height);
        createScriptGroup();
    }

    private void createScriptGroup() {
        ScriptIntrinsicYuvToRGB yuv = ScriptIntrinsicYuvToRGB.create(rs, outElement);

        ScriptC_color color = new ScriptC_color(rs, ctx.getResources(), R.raw.color);
        
        ScriptIntrinsicConvolve3x3 kernel = ScriptIntrinsicConvolve3x3.create(rs, outElement);
        kernel.setCoefficients(sobel);

        threshold = new ScriptC_threshold(rs, ctx.getResources(), R.raw.threshold);
        threshold.set_color(green);
        threshold.set_threshold(42);
        threshold.set_softEdges(true);

        final ScriptGroup.Builder builder = new ScriptGroup.Builder(rs);
        builder.addKernel(yuv.getKernelID());
        builder.addKernel(color.getKernelID_root());
        builder.addKernel(kernel.getKernelID());
        builder.addKernel(threshold.getKernelID_root());
        builder.addConnection(out.getType(), yuv.getKernelID(), color.getFieldID_in());
        builder.addConnection(out.getType(), color.getKernelID_root(), kernel.getFieldID_Input());
        builder.addConnection(out.getType(), kernel.getKernelID(), threshold.getFieldID_in());
        yuv.setInput(in); //yuv is dumb. documentation is bad here.
        scriptGroup = builder.create();
        scriptGroup.setInput(yuv.getKernelID(), in);
        scriptGroup.setOutput(threshold.getKernelID_root(), out);
    }
    
    @Override
    public Bitmap convertFrame(final byte[] yuvFrame) {
        in.copyFrom(yuvFrame);
        scriptGroup.execute();
        out.copyTo(bitmap);
        return bitmap;
    }

    private void createIn(final int width, final int height) {
        final Type.Builder tb = new Type.Builder(rs, inElement);
        tb.setX(width);
        tb.setY(height);
        tb.setYuvFormat(ImageFormat.NV21);
        in = Allocation.createTyped(rs, tb.create(), Allocation.USAGE_SCRIPT);
    }

    private void createOut(final int width, final int height) {
        final Type.Builder tb = new Type.Builder(rs, outElement);
        tb.setX(width);
        tb.setY(height);
        out = Allocation.createTyped(rs, tb.create(), Allocation.USAGE_SCRIPT);
    }
    
    @Override
    public void setThreshold(int threshold) {
        this.threshold.set_threshold(threshold);
    }
    
    @Override
    public void setColor(int color) {
        final short r = (short) ((color & 0xFF000000) >>> 24);
        final short g = (short) ((color & 0x00FF0000) >>> 16);
        final short b = (short) ((color & 0x0000FF00) >>> 8);
        this.threshold.set_color(new Short4(r, g, b, (short)255));
    }
    
    @Override public void setSoftEdges(boolean softEdges) {
        this.threshold.set_softEdges(softEdges);
    }
    
    @Override public void setMedianFiltering(boolean medianFiltering) {}
    @Override public void setGrayscaleOnly(boolean grayscaleOnly) {}
    @Override public void setAutomaticThreshold(boolean automaticThreshold) {}
    @Override public void setLogarithmicTransform(boolean logarithmicTransform) {}
}
