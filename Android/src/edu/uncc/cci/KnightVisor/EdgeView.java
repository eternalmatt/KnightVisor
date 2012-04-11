package edu.uncc.cci.KnightVisor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.view.View;

public class EdgeView extends View implements PreviewCallback {

    private final Lock  cameraPreviewLock   = new ReentrantLock();
    private final Paint edgePaint           = new Paint();
    
	private boolean    cameraPreviewValid   = false;
    private int[]     cameraPreview        = null;
	private int        width                = 0;
	private int        height               = 0;
	
	public static final int[][] sobel = { { -1, -2, -1}, {0, 0, 0}, {1, 2, 1} };
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
	
	
	
    private void sobelDetection(Canvas canvas)
	{
	    int[][] f = new int[height][width];
	    int r,c,r_width;
	    for(r=0; r < height; r++)
	    {
	        r_width = r * width;
    	    for(c=0; c < width; c++)
    	    {
    	        f[r][c] = cameraPreview[r_width + c];
    	    }
	    }
	    
	    int[][] gx = Toolbox.imfilter(f, sobel);
	    int[] gm = new int[width*height];
	    
	    int color;
	    for(r=0; r < height; r++)
        {
	        r_width = r * width;
            for(c=0; c < width; c++)
            {
                color = gx[r][c];
                gm[r_width + c] = Color.rgb(color, color, color);
            }
        }
	    
	    canvas.drawBitmap(Bitmap.createBitmap(gm, width, height, Bitmap.Config.RGB_565), 0, 0, null);
	}
	
	@SuppressWarnings("unused")
    private void edgeDetection(Canvas canvas)
	{
	    /* a rather poor implementation of edge detection.
	     * code is a translation of William Beene's C code. */
	    int px, cx, nx, ly, val, y, x, y_width;
	    int threshold = 30;
	    
	    Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.RGB_565);
        
	    for (y = 1; y < height-1; y++) {

	        y_width = y*width;

	        px = cameraPreview[y_width]/2; // init previous x
	        cx = cameraPreview[y_width+1]/2; // init current x

	        for (x = 1; x < width-1; x++) {
	            nx = cameraPreview[y_width+x+1]/2; // next x

	            ly = (cameraPreview[(y-1)*width+x]/2) - (cameraPreview[(y+1)*width+x]/2);
	            val = Math.abs(px - nx) + Math.abs(ly);

	            if(val > threshold) {
	                bitmap.setPixel(x, y, Color.GREEN);
	                //canvas.drawPoint((float)x, (float)y, edgePaint);
	            }

	            // previous x becomes current x and current x becomes next x
	            px = cx;
	            cx = nx;
	        }
	    }
	    canvas.drawBitmap(bitmap, 0, 0, null);
	    
	}
	
	@SuppressWarnings("unused")
    private void drawEverything(Canvas canvas)
	{
	    Bitmap bitmap = Bitmap.createBitmap(cameraPreview, width, height, Bitmap.Config.RGB_565);
	    canvas.drawBitmap(bitmap, 0, 0, null);
	    
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {

		if (cameraPreviewValid && cameraPreview != null && cameraPreviewLock.tryLock()) {
			try {
			    //edgeDetection(canvas);
			    sobelDetection(canvas);
				//drawEverything(canvas);
			    
			} finally {
				cameraPreviewLock.unlock();
                cameraPreviewValid = false;
			}
		}
	}
	
	public void onPreviewFrame(byte[] yuv, Camera camera) {
		if (cameraPreviewValid == false && cameraPreviewLock.tryLock()) {
			try {

				Size size = camera.getParameters().getPreviewSize();
				width  = size.width;
				height = size.height;
				int length = width * height;
				
				if(cameraPreview == null || cameraPreview.length != length) {
					cameraPreview = new int[length];
				}
				
				/* take the Y channel (luminocity) and convert to proper grayscale */
				for(int i=0, c; i < length; i++)
				{
				    c = yuv[i] + 256; //convert luminocity from [-255,255] to [0,565]
				    cameraPreview[i] = Color.rgb(c, c, c); //create gray color
				}
				
				
			} finally {
				cameraPreviewLock.unlock();
                cameraPreviewValid = true;
				postInvalidate();
			}
		}
	}

}
