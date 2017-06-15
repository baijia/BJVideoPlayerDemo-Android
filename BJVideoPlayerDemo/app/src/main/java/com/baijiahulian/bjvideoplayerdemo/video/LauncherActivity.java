package com.baijiahulian.bjvideoplayerdemo.video;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.baijiahulian.bjvideoplayerdemo.R;
import com.baijiahulian.bjvideoplayerdemo.download.DownloadActivity;
import com.baijiahulian.player.BJPlayerView;

public class LauncherActivity extends AppCompatActivity {
    private Button btnTest, btnBeta, btnOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        btnTest = (Button) findViewById(R.id.btn_test);
        btnBeta = (Button) findViewById(R.id.btn_beta);
        btnOnline = (Button) findViewById(R.id.btn_online);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LauncherActivity.this, MainActivity.class);
                i.putExtra("extra_data", BJPlayerView.PLAYER_DEPLOY_DEBUG);
                startActivity(i);
            }
        });
        btnBeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LauncherActivity.this, MainActivity.class);
                i.putExtra("extra_data", BJPlayerView.PLAYER_DEPLOY_BETA);
                startActivity(i);
            }
        });
        btnOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LauncherActivity.this, MainActivity.class);
                i.putExtra("extra_data", BJPlayerView.PLAYER_DEPLOY_ONLINE);
                startActivity(i);
            }
        });

        findViewById(R.id.btn_download_lab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LauncherActivity.this, DownloadActivity.class));
            }
        });
    }
}
