package moe.banana.zxing;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

@SuppressWarnings("deprecation")
final class PreviewCallback implements Camera.PreviewCallback {

    private static final String TAG = PreviewCallback.class.getSimpleName();

    private final Handler mHandler;
    private final int mMessageId;

    PreviewCallback(Handler handler, int messageId) {
        mHandler = handler;
        mMessageId = messageId;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mHandler != null) {
            Message.obtain(mHandler, mMessageId, data).sendToTarget();
        } else {
            Log.d(TAG, "Got preview callback, but no handler or resolution available");
        }
    }

}
