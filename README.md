# 百家云点播sdk集成文档
请参考 http://dev.baijiacloud.com/default/wiki/detail/8

CHANGELOG videoplayer 1.7.16
==============
- 修复播放器未初始化完成调用暂停失效的bug
- DownloadService移出sdk,用户外部实现下载保活
- bugfix

CHANGELOG videoplayer 1.7.7
==============
- 新增setLooping(boolean looping)实现循环播放
- 在线播放出错自动切换cdn重试
- bugfix

CHANGELOG videoplayer 1.7.3
==============
- 视频加载速度优化，MP4和flv格式可配置

CHANGELOG videoplayer 1.7.2
==============
- 新增记忆播放功能
- 支持任意倍速播放（0.5~2.0之间）
- 下载同步逻辑修改
- 修复老版本下载加密参数未起作用的bug
- 修复userAgent有中文导致的崩溃
- 修复屏蔽网络监听失效
- 兼容未加后缀名的加密格式视频

CHANGELOG videoplayer 1.7.1
==============
- 使用新的视频加密方案
- 新增旧版下载的数据库数据迁移到新版下载的文件缓存的工具类
- 修复了一些已知问题

CHANGELOG videoplayer 1.7.0
==============
- 优化进度条loading显示效果  
- 修复下载模块的bug  
- 修复一些已知问题


CHANGELOG videoplayer 1.6.9
==============
- 重构下载模块
- 修复了一些已知问题
