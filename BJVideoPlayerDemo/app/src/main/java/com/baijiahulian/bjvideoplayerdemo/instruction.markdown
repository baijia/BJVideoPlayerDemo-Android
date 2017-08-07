# 百家云点播 Android SDK 集成文档
github链接: [https://github.com/baijia/BJVideoPlayerDemo-Android](https://github.com/baijia/BJVideoPlayerDemo-Android)

## 1、添加依赖(目前仅支持使用 Android Studio 的方式集成)
在工程的根 build.gradle 文件中添加百家云的 maven 源
```gradle
allprojects {
      repositories {
        jcenter()
        maven { url 'https://raw.github.com/baijia/maven/master/' }
     }
}
```

在需要使用点播播放器的 module 中添加 library 的依赖
```gradle
dependencies {
   // 版本号要根据目前已经发布的版本去设置，这里1.4.3只是个例子，推荐使用最新版本
   compile 'com.baijia.player:videoplayer:1.4.3'
}
```
版本信息：[https://github.com/baijia/maven/tree/master/com/baijia/player/videoplayer](https://github.com/baijia/maven/tree/master/com/baijia/player/videoplayer)
在app主module中配置ndk：
```
ndk {
      abiFilters 'armeabi-v7a', 'armeabi', 'x86' //x86虚拟机测试用，发版可去掉
    }
```

## 2、在 xml 布局文件中添加视频播放控件
```xml
<com.baijiahulian.player.BJPlayerView
  android:id="@+id/videoView"
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  app:top_controller="@layout/bjplayer_layout_top_controller"
  app:bottom_controller="@layout/bjplayer_layout_bottom_controller"
  app:center_controller="@layout/bjplayer_layout_center_controller"
  app:aspect_ratio="fit_parent">
</com.baijiahulian.player.BJPlayerView>

```

#### 以下配置均为可选， 不设置即使用默认值
- a) aspect_ratio: 播放窗口宽高比， 有fit_parent，fit_parent_16_9等多种
- b) top_controller: 视频播放窗口顶部控制布局
- c) bottom_controller: 视频播放窗口底部控制布局
- d) center_controller: 视频播放窗口中部控制布局



## 3、初始化 BJPlayerView 配置
首先在application中初始化全局配置[application配置适用于1.3.34及以上版本]
```
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BJVideoPlayerSDK.getInstance().init(this);
    }
}
```
然后在manifest.xml文件中配置
```
 <application
        android:name=".App"
        ...
```

在需要使用播放器的Activity或者fragment中配置如下代码：
```java
playerView = (BJPlayerView) findViewById(R.id.videoView);
playerView.setBottomPresenter(new BJBottomViewPresenter(playerView.getBottomView()));
playerView.setTopPresenter(new BJTopViewPresenter(playerView.getTopView()));
playerView.setCenterPresenter(new BJCenterViewPresenter(playerView.getCenterView()));

playerView.initPartner(1234567, BJPlayerView.PLAYER_DEPLOY_DEBUG); // 使用前要初始化PartnerId
//设置监听
playerView.setOnPlayerViewListener(new OnPlayerViewListener() {
    @Override
    public void onVideoInfoInitialized(BJPlayerView playerView, HttpException exception) {
        //TODO: 视频信息初始化结束
        if (exception != null) {
            // 视频信息初始化成功
            VideoItem videoItem = playerView.getVideoItem();
        }
    }
    @Override
    public void onPause(BJPlayerView playerView) {
        //TODO: video暂停
    }
    @Override
    public void onPlay(BJPlayerView playerView) {
        //TODO: 开始播放
    }
    @Override
    public void onError(BJPlayerView playerView, int code) {
        //TODO: 播放出错
    }
    @Override
    public void onUpdatePosition(BJPlayerView playerView, int position) {
        //TODO: 播放过程中更新播放位置
    }
    @Override
    public void onSeekComplete(BJPlayerView playerView, int position) {
        //TODO: 拖动进度条
    }
    @Override
    public void onSpeedUp(BJPlayerView playerView, float speedUp) {
        //TODO: 设置倍速播放
    }
    @Override
    public void onVideoDefinition(BJPlayerView playerView, int definition) {
        //TODO: 设置清晰度完成
    }
    @Override
    public void onPlayCompleted(BJPlayerView playerView, VideoItem item, SectionItem nextSection) {
        //TODO: 当前视频播放完成 [nextSection已被废弃，请勿使用]
    }
    @Override
    public void onVideoPrepared(BJPlayerView playerView) {
        //TODO: 准备好了，马上要播放
        // 可以在这时获取视频时长
        playerView.getDuration();
    }
});

```
- a) setXXXPresenter(): 设置对应(上、中、下) 区域布局的控制实现。 如果使用的是默认播放器样式， 请使用 SDK 提供的默认实现
- b) setOnVideoPlayerListener(): 监听播放器回调
- c) onVideoInfoInitialized(): 正式播放之前， 会从服务器获取视频信息。 初始化完成可以得到 VideoItem 对象
- d) onPlayCompletion(): 当一个视频播放完成，或者被切换之后，回调这个方法， 同时给出下一个应该播放的视频的建议(nextSection)

