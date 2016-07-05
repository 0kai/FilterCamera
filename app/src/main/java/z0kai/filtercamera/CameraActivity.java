package z0kai.filtercamera;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import z0kai.filterlib.filters.BaseFilter;
import z0kai.filterlib.filters.BeautyFilter;
import z0kai.filterlib.filters.SepiaFilter;

public class CameraActivity extends AppCompatActivity {

    @BindView(R.id.rv_filter_buttons)
    RecyclerView rvFilterButtons;
    @BindView(R.id.camera_view)
    CameraGLSurfaceView cameraView;

    List<BaseFilter> filterList;
    List<String> filterName;

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
        mContext = this;
        initFilters();
    }

    private void initFilters() {
        filterList = new ArrayList<>();
        filterName = new ArrayList<>();

        addFilter("原图", new BaseFilter());
        addFilter("复古", new SepiaFilter());
        addFilter("美颜1", new BeautyFilter(getResources(), 1));
        addFilter("美颜2", new BeautyFilter(getResources(), 3));
        addFilter("美颜3", new BeautyFilter(getResources(), 5));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        rvFilterButtons.setLayoutManager(layoutManager);
        rvFilterButtons.setAdapter(new ButtonAdapter());
    }

    private void addFilter(String name, final BaseFilter filter) {
        filterName.add(name);
        filterList.add(filter);
    }

    private class ButtonAdapter extends RecyclerView.Adapter<ButtonAdapter.ViewHolder> {

        @Override
        public ButtonAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Button button = new Button(mContext);
            return new ViewHolder(button);
        }

        @Override
        public void onBindViewHolder(final ButtonAdapter.ViewHolder holder, int position) {
            holder.itemView.setText(filterName.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cameraView.setFilter(filterList.get(holder.getLayoutPosition()));
                }
            });
        }

        @Override
        public int getItemCount() {
            return filterName.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private Button itemView;

            public ViewHolder(Button itemView) {
                super(itemView);
                this.itemView = itemView;
            }
        }
    }
}
