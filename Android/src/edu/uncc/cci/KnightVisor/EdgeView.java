package edu.uncc.cci.KnightVisor;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class EdgeView extends View implements Camera.PreviewCallback
{
    public static final String TAG = EdgeView.class.getSimpleName();
    
    private final Lock  cameraPreviewLock = new ReentrantLock();
    
    private Bitmap      bitmap            = null;
    private Rect        canvasRect        = null;
    private Rect        cameraRect        = null;
    
    private IntBuffer   intBuffer         = null;
	private byte[]      cameraPreview     = null;
	private int         width             = 0;
	private int         height            = 0;
	private long        time              = System.currentTimeMillis();
    private int         framesPerSecond   = 0;
    private int         frames            = 0;
    
    private Paint paint = null;
	
	static 
	{
	    System.loadLibrary("native"); 
    }
    public native void nativeProcessing(byte[] f, int width, int height, IntBuffer output);
    public native void setThresholdManually(int threshold);
    public native void setColorSelected(int color);
    public native void setMedianFiltering(boolean on);
    public native void grayscaleOnly(boolean gray);
    public native void automaticThresholding(boolean automatic);
    public native void logarithmicTransform(boolean on);
    public native void setSoftEdges(boolean soft);
    
	public EdgeView(Context context, AttributeSet set)
	{
		super(context, set);
		paint = new Paint();
		paint.setColor(Color.GREEN);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		if (cameraPreview == null || cameraPreviewLock.tryLock() == false)
		{
		    return;
		}
		else try
		{
		    if (canvasRect == null)
		        canvasRect = canvas.getClipBounds();

            /* draw the canvas onto the screen */
            canvas.drawBitmap(bitmap, cameraRect, canvasRect, null);
            canvas.drawText(String.valueOf(framesPerSecond), 20, 20, paint);
           
            long now = System.currentTimeMillis();
		    if (System.currentTimeMillis() - time > 1000)
		    {
		        framesPerSecond = frames;
		        frames = 0;
		        time = now;
		    }
		    else
	        {
		        frames++;
	        }
            
		    
		} finally {
			cameraPreviewLock.unlock();
		}
	}
	
	
	
	public void onPreviewFrame(byte[] yuv, Camera camera) 
	{
		if (cameraPreviewLock.tryLock() && yuv != null)
			try {

				Size size = camera.getParameters().getPreviewSize();
				width  = size.width;
				height = size.height;
				final int length = width * height;
				
				if (cameraPreview == null || cameraPreview.length != length)
				{
	                cameraRect = new Rect(0, 0, width, height);
				    cameraPreview = new byte[length];
				    intBuffer = ByteBuffer.allocateDirect(length * 4).asIntBuffer();
				    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				}
				
				/* TODO: lop off the first N rows based on how many pixels
				 * are being occupied by the GUI */
				
				if (yuv.length < cameraPreview.length)
				    Log.e(TAG, "This camera frame is too damn short!");
				else
				{    
    				/* do some processing in seaworld */
    	            nativeProcessing(yuv, width, height, intBuffer);
    	            
    	            /* copy the pixels from intBuffer to bitmap */
    	            bitmap.copyPixelsFromBuffer(intBuffer);      
				}
			} finally {
				cameraPreviewLock.unlock();
				postInvalidate();
			}
		
		/* the documentation doesn't say anything about this 
		 * but it is necessary.....  :( */
		camera.addCallbackBuffer(yuv);
	}

}
