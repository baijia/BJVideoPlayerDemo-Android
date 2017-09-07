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

import com.baijiahulian.BJVideoPlayerSDK;
import com.baijiahulian.bjvideoplayerdemo.R;
import com.baijiahulian.downloader.download.VideoDownloadManager;
import com.baijiahulian.downloader.download.VideoDownloadService;
import com.baijiahulian.downloader.download.VideoNetExceptionBean;
import com.baijiahulian.player.BJPlayerView;
import com.baijiahulian.player.bean.VideoItem;

import java.util.List;

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
    @Bind(R.id.et_video_name)
    EditText etFileName;
    @Bind(R.id.tv_video_got_all_definition)
    TextView tvAllDefinition;
    @Bind(R.id.btn_goto_video_get_all_definition)
    Button btnGetDefinition;

    private VideoDownloadManager downloadManager;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * 释放下载模块
         * */
        BJVideoPlayerSDK.getInstance().releaseDownloadClient();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);

        //初始化
        downloadManager = VideoDownloadService.getDownloadManager(this);
        /**
         * 请改成您的partnerId和部署环境
         *     public static final int PLAYER_DEPLOY_DEBUG = 0; 百家云测试
         *     public static final int PLAYER_DEPLOY_BETA = 1;  百家云测试
         *     public static final int PLAYER_DEPLOY_ONLINE = 2;  客户集成使用
         * */
        downloadManager.initDownloadPartner(32975272, BJPlayerView.PLAYER_DEPLOY_DEBUG);
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

        btnGetDefinition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vid = etVid.getText().toString().trim();
                String token = etToken.getText().toString().trim();
                tvAllDefinition.setText("");
                downloadManager.getVideoDefinitionById(Integer.valueOf(vid), token, new VideoDownloadManager.OnVideoDefinitionListener() {
                    @Override
                    public void onVideoDefinitionSuccess(List<VideoItem.DefinitionItem> definitionItemList) {
                        for (VideoItem.DefinitionItem item : definitionItemList) {
                            tvAllDefinition.append(item.name + " ");
                        }
                    }

                    @Override
                    public void onVideoDefinitionFailed(String msg) {
                        Toast.makeText(DownloadActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
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

                String fileName = etFileName.getText().toString().trim();

                //添加一个下载任务，vid:视频id，token:视频token，type：视频清晰度（0普清 1高清 2超清），encryptType：加密类型（0 不加密，1加密）
                downloadManager.addDownloadVideoTask(fileName, Integer.valueOf(vid), token, type, encryptType, new VideoDownloadManager.OnVideoInfoGetListener() {
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
                    public void onVideoInfoGetFailed(final VideoNetExceptionBean netExceptionBean) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DownloadActivity.this, netExceptionBean.msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }
}