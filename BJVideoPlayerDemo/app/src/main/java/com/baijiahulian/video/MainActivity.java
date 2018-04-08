package com.baijiahulian.video;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baijiahulian.BJVideoPlayerSDK;
import com.baijiahulian.common.networkv2.HttpException;
import com.baijiahulian.common.permission.AppPermissions;
import com.baijiahulian.download.DownloadActivity;
import com.baijiahulian.download.SimpleVideoDownloadActivity;
import com.baijiahulian.player.BJPlayerView;
import com.baijiahulian.player.SimpleOnPlayerViewListener;
import com.baijiahulian.player.bean.SectionItem;
import com.baijiahulian.player.bean.VideoItem;
import com.baijiahulian.player.playerview.PlayerConstants;
import com.baijiayun.persistence.MemoryPlayHelper;

import java.io.File;
import java.io.IOException;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    BJPlayerView playerView;
    EditText etToken;
    private TextView tvDeploy;
    private EditText etDBName;
    private BJBottomViewPresenterCopy bottomViewPresenterCopy;
    private int encryptType = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        final int type = getIntent().getIntExtra("extra_data", 0);

        etToken = (EditText) findViewById(R.id.et_token);
        tvDeploy = (TextView) findViewById(R.id.tv_deploy_type);
        etDBName = (EditText) findViewById(R.id.et_user_db_name);

        findViewById(R.id.button_download_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppPermissions.newPermissions(MainActivity.this)
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                if (aBoolean) {
                                    Intent intent = new Intent(MainActivity.this, SimpleVideoDownloadActivity.class);
                                    intent.putExtra("encryptType", encryptType);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MainActivity.this, "没有获取读写sd卡权限", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        if (type == BJPlayerView.PLAYER_DEPLOY_DEBUG) {
            etToken.setText("test12345678");
            tvDeploy.setText("当前环境： test");
        } else if (type == BJPlayerView.PLAYER_DEPLOY_BETA) {
            etToken.setText("");
            tvDeploy.setText("当前环境： beta");
        } else if (type == BJPlayerView.PLAYER_DEPLOY_ONLINE) {
            etToken.setText("");
            tvDeploy.setText("当前环境： online");
        }
        //设置点播下载的服务器环境，默认值正式环境
        PlayerConstants.DEPLOY_TYPE = type;
        playerView = (BJPlayerView) findViewById(R.id.videoView);
        //TODO 如果不需要自定义播放器样式，则centerview bottomview topview使用非Copy结尾的类替换
        bottomViewPresenterCopy = new BJBottomViewPresenterCopy(playerView.getBottomView());
        final BJCenterViewPresenterCopy centerpresenter = new BJCenterViewPresenterCopy(playerView.getCenterView());
        playerView.setPresenter(new BJTopViewPresenterCopy(playerView.getTopView()), centerpresenter, bottomViewPresenterCopy);
        playerView.initPartner(32975272, type, encryptType);
        playerView.setVideoEdgePaddingColor(Color.argb(255, 0, 0, 150));
        playerView.setEnableNetWatcher(false);

        EditText videoIdET = (EditText) findViewById(R.id.videoId);
        long videoId = Long.valueOf(videoIdET.getText().toString());
        playerView.setVideoId(videoId, etToken.getText().toString().trim());

        //传入SimpleOnPlayerViewListener,仅需实现对集成者有用的接口，更加简洁。
        playerView.setOnPlayerViewListener(new SimpleOnPlayerViewListener() {
            @Override
            public void onVideoInfoInitialized(BJPlayerView playerView, HttpException exception) {
                //TODO: 视频信息初始化结束
                if (exception != null) {
                    // 视频信息初始化成功
                    VideoItem videoItem = playerView.getVideoItem();
                }
            }

            @Override
            public void onPause(BJPlayerView playerView) {
                //TODO: video暂停
            }

            @Override
            public void onPlay(BJPlayerView playerView) {
                //TODO: 开始播放
            }

            @Override
            public void onError(BJPlayerView playerView, int code) {
                //TODO: 播放出错
                //code == -1 断网   code == -2 使用移动网络播放
            }

            @Override
            public void onUpdatePosition(BJPlayerView playerView, int position) {
                //TODO: 播放过程中更新播放位置
            }

            @Override
            public void onSeekComplete(BJPlayerView playerView, int position) {
                //TODO: 拖动进度条
            }

            @Override
            public void onSpeedUp(BJPlayerView playerView, float speedUp) {
                //TODO: 设置倍速播放
            }

            @Override
            public void onVideoDefinition(BJPlayerView playerView, int definition) {
                //TODO: 设置清晰度完成
            }

            @Override
            public void onPlayCompleted(BJPlayerView playerView, VideoItem item, SectionItem nextSection) {
                //TODO: 当前视频播放完成 [nextSection已被废弃，请勿使用]
            }

            @Override
            public void onVideoPrepared(BJPlayerView playerView) {
                //TODO: 准备好了，马上要播放
                // 可以在这时获取视频时长
                playerView.getDuration();
            }

            @Override
            public String getVideoTokenWhenInvalid() {
                //TODO 视频token出错，需要集成方重新获取并传入BJPlayerview。
                return "test12345678";
            }
        });

        MemoryPlayHelper memoryPlayHelper = MemoryPlayHelper.getInstance();
        memoryPlayHelper.init(MainActivity.this, true);
        playerView.setMemoryPlayHelper(memoryPlayHelper);


        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText videoIdET = (EditText) findViewById(R.id.videoId);
                long videoId = Long.valueOf(videoIdET.getText().toString());
                bottomViewPresenterCopy.setCurrentPosition(0);
                playerView.setVideoId(videoId, etToken.getText().toString().trim());
                EditText startPosText = (EditText) findViewById(R.id.startPos);
                int pos = Integer.valueOf(startPosText.getText().toString());
                playerView.playVideo(pos);

//                playerView.setVideoPath("http://d.gsxservice.com/logo_video_start.mp4");
                Toast.makeText(MainActivity.this, "开始播放", Toast.LENGTH_SHORT).show();
            }
        });

        //离线播放，传视频网络地址或者本地文件绝对路径
        findViewById(R.id.button_localVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = ((EditText) findViewById(R.id.localVideo)).getText().toString();
                if (input.startsWith("http://")) {
                    playerView.setVideoPath(input);
                    playerView.playVideo(0);
                    Toast.makeText(MainActivity.this, "设置网络地址" + input, Toast.LENGTH_SHORT).show();
                    return;
                }
                AppPermissions.newPermissions(MainActivity.this)
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                if (aBoolean) {
                                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                                        String path = root + File.separator + "bb_video_downloaded" + File.separator +
                                                ((EditText) findViewById(R.id.localVideo)).getText().toString();
                                        File file = new File(path);
                                        if (file.exists()) {
                                            //进度条进度归零
                                            bottomViewPresenterCopy.setCurrentPosition(0);
                                            playerView.setVideoPath(path);
                                            playerView.playVideo(0);
                                        } else {
                                            Toast.makeText(MainActivity.this, path + "不存在的", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "找不到存储卡！", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "没有获取读写sd卡权限", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        ((RadioGroup) findViewById(R.id.ad_play_method)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedBtnId) {
                int yesBtnId = findViewById(R.id.ad_yes).getId();
                if (checkedBtnId == yesBtnId) {
                    playerView.setHeadTailPlayMethod(BJPlayerView.HEAD_TAIL_PLAY_EVERY);
                } else {
                    playerView.setHeadTailPlayMethod(BJPlayerView.HEAD_TAIL_PLAY_NONE);
                }
            }
        });

        ((RadioGroup) findViewById(R.id.rg_seek_bar_draggable)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                int yesId = findViewById(R.id.seek_yes).getId();
                playerView.getBottomViewPresenter().setSeekBarDraggable(checkedId == yesId);
                playerView.enableSeekGesture(checkedId == yesId);
            }
        });

        ((RadioGroup) findViewById(R.id.ad_play_method_type)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                int typeImage = findViewById(R.id.ad_type_image).getId();
                if (checkedId == typeImage) {
                    playerView.setAdType(BJPlayerView.AD_TYPE_IMAGE);
                } else {
                    playerView.setAdType(BJPlayerView.AD_TYPE_VIDEO);
                }
            }
        });

        findViewById(R.id.button_ping).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PingActivity.class);
                startActivity(intent);
            }
        });
        //上传日志文件给后台，要有读写sd卡权限
        findViewById(R.id.button_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppPermissions.newPermissions(MainActivity.this)
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                if (aBoolean) {
                                    try {
                                        Runtime.getRuntime().exec("logcat -v time -f /sdcard/baijia_log.txt");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(MainActivity.this, "日志收集已开启", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "没有获取读写sd卡权限", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
//                BJFileLog.getInstance().uploadLogFile("12312", "322322", new BJFileLog.OnLogFileUploadListener() {
//                    @Override
//                    public void onLogFileUploadSuccess() {
//                        //TODO:after log upload success.
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(MainActivity.this, "log upload success!", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onLogFileUploadFailed(final String errorMsg) {
//                        //TODO: after log upload failed.
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(MainActivity.this, "log upload failed:  " + errorMsg, Toast.LENGTH_LONG).show();
//                            }
//                        });
//                    }
//                });
            }
        });

        findViewById(R.id.btn_download_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dbName = etDBName.getText().toString().trim();
                /**
                 * 设置用户数据库名称，比如123.db  如果app不使用sdk下载模块或者下载不区分用户则不必调用
                 * 请务必在VideoDownloadManager初始化之前调用
                 * 可以不调用但不要传空值
                 * */
                BJVideoPlayerSDK.getInstance().setCurUserDBName(dbName);
                AppPermissions.newPermissions(MainActivity.this)
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                if (aBoolean) {
                                    Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                                    intent.putExtra("encryptType", encryptType);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MainActivity.this, "没有获取读写sd卡权限", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        playerView.getTopViewPresenter().setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ((RadioGroup) findViewById(R.id.encrypt_group)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.encrypt_rb1) {
                    playerView.initPartner(32975272, type, 1);
                    encryptType = 1;
                } else {
                    playerView.initPartner(32975272, type, 0);
                    encryptType = 0;
                }
            }
        });

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (playerView != null) {
            playerView.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        if (!playerView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playerView != null) {
            playerView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playerView != null) {
            playerView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playerView != null) {
            playerView.onDestroy();
        }
    }
}
