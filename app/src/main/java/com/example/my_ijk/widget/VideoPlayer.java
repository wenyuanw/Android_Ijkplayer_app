package com.example.my_ijk.widget;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoPlayer extends FrameLayout {

    /**
     * 由ijkplayer提供，用于播放视频，需要给他传入一个surfaceView
     */
    private IMediaPlayer mMediaPlayer = null;
    /**
     * 视频文件地址
     */
    private String mPath ;
    /**
     * 视频请求header
     */
    private Map<String,String> mHeader;

    private SurfaceView mSurfaceView;

    private Context mContext;
    private boolean mEnableMediaCodec;

    private VideoListener mListener;
    private AudioManager mAudioManager;
    private AudioFocusHelper mAudioFocusHelper;


    public VideoPlayer(@NonNull Context context) {
        this(context, null);
    }

    public VideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    //初始化
    private void init(Context context) {
        mContext = context;
        setBackgroundColor(Color.BLACK);
        createSurfaceView();
        mAudioManager = (AudioManager)mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusHelper = new AudioFocusHelper();
    }

    //创建surfaceView
    private void createSurfaceView() {
        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setDisplay(surfaceHolder);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT
                , LayoutParams.MATCH_PARENT, Gravity.CENTER);
        mSurfaceView.setLayoutParams(layoutParams);
        addView(mSurfaceView,0,layoutParams);
    }

    //创建一个新的player
    private IMediaPlayer createPlayer() {
        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
        // 设置之后播放RTSP可以秒加载
        ijkMediaPlayer.setOption(1, "analyzemaxduration", 100L);
        ijkMediaPlayer.setOption(1, "probesize", 1024*16);
        ijkMediaPlayer.setOption(1, "flush_packets", 1L);
        ijkMediaPlayer.setOption(4, "packet-buffering", 0);
        ijkMediaPlayer.setOption(4, "framedrop", 1);

        ijkMediaPlayer.setVolume(1.0f, 1.0f);

        setEnableMediaCodec(ijkMediaPlayer,mEnableMediaCodec);
        return ijkMediaPlayer;
    }

    //设置是否开启硬解码
    private void setEnableMediaCodec(IjkMediaPlayer ijkMediaPlayer, boolean isEnable) {
        int value = isEnable ? 1 : 0;
        ijkMediaPlayer.setOption(4, "mediacodec", value);//开启硬解码
        ijkMediaPlayer.setOption(4, "mediacodec-auto-rotate", value);
        ijkMediaPlayer.setOption(4, "mediacodec-handle-resolution-change", value);
    }

    public void setEnableMediaCodec(boolean isEnable){
        mEnableMediaCodec = isEnable;
    }

    //设置ijkplayer的监听
    private void setListener(IMediaPlayer player){
        player.setOnPreparedListener(mPreparedListener);
        player.setOnVideoSizeChangedListener(mVideoSizeChangedListener);
    }

    /**
     * 设置自己的player回调
     */
    public void setVideoListener(VideoListener listener){
        mListener = listener;
    }

    //设置播放地址
    public void setPath(String path) {
        setPath(path,null);
    }

    public void setPath(String path,Map<String,String> header){
        mPath = path;
        mHeader = header;
    }

    //开始加载视频
    public void load() throws IOException {
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        mMediaPlayer = createPlayer();
        setListener(mMediaPlayer);
        mMediaPlayer.setDisplay(mSurfaceView.getHolder());
        mMediaPlayer.setDataSource(mContext, Uri.parse(mPath),mHeader);

        mMediaPlayer.prepareAsync();
    }

    public void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            mAudioFocusHelper.requestFocus();
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mAudioFocusHelper.abandonFocus();
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            mAudioFocusHelper.abandonFocus();
        }
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mAudioFocusHelper.abandonFocus();
        }
    }


    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mAudioFocusHelper.abandonFocus();
        }
    }


    public long getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }


    public long getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }


    public void seekTo(long l) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(l);
        }
    }

    public boolean isPlaying(){
        if(mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    public void initVideoView() {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int screenWidth=dm.widthPixels;

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
        lp.height = screenWidth * 9 / 16;
        lp.width = screenWidth;
        mSurfaceView.setLayoutParams(lp);

    }

    //------------------  各种listener 赋值 ---------------------//

    private IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener(){

        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            if(mListener != null){
                mListener.onPrepared(iMediaPlayer);
            }
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener mVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
            int videoWidth = iMediaPlayer.getVideoWidth();
            int videoHeight = iMediaPlayer.getVideoHeight();
            if (videoWidth != 0 && videoHeight != 0) {
                mSurfaceView.getHolder().setFixedSize(videoWidth, videoHeight);
            }
        }
    };


    //横竖屏监听
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int screenWidth=dm.widthPixels;
        int screenHeight=dm.heightPixels;

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {//切换为横屏
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
            lp.height = screenHeight;
            lp.width = screenHeight* 16 / 9;
            mSurfaceView.setLayoutParams(lp);
        }else{
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
            lp.height = screenWidth * 9 / 16;
            lp.width = screenWidth;
            mSurfaceView.setLayoutParams(lp);
        }
    }

    //------------------  音频监听 ---------------------//

    /**
     * 音频焦点改变监听
     */
    private class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {
        boolean startRequested = false;
        boolean pausedForLoss = false;
        int currentFocus = 0;

        @Override
        public void onAudioFocusChange(int focusChange) {
            if (currentFocus == focusChange) {
                return;
            }

            currentFocus = focusChange;
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN://获得焦点
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT://暂时获得焦点
                    if (startRequested || pausedForLoss) {
                        start();
                        startRequested = false;
                        pausedForLoss = false;
                    }
                    if (mMediaPlayer != null)//恢复音量
                        mMediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS://焦点丢失
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://焦点暂时丢失
                    if (isPlaying()) {
                        pausedForLoss = true;
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK://此时需降低音量
                    if (mMediaPlayer != null && isPlaying()) {
                        mMediaPlayer.setVolume(0.1f, 0.1f);
                    }
                    break;
            }
        }

        boolean requestFocus() {
            if (currentFocus == AudioManager.AUDIOFOCUS_GAIN) {
                return true;
            }

            if (mAudioManager == null) {
                return false;
            }

            int status = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == status) {
                currentFocus = AudioManager.AUDIOFOCUS_GAIN;
                return true;
            }

            startRequested = true;
            return false;
        }

        boolean abandonFocus() {

            if (mAudioManager == null) {
                return false;
            }

            startRequested = false;
            int status = mAudioManager.abandonAudioFocus(this);
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == status;
        }
    }
}
