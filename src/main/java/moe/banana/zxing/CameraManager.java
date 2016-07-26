package moe.banana.zxing;

import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;

import com.google.zxing.PlanarYUVLuminanceSource;

@SuppressWarnings("deprecation")
public abstract class CameraManager {

    public static final String TAG = CameraManager.class.getSimpleName();

    /**
     * Override the method to provide a camera instance.
     *
     * @return a {@link Camera} object to request images from.
     */
    public abstract Camera getCamera();

    /**
     * Override the method to specify a framing rect.
     *
     * @return a {@link Rect} area representing barcode decode area in preview frame
     */
    public abstract Rect getFramingRectInPreview();

    public Camera.Parameters getParameters() {
        Camera camera = getCamera();
        if (camera != null) {
            try {
                return camera.getParameters();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public Camera.Size getPreviewSize() {
        Camera.Parameters parameters = getParameters();
        if (parameters != null) {
            return parameters.getPreviewSize();
        }
        return null;
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   A preview frame.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data) {
        Rect rect = getFramingRectInPreview();
        if (rect == null) {
            return null;
        }

        Camera.Size previewSize = getPreviewSize();
        if (previewSize == null) {
            return null;
        }

        // limit boundary of detection area
        if (rect.left < 0) rect.left = 0;
        if (rect.top < 0) rect.top = 0;
        if (rect.right >= previewSize.width) rect.right = previewSize.width - 1;
        if (rect.bottom >= previewSize.height) rect.bottom = previewSize.height - 1;

        return new PlanarYUVLuminanceSource(data,
                previewSize.width, previewSize.height, rect.left, rect.top,
                rect.width(), rect.height(), false);
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    public synchronized final void requestPreviewFrame(Handler handler, int message) {
        Camera camera = getCamera();
        if (camera != null) {
            camera.setOneShotPreviewCallback(new PreviewCallback(handler, message));
        }
    }

}
