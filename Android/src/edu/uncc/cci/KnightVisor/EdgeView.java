package edu.uncc.cci.KnightVisor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.View;

public class EdgeView extends View implements PreviewCallback {

    private final Lock  cameraPreviewLock   = new ReentrantLock();
    private final Paint edgePaint           = new Paint();
    
	private boolean    cameraPreviewValid   = false;
    private byte[]     cameraPreview        = null;
	private int        width                = 0;
	private int        height               = 0;
	
	public static final byte[][] sobel = { { -1 -2 -1}, {0, 0, 0}, {1, 2, 1}};
	private Paint[] grayscale = new Paint[256];
	
	public EdgeView(Context context) {
		super(context);
		edgePaint.setColor(Color.GREEN);
		for(int i=0; i < 256; i++)
		{
		    Paint p = new Paint();
		    p.setARGB(255, i, i, i);
		    grayscale[i] = p;
		}
	}
	
	private void edgeDetection(Canvas canvas)
	{
	    /* a rather poor implementation of edge detection.
	     * code is a translation of William Beene's C code. */
	    int px, cx, nx, ly, val, y, x, y_width;
	    int threshold = 30;
	    
	    Bitmap bitmap = BitmapFactory.decodeByteArray(cameraPreview, 0, width*height);
	    if (bitmap == null) Log.e("edgeview", "bitmap was null");
	    else canvas.drawBitmap(bitmap, 0, 0, null);
        
	    for (y = 1; y < height-1; y++) {

	        y_width = y*width;

	        px = cameraPreview[y_width]/2; // init previous x
	        cx = cameraPreview[y_width+1]/2; // init current x

	        for (x = 1; x < width-1; x++) {
	            nx = cameraPreview[y_width+x+1]/2; // next x

	            ly = (cameraPreview[(y-1)*width+x]/2) - (cameraPreview[(y+1)*width+x]/2);
	            val = Math.abs(px - nx) + Math.abs(ly);

	            if(val > threshold) {
	                //bitmap.setPixel(x, y, Color.GREEN);
	                canvas.drawPoint((float)x, (float)y, edgePaint);
	            }
	            //else canvas.drawPoint(x, y, grayscale[Math.abs(cx)]);

	            // previous x becomes current x and current x becomes next x
	            px = cx;
	            cx = nx;
	        }
	    }
	    
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		//canvas.drawColor(Color.argb(255, 0, 0, 0));

		if (cameraPreviewValid && cameraPreview != null && cameraPreviewLock.tryLock()) {
			try {
			    edgeDetection(canvas);
					
			    
			} finally {
				cameraPreviewLock.unlock();
                cameraPreviewValid = false;
			}
		}
	}
	
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (cameraPreviewValid == false && cameraPreviewLock.tryLock()) {
			try {

				Size size = camera.getParameters().getPreviewSize();
				width  = size.width;
				height = size.height;
				int length = width * height;
				
				if(cameraPreview == null || cameraPreview.length != length) {
					cameraPreview = new byte[length];
				}
				
				System.arraycopy(data, 0, cameraPreview, 0, length);
				
			} finally {
				cameraPreviewLock.unlock();
                cameraPreviewValid = true;
				postInvalidate();
			}
		}
	}

}
