package com.baijiahulian.video;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.baijiahulian.player.BJPlayerView;
import com.baijiahulian.player.playerview.BJBottomViewPresenter;
import com.baijiahulian.player.playerview.BJCenterViewPresenter;
import com.baijiahulian.player.playerview.BJTopViewPresenter;

import java.util.ArrayList;
import java.util.List;


public class RvVideoActivity extends AppCompatActivity {

    List<BJPlayerView> bjPlayerViewList = new ArrayList<>();
    RecyclerView videoRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv_video);
        videoRv = findViewById(R.id.video_rv);
        videoRv.setLayoutManager(new LinearLayoutManager(this));
        videoRv.setAdapter(new VideoAdapter());
        for (int i = 0; i < 3; i++) {
            BJPlayerView bjPlayerView = new BJPlayerView(this);
            bjPlayerView.setId(R.id.bj_player_view);
            bjPlayerViewList.add(bjPlayerView);
            bjPlayerView.initPartner(32975272, 0, 0);
            bjPlayerView.setVideoEdgePaddingColor(Color.argb(255, 0, 0, 150));
            bjPlayerView.setPresenter(new BJTopViewPresenter(bjPlayerView.getTopView()),
                    new BJCenterViewPresenter(bjPlayerView.getCenterView()), new BJBottomViewPresenter(bjPlayerView.getBottomView()));
            bjPlayerView.setVideoId(197052, "test12345678");
//            bjPlayerView.setAutoPlay(false);
//            bjPlayerView.playVideo();
        }

        videoRv.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                BJPlayerView bjPlayerView = view.findViewById(R.id.bj_player_view);
                if (bjPlayerView != null) {
                    bjPlayerView.setAutoPlay(true);
                    bjPlayerView.playVideo();
                }
                Log.d("yjm", "onChildViewAttachedToWindow bjPlayerView" + bjPlayerView.hashCode());

                Log.d("yjm", "bj0 " + videoRv.getChildAt(0).findViewById(R.id.bj_player_view).hashCode());
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                BJPlayerView bjPlayerView = view.findViewById(R.id.bj_player_view);
                if (bjPlayerView != null) {
                    bjPlayerView.pauseVideo();
                }
                Log.d("yjm", "onChildViewDetachedFromWindow bjPlayerView " + bjPlayerView.hashCode());
            }
        });
    }


    class VideoAdapter extends RecyclerView.Adapter<VideoViewHolder> {

        @NonNull
        @Override
        public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_video, parent, false);
            return new VideoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
            BJPlayerView bjPlayerView = bjPlayerViewList.get(position % 3);
            holder.container.removeAllViews();
            holder.container.addView(bjPlayerView);
//            bjPlayerView.setAutoPlay(true);
//            bjPlayerView.playVideo();
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {

        FrameLayout container;

        public VideoViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
        }
    }
}
