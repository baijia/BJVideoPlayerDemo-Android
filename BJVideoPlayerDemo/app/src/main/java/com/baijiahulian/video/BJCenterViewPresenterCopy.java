package com.baijiahulian.video;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.baijiahulian.player.BJPlayerView;
import com.baijiahulian.player.bean.SectionItem;
import com.baijiahulian.player.bean.VideoItem;
import com.baijiahulian.player.playerview.CenterViewStatus;
import com.baijiahulian.player.playerview.IPlayerCenterContact;
import com.baijiahulian.player.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanglei on 2016/11/7.
 */
public class BJCenterViewPresenterCopy implements IPlayerCenterContact.CenterView {

    private static final int CENTER_PAGE_INIT = 0;
    private static final int CENTER_PAGE_FRAME = 1 << 0;
    private static final int CENTER_PAGE_RATE = 1 << 1;
    private static final int CENTER_PAGE_SEGMENTS = 1 << 2;


    private CenterViewStatus centerViewStatus = CenterViewStatus.NONE;
    private RecyclerView courseView;
    private CourseAdapter courseAdapter;

    private RecyclerView rvDefinition;
    private DefinitionAdapter definitionAdapter;

    private int mCenterPageState = CENTER_PAGE_INIT;

    private QueryCopy $;
    private IPlayerCenterContact.IPlayer mPlayer;
    private CenterHandler mHandler;
    private boolean isDialogShowing = false;
    private List<VideoItem.DefinitionItem> definitionItemList;

    private boolean isRightMenuHidden = false;
    private View centerView;
    private boolean isBackTouch;

    public void setRightMenuHidden(boolean rightMenuHidden) {
        isRightMenuHidden = rightMenuHidden;
    }

    public BJCenterViewPresenterCopy(final View centerView) {
        this.centerView = centerView;
        $ = QueryCopy.with(centerView);
        mHandler = new CenterHandler(this);

        $.id(R.id.bjplayer_center_video_functions_rate_tv).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer.getOrientation() != BJPlayerView.VIDEO_ORIENTATION_LANDSCAPE)
                    return;
                mCenterPageState = CENTER_PAGE_RATE;
                setPageView();
            }
        });

        $.id(R.id.bjplayer_center_video_functions_segments_tv).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer.getOrientation() != BJPlayerView.VIDEO_ORIENTATION_LANDSCAPE)
                    return;
                mCenterPageState = CENTER_PAGE_SEGMENTS;
                setPageView();
            }
        });

        $.id(R.id.bjplayer_center_video_functions_frame_tv).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer.getOrientation() != BJPlayerView.VIDEO_ORIENTATION_LANDSCAPE)
                    return;
                mCenterPageState = CENTER_PAGE_FRAME;
                setPageView();
            }
        });

        initFunctions();
        definitionItemList = new ArrayList<>();
        definitionAdapter = new DefinitionAdapter(centerView.getContext());
        rvDefinition =  centerView.findViewById(com.baijiahulian.player.R.id.rv_bjplayer_center_view_definition);
