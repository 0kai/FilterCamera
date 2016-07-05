package z0kai.filterlib;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.Queue;

import z0kai.filterlib.filters.BaseFilter;
import z0kai.filterlib.filters.SepiaFilter;

/**
 * Created by Z0Kai on 2016/7/4.
 */

public class FilterRender {

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

    static float textureVertices[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };

    private int texture;

    private BaseFilter mFilter;
    private int mProgram;
    private int mOutputWidth, mOutputHeight;

    public FilterRender(int texture) {
        this(texture, new BaseFilter());
    }

    public FilterRender(int texture, @NonNull BaseFilter filter)
    {
        mRunOnDraw = new LinkedList<>();

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

        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb2.asFloatBuffer();
        textureVerticesBuffer.put(textureVertices);
        textureVerticesBuffer.position(0);

        mFilter = filter;
        mFilter.init();
        mProgram = mFilter.getProgram();
    }

    public void setOutputSize(int width, int height) {
        mOutputWidth = width;
        mOutputHeight = height;
        mFilter.onOutputSizeChanged(width, height);
    }

    public void setFilter(@NonNull final BaseFilter filter) {
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

    public void draw(float[] mtx)
    {
        runAll(mRunOnDraw);
        GLES20.glUseProgram(mProgram);
        mFilter.runPendingOnDrawTasks();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);

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

//    public Bitmap getFilterBitmap(Bitmap bitmap) {
//        GPUImageRenderer renderer = new GPUImageRenderer(mFilter);
//        PixelBuffer buffer = new PixelBuffer(bitmap.getWidth(), bitmap.getHeight());
//        buffer.setRenderer(renderer);
//        renderer.setImageBitmap(bitmap, false);
//        Bitmap result = buffer.getBitmap();
//        mFilter.destroy();
//        renderer.deleteImage();
//        buffer.destroy();
//    }

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

