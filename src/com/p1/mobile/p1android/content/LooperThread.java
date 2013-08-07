package com.p1.mobile.p1android.content;

import android.os.Handler;
import android.os.Looper;

public class LooperThread extends Thread{
    public static final String TAG = LooperThread.class.getSimpleName();
    private Handler mHandler;
    
    public Handler getHandler(){
        
        // Ensure that the looperThread has had time to create the handler
        while(mHandler == null){ // Yes, this is an ugly solution. Feel free to improve
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        return mHandler;
        
    }
    
    public void run(){
        Looper.prepare();
        
        mHandler = new Handler();
        
        Looper.loop();
    }

}
