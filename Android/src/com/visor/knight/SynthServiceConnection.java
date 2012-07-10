package com.visor.knight;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import as.adamsmith.etherealdialpad.dsp.ISynthService;

public class SynthServiceConnection implements ServiceConnection {

    private final List<DialpadView> connectedViews = new ArrayList<DialpadView>();

    public void addViewToBeNotified(DialpadView view) {
        connectedViews.add(view);
    }

    public void removeViewToBeNotified(DialpadView view) {
        connectedViews.remove(view);
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        for (DialpadView view : connectedViews) {
            view.setSynthService(ISynthService.Stub.asInterface(service));
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        for (DialpadView view : connectedViews) {
            view.setSynthService(null);
        }
    }
}
