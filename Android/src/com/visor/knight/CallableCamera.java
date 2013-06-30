package com.visor.knight;

import java.util.concurrent.Callable;

import android.hardware.Camera;

public class CallableCamera implements Callable<Camera>{

    final int cameraId;
    public CallableCamera(int cameraId){
        this.cameraId = cameraId;
    }
    
    public Camera call() {
        return Camera.open(cameraId);
    }
    
};