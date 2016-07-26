package moe.banana.zxing;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

import java.util.Map;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
public final class BarcodeScanHandler extends Handler {

    private static final String TAG = BarcodeScanHandler.class.getSimpleName();

    public static final int SIGNAL_CONTROL_START  = 0xFA;
    public static final int SIGNAL_CONTROL_STOP   = 0xFB;

    public static final int SIGNAL_DECODE_REQUEST = 0x0A;
    public static final int SIGNAL_DECODE_SUCCEED = 0xA1;
    public static final int SIGNAL_DECODE_FAILED = 0xA0;

    private final BarcodeScanManager mBarcodeScanManager;
    private final DecodeThread mDecodeThread;

    private State state;

    private enum State {
        RUNNING,
        STOPPED,
        CLOSED
    }

    BarcodeScanHandler(BarcodeScanManager manager) {

        super(Looper.getMainLooper());

        this.mBarcodeScanManager = manager;

        // setup state machine initial state
        state = State.STOPPED;

        // setup decode thread
        mDecodeThread = new DecodeThread(manager);
        mDecodeThread.start();

    }

    @Override
    public void handleMessage(Message message) {

        if (state == State.CLOSED) {
            return;
        }

        // handle control messages
        switch (message.what) {
            case SIGNAL_CONTROL_START:
                if (state == State.STOPPED) {
                    state = State.RUNNING;
                    removeMessages(SIGNAL_DECODE_REQUEST);
                    Message.obtain(this, SIGNAL_DECODE_REQUEST).sendToTarget();
                    mBarcodeScanManager.fireStart();
                } else {
                    Log.w(TAG, "Receive SIGNAL_CONTROL_START on STATE(" + state.name() + ")");
                }
                break;
            case SIGNAL_CONTROL_STOP:
                if (state == State.RUNNING) {
                    state = State.STOPPED;
                    removeMessages(SIGNAL_DECODE_REQUEST);
                    mBarcodeScanManager.fireStop();
                } else {
                    Log.w(TAG, "Receive SIGNAL_CONTROL_STOP on STATE(" + state.name() + ")");
                }
                break;
        }

        // handle functional messages
        switch (message.what) {
            case SIGNAL_DECODE_SUCCEED:
                if (state == State.RUNNING) {
                    mBarcodeScanManager.fireResult((Result) message.obj);
                } else {
                    Log.w(TAG, "Receive SIGNAL_DECODE_SUCCEED on STATE(" + state.name() + ")");
                }
            case SIGNAL_DECODE_REQUEST:
            case SIGNAL_DECODE_FAILED:
                // We're decoding as fast as possible, so when one decode fails, start another.
                if (state == State.RUNNING) {
                    mBarcodeScanManager.getCameraManager().requestPreviewFrame(mDecodeThread.getHandler(), DecodeHandler.SIGNAL_DECODE_REQUEST);
                } else {
                    Log.w(TAG, "Receive SIGNAL_DECODE_REQUEST/SIGNAL_DECODE_FAILED on STATE(" + state.name() + ")");
                }
                break;
        }

    }

    public void setHints(Map<DecodeHintType, Object> hints) {
        Message.obtain(mDecodeThread.getHandler(), DecodeHandler.SIGNAL_DECODE_HINTS, hints).sendToTarget();
    }

    public void start() {
        Message.obtain(this, SIGNAL_CONTROL_START).sendToTarget();
    }

    public void stop() {
        Message.obtain(this, SIGNAL_CONTROL_STOP).sendToTarget();
    }

    /**
     * quit decoder thread and stop message processing queue synchronously
     */
    public void quit() {

        state = State.CLOSED;

        Message.obtain(mDecodeThread.getHandler(), DecodeHandler.SIGNAL_CONTROL_QUIT).sendToTarget();

        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            mDecodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        Log.d(TAG, "Decoder thread stopped.");

        // Be absolutely sure we don't send any queued up messages
        removeMessages(SIGNAL_DECODE_SUCCEED);
        removeMessages(SIGNAL_DECODE_FAILED);

    }

}
