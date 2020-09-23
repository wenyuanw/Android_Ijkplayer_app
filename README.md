# Android RTSP视频播放器简易实现（20200923）

> 基于Ijkplayer实现的简单安卓视频播放器
>
> 界面简陋的播放器，一并实现了一个TCPClient类，用于TCP通信。
>
> Github链接：https://github.com/wenyuanw/Android_Ijkplayer_app



## **具体代码看Github仓库**



**实现功能：**

- 输入**RTSP**视频流的URL之后播放视频
- 全屏半屏相互切换（bug：重力旋转失灵）
- **GestureDetector**实现上下左右滑动手势操作（没有用于快进快退等操作）
- 上下左右手势操作的时候**TCP**连接发送数据到C++服务端

输入URL之后播放视频，可以全屏播放，不过能力有限，软件还存在一些Bug，基本功能都实现了。



**Reference：**

GSY：https://github.com/CarGuo/GSYVideoPlayer

Ijkplayer：https://github.com/bilibili/ijkplayer

 ijkplayer接入使用：https://www.jianshu.com/p/a57bbdd78798

Android集成ijkplayer：https://www.jianshu.com/p/79b434b2d5c8

全屏切换：https://blog.csdn.net/u012560369/article/details/78021085







已完成：

1. 可以秒加载视频了
2. 回车之后可以隐藏键盘
3. SurfaceView是根据视频的内容进行自适应调整了
4. 动态获取存储权限
5. 全屏功能



现有问题：

1. 无法跟随重力感应的方向改变播放界面的方向
2. 系统返回键按下返回的时候，不显示url输入框（估计应该重写系统返回键的函数）
3. 要考虑异型屏幕适配

