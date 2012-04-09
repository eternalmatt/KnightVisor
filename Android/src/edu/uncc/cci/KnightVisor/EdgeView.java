package edu.uncc.cci.KnightVisor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
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
    private byte[]     cameraPreview        = null;
	private int        width                = 0;
	private int        height               = 0;
	
	public EdgeView(Context context) {
		super(context);
		edgePaint.setColor(Color.GREEN);
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.BLACK);

		if (cameraPreviewValid && cameraPreview != null && cameraPreviewLock.tryLock()) {
			try {
						
				/* need to implement actual edge detection.
				 * this is currently drawing a thick green line */
				for(float i=0; i < width; i++)
			    for(float j=height/2; j < height/2 + 5; j++)
			        canvas.drawPoint(i, j, edgePaint);
					
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
