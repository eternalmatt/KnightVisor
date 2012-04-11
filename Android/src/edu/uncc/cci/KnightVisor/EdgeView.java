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

public class EdgeView extends View implements PreviewCallback
{
    public static final String TAG = EdgeView.class.getSimpleName();
    
    private final Lock  cameraPreviewLock   = new ReentrantLock();
    private final Paint edgePaint           = new Paint();
    
	private boolean    cameraPreviewValid   = false;
    private int[]      cameraPreview        = null;
	private int        width                = 0;
	private int        height               = 0;
	
	public static final byte[][] sobelNorm = { { -1, -2, -1}, { 0, 0, 0}, { 1, 2, 1} };
	public static final byte[][] sobelTran = { { -1,  0,  1}, {-2, 0, 2}, {-1, 0, 1} };
	
	public final int[] grayscale = new int[256];
	
	public int[] convertToGrayscale(int [] f)
    {
        final int length = f.length;
        final int[] g = new int[length];
        for(int i=0; i < length; i++)
            g[i] = grayscale[f[i]];
        return g;
    }
	
	public EdgeView(Context context) {
		super(context);
		edgePaint.setColor(Color.GREEN);
		for(int i=0; i < 256; i++)
		    grayscale[i] = Color.rgb(i, i, i);
	}
	
	
	
    private void sobelDetection(Canvas canvas)
	{
	    int[][] f = new int[height][width];
	    int[]   g = new int[width * height];
	    int r,c,r_width;
	    for(r=0; r < height; r++)
        {
            r_width = r * width;
            for(c=0; c < width; c++)
            {
                f[r][c] = cameraPreview[r_width + c];
            }
        }
	    
	    f = Toolbox.smooth(f);
	    
	    for(r=0; r < height; r++)
        {
            r_width = r * width;
            for(c=0; c < width; c++)
            {
                g[r_width + c] = grayscale[f[r][c]];
            }
        }
	    
	    
	    /*
	    int[][] gx = Toolbox.imfilter(f, sobelNorm);
	    int[][] gy = Toolbox.imfilter(f, sobelTran);
	    int[][] gm = Toolbox.transform(gx, gy, new Operation() {
                    public int it(int a, int b) {
                        return (int) Math.sqrt(a*a + b*b);
                        }
        });
	    
	    int color;
	    for(r=0; r < height; r++)
        {
	        r_width = r * width;
            for(c=0; c < width; c++)
            {
                color = gm[r][c];
                if      (color < 0)   color = 0;
                else if (color > 565) color = 565;
                g[r_width + c] = Color.rgb(color, color, color);
            }
        }
        */
	    
	    canvas.drawBitmap(Bitmap.createBitmap(g, width, height, Bitmap.Config.ARGB_8888), 0, 0, null);
	}
	
	@SuppressWarnings("unused")
    private void edgeDetection(Canvas canvas)
	{
	    /* a rather poor implementation of edge detection.
	     * code is a translation of William Beene's C code. */
	    int px, cx, nx, ly, val, y, x, y_width;
	    int threshold = 30;
	    
	    Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        
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
	            //else bitmap.setPixel(x, y, Color.TRANSPARENT);

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
	    cameraPreview = convertToGrayscale(cameraPreview);
	    Bitmap bitmap = Bitmap.createBitmap(cameraPreview, width, height, Bitmap.Config.ARGB_8888);
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
				    c = (yuv[i] + 256) / 2; //convert luminocity from [-128,128] to [256]
				    cameraPreview[i] = c;//grayscale[c]; //get gray color
				}
				
				
			} finally {
				cameraPreviewLock.unlock();
                cameraPreviewValid = true;
				postInvalidate();
			}
		}
	}

}
