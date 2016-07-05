package z0kai.filterlib.filters;

import android.opengl.GLES20;

/**
 * Created by Z0Kai on 2016/7/5.
 */

public class ColorMatrixFilter extends BaseFilter {
    public static final String COLOR_MATRIX_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n"+
            "precision mediump float;" +
            "varying vec2 textureCoordinate;\n" +
            "uniform samplerExternalOES s_texture;\n" +
            "uniform lowp mat4 colorMatrix;\n" +
            "uniform lowp float intensity;\n" +

            "void main() {" +
            "    lowp vec4 textureColor = texture2D(s_texture, textureCoordinate);\n" +
            "    lowp vec4 outputColor = textureColor * colorMatrix;\n" +
            "    gl_FragColor = (intensity * outputColor) + ((1.0 - intensity) * textureColor);\n" +
            "}";

    private float mIntensity;
    private float[] mColorMatrix;
    private int mColorMatrixLocation;
    private int mIntensityLocation;

    public ColorMatrixFilter() {
        this(1.0f, new float[] {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        });
    }

    public ColorMatrixFilter(final float intensity, final float[] colorMatrix) {
        super(NO_FILTER_VERTEX_SHADER, COLOR_MATRIX_FRAGMENT_SHADER);
        mIntensity = intensity;
        mColorMatrix = colorMatrix;
    }

    @Override
    public void onInit() {
        super.onInit();
        mColorMatrixLocation = GLES20.glGetUniformLocation(getProgram(), "colorMatrix");
        mIntensityLocation = GLES20.glGetUniformLocation(getProgram(), "intensity");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setIntensity(mIntensity);
        setColorMatrix(mColorMatrix);
    }

    public void setIntensity(final float intensity) {
        mIntensity = intensity;
        setFloat(mIntensityLocation, intensity);
    }

    public void setColorMatrix(final float[] colorMatrix) {
        mColorMatrix = colorMatrix;
        setUniformMatrix4f(mColorMatrixLocation, colorMatrix);
    }
}
