package com.visor.knight.view;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.visor.knight.camera.PictureHandler;
import com.visor.knight.converter.EdgeConverter;

public class EdgeView extends DialpadView implements Camera.PreviewCallback, Camera.PictureCallback {

	private static final String TAG = EdgeView.class.getSimpleName();
    private final Paint frameRateText = new Paint();
    private final Lock cameraPreviewLock = new ReentrantLock();

    private Bitmap bitmap = null;
    private Rect canvasRect = null;
    private Rect cameraRect = null;

    private byte[] cameraPreview = null;
    private long time = System.currentTimeMillis();
    private int framesPerSecond = 0;
    private int frames = 0;
    private float textSize = 30;
    private boolean captureNextFrame = false;
    
    private EdgeConverter edgeConverter = null;

    public EdgeConverter getEdgeConverter() {
        return edgeConverter;
    }

    public void setEdgeConverter(EdgeConverter edgeConverter) {
        this.edgeConverter = edgeConverter;
    }

    public void captureNextFrame() {
        this.captureNextFrame = true;
    }
    
    {   /* Class initializer. This should happen no matter what constructor is used */
    	frameRateText.setColor(Color.GREEN);
    	frameRateText.setTextSize(textSize);
    	setDrawingCacheEnabled(true);
    	setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    }
    
	public EdgeView(Context context) {
		super(context);
	}
	
	public EdgeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EdgeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        int r = (int) (255 * x / (float) getWidth());
        int g = (int) (255 * y / (float) getHeight());
        int b = 0;

        int color = Color.rgb(r, g, b);
        EdgeConverter converter = getEdgeConverter();
        if (converter != null) {
            converter.setColor(color);
        }

        /* Must call super for the DialpadView to intercept touch */
        return super.onTouch(view, event);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (cameraPreview == null || cameraPreviewLock.tryLock() == false)
            return;
        else
            try {
                if (canvasRect == null) {
                    canvasRect = canvas.getClipBounds();
                }

                /* draw the canvas onto the screen */
                canvas.drawBitmap(bitmap, cameraRect, canvasRect, null);
                canvas.drawText(String.valueOf(framesPerSecond), 0, textSize, frameRateText);

                if (captureNextFrame) {
                    captureNextFrame = false;
                    PictureHandler.savePicture(getContext(), bitmap);
                }

                final long now = System.currentTimeMillis();
                if (now - time > 1000) {
                    framesPerSecond = frames;
                    frames = 0;
                    time = now;
                } else {
                    frames++;
                }

            } finally {
                cameraPreviewLock.unlock();
            }
    }

    public void onPreviewFrame(byte[] yuv, Camera camera) {
        if (cameraPreviewLock.tryLock() && yuv != null) {
            try {

                final Camera.Size size = camera.getParameters().getPreviewSize();
                final int width = size.width;
                final int height = size.height;
                final int length = width * height;

                if (cameraPreview == null || cameraPreview.length != length) {
                    cameraRect = new Rect(0, 0, width, height);
                    cameraPreview = new byte[length];
                }

                if (yuv.length < cameraPreview.length)
                    Log.e(TAG, "This camera frame is too damn short!");
                else {

                    if (edgeConverter == null) {
                        edgeConverter = EdgeConverter.getDefaultConverter(getContext(), width, height);
                        edgeConverter.setSize(width, height);
                    }
                    bitmap = edgeConverter.convertFrame(yuv);
                    
                }
            } catch(Exception e) {
            	Log.e(TAG, Log.getStackTraceString(e));
            } finally {
                cameraPreviewLock.unlock();
                postInvalidate();
            }

            /*
             * the documentation doesn't say anything about this but it is
             * necessary..... :(
             */
            camera.addCallbackBuffer(yuv);
        }
    }

    public void onPictureTaken(byte[] data, Camera camera) {
        this.captureNextFrame = true;
        Log.d(TAG, "Normal length=" + cameraPreview.length + ", got length=" + data.length);
    }

    public void setSoftEdges(boolean softEdges) {
        edgeConverter.setSoftEdges(softEdges);
    }

}
