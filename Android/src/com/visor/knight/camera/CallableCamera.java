package com.visor.knight.camera;

import java.util.concurrent.Callable;

import android.hardware.Camera;

class CallableCamera implements Callable<Camera>{

    final int cameraId;
    public CallableCamera(int cameraId){
        this.cameraId = cameraId;
    }
    
    @Override
    public Camera call() {
        return Camera.open(cameraId);
    }
    
};