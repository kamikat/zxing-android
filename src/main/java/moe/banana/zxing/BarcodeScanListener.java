package moe.banana.zxing;

import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;

/**
 * Listener to BarcodeScanManager events
 */
public interface BarcodeScanListener extends ResultPointCallback {

    /**
     * Scanner started
     */
    void onStart();

    /**
     * Scanner stopped
     */
    void onStop();

    /**
     * Internal error event
     * @param e exception object
     */
    void onError(Exception e);

    /**
     * Receive result from Barcode Scanner, the event will be triggered after onStop()
     *
     * @param result result object
     */
    void onResult(Result result);

}
