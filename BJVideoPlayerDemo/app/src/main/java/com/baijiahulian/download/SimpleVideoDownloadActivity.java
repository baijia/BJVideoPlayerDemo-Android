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
import com.baijiahulian.player.utils.Utils;
import com.baijiahulian.video.R;
import com.baijiayun.download.DownloadListener;
import com.baijiayun.download.DownloadManager;
import com.baijiayun.download.DownloadModel;
import com.baijiayun.download.DownloadTask;
import com.baijiayun.download.IRecoveryCallback;
import com.baijiayun.download.OnNetChangeListener;
import com.baijiayun.download.RecoverDbHelper;
import com.baijiayun.download.constant.DownloadInfo;
import com.baijiayun.download.constant.TaskStatus;
import com.baijiayun.download.constant.VideoDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Shubo on 2017/12/11.
 */

public class SimpleVideoDownloadActivity extends AppCompatActivity {

    @BindView(R.id.activity_simple_download_et)
    EditText etVideoID;
    @BindView(R.id.activity_simple_download_token)
    EditText etToken;
    @BindView(R.id.activity_simple_download_add)
    Button btnAdd;
    @BindView(R.id.activity_simple_download_rv)
    RecyclerView rvDownload;
    @BindView(R.id.all_start)
    Button allStartBtn;
    @BindView(R.id.all_stop)
    Button allStopBtn;
    @BindView(R.id.batch_create_download)
    Button batchCreateDownloadBtn;

