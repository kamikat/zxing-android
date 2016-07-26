package moe.banana.zxing;

import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

import java.util.Map;

@SuppressWarnings("unused")
public class BarcodeScanManager {

    private static final String TAG = BarcodeScanManager.class.getSimpleName();

    private BarcodeScanHandler mHandler;

    private CameraManager mCameraManager;

    private BarcodeScanListener mBarcodeScanListener;

    public BarcodeScanManager(CameraManager manager) {
        setCameraManager(manager);
        mHandler = new BarcodeScanHandler(this);
    }

    public synchronized CameraManager getCameraManager() {
        return mCameraManager;
    }

    public synchronized void setCameraManager(CameraManager camera) {
        mCameraManager = camera;
    }

    public void start() {
        mHandler.start();
    }

    public void stop() {
        mHandler.stop();
    }

    public void quit() {
        mHandler.quit();
        mHandler = null;
    }

    public void setHints(Map<DecodeHintType, Object> hints) {
        mHandler.setHints(hints);
    }

    public BarcodeScanHandler getHandler() {
        return mHandler;
    }

    void fireStart() {
        if (mBarcodeScanListener != null) mBarcodeScanListener.onStart();
    }

    void fireStop() {
        if (mBarcodeScanListener != null) mBarcodeScanListener.onStop();
    }

    void fireResult(Result result) {
        if (mBarcodeScanListener != null) mBarcodeScanListener.onResult(result);
    }

    void fireError(Exception e) {
        if (mBarcodeScanListener != null) mBarcodeScanListener.onError(e);
    }

    public void setBarcodeScanListener(BarcodeScanListener listener) {
        mBarcodeScanListener = listener;
    }

}
