package com.baijiahulian.video;

import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import com.baijiahulian.player.BJPlayerView;
import com.baijiahulian.player.playerview.IPlayerBottomContact;
import com.baijiahulian.player.utils.Utils;

/**
 * Created by yanglei on 2016/11/3.
 */
public class BJBottomViewPresenterCopy implements IPlayerBottomContact.BottomView {
    private QueryCopy $;
    private IPlayerBottomContact.IPlayer mPlayer;

    private int mDuration = 0;
    private int mCurrentPosition = 0;
    private SeekBar mSeekBar;
    private boolean isSeekBarDraggable = true;

    public BJBottomViewPresenterCopy(View bottomView) {
        $ = QueryCopy.with(bottomView);

        mSeekBar = (SeekBar) $.id(R.id.bjplayer_seekbar).view();

        updateVideoProgress();


        $.id(R.id.bjplayer_video_player_btn).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    if (mPlayer.isPlaying()) {
                        mPlayer.pauseVideo();
                    } else {
                        mPlayer.playVideo();
                    }
                }
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean userTouch;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (userTouch) {
                    int pos = seekBar.getProgress() * mDuration / 100;
                    mPlayer.seekVideo(pos);
                }
                userTouch = false;
            }
        });

        mSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return !isSeekBarDraggable;
            }
        });

        $.id(R.id.bjplayer_orientation_switch_btn).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    mPlayer.switchOrientation();
                }
            }
        });
    }

    @Override
    public void onBind(IPlayerBottomContact.IPlayer player) {
        mPlayer = player;
        setOrientation(mPlayer.getOrientation());
        setIsPlaying(mPlayer.isPlaying());
    }

    @Override
    public void setDuration(int duration) {
        mDuration = duration;
        updateVideoProgress();
    }

    @Override
    public void setCurrentPosition(int position) {
        mCurrentPosition = position;
        updateVideoProgress();
    }

    @Override
    public void setIsPlaying(boolean isPlaying) {
        if (isPlaying) {
            $.id(R.id.bjplayer_video_player_btn)
                    .image(com.baijiahulian.player.R.drawable.bjplayer_ic_pause);
        } else {
            $.id(R.id.bjplayer_video_player_btn)
                    .image(com.baijiahulian.player.R.drawable.bjplayer_ic_play);
        }

//        $.id(R.id.bjplayer_video_next_btn).enable(mPlayer.hasNext());
    }

    @Override
    public void setOrientation(int orientation) {
        if (orientation == BJPlayerView.VIDEO_ORIENTATION_PORTRAIT) {
//            $.id(R.id.bjplayer_video_next_btn).gone();
            $.id(R.id.bjplayer_current_pos_tx).gone();
            $.id(R.id.bjplayer_duration_tx).gone();

            $.id(R.id.bjplayer_current_pos_duration_tx).visible();
            $.id(R.id.bjplayer_orientation_switch_btn).visible();
        } else {
//            $.id(R.id.bjplayer_video_next_btn).visible();
            $.id(R.id.bjplayer_current_pos_tx).visible();
            $.id(R.id.bjplayer_duration_tx).visible();

            $.id(R.id.bjplayer_current_pos_duration_tx).gone();
            $.id(R.id.bjplayer_orientation_switch_btn).gone();
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        // 只有 100ms 的 buf, ui 上根本看不出来
//        mSeekBar.setSecondaryProgress(mDuration == 0 ? 0 : mSeekBar.getProgress() + percent * 100 / mDuration);
    }

    @Override
    public void setSeekBarDraggable(boolean canDrag) {
        this.isSeekBarDraggable = canDrag;
    }

    private void updateVideoProgress() {
        String durationText = Utils.formatDuration(mDuration);
        String positionText = Utils.formatDuration(mCurrentPosition, mDuration >= 3600);
        $.id(R.id.bjplayer_current_pos_tx).text(positionText);
        $.id(R.id.bjplayer_duration_tx).text(durationText);
        $.id(R.id.bjplayer_current_pos_duration_tx).text(String.format("%s/%s", positionText, durationText));

        mSeekBar.setProgress(mDuration == 0 ? 0 : mCurrentPosition * 100 / mDuration);
    }
}
