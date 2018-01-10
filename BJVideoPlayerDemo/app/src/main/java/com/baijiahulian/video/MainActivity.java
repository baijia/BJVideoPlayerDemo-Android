package com.baijiahulian.video;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baijiahulian.BJVideoPlayerSDK;
import com.baijiahulian.common.networkv2.BJNetCall;
import com.baijiahulian.common.networkv2.BJNetCallback;
import com.baijiahulian.common.networkv2.BJNetRequestManager;
import com.baijiahulian.common.networkv2.BJNetworkClient;
import com.baijiahulian.common.networkv2.BJRequestBody;
import com.baijiahulian.common.networkv2.BJResponse;
import com.baijiahulian.common.networkv2.HttpException;
import com.baijiahulian.common.permission.AppPermissions;
import com.baijiahulian.download.DownloadActivity;
import com.baijiahulian.download.SimpleVideoDownloadActivity;
import com.baijiahulian.player.BJFileLog;
import com.baijiahulian.player.BJPlayerView;
import com.baijiahulian.player.OnPlayerViewListener;
import com.baijiahulian.player.bean.SectionItem;
import com.baijiahulian.player.bean.VideoItem;
import com.baijiahulian.player.playerview.BJBottomViewPresenter;
import com.baijiahulian.player.playerview.BJCenterViewPresenter;
import com.baijiahulian.player.playerview.BJTopViewPresenter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    BJPlayerView playerView;
    EditText etToken;
    private String TOKEN = "test12345678";
    private TextView tvDeploy;
    private EditText etDBName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        int type = getIntent().getIntExtra("extra_data", 0);

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
        playerView = (BJPlayerView) findViewById(R.id.videoView);
        playerView.setBottomPresenter(new BJBottomViewPresenter(playerView.getBottomView()));
        playerView.setTopPresenter(new BJTopViewPresenter(playerView.getTopView()));
        BJCenterViewPresenter centerpresenter = new BJCenterViewPresenter(playerView.getCenterView());
        centerpresenter.setRightMenuHidden(true);
        playerView.setCenterPresenter(centerpresenter);

        playerView.initPartner(32975272, type);
//        playerView.setHeadTailPlayMethod(BJPlayerView.HEAD_TAIL_PLAY_NONE);
        playerView.setVideoEdgePaddingColor(Color.argb(255, 0, 0, 150));

//        playerView.setVideoId(153812L, "test12345678");

        playerView.setOnPlayerViewListener(new OnPlayerViewListener() {
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
        });

//        playerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
//            @Override
//            public void onViewAttachedToWindow(View v) {
//
//            }
//
//            @Override
//            public void onViewDetachedFromWindow(View v) {
//
//            }
//        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText videoIdET = (EditText) findViewById(R.id.videoId);
                long videoId = Long.valueOf(videoIdET.getText().toString());

                playerView.setVideoId(videoId, etToken.getText().toString().trim());

                EditText startPosText = (EditText) findViewById(R.id.startPos);
                int pos = Integer.valueOf(startPosText.getText().toString());
                playerView.playVideo(pos);

//                playerView.setVideoPath("http://d.gsxservice.com/logo_video_start.mp4");
                Toast.makeText(MainActivity.this, "开始播放", Toast.LENGTH_SHORT).show();
            }
        });

        //不加密播放，传视频网络地址或者本地文件绝对路径
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
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String path = root + File.separator + "bb_video_downloaded" + File.separator +
                            ((EditText) findViewById(R.id.localVideo)).getText().toString();
                    File file = new File(path);
                    if (file.exists()) {
                        playerView.setVideoPath(path);
                        playerView.playVideo(0);
                    } else {
                        Toast.makeText(MainActivity.this, path + "不存在的", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "找不到存储卡！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //加密播放，传本地视频file协议
        findViewById(R.id.button_localVideo_encrypt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String encryptFileName = ((EditText) findViewById(R.id.local_video_encrypt)).getText().toString();
                    String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String path = root + File.separator + "bb_video_downloaded" + File.separator + encryptFileName;
                    File file = new File(path);
                    if (file.exists()) {
                        playerView.setVideoPath("file://" + path);
                        playerView.playVideo(0);
                    } else {
                        Toast.makeText(MainActivity.this, path + "不存在的", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "找不到存储卡！", Toast.LENGTH_SHORT).show();
                }
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
                BJFileLog.getInstance().uploadLogFile("12312", "322322", new BJFileLog.OnLogFileUploadListener() {
                    @Override
                    public void onLogFileUploadSuccess() {
                        //TODO:after log upload success.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "log upload success!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onLogFileUploadFailed(final String errorMsg) {
                        //TODO: after log upload failed.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "log upload failed:  " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
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
                                    startActivity(new Intent(MainActivity.this, DownloadActivity.class));
                                } else {
                                    Toast.makeText(MainActivity.this, "没有获取读写sd卡权限", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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

    /**
     * @deprecated
     */
    public void querySectionListThenSet(final long serialId, final long videoId, final String sign, final Handler handler) {
        String queryUrl = "http://test-api.baijiacloud.com/vod/video/getPlayList";
        Map<String, String> params = new HashMap<>();
        params.put("vid", String.valueOf(videoId));
        params.put("sid", String.valueOf(serialId));
        params.put("sign", sign);
        BJNetworkClient client = new BJNetworkClient.Builder().setEnableLog(true).build();
        BJNetRequestManager requestManager = new BJNetRequestManager(client);
        BJRequestBody requestBody = BJRequestBody.createWithFormEncode(params);
        BJNetCall call = requestManager.newPostCall(queryUrl, requestBody);
        call.executeAsync(this, new BJNetCallback() {
            @Override
            public void onFailure(HttpException e) {
                handler.sendEmptyMessage(-2);
            }

            @Override
            public void onResponse(BJResponse bjResponse) {
                if (bjResponse.isSuccessful()) {
                    String responseContent = null;
                    try {
                        responseContent = bjResponse.getResponseString();
                        JsonParser parser = new JsonParser();
                        JsonElement je = parser.parse(responseContent);
                        final JsonObject jObj = je.getAsJsonObject();
                        int code = jObj.get("code").getAsInt();
                        if (code == 0) {
                            GsonBuilder gb = new GsonBuilder();
                            Gson g = gb.create();
                            JsonArray jArr = jObj.getAsJsonObject("data").getAsJsonArray("section_list");
                            SectionItem[] sectionList = g.fromJson(jArr, new TypeToken<SectionItem[]>() {
                            }.getType());
                            playerView.setVideoId(videoId, etToken.getText().toString().trim());
                            playerView.setCustomSectionList(sectionList);
                            handler.sendEmptyMessage(sectionList.length);
                            return;
                        }
                    } catch (IOException e) {
                    }
                }
                handler.sendEmptyMessage(-1);
            }
        });
    }
}
