package com.baijiahulian.bjvideoplayerdemo.video;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.baijiahulian.bjvideoplayerdemo.R;
import com.stealthcopter.networktools.Ping;
import com.stealthcopter.networktools.ping.PingResult;
import com.stealthcopter.networktools.ping.PingStats;

//import android.support.v7.widget.Toolbar;
//import com.stealthcopter.networktools.ARPInfo;


public class PingActivity extends AppCompatActivity {


    private TextView resultText;
    private EditText editIpAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);


//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        resultText = (TextView) findViewById(R.id.myText);
        editIpAddress = (EditText) findViewById(R.id.editIpAddress);

        findViewById(R.id.pingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doPing();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    private void appendResultsText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultText.append(text + "\n");
            }
        });
    }

    private void doPing() throws Exception {
        String ipAddress = editIpAddress.getText().toString();

        if (TextUtils.isEmpty(ipAddress)) {
            appendResultsText("Invalid Ip Address");
            return;
        }

        // Perform a single synchronous ping
        PingResult pingResult = Ping.onAddress(ipAddress).setTimeOutMillis(1000).doPing();

        appendResultsText("Pinging Address: " + pingResult.getAddress().getHostAddress());
        appendResultsText("HostName: " + pingResult.getAddress().getHostName());
        appendResultsText(String.format("%.2f ms", pingResult.getTimeTaken()));


        // Perform an asynchronous ping
        Ping.onAddress(ipAddress).setTimeOutMillis(1000).setTimes(5).doPing(new Ping.PingListener() {
            @Override
            public void onResult(PingResult pingResult) {
                appendResultsText(String.format("%.2f ms", pingResult.getTimeTaken()));
            }

            @Override
            public void onFinished(PingStats pingStats) {
                appendResultsText(String.format("Pings: %d, Packets lost: %d",
                        pingStats.getNoPings(), pingStats.getPacketsLost()));
                appendResultsText(String.format("Min/Avg/Max Time: %.2f/%.2f/%.2f ms",
                        pingStats.getMinTimeTaken(), pingStats.getAverageTimeTaken(), pingStats.getMaxTimeTaken()));
            }
        });

    }
}
