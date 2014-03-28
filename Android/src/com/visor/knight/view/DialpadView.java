package com.visor.knight.view;

import android.content.Context;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import as.adamsmith.etherealdialpad.dsp.ISynthService;

class DialpadView extends View implements View.OnTouchListener {

    private ISynthService synthService = null;
    
    { /* class initializer */
    	setOnTouchListener(this);
    }
    
    public DialpadView(Context context) {
    	super(context);
    }
    
    public DialpadView(Context context, AttributeSet attrs) {
    	super(context, attrs);
    }

    public DialpadView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ISynthService getSynthService() {
        return this.synthService;
    }

    public void setSynthService(ISynthService synthService) {
        this.synthService = synthService;
    }

    public boolean onTouch(View view, MotionEvent event) {

        if (synthService == null)
            return true;

        final float u = Math.min(1, Math.max(0, event.getX() / getWidth()));
        final float v = Math.min(1, Math.max(0, 1.0f - event.getY() / getHeight()));

        try {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    synthService.primaryOff();
                    synthService.primaryXY(u, v);
                    break;
                case MotionEvent.ACTION_DOWN:
                    synthService.primaryOn();
                case MotionEvent.ACTION_MOVE:
                    synthService.primaryXY(u, v);
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }
}