### 3.1 BJPlayerView 对应的 Activity 重载实现以下方法

```java
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
```

### 3.2 为了防止屏幕旋转导致界面重新绘制，Activity 增加 configChanges 配置, 如下
```xml
<activity android:name=".MainActivity"
  android:theme="@style/Theme.AppCompat.NoActionBar"
  android:configChanges="keyboardHidden|orientation|screenSize">
    <intent-filter>
      <action android:name="android.intent.action.MAIN"/>
      <category android:name="android.intent.category.LAUNCHER"/>
  </intent-filter>
</activity>

```

### 3.3 配置合作方ID
```java
/**
 * 设置 合作方 ID。 如果没有设置， 无法使用 SDK
 *
 * @param partnerId 合作方 ID
 * @param deploy    运行环境;
 *  <ul>
 *   <li>测试环境: PLAYER_DEPLOY_DEBUG</li>
 *   <li>Beta 环境: PLAYER_DEPLOY_BETA</li>
 *   <li>线上环境: PLAYER_DEPLOY_ONLINE</li>
 * </ul>
 */
playerView.initPartner(long partnerId, int deploy);
```


## 4、设置视频源
```java
/**
* <p>设置视频源</p>
* @param videoId   视频 id
* @param specialToken 这个视频对应的特殊token
*/
playerView.setVideoId(long videoId, String specialToken);
```

```java
/**
*  <p>设置视频源</p>
* @param sectioNid 视频集 Id 已废弃，可以直接传0
* @param videoId 视频 id
* @param specialToken 这个视频对应的特殊token
*/
playerView.setVideoId(long sectionId, long videoId, String specialToken);
```

### 4.1 设置私有用户信息，作为最终统计报表的过滤标记。
```java
/**
 * <p>设置用户自定义信息</p>
 *
 * @param userInfo 用户自定义消息
 */
public void setUserInfo(String userInfo);
```
*字符串的具体格式可根据自己的业务需求自定义*

## 5、常用 apis
***视频播放控制相关***

BJPlayerView对象的实例方法：

```java
/**
* <p>播放视频</p>
*/
@Override
public void playVideo();
```

```java
/**
* <p>暂停视频</p>
*/
@Override
public void pauseVideo()
```

```java
/**
* 调整进度
* @param position
*/
@Override
public void seekVideo(int position)
```

```java
/**
* <p>设置播放视频清晰度</p>
* @param definition 清晰度
* <ul>
*      <li>标清: VIDEO_DEFINITION_STD</li>
*      <li>高清: VIDEO_DEFINITION_HIGH</li>
*      <li>超清: VIDEO_DEFINITION_SUPER</li>
* </ul>
*/
@Override
public void setVideoDefinition(int definition);
```

```java
/**
* <p>设置视频播放倍速</p>
*
* @param rate 播放速率
* <ul>
*      <li>1 倍正常速率: VIDEO_RATE_1_X</li>
*      <li>1.1 倍正常速率: VIDEO_RATE_1_1_X</li>
*      <li>1.2 倍正常速率: VIDEO_RATE_1_2_X</li>
*      <li>1.5 倍速率: VIDEO_RATE_1_5_X</li>
*      <li>1.8 倍正常速率: VIDEO_RATE_1_8_X</li>
*      <li>2 倍速率: VIDEO_RATE_2_X</li>
* </ul>
*/
@Override
public void setVideoRate(int rate);
```

```java
/**
* 获取当前播放视频对应的bean，可通过返回对象获取视频时长、片头片尾等信息
*/
@Override
public VideoItem getVideoItem();
```

***播放器本地日志记录设置（此功能默认关闭状态，必须主动开启）***

BJFileLog的类方法：

```java
/** 设置本地日志文件路径(若设为空字符串则使用默认路径，默认路径是sd卡根目录下bj_player.log)*/
public static void setLogFilePath(String path);
```

播放器日志可以上传至百家云服务器，调用以下方法
```
BJFileLog.getInstance().uploadLogFile("your partnerId","your userInfo(根据业务自定义)",your listener);
```

