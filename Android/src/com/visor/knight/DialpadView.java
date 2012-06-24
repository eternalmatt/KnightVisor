package com.visor.knight;

import android.content.Context;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import as.adamsmith.etherealdialpad.dsp.ISynthService;

public class DialpadView extends View implements View.OnTouchListener {

    public ISynthService synthService = null;

    public DialpadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnTouchListener(this);
    }

    public boolean onTouch(View view, MotionEvent event) {

        if (synthService == null) return true;

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
                default:
                    return false;
            }
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }
}
