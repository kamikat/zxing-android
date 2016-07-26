package moe.banana.zxing;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;

final class DecodeThread extends Thread {

    private final BarcodeScanManager mBarcodeScanManager;

    private final CountDownLatch mHandlerInitLatch;
    private DecodeHandler mHandler;

    DecodeThread(BarcodeScanManager manager) {
        mBarcodeScanManager = manager;
        mHandlerInitLatch = new CountDownLatch(1);
    }

    public Handler getHandler() {
        try {
            mHandlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return mHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new DecodeHandler(mBarcodeScanManager);
        mHandlerInitLatch.countDown();
        Looper.loop();
    }

}
