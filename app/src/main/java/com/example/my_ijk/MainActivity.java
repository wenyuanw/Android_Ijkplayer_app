package com.example.my_ijk;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.MotionEvent;

import permissions.dispatcher.PermissionUtils;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import com.example.my_ijk.widget.VideoPlayer;
import com.example.my_ijk.widget.VideoListener;
import com.example.my_ijk.utils.DensityUtil;
import com.example.my_ijk.utils.TcpClient;

import java.io.IOException;





public class MainActivity extends Activity implements VideoListener, View.OnClickListener {

    final String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private VideoPlayer videoPlayer;
    private EditText et_url;
    private String path="";
    private boolean ISFULLSCREEN = false;
    private Button btn_full_screen;
    private Button btn_play_video;
    private Button btn_video_back;
    private Button start;
    private FrameLayout fl_group;
    private String serverIp = "192.168.2.110";
    private int serverPort= 57061;
    private TcpClient tcpClient;
    GestureDetector mGestureDetector;

    private TextView geture_tv_RL_position;
    private TextView geture_tv_BF_posiyion;
    private ImageView geture_tip;
    private TextView Time_View;


    private boolean firstScroll = false;// 每次触摸屏幕后，第一次scroll的标志
    private int GESTURE_FLAG = 0;// 1,调节左右，2，调节前后
    private static final int GESTURE_MODIFY_LEFT_RIGHT = 1;
    private static final int GESTURE_MODIFY_BACKW_FORWARD = 2;

    int currentPosition_BF = 50 ;
    int currentPosition_RL = 50 ;



    @SuppressLint({"ShowToast", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mHandler.sendMessageDelayed(mHandler.obtainMessage(100), 3000);
        //动态权限获取
        boolean hadPermission = PermissionUtils.hasSelfPermissions(this, permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hadPermission) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions, 1110);
        }

        mGestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                firstScroll = true;// 设定是触摸屏幕后第一次scroll的标志
                btn_video_back.setVisibility(View.VISIBLE);
                btn_play_video.setVisibility(View.VISIBLE);
                btn_full_screen.setVisibility(View.VISIBLE);
                mHandler.sendEmptyMessageDelayed(100, 3000);
                Log.i("MyGesture", "onDown");
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                geture_tip.setVisibility(View.VISIBLE);
                int y = (int) e2.getRawY();
                if (firstScroll) {// 以触摸屏幕后第一次滑动为标准，避免在屏幕上操作切换混乱
                    // 横向的距离变化大则调整左<->右，纵向的变化大则调整前<->后
                    if (Math.abs(distanceX) >= Math.abs(distanceY)) {
                        GESTURE_FLAG = GESTURE_MODIFY_LEFT_RIGHT;
                    } else {
                        GESTURE_FLAG = GESTURE_MODIFY_BACKW_FORWARD;
                    }
                }
                // 如果每次触摸屏幕后第一次scroll是调节左<->右，那之后的scroll事件都处理左<->右，直到离开屏幕执行下一次操作
                if (GESTURE_FLAG == GESTURE_MODIFY_LEFT_RIGHT) {
                    int temp = currentPosition_RL;
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {// 横向移动大于纵向移动
                        if (distanceX >= DensityUtil.dip2px(MainActivity.this, 2f)) {// 为避免调节过快，distanceX应大于一个设定值
                            if (currentPosition_RL > 0) {
                                currentPosition_RL--;
                                if (currentPosition_RL != temp){
//                                    tcpClient.sendMsg("R"+currentPosition_RL);
                                    tcpClient.sendMsg("R"+System.currentTimeMillis());
//                                    Time_View.setText("毫秒时间"+System.currentTimeMillis() );
                                }
                            }
                        } else if (distanceX <= -DensityUtil.dip2px(MainActivity.this, 2f)) {
                            if (currentPosition_RL < 100) {
                                currentPosition_RL++;
                                if (currentPosition_RL != temp){
//                                    tcpClient.sendMsg("R"+currentPosition_RL);
                                    tcpClient.sendMsg("R"+System.currentTimeMillis());
//                                    Time_View.setText("毫秒时间"+System.currentTimeMillis() );
                                }
                            }
                        }
                        geture_tv_RL_position.setText("观察左右变化情况"+currentPosition_RL );
                    }
                }
                // 如果每次触摸屏幕后第一次scroll是调节前<->后，那之后的scroll事件都处理前<->后，直到离开屏幕执行下一次操作
                else if (GESTURE_FLAG == GESTURE_MODIFY_BACKW_FORWARD) {
                    int temp = currentPosition_BF;
                    if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                        if (distanceY >= DensityUtil.dip2px(MainActivity.this, 2f)) {
                            if (currentPosition_BF < 100) {// 为避免调节过快，distanceY应大于一个设定值
                                currentPosition_BF++;
                                if (currentPosition_BF != temp){
//                                    tcpClient.sendMsg("B"+currentPosition_BF);
                                    tcpClient.sendMsg("B"+System.currentTimeMillis());
//                                    Time_View.setText("毫秒时间"+System.currentTimeMillis() );
                                }
                            }
                        } else if (distanceY <= -DensityUtil.dip2px(MainActivity.this, 2f)) {// 音量调小
                            if (currentPosition_BF > 0) {
                                currentPosition_BF--;
                                if (currentPosition_BF != temp){
//                                    tcpClient.sendMsg("B"+currentPosition_BF);
                                    tcpClient.sendMsg("B"+System.currentTimeMillis());

//                                    Time_View.setText("毫秒时间"+System.currentTimeMillis() );
                                }
                            }
                        }
                        geture_tv_BF_posiyion.setText("观察前后变化情况"+currentPosition_BF );
                    }
                }
                firstScroll = false;// 第一次scroll执行完成，修改标志
                Log.i("MyGesture", "onScroll:");
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });


        // 步骤2：让TextView检测手势：重写View的onTouch函数，将触屏事件交给GestureDetector处理，从而对用户手势作出响应
        videoPlayer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    GESTURE_FLAG = 0;// 手指离开屏幕后，重置调节音量或进度的标志
                    geture_tip.setVisibility(View.GONE);
                    Log.i("MyGesture", "手指离开屏幕后，重置标志 GESTURE_FLAG = 0");
                }
                return true;
            }
        });
    }

    private void initView() {
        et_url=(EditText) findViewById(R.id.et_url);
        videoPlayer=findViewById(R.id.video);
        btn_full_screen = (Button) findViewById(R.id.btn_full_video);
        btn_play_video = (Button) findViewById(R.id.btn_play_video);
        btn_video_back = (Button) findViewById(R.id.btn_video_back);
        start = (Button) findViewById(R.id.start);
        fl_group = (FrameLayout) findViewById(R.id.fl_group);
        geture_tip=findViewById(R.id.geture_tip);
        videoPlayer.setVideoListener(this);
        btn_full_screen.setOnClickListener(this);
        btn_play_video.setOnClickListener(this);
        start.setOnClickListener(this);
        btn_video_back.setOnClickListener(this);
        geture_tv_RL_position =findViewById(R.id.geture_tv_RL_percentage);
        geture_tv_BF_posiyion =findViewById(R.id.geture_tv_BF_percentage);
        Time_View = findViewById(R.id.Time_View);
        geture_tip.setVisibility(View.GONE);

//        Log.i("CurrentTime ", "Is "+System.currentTimeMillis());
        new TimeThread().start();
        tcpClient = new TcpClient(serverIp, serverPort);
        tcpClient.startConn();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(ISFULLSCREEN){   //全屏切换半屏
                    ISFULLSCREEN = false;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 手动横屏
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            DensityUtil.dip2px(MainActivity.this, 255));
                    et_url.setVisibility(View.VISIBLE);
                    start.setVisibility(View.VISIBLE);
                    fl_group.setLayoutParams(lp);
                    return true;
                }
                break;

            default:
                break;
        }
        return super.onKeyDown(keyCode, event);

    }

    /**
     * 触摸屏幕 按键出现
     */
    public boolean onTouchEvent(android.view.MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                btn_video_back.setVisibility(View.VISIBLE);
                btn_play_video.setVisibility(View.VISIBLE);
                btn_full_screen.setVisibility(View.VISIBLE);
                mHandler.sendEmptyMessageDelayed(100, 3000);
                break;

            default:
                break;
        }
        return true;
    };

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_full_video:

                if (ISFULLSCREEN) { // 全屏转半屏

                    ISFULLSCREEN = false;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 手动横屏
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            DensityUtil.dip2px(MainActivity.this, 255));
                    fl_group.setLayoutParams(lp);

                    et_url.setVisibility(View.VISIBLE);
                    start.setVisibility(View.VISIBLE);

                    path = et_url.getText().toString();
                    videoPlayer.setPath(path);
                    try {
                        videoPlayer.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else { // 非全屏切换全屏
                    ISFULLSCREEN = true;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // 手动横屏
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    fl_group.setLayoutParams(lp);
                    et_url.setVisibility(View.GONE);
                    start.setVisibility(View.GONE);

                }

                break;

            case R.id.btn_video_back:

                if (ISFULLSCREEN) { // 全屏转半屏

                    ISFULLSCREEN = false;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 手动横屏
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            DensityUtil.dip2px(MainActivity.this, 255));
                    fl_group.setLayoutParams(lp);
                    et_url.setVisibility(View.VISIBLE);
                    start.setVisibility(View.VISIBLE);

                }
                break;

            case R.id.btn_play_video:

            case R.id.start:
                if(videoPlayer.isPlaying()&&videoPlayer!=null){   //视频的播放与暂停
                    videoPlayer.pause();
                    btn_play_video.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_play, null));
                    tcpClient.closeTcpSocket();
                }else{

                    btn_play_video.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_topause, null));
                    playVideo();
//                    tcpClient.startConn();

                }

                break;

            default:
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler=new Handler() {

        @SuppressLint("HandlerLeak")
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    long sysTime = System.currentTimeMillis();//获取系统时间
                    Time_View.setText("毫秒时间："+sysTime); //更新时间
                    tcpClient.sendMsg("R"+sysTime);
                    break;

                case 100:
                    btn_full_screen.setVisibility(View.GONE);
                    btn_play_video.setVisibility(View.GONE);
                    btn_video_back.setVisibility(View.GONE);

                    break;

                default:
                    break;
            }

        };
    };

    protected void playVideo() {

        path = et_url.getText().toString();
        videoPlayer.setPath(path);
        videoPlayer.initVideoView();
        try {
            videoPlayer.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean sdPermissionResult = PermissionUtils.verifyPermissions(grantResults);
        if (!sdPermissionResult) {
            Toast.makeText(this, "没获取到sd卡权限，无法播放本地视频哦", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        videoPlayer.start();
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

    }

    class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(100);
                    Message msg = new Message();
                    msg.what = 1;  //消息(一个整型值)
                    mHandler.sendMessage(msg);// 每隔100ms发送一个msg给mHandler

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

}