//        rvDefinition.setLayoutManager(new WrappingRecyclerViewLayoutManager(centerView.getContext()));
        rvDefinition.setLayoutManager(new LinearLayoutManager(centerView.getContext()));
        rvDefinition.setAdapter(definitionAdapter);
        definitionAdapter.setOnRvItemClickListener(new OnRvItemClickListener() {
            @Override
            public void onItemClick(View view, int index) {
                mPlayer.setVideoDefinition(Utils.getVideoDefinitionFromString(definitionItemList.get(index).type));
                definitionAdapter.notifyDataSetChanged();
                onBackTouch();
            }
        });

        courseAdapter = new CourseAdapter(centerView.getContext());
        courseView =  centerView.findViewById(com.baijiahulian.player.R.id.bjplayer_layout_center_video_functions_segments_list_rv);
        courseView.setLayoutManager(new LinearLayoutManager(centerView.getContext()));
        courseView.setAdapter(courseAdapter);
        courseAdapter.setOnItemClickListener(new OnRvItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPlayer.onPlaySection(position);
            }
        });
    }

    @Override
    public void onBind(IPlayerCenterContact.IPlayer player) {
        mPlayer = player;
        setPageView();
    }

    @Override
    public boolean onBackTouch() {
        if (mCenterPageState > CENTER_PAGE_INIT) {
            mCenterPageState = CENTER_PAGE_INIT;
            isBackTouch = true;
            setPageView();
            isBackTouch = false;
            return true;
        }
        return false;
    }

    @Override
    public void setOrientation(int orientation) {
        if (orientation == BJPlayerView.VIDEO_ORIENTATION_PORTRAIT) {
            onHide();
            $.id(com.baijiahulian.player.R.id.bjplayer_center_video_functions_ll).gone();
        }
    }

    @Override
    public void showProgressSlide(int delta) {
        centerView.setVisibility(View.VISIBLE);
        $.id(R.id.bjplayer_center_video_progress_dialog_ll).visible();
        $.id(R.id.bjplayer_center_video_progress_dialog_loading_pb).gone();
        $.id(R.id.bjplayer_center_video_progress_dialog_title_iv).visible();
        $.id(R.id.bjplayer_center_video_progress_dialog_message_tv).visible();
        $.id(R.id.bjplayer_center_video_progress_dialog_buttons_ll).gone();
        $.id(R.id.bjplayer_center_controller_volume_dialog_ll).gone();

        String durationText = Utils.formatDuration(mPlayer.getDuration());
        String positionText = Utils.formatDuration(mPlayer.getCurrentPosition() + delta, mPlayer.getDuration() >= 3600);
        $.id(R.id.bjplayer_center_video_progress_dialog_message_tv).text(String.format("%s/%s", positionText, durationText));

//        String title = delta > 0 ? ("+ " + Utils.formatDuration(delta)) : "" + Utils.formatDuration(delta);
        if (delta > 0) {
            $.id(R.id.bjplayer_center_video_progress_dialog_title_iv).image(com.baijiahulian.player.R.drawable.bjplayer_ic_kuaijin);
        } else {
            $.id(R.id.bjplayer_center_video_progress_dialog_title_iv).image(com.baijiahulian.player.R.drawable.bjplayer_ic_huitui);
        }
        centerViewStatus = CenterViewStatus.FUNCTION;
    }

    @Override
    public void showLoading(String message) {
        isDialogShowing = true;
        centerView.setVisibility(View.VISIBLE);
        $.id(R.id.bjplayer_center_video_progress_dialog_ll).visible();
        $.id(R.id.bjplayer_center_video_progress_dialog_loading_pb).visible();
        $.id(R.id.bjplayer_center_video_progress_dialog_title_iv).gone();
        $.id(R.id.bjplayer_center_video_progress_dialog_message_tv).visible();
        $.id(R.id.bjplayer_center_video_progress_dialog_message_tv).text(message);
        $.id(R.id.bjplayer_center_video_progress_dialog_buttons_ll).gone();
        $.id(R.id.bjplayer_center_controller_volume_dialog_ll).gone();
        centerViewStatus = CenterViewStatus.LOADING;
    }

    @Override
    public void dismissLoading() {
        isDialogShowing = false;
        $.id(R.id.bjplayer_center_video_progress_dialog_ll).gone();
        $.id(R.id.bjplayer_center_controller_volume_dialog_ll).gone();
        centerViewStatus = CenterViewStatus.NONE;
    }

    @Override
    public void showVolumeSlide(int volume, int maxVolume) {
        centerView.setVisibility(View.VISIBLE);
        $.id(R.id.bjplayer_center_video_progress_dialog_ll).gone();
        $.id(R.id.bjplayer_center_controller_volume_dialog_ll).visible();

        int value = volume * 100 / maxVolume;
        if (value == 0) {
            $.id(R.id.bjplayer_center_controller_volume_ic_iv).image(com.baijiahulian.player.R.drawable.bjplayer_ic_volume_off_white);
            $.id(R.id.bjplayer_center_controller_volume_tv).text("off");
        } else {
            $.id(R.id.bjplayer_center_controller_volume_ic_iv).image(com.baijiahulian.player.R.drawable.bjplayer_ic_volume_up_white);
            $.id(R.id.bjplayer_center_controller_volume_tv).text(value + "%");
        }
        mHandler.sendMsgDismissDialogDelay();
        centerViewStatus = CenterViewStatus.FUNCTION;
    }

    @Override
    public void showBrightnessSlide(int brightness) {
        centerView.setVisibility(View.VISIBLE);
        $.id(R.id.bjplayer_center_video_progress_dialog_ll).gone();
        $.id(R.id.bjplayer_center_controller_volume_dialog_ll).visible();

        $.id(R.id.bjplayer_center_controller_volume_ic_iv).image(com.baijiahulian.player.R.drawable.bjplayer_ic_brightness);
        $.id(R.id.bjplayer_center_controller_volume_tv).text(brightness + "%");
        mHandler.sendMsgDismissDialogDelay();
        centerViewStatus = CenterViewStatus.FUNCTION;
    }

    @Override
    public void showError(int what, int extra) {
        String[] errorTips = $.contentView().getContext().getResources().getStringArray(com.baijiahulian.player.R.array.bjplayer_error_tips);
        int index = what - 1;
        String error;
        if (what >= 0 && what < errorTips.length) {
            error = errorTips[index];
        } else {
            error = $.contentView().getContext().getString(com.baijiahulian.player.R.string.bjplayer_error_unknow);
        }
        if(what == 500){
            error = $.contentView().getContext().getString(com.baijiahulian.player.R.string.bjplayer_network_error);
        }
        showError(what, error);
    }

    @Override
    public void showError(final int code, String message) {
        mHandler.removeCallbacksAndMessages(null);
        isDialogShowing = true;
        centerView.setVisibility(View.VISIBLE);
        onHide();
        $.id(R.id.bjplayer_center_controller_volume_dialog_ll).gone();
        $.id(R.id.bjplayer_center_video_progress_dialog_ll).visible();
        $.id(R.id.bjplayer_center_video_progress_dialog_loading_pb).gone();
        $.id(R.id.bjplayer_center_video_progress_dialog_title_iv).gone();
        $.id(R.id.bjplayer_center_video_progress_dialog_message_tv).text(message + "\n[" + code + "]\n");
        $.id(R.id.bjplayer_center_video_progress_dialog_message_tv).visible();
        $.id(R.id.bjplayer_center_video_progress_dialog_buttons_ll).visible();
        $.id(R.id.bjplayer_center_video_progress_dialog_button2_tv).gone();
        //videoId未初始化时不显示重新加载的按钮
        if(code != -3){
            $.id(R.id.bjplayer_center_video_progress_dialog_button1_tv).visible();
            $.id(R.id.bjplayer_center_video_progress_dialog_button1_tv).text($.contentView().getContext().getString(com.baijiahulian.player.R.string.bjplayer_video_reload));
            $.id(R.id.bjplayer_center_video_progress_dialog_button1_tv).clicked(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissLoading();
                    if(code == 500){
                        mPlayer.ijkInternalError();
                    } else{
                        mPlayer.playVideo();
                    }
                }
            });
        }
        centerViewStatus = CenterViewStatus.ERROR;
    }

    @Override
    public void showWarning(String warn) {
        mHandler.removeCallbacksAndMessages(null);
        isDialogShowing = true;
        centerView.setVisibility(View.VISIBLE);
        onHide();

        $.id(R.id.bjplayer_center_controller_volume_dialog_ll).gone();
        $.id(R.id.bjplayer_center_video_progress_dialog_ll).visible();
        $.id(R.id.bjplayer_center_video_progress_dialog_loading_pb).gone();
        $.id(R.id.bjplayer_center_video_progress_dialog_title_iv).gone();
        $.id(R.id.bjplayer_center_video_progress_dialog_message_tv).text(warn + "\n");
        $.id(R.id.bjplayer_center_video_progress_dialog_message_tv).visible();
        $.id(R.id.bjplayer_center_video_progress_dialog_buttons_ll).visible();
        $.id(R.id.bjplayer_center_video_progress_dialog_button1_tv).visible();
        $.id(R.id.bjplayer_center_video_progress_dialog_button2_tv).gone();
        $.id(R.id.bjplayer_center_video_progress_dialog_button1_tv).text($.contentView().getContext().getString(com.baijiahulian.player.R.string.bjplayer_video_goon));
        $.id(R.id.bjplayer_center_video_progress_dialog_button1_tv).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissLoading();
                mPlayer.setEnableNetWatcher(false);
                mPlayer.playVideo();
            }
        });
        centerViewStatus = CenterViewStatus.WARNING;
    }

    @Override
    public void onShow() {
        if (mPlayer != null && mPlayer.getOrientation() == BJPlayerView.VIDEO_ORIENTATION_LANDSCAPE && !isRightMenuHidden) {
            $.id(R.id.bjplayer_center_video_functions_ll).visible();
        } else {
            $.id(R.id.bjplayer_center_video_functions_ll).gone();
        }
    }

    @Override
    public void onHide() {
        $.id(R.id.bjplayer_layout_center_video_functions_segments_ll).gone();
        $.id(R.id.bjplayer_layout_center_video_functions_rate_ll).gone();
        $.id(R.id.bjplayer_layout_center_video_functions_frame_ll).gone();
        $.id(R.id.bjplayer_center_video_functions_ll).gone();
        mCenterPageState = CENTER_PAGE_INIT;
    }

    @Override
    public void onVideoInfoLoaded(VideoItem videoItem) {
        if (videoItem == null) {
            return;
        }
        if (courseAdapter != null) {
            courseAdapter.notifyDataSetChanged();
        }
        if (videoItem.definition != null) {
            definitionItemList = videoItem.definition;
            if(!TextUtils.isEmpty(videoItem.audioUrl)){
                VideoItem.DefinitionItem audioItem = new VideoItem.DefinitionItem("audio", "音频");
                definitionItemList.add(audioItem);
            }
            definitionAdapter.notifyDataSetChanged();
        }
        if (mPlayer.isPlayLocalVideo()) {
            $.id(R.id.bjplayer_center_video_functions_frame_tv).gone();
        } else {
            $.id(R.id.bjplayer_center_video_functions_frame_tv).visible();
        }
        updateDefinition();
    }

    @Override
    public boolean isDialogShowing() {
        return isDialogShowing;
    }

    private void setPageView() {
        switch (mCenterPageState) {
            case CENTER_PAGE_INIT:
                $.id(R.id.bjplayer_layout_center_video_functions_segments_ll).gone();
                $.id(R.id.bjplayer_layout_center_video_functions_rate_ll).gone();
                $.id(R.id.bjplayer_layout_center_video_functions_frame_ll).gone();
                if (mPlayer == null) {
                    $.id(R.id.bjplayer_center_video_functions_ll).gone();
                }
                if (mPlayer.getOrientation() == BJPlayerView.VIDEO_ORIENTATION_LANDSCAPE && !isRightMenuHidden && !isBackTouch) {
                    setAnimationVisible(R.id.bjplayer_center_video_functions_ll);
                } else {
                    $.id(R.id.bjplayer_center_video_functions_ll).gone();
                }
                mPlayer.showTopAndBottom();
                break;
            case CENTER_PAGE_FRAME:
                $.id(R.id.bjplayer_layout_center_video_functions_segments_ll).gone();
                $.id(R.id.bjplayer_layout_center_video_functions_rate_ll).gone();
                setAnimationVisible(R.id.bjplayer_layout_center_video_functions_frame_ll);
                $.id(R.id.bjplayer_center_video_functions_ll).gone();
//                setFocusDefinition();
                mPlayer.hideTopAndBottom();
                break;
            case CENTER_PAGE_RATE:
                $.id(R.id.bjplayer_layout_center_video_functions_segments_ll).gone();
                setAnimationVisible(R.id.bjplayer_layout_center_video_functions_rate_ll);
                $.id(R.id.bjplayer_layout_center_video_functions_frame_ll).gone();
                $.id(R.id.bjplayer_center_video_functions_ll).gone();
                setFocusRate();
                mPlayer.hideTopAndBottom();
                break;
            case CENTER_PAGE_SEGMENTS:
                setAnimationVisible(R.id.bjplayer_layout_center_video_functions_segments_ll);
                $.id(R.id.bjplayer_layout_center_video_functions_rate_ll).gone();
                $.id(R.id.bjplayer_layout_center_video_functions_frame_ll).gone();
                $.id(R.id.bjplayer_center_video_functions_ll).gone();
                mPlayer.hideTopAndBottom();
                break;
        }
        updateDefinition();
    }

    /**
     * 更新清晰度，读VideoItem的definition字段,默认是第一个，默认的逻辑在player presenter处理了
     */
    @Override
    public void updateDefinition() {
        if (mPlayer != null) {
            switch (mPlayer.getVideoDefinition()) {
                case BJPlayerView.VIDEO_DEFINITION_AUDIO:
                    $.id(R.id.bjplayer_center_video_functions_frame_tv).text($.contentView().getContext().getString(R.string.bjplayer_video_frame_audio));
                    break;
                case BJPlayerView.VIDEO_DEFINITION_720p:
                    $.id(R.id.bjplayer_center_video_functions_frame_tv).text($.contentView().getContext().getString(R.string.bjplayer_video_frame_720p));
                    break;
                case BJPlayerView.VIDEO_DEFINITION_1080p:
                    $.id(R.id.bjplayer_center_video_functions_frame_tv).text($.contentView().getContext().getString(com.baijiahulian.player.R.string.bjplayer_video_frame_1080p));
                    break;
                case BJPlayerView.VIDEO_DEFINITION_HIGH:
                    $.id(R.id.bjplayer_center_video_functions_frame_tv).text($.contentView().getContext().getString(com.baijiahulian.player.R.string.bjplayer_video_frame_high));
                    break;
                case BJPlayerView.VIDEO_DEFINITION_SUPER:
                    $.id(R.id.bjplayer_center_video_functions_frame_tv).text($.contentView().getContext().getString(com.baijiahulian.player.R.string.bjplayer_video_frame_super));
                    break;
                case BJPlayerView.VIDEO_DEFINITION_STD:
                default:
                    $.id(R.id.bjplayer_center_video_functions_frame_tv).text($.contentView().getContext().getString(com.baijiahulian.player.R.string.bjplayer_video_frame_low));
                    break;
            }
        }

    }

    @Override
    public CenterViewStatus getStatus() {
        return centerViewStatus;
    }

    private void setAnimationVisible(final int id) {
        $.id(id).visible();
        Animation translateAnimation = new TranslateAnimation($.id(id).view().getWidth(), 0, 0, 0);
        translateAnimation.setDuration(500);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                $.id(id).view().clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        $.id(id).view().setAnimation(translateAnimation);


    }

    private void setAnimationGone(final int id) {
        Animation translateAnimation = new TranslateAnimation(0, $.id(id).view().getWidth(), 0, 0);
        translateAnimation.setDuration(400);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                $.id(id).view().clearAnimation();
                $.id(id).gone();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        $.id(id).view().startAnimation(translateAnimation);
    }

    private void initFunctions() {
        $.id(com.baijiahulian.player.R.id.bjplayer_layout_center_video_functions_rate_0_7_btn).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setVideoRate(0.7f);
                onBackTouch();
            }
        });
        //set functions clicked
        $.id(com.baijiahulian.player.R.id.bjplayer_layout_center_video_functions_rate_1_btn).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setVideoRate(1.0f);
                onBackTouch();
            }
        });

        $.id(com.baijiahulian.player.R.id.bjplayer_layout_center_video_functions_rate_1_2_btn).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setVideoRate(1.2f);
                onBackTouch();
            }
        });

        $.id(com.baijiahulian.player.R.id.bjplayer_layout_center_video_functions_rate_1_5_btn).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setVideoRate(1.5f);
                onBackTouch();
            }
        });

        $.id(com.baijiahulian.player.R.id.bjplayer_layout_center_video_functions_rate_2_btn).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.setVideoRate(2.0f);
                onBackTouch();
            }
        });
    }

    private void setFocusRate() {
        TextView rate07 = (TextView) $.id(com.baijiahulian.player.R.id.bjplayer_layout_center_video_functions_rate_0_7_btn).view();
        TextView rate1 = (TextView) $.id(com.baijiahulian.player.R.id.bjplayer_layout_center_video_functions_rate_1_btn).view();
        TextView rate12 = (TextView) $.id(com.baijiahulian.player.R.id.bjplayer_layout_center_video_functions_rate_1_2_btn).view();
        TextView rate15 = (TextView) $.id(com.baijiahulian.player.R.id.bjplayer_layout_center_video_functions_rate_1_5_btn).view();
        TextView rate2 = (TextView) $.id(com.baijiahulian.player.R.id.bjplayer_layout_center_video_functions_rate_2_btn).view();

        TextView[] rateArray = new TextView[]{rate07, rate1, rate12, rate15, rate2};
        for (TextView textView : rateArray) {
            textView.setTextColor(ContextCompat.getColor($.contentView().getContext(), android.R.color.white));
            textView.setBackgroundResource(com.baijiahulian.player.R.drawable.bjplayer_bg_radius_12);
        }

        if (mPlayer.getVideoRateInFloat() == 0.7f) {
            rate07.setTextColor(ContextCompat.getColor($.contentView().getContext(), com.baijiahulian.player.R.color.bjplayer_color_primary));
            rate07.setBackgroundResource(com.baijiahulian.player.R.drawable.bjplayer_bg_primary_radius_12);
        } else if (mPlayer.getVideoRateInFloat() == 1.0f) {
            rate1.setTextColor(ContextCompat.getColor($.contentView().getContext(), com.baijiahulian.player.R.color.bjplayer_color_primary));
            rate1.setBackgroundResource(com.baijiahulian.player.R.drawable.bjplayer_bg_primary_radius_12);
        } else if (mPlayer.getVideoRateInFloat() == 1.2f) {
            rate12.setTextColor(ContextCompat.getColor($.contentView().getContext(), com.baijiahulian.player.R.color.bjplayer_color_primary));
            rate12.setBackgroundResource(com.baijiahulian.player.R.drawable.bjplayer_bg_primary_radius_12);
        } else if (mPlayer.getVideoRateInFloat() == 1.5f) {
            rate15.setTextColor(ContextCompat.getColor($.contentView().getContext(), com.baijiahulian.player.R.color.bjplayer_color_primary));
            rate15.setBackgroundResource(com.baijiahulian.player.R.drawable.bjplayer_bg_primary_radius_12);
        } else if (mPlayer.getVideoRateInFloat() == 2.0f) {
            rate2.setTextColor(ContextCompat.getColor($.contentView().getContext(), com.baijiahulian.player.R.color.bjplayer_color_primary));
            rate2.setBackgroundResource(com.baijiahulian.player.R.drawable.bjplayer_bg_primary_radius_12);
        }
    }

    private static class CenterHandler extends Handler {
        private WeakReference<BJCenterViewPresenterCopy> presenter;

        private CenterHandler(BJCenterViewPresenterCopy presenter) {
            this.presenter = new WeakReference<>(presenter);
        }

        private static final int WHAT_DISMISS_DIALOG = 0;

        private void sendMsgDismissDialogDelay() {
            removeMessages(WHAT_DISMISS_DIALOG);
            Message msg = obtainMessage(WHAT_DISMISS_DIALOG);
            sendMessageDelayed(msg, 2000);
        }

        @Override
        public void handleMessage(Message msg) {
            if (presenter.get() == null) return;
            switch (msg.what) {
                case WHAT_DISMISS_DIALOG:
                    presenter.get().dismissLoading();
                    break;

            }
        }
    }

    interface OnRvItemClickListener {
        void onItemClick(View view, int index);
    }

    class DefinitionAdapter extends RecyclerView.Adapter<DefinitionAdapter.DefinitionViewHolder> implements View.OnClickListener {
        Context context;
        OnRvItemClickListener onRvItemClickListener = null;

        DefinitionAdapter(Context context) {
            this.context = context;
        }

        public void setOnRvItemClickListener(OnRvItemClickListener onRvItemClickListener) {
            this.onRvItemClickListener = onRvItemClickListener;
        }

        @Override
        public DefinitionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View definitionView = LayoutInflater.from(context).inflate(R.layout.bjplayer_item_center_definition, parent, false);
            definitionView.setOnClickListener(this);
            return new DefinitionViewHolder(definitionView);
        }

        @Override
        public void onBindViewHolder(DefinitionViewHolder holder, int position) {
            holder.itemView.setTag(position);
            String defName = definitionItemList.get(position).name;
            String defType = definitionItemList.get(position).type;
            holder.tvDefinition.setText(defName);
            if (Utils.getVideoDefinitionFromInt(mPlayer.getVideoDefinition()).equals(defType)) {
                holder.tvDefinition.setTextColor(context.getResources().getColor(com.baijiahulian.player.R.color.bjplayer_color_primary));
                holder.tvDefinition.setBackgroundResource(com.baijiahulian.player.R.drawable.bjplayer_bg_primary_radius_12);
            } else {
                holder.tvDefinition.setTextColor(context.getResources().getColor(android.R.color.white));
                holder.tvDefinition.setBackgroundResource(com.baijiahulian.player.R.drawable.bjplayer_bg_radius_12);
            }
            if (definitionItemList.size() == 0 || position == definitionItemList.size() - 1) {
                holder.dividerView.setVisibility(View.GONE);
            } else {
                holder.dividerView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return definitionItemList.size();
        }

        @Override
        public void onClick(View v) {
            if (onRvItemClickListener != null) {
                if (v.getTag() != null) {
                    int position = (int) v.getTag();
                    onRvItemClickListener.onItemClick(v, position);
                }
            }
        }

        class DefinitionViewHolder extends RecyclerView.ViewHolder {
            View itemView;
            TextView tvDefinition;
            View dividerView;

            public DefinitionViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                tvDefinition = itemView.findViewById(R.id.tv_bjplayer_item_center_definition);
                dividerView = itemView.findViewById(R.id.v_bjplayer_item_divider);
            }
        }
    }

    class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> implements View.OnClickListener {
        LayoutInflater inflater;
        Context mContext;

        public CourseAdapter(Context context) {
            this.mContext = context;
            inflater = LayoutInflater.from(mContext);
        }

        OnRvItemClickListener onRvItemClickListener = null;

        @Override
        public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View courseView = LayoutInflater.from(mContext).inflate(R.layout.bjplayer_course_item, parent, false);
            CourseViewHolder courseViewHolder = new CourseViewHolder(courseView);
            courseView.setOnClickListener(this);
            return courseViewHolder;
        }

        @Override
        public void onClick(View v) {
            if (onRvItemClickListener != null) {
                if (v.getTag() != null) {
                    int position = (int) v.getTag();
                    onRvItemClickListener.onItemClick(v, position);
                }
            }
        }

        public void setOnItemClickListener(OnRvItemClickListener listener) {
            this.onRvItemClickListener = listener;
        }

        @Override
        public void onBindViewHolder(CourseViewHolder holder, int position) {
            SectionItem item = null;
            if (mPlayer.getVideoItem().sectionList == null || mPlayer.getVideoItem().sectionList.length == 0) {
                item = null;
            } else {
                item = mPlayer.getVideoItem().sectionList[position];
            }
            if (item == null) {
                holder.courseName.setText((position + 1) + ". " + mPlayer.getVideoItem().videoInfo.title);
                holder.courseName.setTextColor(ContextCompat.getColor(mContext, com.baijiahulian.player.R.color.bjplayer_color_primary));
            } else {
                holder.courseName.setText((position + 1) + ". " + item.title);
                holder.itemView.setTag(position);

                if (item.videoId == mPlayer.getVideoItem().videoId) {
                    holder.courseName.setTextColor(ContextCompat.getColor(mContext, com.baijiahulian.player.R.color.bjplayer_color_primary));
                } else {
                    holder.courseName.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));
                }
            }

        }

        @Override
        public int getItemCount() {
            if (mPlayer.getVideoItem() == null) return 0;
            if (mPlayer.getVideoItem().sectionList == null ||
                    mPlayer.getVideoItem().sectionList.length == 0) {
                return 1;
            }
            return mPlayer.getVideoItem().sectionList.length;
        }

        class CourseViewHolder extends RecyclerView.ViewHolder {
            TextView courseName;

            public CourseViewHolder(View itemView) {
                super(itemView);
                courseName = (TextView) itemView.findViewById(R.id.bjplayer_course_item_text);
            }
        }
    }
}
