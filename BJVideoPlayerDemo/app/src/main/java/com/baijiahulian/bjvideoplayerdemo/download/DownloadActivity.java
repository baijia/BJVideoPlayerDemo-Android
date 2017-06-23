package com.baijiahulian.bjvideoplayerdemo.download;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baijiahulian.bjvideoplayerdemo.R;
import com.baijiahulian.downloader.download.VideoDownloadManager;
import com.baijiahulian.downloader.download.VideoDownloadService;

import butterknife.Bind;
import butterknife.ButterKnife;


public class DownloadActivity extends AppCompatActivity {

    @Bind(R.id.targetFolder)
    TextView targetFolder;
    @Bind(R.id.tvCorePoolSize)
    TextView tvCorePoolSize;
    @Bind(R.id.sbCorePoolSize)
    SeekBar sbCorePoolSize;
    @Bind(R.id.openManager)
    Button openManager;
    @Bind(R.id.btn_goto_video_download)
    Button btnVideoDownload;
    @Bind(R.id.et_vid)
    EditText etVid;
    @Bind(R.id.et_token)
    EditText etToken;
    @Bind(R.id.et_video_type)
    EditText etVideoType;
    @Bind(R.id.rg_encode_group)
    RadioGroup rgEncode;

    private VideoDownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);

        //初始化
        downloadManager = VideoDownloadService.getDownloadManager(this);
        //设置下载目标路径
        downloadManager.setTargetFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa_video_downloaded/");

        targetFolder.setText("下载路径: " + downloadManager.getTargetFolder());
        sbCorePoolSize.setMax(5);
        sbCorePoolSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //设置最大下载并发数，默认3个
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
        openManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DownloadManagerActivity.class));
            }
        });
        btnVideoDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vid = etVid.getText().toString().trim();
                String token = etToken.getText().toString().trim();
                String videoType = etVideoType.getText().toString().trim();
                int type;
                if (TextUtils.isEmpty(videoType)) {
                    type = 0;
                } else {
                    type = Integer.valueOf(videoType);
                }
                int encryptType;
                if (rgEncode.getCheckedRadioButtonId() == R.id.rb_encode_yes) {
                    encryptType = 1;
                } else {
                    encryptType = 0;
                }

                //添加一个下载任务，vid:视频id，token:视频token，type：视频清晰度（0普清 1高清 2超清），encryptType：加密类型（0 不加密，1加密）
                downloadManager.addDownloadVideoTask(Integer.valueOf(vid), token, type, encryptType, new VideoDownloadManager.OnVideoInfoGetListener() {
                    @Override
                    public void onVideoInfoGetSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DownloadActivity.this, "任务已添加，请到下载管理查看", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onVideoInfoGetFailed(final String msg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DownloadActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }
}