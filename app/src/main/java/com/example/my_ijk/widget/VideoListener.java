package com.example.my_ijk.widget;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public interface VideoListener extends IMediaPlayer.OnBufferingUpdateListener,IMediaPlayer.OnCompletionListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnSeekCompleteListener {

}
