package moe.banana.zxing;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

@SuppressWarnings("unused")
public class DecodeHintsBuilder {

    public static DecodeHintsBuilder create() {
        return new DecodeHintsBuilder();
    }

    private final Map<DecodeHintType, Object> mHints;

    private DecodeHintsBuilder() {
        mHints = new EnumMap<>(DecodeHintType.class);
    }

    public DecodeHintsBuilder formats(BarcodeFormat... formats) {
        EnumSet<BarcodeFormat> _formats = EnumSet.noneOf(BarcodeFormat.class);
        _formats.addAll(Arrays.asList(formats));
        mHints.put(DecodeHintType.POSSIBLE_FORMATS, _formats);
        return this;
    }

    public DecodeHintsBuilder charset(String characterSet) {
        mHints.put(DecodeHintType.CHARACTER_SET, characterSet);
        return this;
    }

    public DecodeHintsBuilder callback(ResultPointCallback callback) {
        mHints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, callback);
        return this;
    }

    public Map<DecodeHintType, Object> build() {
        return mHints;
    }

    public void apply(BarcodeScanManager manager) {
        manager.setHints(this.build());
    }

}
