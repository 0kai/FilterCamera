package z0kai.filterlib.filters;

import android.graphics.PointF;
import android.opengl.GLES20;

/**
 * Created by Z0Kai on 2016/7/6.
 */

public class VignetteFilter extends BaseFilter {
    public static final String VIGNETTING_FRAGMENT_SHADER = "" +
            " varying highp vec2 textureCoordinate;\n" +
            " \n" +
            " uniform lowp vec2 vignetteCenter;\n" +
            " uniform lowp vec3 vignetteColor;\n" +
            " uniform highp float vignetteStart;\n" +
            " uniform highp float vignetteEnd;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     /*\n" +
            "     lowp vec3 rgb = texture2D(inputImageTexture, textureCoordinate).rgb;\n" +
            "     lowp float d = distance(textureCoordinate, vec2(0.5,0.5));\n" +
            "     rgb *= (1.0 - smoothstep(vignetteStart, vignetteEnd, d));\n" +
            "     gl_FragColor = vec4(vec3(rgb),1.0);\n" +
            "      */\n" +
            "     \n" +
            "     lowp vec3 rgb = texture2D(inputImageTexture, textureCoordinate).rgb;\n" +
            "     lowp float d = distance(textureCoordinate, vec2(vignetteCenter.x, vignetteCenter.y));\n" +
            "     lowp float percent = smoothstep(vignetteStart, vignetteEnd, d);\n" +
            "     gl_FragColor = vec4(mix(rgb.x, vignetteColor.x, percent), mix(rgb.y, vignetteColor.y, percent), mix(rgb.z, vignetteColor.z, percent), 1.0);\n" +
            " }";

    private int mVignetteCenterLocation;
    private PointF mVignetteCenter;
    private int mVignetteColorLocation;
    private float[] mVignetteColor;
    private int mVignetteStartLocation;
    private float mVignetteStart;
    private int mVignetteEndLocation;
    private float mVignetteEnd;

    public VignetteFilter() {
        this(new PointF(0.5f, 0.5f), new float[] {0.0f, 0.0f, 0.0f}, 0.40f, 0.8f);
    }

    public VignetteFilter(final PointF vignetteCenter, final float[] vignetteColor, final float vignetteStart, final float vignetteEnd) {
        super(NO_FILTER_VERTEX_SHADER, VIGNETTING_FRAGMENT_SHADER);
        mVignetteCenter = vignetteCenter;
        mVignetteColor = vignetteColor;
        mVignetteStart = vignetteStart;
        mVignetteEnd = vignetteEnd;

    }

    @Override
    public void onInit() {
        super.onInit();
        mVignetteCenterLocation = GLES20.glGetUniformLocation(getProgram(), "vignetteCenter");
        mVignetteColorLocation = GLES20.glGetUniformLocation(getProgram(), "vignetteColor");
        mVignetteStartLocation = GLES20.glGetUniformLocation(getProgram(), "vignetteStart");
        mVignetteEndLocation = GLES20.glGetUniformLocation(getProgram(), "vignetteEnd");

        setVignetteCenter(mVignetteCenter);
        setVignetteColor(mVignetteColor);
        setVignetteStart(mVignetteStart);
        setVignetteEnd(mVignetteEnd);
    }


    public void setVignetteCenter(final PointF vignetteCenter) {
        mVignetteCenter = vignetteCenter;
        setPoint(mVignetteCenterLocation, mVignetteCenter);
    }

    public void setVignetteColor(final float[] vignetteColor) {
        mVignetteColor = vignetteColor;
        setFloatVec3(mVignetteColorLocation, mVignetteColor);
    }

    public void setVignetteStart(final float vignetteStart) {
        mVignetteStart = vignetteStart;
        setFloat(mVignetteStartLocation, mVignetteStart);
    }

    public void setVignetteEnd(final float vignetteEnd) {
        mVignetteEnd = vignetteEnd;
        setFloat(mVignetteEndLocation, mVignetteEnd);
    }
}
