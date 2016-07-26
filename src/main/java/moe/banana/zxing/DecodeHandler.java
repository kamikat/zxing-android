package moe.banana.zxing;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Map;

final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    public static final int SIGNAL_CONTROL_QUIT = 0xFF;

    public static final int SIGNAL_DECODE_REQUEST = 0x1A;
    public static final int SIGNAL_DECODE_HINTS = 0x1B;

    private final BarcodeScanManager mBarcodeScanManager;
    private final MultiFormatReader mMultiFormatReader;

    DecodeHandler(BarcodeScanManager manager) {
        mMultiFormatReader = new MultiFormatReader();
        mBarcodeScanManager = manager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleMessage(Message message) {

        // handle control signals
        switch (message.what) {
            case SIGNAL_CONTROL_QUIT:
                Looper.myLooper().quit();
                break;
        }

        // handle functional requests
        switch (message.what) {
            case SIGNAL_DECODE_HINTS:
                mMultiFormatReader.setHints((Map) message.obj);
                break;
            case SIGNAL_DECODE_REQUEST:
                decode((byte[]) message.obj);
                break;
        }

    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     */
    private void decode(byte[] data) {

        long start = System.currentTimeMillis();

        Result rawResult = null;
        PlanarYUVLuminanceSource source = mBarcodeScanManager.getCameraManager().buildLuminanceSource(data);

        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = mMultiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                mMultiFormatReader.reset();
            }
        }

        Log.d(TAG, "Detection completed in " + (System.currentTimeMillis() - start) + " ms");

        Handler handler = mBarcodeScanManager.getHandler();

        if (handler != null) {
            if (rawResult != null) {
                Log.d(TAG, "Barcode DETECTED");
                Message.obtain(handler, BarcodeScanHandler.SIGNAL_DECODE_SUCCEED, rawResult).sendToTarget();
            } else {
                Message.obtain(handler, BarcodeScanHandler.SIGNAL_DECODE_FAILED).sendToTarget();
            }
        }

    }

}