    private DownloadManager manager;
    private DownloadAdapter adapter;
    int encryptType;
    private Subscription subscription;
    private List<VideoDefinition> definitionList = new ArrayList<>(Arrays.asList(VideoDefinition._720P,
            VideoDefinition.SHD, VideoDefinition.HD, VideoDefinition.SD, VideoDefinition._1080P));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_download);
        ButterKnife.bind(this);
        encryptType = getIntent().getIntExtra("encryptType", 1);
        //初始化下载
        manager = CustomDownloadService.getDownloadManager(this);
        //自定义线程池
        //manager.setExecutorService(Executors.newSingleThreadExecutor());
        //设置缓存文件路径
        manager.setTargetFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/bb_video_downloaded/");
        //TODO RecoverDbHelper为恢复旧版下载记录的工具类。没有从旧版迁移到新版的需求不用调用此工具类
        RecoverDbHelper.getInstance().init(getApplicationContext(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa_video_downloaded/",
                new IRecoveryCallback() {
                    @Override
                    public void recoverySuccess() {
                        //重新获取taskList，true代表强制刷新
                        manager.loadDownloadInfo(32975272, encryptType, true);
                        adapter.notifyDataSetChanged();
                    }
                });
        //读取磁盘缓存的下载任务
        manager.loadDownloadInfo(32975272, encryptType);
        //TODO 这一句必须在manager.loadDownloadInfo()之后，确保DownloadManager已初始化完毕
        RecoverDbHelper.getInstance().recoveryDbData();

        adapter = new DownloadAdapter();
        rvDownload.setLayoutManager(new LinearLayoutManager(this));
        rvDownload.setAdapter(adapter);
        initListener();
        registerNetReceiver();
        subscribeBatchCreateDownload();
    }

    /**
     * 创建单个下载任务 批量创建请使用DownloadManager.batchCreateVideoDownloadTask
     * @param videoId
     * @param token
     */
    private void newDownloadTask(String videoId, String token) {
        try {
            // 点播下载
            DownloadInfo downloadInfo = new DownloadInfo(Long.parseLong(videoId), token, "video", "extraInfo");
            manager.newDownloadTask(downloadInfo, definitionList)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<DownloadTask>() {
                        @Override
                        public void call(DownloadTask task) {
                            //开启下载的守护service
                            CustomDownloadService.startService();
                            adapter.notifyDataSetChanged();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            HttpException httpException = Utils.convertException(throwable);
                            Toast.makeText(SimpleVideoDownloadActivity.this, "[" + httpException.getCode() + "]" + httpException.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (NumberFormatException exception) {
            Toast.makeText(SimpleVideoDownloadActivity.this, "VideoId格式不对", Toast.LENGTH_LONG).show();
        }
    }

    private void initListener(){
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String videoId = etVideoID.getText().toString();
                if (TextUtils.isEmpty(videoId)) {
                    return;
                }
                String token = etToken.getText().toString();
                if (TextUtils.isEmpty(token)) {
                    token = "test12345678";
                }
                newDownloadTask(videoId, token);
            }
        });

        allStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (DownloadTask downloadTask : manager.getAllTasks()) {
                    downloadTask.start();
                }
            }
        });

        allStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (DownloadTask downloadTask : manager.getAllTasks()) {
                    downloadTask.pause();
                }
            }
        });
        batchCreateDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<DownloadInfo> downloadInfos = new ArrayList<>();
                downloadInfos.add(new DownloadInfo(197052L, "test12345678", "video", "extraInfo"));
                downloadInfos.add(new DownloadInfo(222357L, "test12345678", "video", "extraInfo"));
                downloadInfos.add(new DownloadInfo(219333L, "test12345678", "video", "extraInfo"));
                downloadInfos.add(new DownloadInfo(214453L, "test12345678", "video", "extraInfo"));
                downloadInfos.add(new DownloadInfo(213699L, "test12345678", "video", "extraInfo"));
                downloadInfos.add(new DownloadInfo(213509L, "test12345678", "video", "extraInfo"));
                downloadInfos.add(new DownloadInfo(213497L, "test12345678", "video", "extraInfo"));
                downloadInfos.add(new DownloadInfo(213495L, "test12345678", "video", "extraInfo"));
                downloadInfos.add(new DownloadInfo(213484L, "test12345678", "video", "extraInfo"));
                downloadInfos.add(new DownloadInfo(212226L, "test12345678", "video", "extraInfo"));
                manager.batchCreateVideoDownloadTask(downloadInfos, definitionList);
            }
        });
    }

    private void registerNetReceiver(){
        //下载过程中监听网络变化
        manager.registerNetReceiver(new OnNetChangeListener() {
            @Override
            public void onMobile() {
                Toast.makeText(SimpleVideoDownloadActivity.this, "当前使用手机流量", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDisConnect() {
                Toast.makeText(SimpleVideoDownloadActivity.this, "网络断开，请检查网络", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNoAvailable() {
                Toast.makeText(SimpleVideoDownloadActivity.this, "当前无可用网络", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 监听批量下载
     */
    private void subscribeBatchCreateDownload(){
        subscription = manager.getBatchDownloadObserver()
                .onBackpressureBuffer()
                //减少notifyDataSetChange次数, buffer count 可自定义
                .buffer(10)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<DownloadTask>>() {
                    @Override
                    public void call(List<DownloadTask> downloadTasks) {
                        Log.d("yjm", "call invoke " + downloadTasks.size());
                        adapter.notifyDataSetChanged();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        adapter.notifyDataSetChanged();
                        Log.d("yjm", "error call");
                        //onError后需重新subscribe
                        subscribeBatchCreateDownload();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Log.d("yjm", "onComplete");
                        //onComplete后需重新subscribe
                        subscribeBatchCreateDownload();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //反注册网络监听
        manager.unregisterNetReceiver();
        RecoverDbHelper.getInstance().setRecoveryCallback(null);
        for(DownloadTask downloadTask : manager.getAllTasks()){
            downloadTask.setDownloadListener(null);
        }
        if(!subscription.isUnsubscribed()){
            subscription.unsubscribe();
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
            holder.videoName.setText("视频名称：" + model.videoName);
            holder.fileName.setText("文件名称：" + model.targetName);
            String downloadLength = Formatter.formatFileSize(SimpleVideoDownloadActivity.this, model.downloadLength);
            String totalLength = Formatter.formatFileSize(SimpleVideoDownloadActivity.this, model.totalLength);
            holder.downloadSize.setText(downloadLength + "/" + totalLength);
            int progress = (int) (model.downloadLength / (float) model.totalLength * 100);
            holder.pb.setProgress(progress);
            holder.tvProgress.setText(progress + "%");
            holder.netSpeed.setVisibility(task.getTaskStatus() == TaskStatus.Downloading ? View.VISIBLE : View.INVISIBLE);
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
                public void onProgress(final DownloadTask task) {
                    String downloadLength = Formatter.formatFileSize(SimpleVideoDownloadActivity.this, model.downloadLength);
                    String totalLength = Formatter.formatFileSize(SimpleVideoDownloadActivity.this, model.totalLength);
                    holder.downloadSize.setText(downloadLength + "/" + totalLength);
                    holder.pb.setProgress((int) (model.downloadLength / (float) model.totalLength * 100));
                    holder.netSpeed.setVisibility(View.VISIBLE);
                    String speed = Formatter.formatFileSize(SimpleVideoDownloadActivity.this, task.getSpeed());
                    holder.netSpeed.setText(speed + "/s");
                    holder.tvProgress.setText(task.getProgress() + "%");
                    holder.download.setText("暂停");
                    holder.download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            task.pause();
                        }
                    });
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
                    //下载地址已失效,5103(token已失效)
                    if (e.getCode() == 403 || e.getCode() >= 5101 && e.getCode() <= 5103) {
                        //TODO 需要用户传入新的token, 重新获取视频下载地址
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
                    Log.d("taskCallback", "onPaused invoke");
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
                    Log.d("taskCallback", "onStarted invoke");
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
                    Log.d("taskCallback", "onFinish invoke");
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

        private TextView videoName;
        private TextView fileName;
        private TextView downloadSize;
        private TextView tvProgress;
        private TextView netSpeed;
        private Button download;
        private ProgressBar pb;
        private Button removeFile;

        public DownloadViewHolder(View convertView) {
            super(convertView);
            videoName = convertView.findViewById(R.id.video_name_tv);
            fileName = convertView.findViewById(R.id.file_name_tv);
            downloadSize = convertView.findViewById(R.id.downloadSize);
            tvProgress = convertView.findViewById(R.id.tvProgress);
            netSpeed = convertView.findViewById(R.id.netSpeed);
            download = convertView.findViewById(R.id.start);
            pb = convertView.findViewById(R.id.pb_progress);
            removeFile = convertView.findViewById(R.id.remove_file);
        }
    }

}
