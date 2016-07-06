package z0kai.filtercamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import z0kai.filterlib.FilterRender;
import z0kai.filterlib.RotationUtil;
import z0kai.filterlib.filters.BaseFilter;
import z0kai.filterlib.filters.SepiaFilter;

/**
 * Created by Z0Kai on 2016/7/4.
 */

public class CameraGLSurfaceView extends GLSurfaceView implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = CameraGLSurfaceView.class.getSimpleName();
    Context mContext;
    FilterRender mFilterRender;

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mFilterRender = new FilterRender(new BaseFilter());
        CameraInstance.getInstance().tryOpenCamera(null, Camera.CameraInfo.CAMERA_FACING_FRONT);
        mFilterRender.setUpCamera(CameraInstance.getInstance().getCameraDevice(), true);
        mFilterRender.setGLSurfaceView(this);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        CameraInstance.getInstance().stopCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        CameraInstance.getInstance().stopCamera();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }

    public void setFilter(@NonNull BaseFilter filter) {
        mFilterRender.setFilter(filter);
    }

}
