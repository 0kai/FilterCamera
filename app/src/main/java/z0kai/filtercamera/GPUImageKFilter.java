package z0kai.filtercamera;

import android.opengl.GLES20;

import jp.co.cyberagent.android.gpuimage.GPUImageColorMatrixFilter;

/**
 * Created by Z0Kai on 2016/7/4.
 */

public class GPUImageKFilter extends GPUImageColorMatrixFilter {

    private static final float[] colorMatrix = new float[] {
            0.3588f, 0.7044f, 0.1368f, 0.0f,
            0.2990f, 0.5870f, 0.1140f, 0.0f,
            0.2392f, 0.4696f, 0.0912f, 0.0f,
            0f, 0f, 0f, 1.0f
    };

    public GPUImageKFilter() {
        this(1.0f);
    }

    public GPUImageKFilter(final float intensity) {
        super(intensity, colorMatrix);
    }

    @Override
    public void onInit() {
        super.onInit();
        setFloat(GLES20.glGetUniformLocation(getProgram(), "intensity"), 1.0f);
        setUniformMatrix4f(GLES20.glGetUniformLocation(getProgram(), "colorMatrix"), colorMatrix);
    }
}