```
/**
 * 日志上传
 *
 * @param pid partnerId(合作方id)
 * @param uid user id
 */
public void uploadLogFile(String pid, String uid, OnLogFileUploadListener listener)
```

## 6、自定义播放器样式
播放器 SDK 能够支持灵活的样式调整。

### 6.1、调整播放器主题色

>  如果只是需要调整播放器的主题色，在宿主工程中添加一个颜色定义
```xml
<color name="bjplayer_color_primary">#ff9900</color>
```


### 6.2、完整的样式更换
>  如果不希望使用 SDK 默认提供的样式，可以完全自己实现一套布局。

#### 一: 将上文第二步中布局内的播放器控件的 xxx_controller 指向自定义的布局
#### 二: 针对上、中、下三个布局，依次实现三个接口, 具体实现可参考 BJXXXViewPresenter 的实现。 如下：
```java
interface TopView {
  void onBind(IPlayer player);
  void setTitle(String title);
  void setOrientation(int orientation);
  void setOnBackClickListener(View.OnClickListener listener);
}
```

```java
interface CenterView {

  void onBind(IPlayer player);
  boolean onBackTouch();
  void setOrientation(int orientation);
  void showProgressSlide(int delta);
  void showLoading(String message);
  void dismissLoading();
  void showVolumeSlide(int volume, int maxVolume);
  void showBrightnessSlide(int brightness);
  void showError(int what, int extra);
  void showError(int code, String message);
  void showWarning(String warn);

  void onShow();
  void onHide();
  void onVideoInfoLoaded(VideoItem videoItem);

  boolean isDialogShowing();
}
```

```java
interface BottomView {

  void onBind(IPlayer player);

  void setDuration(int duration);

  void setCurrentPosition(int position);

  void setIsPlaying(boolean isPlaying);

  void setOrientation(int orientation);

  void onBufferingUpdate(int percent);
}
```
#### 三: 将 XXXView 接口的实现，传入 BJPlayerView 中
```java
playerView.setBottomPresenter(BottomViewImpl);
playerView.setTopPresenter(TopViewImpl);
playerView.setCenterPresenter(CenterViewImpl);
```

##7、视频缓存模块
### 7.1 初始化
```
//初始化下载管理者
VideoDownloadManager downloadManager = VideoDownloadService.getDownloadManager(this);
//初始化合作方，第一个参数为合作方id，第二个参数为部署环境
downloadManager.initDownloadPartner(1234567, BJPlayerView.PLAYER_DEPLOY_DEBUG);
//设置下载目标路径,请注意Android6.0的动态权限申请
downloadManager.setTargetFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa_video_downloaded/");
```
### 7.2常用api
####设置最大并发下载数量
```
downloadManager.getThreadPool().setCorePoolSize(3);
```
####添加下载任务
```
/**
 * @param fileName    文件名
 * @param videoId     视频id
 * @param videoToken  视频token
 * @param type        视频清晰度 0:普清 1:高清 2:超清 3:720p 4:1080p
 * @param encryptType 加密类型 0 不加密，1加密
 */
 private void addDownloadVideoTask(String fileName, int videoId, String videoToken, int type,
                                      int encryptType, final OnVideoInfoGetListener listener)
```
####获取所有下载任务
```
List<DownloadInfo> allTask = downloadManager.getAllTask();
```
####全局任务监听
```
//添加全局监听
downloadManager.getThreadPool().getExecutor().addOnAllTaskEndListener(your listener);
//移除全局监听,请在activity的onDestroy回调中调用
downloadManager.getThreadPool().getExecutor().removeOnAllTaskEndListener(your listener);
```
#### 暂停所有任务
```
downloadManager.pauseAllTask();
```
#### 开始所有任务

```
downloadManager.startAllTask();
```
#### 删除所有任务和已下载的所有文件
```
downloadManager.removeAllTaskAndFiles();
```
#### 暂停单个任务
```
//downloadInfo可通过获取所有任务列表或者您自己的业务逻辑拿到
downloadManager.pauseTask(downloadInfo.getTaskKey());
```
#### 删除单个任务
```
//仅删除任务，文件不删除
downloadManager.removeTask(downloadInfo.getTaskKey());
//删除任务和对应已下载的文件
downloadManager.removeTask(downloadInfo.getTaskKey(), true);
```
#### 单个任务下载监听
```
downloadInfo.setListener(new DownloadListener() {
   @Override
   public void onProgress(DownloadInfo downloadInfo) {
   }
   @Override
   public void onFinish(DownloadInfo downloadInfo) {
   }
   @Override
   public void onError(DownloadInfo downloadInfo, String errorMsg, Exception e) {
   }
});
```
