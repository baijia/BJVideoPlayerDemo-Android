package com.baijiahulian.download;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baijiahulian.common.networkv2.HttpException;
import com.baijiahulian.downloader.download.DownloadInfo;
import com.baijiahulian.player.playerview.PlayerConstants;
import com.baijiahulian.video.R;
import com.baijiayun.download.DownloadListener;
import com.baijiayun.download.DownloadManager;
import com.baijiayun.download.DownloadModel;
import com.baijiayun.download.DownloadService;
import com.baijiayun.download.DownloadTask;
import com.baijiayun.download.constant.VideoDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Shubo on 2017/12/11.
 */

public class SimpleVideoDownloadActivity extends AppCompatActivity {

    @Bind(R.id.activity_simple_download_et)
    EditText etVideoID;

    @Bind(R.id.activity_simple_download_add)
    Button btnAdd;

    @Bind(R.id.activity_simple_download_rv)
    RecyclerView rvDownload;

    private DownloadManager manager;

    private DownloadAdapter adapter;

    private List<VideoDefinition> definitionList = new ArrayList<>(Arrays.asList(VideoDefinition._720P,
            VideoDefinition.SHD, VideoDefinition.HD, VideoDefinition.SD, VideoDefinition._1080P));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_download);
        ButterKnife.bind(this);

        //初始化下载
        manager = DownloadService.getDownloadManager(this);
        //设置缓存文件路径
        manager.setTargetFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/bb_video_downloaded/");
        //读取磁盘缓存的下载任务
        manager.loadDownloadInfo(32975272);

        adapter = new DownloadAdapter();
        rvDownload.setLayoutManager(new LinearLayoutManager(this));
        rvDownload.setAdapter(adapter);


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String videoId = etVideoID.getText().toString();
                if (TextUtils.isEmpty(videoId)) {
                    return;
                }
                newDownloadTask(videoId, "test12345678");
            }
        });
    }



    private void newDownloadTask(String videoId, String token){

        try{
            // 点播下载
            manager.newDownloadTask("video_" + videoId, Long.parseLong(videoId), token, definitionList, 0, "haha")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<DownloadTask>() {
                        @Override
                        public void call(DownloadTask task) {
                            adapter.notifyDataSetChanged();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            Toast.makeText(SimpleVideoDownloadActivity.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (NumberFormatException exception){
            Toast.makeText(SimpleVideoDownloadActivity.this, "VideoId格式不对", Toast.LENGTH_LONG).show();
        }
    }

    private class DownloadAdapter extends RecyclerView.Adapter<DownloadViewHolder> {

        @Override
        public DownloadViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = View.inflate(SimpleVideoDownloadActivity.this, R.layout.item_download_manager, null);
            return new DownloadViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final DownloadViewHolder holder, int i) {
            final DownloadTask task = manager.getAllTasks().get(i);
            final DownloadModel model = task.getDownloadInfo();
            holder.name.setText(model.targetName);
            String downloadLength = Formatter.formatFileSize(SimpleVideoDownloadActivity.this, model.downloadLength);
            String totalLength = Formatter.formatFileSize(SimpleVideoDownloadActivity.this, model.totalLength);
            holder.downloadSize.setText(downloadLength + "/" + totalLength);
            holder.pb.setProgress((int) (model.downloadLength / (float) model.totalLength * 100));
            switch (task.getTaskStatus()) {
                case Error:
                    holder.download.setText("出错");
                    holder.download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            task.start();
                        }
                    });
                    break;
                case New:
                    holder.download.setText("下载");
                    holder.tvProgress.setText("0%");
                    holder.download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            task.start();
                        }
                    });
                    break;
                case Pause:
                    holder.download.setText("继续");
                    holder.download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            task.start();
                        }
                    });
                    break;
                case Finish:
                    holder.download.setText("完成");
                    holder.tvProgress.setText("100%");
                    holder.netSpeed.setVisibility(View.INVISIBLE);
                    break;
                case Downloading:
                    holder.download.setText("暂停");
                    holder.download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            task.pause();
                        }
                    });
                    break;
            }

            holder.removeFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    manager.deleteTask(task);
                }
            });

            task.setDownloadListener(new DownloadListener() {
                @Override
                public void onProgress(DownloadTask task) {
                    String downloadLength = Formatter.formatFileSize(SimpleVideoDownloadActivity.this, model.downloadLength);
                    String totalLength = Formatter.formatFileSize(SimpleVideoDownloadActivity.this, model.totalLength);
                    holder.downloadSize.setText(downloadLength + "/" + totalLength);
                    holder.pb.setProgress((int) (model.downloadLength / (float) model.totalLength * 100));
                    holder.netSpeed.setVisibility(View.VISIBLE);
                    String speed = Formatter.formatFileSize(SimpleVideoDownloadActivity.this, (long) task.getSpeed());
                    holder.netSpeed.setText(speed + "/s");
                    holder.tvProgress.setText(task.getProgress() + "%");

                }

                @Override
                public void onError(final DownloadTask task, HttpException e) {
                    holder.download.setText("出错");
                    holder.download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            task.start();
                        }
                    });
                    e.printStackTrace();
                    //下载地址已失效
                    if(e.getCode() == 403){
                        //需要用户传入新的token
                        newDownloadTask(String.valueOf(task.getDownloadInfo().videoId), "test12345678");
                    }
                }

                @Override
                public void onPaused(final DownloadTask task) {
                    holder.download.setText("继续");
                    holder.download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            task.start();
                        }
                    });
                }

                @Override
                public void onStarted(final DownloadTask task) {
                    holder.download.setText("暂停");
                    holder.download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            task.pause();
                        }
                    });
                }

                @Override
                public void onFinish(DownloadTask task) {
                    holder.pb.setProgress(100);
                    holder.tvProgress.setText("100%");
                    holder.netSpeed.setVisibility(View.INVISIBLE);
                    String downloadLength = Formatter.formatFileSize(SimpleVideoDownloadActivity.this, model.downloadLength);
                    String totalLength = Formatter.formatFileSize(SimpleVideoDownloadActivity.this, model.totalLength);
                    holder.downloadSize.setText(downloadLength + "/" + totalLength);
                    holder.download.setText("完成");
                    holder.download.setOnClickListener(null);
                }

                @Override
                public void onDeleted(long vid) {
                    adapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return manager.getAllTasks().size();
        }
    }

    private class DownloadViewHolder extends RecyclerView.ViewHolder {

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

        public DownloadViewHolder(View convertView) {
            super(convertView);
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
    }

}
