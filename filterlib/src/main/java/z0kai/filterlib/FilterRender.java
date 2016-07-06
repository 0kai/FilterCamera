package z0kai.filterlib;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import z0kai.filterlib.filters.BaseFilter;

/**
 * Created by Z0Kai on 2016/7/4.
 */

public class FilterRender implements GLSurfaceView.Renderer{

    private final LinkedList<Runnable> mRunOnDraw;

    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawListBuffer;
    private int mPositionHandle;
    private int mTextureCoordHandle;

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 2;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    static float squareCoords[] = {
            -1.0f,  1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f,  1.0f,
    };

    private int texture;

    private BaseFilter mFilter;
    private int mProgram;
    private int mOutputWidth, mOutputHeight;

    private GLSurfaceView mGLSurfaceView;
    private SurfaceTexture mSurface;
    int mTextureID = OpenGlUtils.NO_TEXTURE;

    private Camera mCamera;

    /**
     * @param filter NoNull
     */
    public FilterRender(BaseFilter filter) {
        mRunOnDraw = new LinkedList<>();
        mFilter = filter;
    }

    public void init(int texture)
    {
        this.texture = texture;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(8 * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb2.asFloatBuffer();
//        textureVerticesBuffer.put(textureVertices);
//        textureVerticesBuffer.position(0);

        if (mCamera == null) {
            setRotation(RotationUtil.TEXTURE_NO_ROTATION);
        }

        mFilter.init();
        mProgram = mFilter.getProgram();
    }

    public void setRotation(final float textureVertices[]) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                textureVerticesBuffer.clear();
                textureVerticesBuffer.put(textureVertices);
                textureVerticesBuffer.position(0);
            }
        });
    }

    public void setUpCamera(Camera camera, boolean isFacingFront) {
        mCamera = camera;

        runOnDraw(new Runnable() {
            @Override
            public void run() {
                mTextureID = OpenGlUtils.createTextureID();
                mSurface = new SurfaceTexture(mTextureID);
                init(mTextureID);
                mSurface.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        if (mGLSurfaceView != null) {
                            mGLSurfaceView.requestRender();
                        }
                    }
                });
                try {
                    mCamera.setPreviewTexture(mSurface);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();
            }
        });

        if (!isFacingFront) {
            setRotation(RotationUtil.TEXTURE_ROTATED_90);
        } else {
            setRotation(RotationUtil.TEXTURE_ROTATED_270_H_FLIP);
        }
        if (mGLSurfaceView != null) {
            mGLSurfaceView.requestRender();
        }
    }

    public void setGLSurfaceView(GLSurfaceView surfaceView) {
        mGLSurfaceView = surfaceView;
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGLSurfaceView.requestRender();
    }

    public void setOutputSize(int width, int height) {
        mOutputWidth = width;
        mOutputHeight = height;
        if (mFilter != null) {
            mFilter.onOutputSizeChanged(width, height);
        }
    }

    public void setFilter(final BaseFilter filter) {
        if (filter == mFilter) {
            return;
        }
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                BaseFilter oldFilter = mFilter;
                mFilter = filter;
                if (oldFilter != null) {
                    oldFilter.destroy();
                }
                mFilter.init();
                mProgram = mFilter.getProgram();
                GLES20.glUseProgram(mProgram);
                mFilter.onOutputSizeChanged(mOutputWidth, mOutputHeight);
            }
        });
    }

    public BaseFilter getFilter() {
        return mFilter;
    }

    public void deleteImage() {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                GLES20.glDeleteTextures(1, new int[]{
                        mTextureID
                }, 0);
                mTextureID = OpenGlUtils.NO_TEXTURE;
            }
        });
        mGLSurfaceView.requestRender();
    }

    public void draw()
    {
        GLES20.glUseProgram(mProgram);
        mFilter.runPendingOnDrawTasks();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        if (mFilter.isForCamera()) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        }
        mFilter.onDrawArraysPre();

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the <insert shape here> coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);

//        textureVerticesBuffer.clear();
//        textureVerticesBuffer.put( transformTextureCoordinates( textureVertices, mtx ));
//        textureVerticesBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
    }

    public Bitmap getFilterBitmap(final Bitmap bitmap) {
        PixelBuffer buffer = new PixelBuffer(bitmap.getWidth(), bitmap.getHeight());
        buffer.setRenderer(this);
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                mTextureID = OpenGlUtils.loadTexture(bitmap, OpenGlUtils.NO_TEXTURE, true);
                mSurface = new SurfaceTexture(mTextureID);
                init(mTextureID);
            }
        });
        Bitmap result = buffer.getBitmap();
//        mFilter.destroy();
        buffer.destroy();
        return result;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        setOutputSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        runAll(mRunOnDraw);
        mSurface.updateTexImage();
//        float[] mtx = new float[16];
//        mSurface.getTransformMatrix(mtx);
        draw();
    }

    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(runnable);
        }
    }
}

