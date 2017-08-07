package com.baijiahulian.bjvideoplayerdemo.download;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baijiahulian.bjvideoplayerdemo.R;
import com.baijiahulian.downloader.download.DownloadInfo;
import com.baijiahulian.downloader.download.DownloadManager;
import com.baijiahulian.downloader.download.VideoDownloadManager;
import com.baijiahulian.downloader.download.VideoDownloadService;
import com.baijiahulian.downloader.download.VideoNetExceptionBean;
import com.baijiahulian.downloader.listener.DownloadListener;
import com.baijiahulian.downloader.task.ExecutorWithListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DownloadManagerActivity extends AppCompatActivity implements View.OnClickListener, ExecutorWithListener.OnAllTaskEndListener {

    private List<DownloadInfo> allTask;
    private MyAdapter adapter;
    private VideoDownloadManager downloadManager;

    @Bind(R.id.listView)
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manager);
        ButterKnife.bind(this);

        downloadManager = VideoDownloadService.getDownloadManager(this);
        //获取所有任务列表
        allTask = downloadManager.getAllTask();
        adapter = new MyAdapter();
        listView.setAdapter(adapter);
        //设置全局监听
        downloadManager.getThreadPool().getExecutor().addOnAllTaskEndListener(this);
    }

    @Override
    public void onAllTaskEnd() {
        for (DownloadInfo downloadInfo : allTask) {
            if (downloadInfo.getState() != DownloadManager.FINISH) {
                Toast.makeText(DownloadManagerActivity.this, "所有下载线程结束，部分下载未完成", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Toast.makeText(DownloadManagerActivity.this, "所有下载任务完成", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //记得移除，否者会回调多次
        downloadManager.getThreadPool().getExecutor().removeOnAllTaskEndListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pauseAll:
                downloadManager.pauseAllTask();
                break;
            case R.id.startAll:
                downloadManager.startAllTask();
                break;
            case R.id.btn_delete_all_file:
                downloadManager.removeAllTaskAndFiles();
                adapter.notifyDataSetChanged();
                break;
        }
    }

    public void printMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return allTask.size();
        }

        @Override
        public DownloadInfo getItem(int position) {
            return allTask.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            DownloadInfo downloadInfo = getItem(position);
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(DownloadManagerActivity.this, R.layout.item_download_manager, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.refresh(downloadInfo);

            holder.name.setText(downloadInfo.getTaskKey());
            holder.download.setOnClickListener(holder);
            holder.remove.setOnClickListener(holder);
            holder.restart.setOnClickListener(holder);
            holder.removeFile.setOnClickListener(holder);

            DownloadListener downloadListener = new MyDownloadListener();
            downloadListener.setUserTag(holder);
            downloadInfo.setListener(downloadListener);
            return convertView;
        }
    }

    private class ViewHolder implements View.OnClickListener {
        private DownloadInfo downloadInfo;
        private TextView name;
        private TextView downloadSize;
        private TextView tvProgress;
        private TextView netSpeed;
        private Button download;
        private Button remove;
        private Button restart;
        private ProgressBar pb;
        private EditText etItemToken;
        private Button removeFile;


        public ViewHolder(View convertView) {
            name = (TextView) convertView.findViewById(R.id.name);
            downloadSize = (TextView) convertView.findViewById(R.id.downloadSize);
            tvProgress = (TextView) convertView.findViewById(R.id.tvProgress);
            netSpeed = (TextView) convertView.findViewById(R.id.netSpeed);
            download = (Button) convertView.findViewById(R.id.start);
            remove = (Button) convertView.findViewById(R.id.remove);
            restart = (Button) convertView.findViewById(R.id.restart);
            pb = (ProgressBar) convertView.findViewById(R.id.pb_progress);
            etItemToken = (EditText) convertView.findViewById(R.id.et_item_token);
            removeFile = (Button) convertView.findViewById(R.id.remove_file);
        }

        public void refresh(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
            refresh();
        }

        private void refresh() {
            String downloadLength = Formatter.formatFileSize(DownloadManagerActivity.this, downloadInfo.getDownloadLength());
            String totalLength = Formatter.formatFileSize(DownloadManagerActivity.this, downloadInfo.getTotalLength());
            downloadSize.setText(downloadLength + "/" + totalLength);
            if (downloadInfo.getState() == DownloadManager.NONE) {
                netSpeed.setText("停止");
                download.setText("下载");
            } else if (downloadInfo.getState() == DownloadManager.PAUSE) {
                netSpeed.setText("暂停中");
                download.setText("继续");
            } else if (downloadInfo.getState() == DownloadManager.ERROR) {
                netSpeed.setText("下载出错");
                download.setText("出错");
            } else if (downloadInfo.getState() == DownloadManager.WAITING) {
                netSpeed.setText("等待中");
                download.setText("等待");
            } else if (downloadInfo.getState() == DownloadManager.FINISH) {
                netSpeed.setText("下载完成");
            } else if (downloadInfo.getState() == DownloadManager.DOWNLOADING) {
                String networkSpeed = Formatter.formatFileSize(DownloadManagerActivity.this, downloadInfo.getNetworkSpeed());
                netSpeed.setText(networkSpeed + "/s");
                download.setText("暂停");
            }
            tvProgress.setText((Math.round(downloadInfo.getProgress() * 10000) * 1.0f / 100) + "%");
            pb.setMax((int) downloadInfo.getTotalLength());
            pb.setProgress((int) downloadInfo.getDownloadLength());
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == download.getId()) {
                switch (downloadInfo.getState()) {
                    case DownloadManager.PAUSE:
                    case DownloadManager.NONE:
                    case DownloadManager.ERROR:
                        //token 上层传过来
                        String token = etItemToken.getText().toString().trim();
                        if (TextUtils.isEmpty(token)) {
                            token = "test12345678";
                        }
                        downloadManager.addDownloadVideoTask(downloadInfo.getFileName(),downloadInfo.getVideoId(), token, downloadInfo.getVideoType(),
                                downloadInfo.getEncryptType(), new VideoDownloadManager.OnVideoInfoGetListener() {
                                    @Override
                                    public void onVideoInfoGetSuccess() {
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onVideoInfoGetFailed(VideoNetExceptionBean netExceptionBean) {
                                        printMsg(netExceptionBean.msg);
                                    }
                                });
                        break;
                    case DownloadManager.DOWNLOADING:
                        downloadManager.pauseTask(downloadInfo.getTaskKey());
                        break;
                    case DownloadManager.FINISH:
                        break;
                }
                refresh();

            } else if (v.getId() == remove.getId()) {
                downloadManager.removeTask(downloadInfo.getTaskKey());
                adapter.notifyDataSetChanged();
            } else if (v.getId() == restart.getId()) {
                downloadManager.restartTask(downloadInfo.getTaskKey());
            } else if (v.getId() == removeFile.getId()) {
                downloadManager.removeTask(downloadInfo.getTaskKey(), true);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private class MyDownloadListener extends DownloadListener {

        @Override
        public void onProgress(DownloadInfo downloadInfo) {
            if (getUserTag() == null) return;
            ViewHolder holder = (ViewHolder) getUserTag();
            holder.refresh();
        }

        @Override
        public void onFinish(DownloadInfo downloadInfo) {

        }

        @Override
        public void onError(DownloadInfo downloadInfo, String errorMsg, Exception e) {
            if (errorMsg != null)
                Toast.makeText(DownloadManagerActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }
}
