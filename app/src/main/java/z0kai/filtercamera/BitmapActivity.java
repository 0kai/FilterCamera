package z0kai.filtercamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import z0kai.filterlib.OpenGlUtils;

import static z0kai.filterlib.OpenGlUtils.NO_TEXTURE;

public class BitmapActivity extends AppCompatActivity {

    @BindView(R.id.iv_bitmap)
    ImageView ivBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmap);

        ButterKnife.bind(this);
        showFilterImage();
    }

    private void showFilterImage() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.z_0kai_pet);
        int texture = OpenGlUtils.loadTexture(bitmap, NO_TEXTURE, true);
    }
}
