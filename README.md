zxing-android
=============

[![Build Status](https://travis-ci.org/kamikat/zxing-android.svg?branch=android)](https://travis-ci.org/kamikat/zxing-android)
[![Release](https://jitpack.io/v/moe.banana/zxing-android.svg)](https://jitpack.io/#moe.banana/zxing-android)

Core components to integrate zxing with Android project.

Install
-------

Add dependency to `build.gradle`

```
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile 'moe.banana:zxing-android:<VERSION>'
}
```

Usage
-----

To integrate QR code for example:

**Step 1** create a camera manager:

```java
class QRCodeCameraManager extends CameraManager {

    private Rect mRect;

    @Override
    public Camera getCamera() {
        return mCamera;
    }

    @Override
    public Rect getFramingRectInPreview() {
        if (mRect == null) {
            Camera.Size size = getPreviewSize();
            if (size == null) {
                return null;
            }
            mRect = new Rect(0, 0, size.width, size.height);
        }
        return mRect;
    }
}
```

Barcode decoder should only decode image in `Rect` object returned by `getFramingRectInPreview`.
In the example above, we request to decode whole image from camera to find a barcode.
Smaller decode area can accelerate recognization obviously.

**Step 2** implement a callback:


```java
class QRCodeScanListener implements BarcodeScanListener {

    @Override
    public void onStart() {
        Log.d(TAG, "Barcode scanning started.");
    }

    @Override
    public void onStop() {
        Log.d(TAG, "Barcode scanning stopped.");
    }

    @Override
    public void onError(Exception e) {
        Log.e(TAG, "Barcode scanning error.", e);
    }

    @Override
    public void onResult(Result result) {
        switch (result.getBarcodeFormat()) {
            case QR_CODE:
                String text = result.getText();
                // do something with decoded text
                break;
            case EAN_8:
            case EAN_13:
            case CODE_39:
            case CODE_93:
            case CODE_128:
                break;
        }
    }

    @Override
    public void foundPossibleResultPoint(ResultPoint point) {
        Log.d(TAG, "Barcode result points");
    }

}
```

**Step 3** create barcode scan manager:

```java
mBarcodeScanManager = new BarcodeScanManager(new QRCodeCameraManager());
mBarcodeScanManager.setBarcodeScanListener(new QRCodeScanListener());
mBarcodeScanManager.setHints(
    DecodeHintsBuilder.create().formats(BarcodeFormat.QR_CODE).build()
);
```

**Finally** when camera is ready, call `mBarcodeScanManager.start()`,
and call `mBarcodeScanManager.stop()` before stop camera preview or anytime the decoding loop should stop.

License
-------

(Apache License 2.0)

