package z0kai.filterlib.filters;

import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.FloatBuffer;
import java.util.LinkedList;

import z0kai.filterlib.OpenGlUtils;

/**
 * Created by Z0Kai on 2016/7/5.
 */

public class BaseFilter {

    protected static final String NO_FILTER_VERTEX_SHADER =
            "attribute vec4 vPosition;" +
                    "attribute vec2 inputTextureCoordinate;" +
                    "varying vec2 textureCoordinate;" +
                    "void main()" +
                    "{"+
                    "gl_Position = vPosition;"+
                    "textureCoordinate = inputTextureCoordinate;" +
                    "}";

    protected static final String CAMERA_FILTER_FRAGMENT_SHADER_HEAD =
            "#extension GL_OES_EGL_image_external : require\n"+
                    "uniform samplerExternalOES inputImageTexture;\n";

    protected static final String BITMAP_FILTER_FRAGMENT_SHADER_HEAD =
            "uniform sampler2D inputImageTexture;\n";

    protected static final String NO_FILTER_FRAGMENT_SHADER =
            "precision mediump float;" +
                    "varying vec2 textureCoordinate;\n" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( inputImageTexture, textureCoordinate );\n" +
                    "}";

    private final LinkedList<Runnable> mRunOnDraw;
    private String mVertexShader;
    private String mFragmentShader;
    private int mProgram;
    private boolean mIsInitialized;

    protected int mOutputWidth;
    protected int mOutputHeight;

    /**
     * default to filter camera image
     */
    private boolean isForCamera = true;

    public BaseFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    public BaseFilter(final String vertexShader, final String fragmentShader) {
        mRunOnDraw = new LinkedList<>();
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
    }

    /**
     * only to filter bitmap, useful before calling init
     */
    public BaseFilter setAsStatic() {
        isForCamera = false;
        return this;
    }

    public final void init() {
        onInit();
        mIsInitialized = true;
        onInitialized();
    }

    public void onInit() {
        int vertexShader    = OpenGlUtils.loadShader(GLES20.GL_VERTEX_SHADER, mVertexShader);
        int fragmentShader  = OpenGlUtils.loadShader(GLES20.GL_FRAGMENT_SHADER,
                (isForCamera ? CAMERA_FILTER_FRAGMENT_SHADER_HEAD : BITMAP_FILTER_FRAGMENT_SHADER_HEAD) +
                        mFragmentShader);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);
    }

    public void onInitialized() {
    }

    public void onDrawArraysPre() {
    }

    public final void destroy() {
        if (!isInitialized()) return;
        GLES20.glDeleteProgram(mProgram);
        mIsInitialized = false;
        onDestroy();
    }

    public void onDestroy() {
    }

    public void onOutputSizeChanged(final int width, final int height) {
        mOutputWidth = width;
        mOutputHeight = height;
    }

    public int getProgram() {
        return mProgram;
    }

    public boolean isForCamera() {
        return isForCamera;
    }

    public void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    protected void setInteger(final int location, final int intValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1i(location, intValue);
            }
        });
    }

    protected void setFloat(final int location, final float floatValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1f(location, floatValue);
            }
        });
    }

    protected void setFloatVec2(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatVec3(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatVec4(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatArray(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1fv(location, arrayValue.length, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setPoint(final int location, final PointF point) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                float[] vec2 = new float[2];
                vec2[0] = point.x;
                vec2[1] = point.y;
                GLES20.glUniform2fv(location, 1, vec2, 0);
            }
        });
    }

    protected void setUniformMatrix3f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0);
            }
        });
    }

    protected void setUniformMatrix4f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0);
            }
        });
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }
}
