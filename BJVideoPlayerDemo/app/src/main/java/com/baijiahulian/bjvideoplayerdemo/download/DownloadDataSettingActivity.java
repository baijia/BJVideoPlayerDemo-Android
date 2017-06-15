package com.baijiahulian.bjvideoplayerdemo.download;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baijiahulian.bjvideoplayerdemo.R;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class DownloadDataSettingActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private List<DownloadModel> downloadModels;
    private MyAdapter adapter;
    private RecyclerView recyclerView;
    private EditText etName, etUrl;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_data_setting);
        sharedPreferences = getSharedPreferences("bj_download_info", MODE_PRIVATE);
        recyclerView = (RecyclerView) findViewById(R.id.rv_download_data);
        etName = (EditText) findViewById(R.id.et_task_name);
        etUrl = (EditText) findViewById(R.id.et_task_url);
        gson = new Gson();
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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
        findViewById(R.id.btn_download_add_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String url = etUrl.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(url)) {
                    Toast.makeText(DownloadDataSettingActivity.this, "任务或url是空，检查后再添加！", Toast.LENGTH_SHORT).show();
                    return;
                }
                DownloadModel model = new DownloadModel(name, url);
                downloadModels.add(model);
                adapter.notifyDataSetChanged();
                //更新缓存
                String newModels = gson.toJson(downloadModels);
                if (!TextUtils.isEmpty(newModels)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("downloadInfo", newModels);
                    editor.apply();
                }
                etName.setText("");
                etUrl.setText("");
            }
        });
        findViewById(R.id.btn_download_delete_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("downloadInfo", "");
                editor.apply();
                downloadModels.clear();
                adapter.notifyDataSetChanged();
            }
        });
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

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(DownloadDataSettingActivity.this).inflate(R.layout.item_download_info, parent, false);
            return new MyViewHolder(view);
        }


        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.tvName.setText(downloadModels.get(position).getName());
            holder.tvUrl.setText(downloadModels.get(position).getUrl());
        }


        @Override
        public int getItemCount() {
            return downloadModels.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView tvName, tvUrl;

            public MyViewHolder(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tv_name);
                tvUrl = (TextView) itemView.findViewById(R.id.tv_url_task);
            }
        }
    }
}
