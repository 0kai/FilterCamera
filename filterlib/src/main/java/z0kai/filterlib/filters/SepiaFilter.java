package z0kai.filterlib.filters;

/**
 * Created by Z0Kai on 2016/7/5.
 */

public class SepiaFilter extends ColorMatrixFilter {
    public SepiaFilter() {
        this(1.0f);
    }

    public SepiaFilter(final float intensity) {
        super(intensity, new float[] {
                0.3588f, 0.7044f, 0.1368f, 0.0f,
                0.2990f, 0.5870f, 0.1140f, 0.0f,
                0.2392f, 0.4696f, 0.0912f, 0.0f,
                0f, 0f, 0f, 1.0f
        });
    }
}
