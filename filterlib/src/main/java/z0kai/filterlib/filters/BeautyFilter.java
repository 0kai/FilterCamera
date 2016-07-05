package z0kai.filterlib.filters;

import android.content.res.Resources;
import android.opengl.GLES20;

import z0kai.filterlib.OpenGlUtils;
import z0kai.filterlib.R;

/**
 * Created by Z0Kai on 2016/7/5.
 */

public class BeautyFilter extends BaseFilter {
    private int mSingleStepOffsetLocation;
    private int mParamsLocation;
    private int level;

    public BeautyFilter(Resources resources) {
        this(resources, 2);
    }

    public BeautyFilter(Resources resources, int level) {
        super(NO_FILTER_VERTEX_SHADER, OpenGlUtils.readShaderFromRawResource(resources, R.raw.beautify_fragment));
        this.level = level;
    }

    public void onInit() {
        super.onInit();
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(getProgram(), "singleStepOffset");
        mParamsLocation = GLES20.glGetUniformLocation(getProgram(), "params");
        setBeautyLevel(level);
    }

    @Override
    public void onOutputSizeChanged(final int width, final int height) {
        super.onOutputSizeChanged(width, height);
        setTexelSize(width, height);
    }

    private void setTexelSize(final float w, final float h) {
        setFloatVec2(mSingleStepOffsetLocation, new float[] {2.0f / w, 2.0f / h});
    }

    public void setBeautyLevel(int level){
        switch (level) {
            case 1:
                setFloatVec4(mParamsLocation, new float[] {1.0f, 1.0f, 0.15f, 0.15f});
                break;
            case 2:
                setFloatVec4(mParamsLocation, new float[] {0.8f, 0.9f, 0.2f, 0.2f});
                break;
            case 3:
                setFloatVec4(mParamsLocation, new float[] {0.6f, 0.8f, 0.25f, 0.25f});
                break;
            case 4:
                setFloatVec4(mParamsLocation, new float[] {0.4f, 0.7f, 0.38f, 0.3f});
                break;
            case 5:
                setFloatVec4(mParamsLocation, new float[] {0.33f, 0.63f, 0.4f, 0.35f});
                break;
            default:
                break;
        }
    }
}
