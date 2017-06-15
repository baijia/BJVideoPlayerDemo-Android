package com.baijiahulian.bjvideoplayerdemo.download;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baijiahulian.bjvideoplayerdemo.R;
import com.baijiahulian.downloader.download.DownloadManager;
import com.baijiahulian.downloader.download.DownloadService;
import com.baijiahulian.downloader.request.GetRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


public class DownloadActivity extends AppCompatActivity {

    @Bind(R.id.targetFolder)
    TextView targetFolder;
    @Bind(R.id.tvCorePoolSize)
    TextView tvCorePoolSize;
    @Bind(R.id.sbCorePoolSize)
    SeekBar sbCorePoolSize;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.openManager)
    Button openManager;

    private ArrayList<DownloadModel> downloadModels;
    private DownloadManager downloadManager;
    private MainAdapter adapter;
    private Gson gson;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);
        sharedPreferences = getSharedPreferences("bj_download_info", MODE_PRIVATE);
        gson = new Gson();
        initData();

        downloadManager = DownloadService.getDownloadManager(this);
        downloadManager.setTargetFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa_video_downloaded/");

        targetFolder.setText("下载路径: " + downloadManager.getTargetFolder());
        sbCorePoolSize.setMax(5);
        sbCorePoolSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                downloadManager.getThreadPool().setCorePoolSize(progress);
                tvCorePoolSize.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        sbCorePoolSize.setProgress(3);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainAdapter(this);
        recyclerView.setAdapter(adapter);
        openManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DownloadManagerActivity.class));
            }
        });
        findViewById(R.id.btn_init_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DownloadActivity.this, DownloadDataSettingActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private class MainAdapter extends BaseRecyclerAdapter<DownloadModel, ViewHolder> {

        public MainAdapter(Context context) {
            super(context, downloadModels);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_download_details, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DownloadModel downloadModel = mDatas.get(position);
            holder.bind(downloadModel);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.name)
        TextView name;
        @Bind(R.id.icon)
        ImageView icon;
        @Bind(R.id.download)
        Button download;

        private DownloadModel downloadModel;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(DownloadModel downloadModel) {
            this.downloadModel = downloadModel;
            if (downloadManager.getDownloadInfo(downloadModel.getUrl()) != null) {
                download.setText("已在队列");
                download.setEnabled(false);
            } else {
                download.setText("下载");
                download.setEnabled(true);
            }
            name.setText(downloadModel.getName());
            download.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.download) {
                if (downloadManager.getDownloadInfo(downloadModel.getUrl()) != null) {
                    Toast.makeText(getApplicationContext(), "任务已经在下载列表中", Toast.LENGTH_SHORT).show();
                } else {
                    GetRequest getRequest = new GetRequest(downloadModel.getUrl());
                    downloadManager.addTask(downloadModel.getUrl(), downloadModel, getRequest, null);

                    download.setText("已在队列");
                    download.setEnabled(false);
                }
            } else {

            }
        }
    }

    private void initData() {
        downloadModels = new ArrayList<>();
        if (TextUtils.isEmpty(sharedPreferences.getString("downloadInfo", ""))) {
            initDummyData();
        } else {
            String models = sharedPreferences.getString("downloadInfo", "");
            JsonParser parser = new JsonParser();
            JsonArray jsonArray = parser.parse(models).getAsJsonArray();
            for (JsonElement element : jsonArray) {
                DownloadModel model = gson.fromJson(element, DownloadModel.class);
                downloadModels.add(model);
            }
        }
    }

    private void initDummyData() {
        DownloadModel model1 = new DownloadModel("学生端app", "http://d.gsxservice.com/app/genshuixue.apk?ct=");
        DownloadModel model2 = new DownloadModel("机构端app", "http://d.gsxservice.com/app/institution_android.apk");
        downloadModels.add(model1);
        downloadModels.add(model2);
        String models = gson.toJson(downloadModels);
        if (!TextUtils.isEmpty(models)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("downloadInfo", models);
            editor.apply();
        }
    }
}