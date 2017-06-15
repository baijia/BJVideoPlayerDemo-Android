package com.baijiahulian.bjvideoplayerdemo.video;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baijiahulian.bjvideoplayerdemo.R;
import com.baijiahulian.bjvideoplayerdemo.StorageUtil;
import com.baijiahulian.common.networkv2.BJNetCall;
import com.baijiahulian.common.networkv2.BJNetCallback;
import com.baijiahulian.common.networkv2.BJNetRequestManager;
import com.baijiahulian.common.networkv2.BJNetworkClient;
import com.baijiahulian.common.networkv2.BJRequestBody;
import com.baijiahulian.common.networkv2.BJResponse;
import com.baijiahulian.common.networkv2.HttpException;
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

public class MainActivity extends AppCompatActivity {

    BJPlayerView playerView;
    EditText etToken;
    private String TOKEN = "test12345678";
    private TextView tvDeploy;
    String sandBoxDir = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        int type = getIntent().getIntExtra("extra_data", 0);

        sandBoxDir = StorageUtil.getSaveDirector(this);
        etToken = (EditText) findViewById(R.id.et_token);
        tvDeploy = (TextView) findViewById(R.id.tv_deploy_type);
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
        playerView.setCenterPresenter(new BJCenterViewPresenter(playerView.getCenterView()));

        playerView.initPartner(32975272, type);
        playerView.setHeadTailPlayMethod(BJPlayerView.HEAD_TAIL_PLAY_NONE);
        playerView.setVideoEdgePaddingColor(Color.argb(255, 200, 0, 0));
        //开启本地文件日志功能
//        BJFileLog.start();

        playerView.setOnPlayerViewListener(new OnPlayerViewListener() {
            @Override
            public void onVideoInfoInitialized(BJPlayerView playerView, HttpException exception) {
                if (exception != null) {
                    // 视频信息初始化成功
                    VideoItem videoItem = playerView.getVideoItem();
                }
            }

            @Override
            public void onPause(BJPlayerView playerView) {

            }

            @Override
            public void onPlay(BJPlayerView playerView) {
                System.out.println("hola: player onPlay");
            }

            @Override
            public void onError(BJPlayerView playerView, int code) {

            }

            @Override
            public void onUpdatePosition(BJPlayerView playerView, int position) {

            }

            @Override
            public void onSeekComplete(BJPlayerView playerView, int position) {

            }

            @Override
            public void onSpeedUp(BJPlayerView playerView, float speedUp) {

            }

            @Override
            public void onVideoDefinition(BJPlayerView playerView, int definition) {

            }

            @Override
            public void onPlayCompleted(BJPlayerView playerView, VideoItem item, SectionItem nextSection) {
                if (nextSection != null) {
                    // play next section
                    playerView.setVideoId(0, nextSection.videoId, etToken.getText().toString().trim());
                    playerView.playVideo();
                }
            }

            @Override
            public void onVideoPrepared(BJPlayerView playerView) {
                // 可以在这时获取视频时长
                playerView.getDuration();
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText sIdET = (EditText) findViewById(R.id.serialId);
                EditText videoIdET = (EditText) findViewById(R.id.videoId);

                long serialId = Long.valueOf(sIdET.getText().toString());
                long videoId = Long.valueOf(videoIdET.getText().toString());

                playerView.setVideoId(serialId, videoId, etToken.getText().toString().trim());
                playerView.setCustomSectionList(null);
//                playerView.setVideoPath("http://d.gsxservice.com/logo_video_start.mp4");
                Toast.makeText(MainActivity.this, "SetVideoId OK!", Toast.LENGTH_SHORT).show();
            }
        });

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == -2) {
                    Toast.makeText(MainActivity.this, "获取播放列表失败! (网络错误)", Toast.LENGTH_SHORT).show();
                }
                if (msg.what == -1) {
                    Toast.makeText(MainActivity.this, "获取播放列表失败! (业务端错误)", Toast.LENGTH_SHORT).show();
                } else if (msg.what >= 0) {
                    Toast.makeText(MainActivity.this, "设置成功! 列表长度为" + msg.what, Toast.LENGTH_LONG).show();
                }
            }
        };

        findViewById(R.id.button_sl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText sIdET = (EditText) findViewById(R.id.serialId);
                EditText videoIdET = (EditText) findViewById(R.id.videoId);

                Long serialId = Long.valueOf(sIdET.getText().toString());
                Long videoId = Long.valueOf(videoIdET.getText().toString());
                querySectionListThenSet(serialId, videoId, "d7wc98+zi3FjODRuqfwBg7Gtj79UKIcTqtgUGaD7zH0=", handler);
            }
        });

        findViewById(R.id.button_localVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = ((EditText) findViewById(R.id.localVideo)).getText().toString();
                if (input.startsWith("http://")) {
                    playerView.setVideoPath(input);
                    Toast.makeText(MainActivity.this, "设置网络地址" + input, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String path = root + "/" + ((EditText) findViewById(R.id.localVideo)).getText().toString();
                    File file = new File(path);
                    if (file.exists()) {
                        playerView.setVideoPath(path);
                    } else {
                        Toast.makeText(MainActivity.this, path + "不存在", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "找不到存储卡！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.button_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerView.pause();
            }
        });

        findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerView.start();
            }
        });

        findViewById(R.id.button_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText startPosText = (EditText) findViewById(R.id.startPos);
                int pos = Integer.valueOf(startPosText.getText().toString());
                playerView.playVideo(pos);
            }
        });

        findViewById(R.id.button_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup newContainer = (ViewGroup) findViewById(R.id.newContainer);
                ViewGroup oldContainer = (ViewGroup) findViewById(R.id.oldContainer);
                oldContainer.removeView(playerView);
                newContainer.addView(playerView);
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

        findViewById(R.id.button_ping).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PingActivity.class);
                startActivity(intent);
            }
        });
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
