# 重要提醒
anyRTC 对该版本已经不再维护，如需音视频呼叫，请前往:https://github.com/anyRTC-UseCase/ARCall

**功能如下：**
- 一对一音视频呼叫
- 一对多音视频呼叫
- 视频通话转音频通话
- 静音开关/视频开关
- AI降噪，极致降噪，不留噪声
- 大小屏切换
- 悬浮窗功能

新版本一行代码，30分钟即可使应用有音视频能力。

更多示列请前往**公司网址： [www.anyrtc.io](https://www.anyrtc.io)**

### AR-Meet-Android SDK for Android
### 简介
AR-Meeting-Android视频会议，基于ARMeetEngine SDK，支持视频、语音多人会议，适用于会议、培训、互动等多人移动会议。


### app体验

##### [点击下载](http://download.anyrtc.io/a31v)

##### [WEB在线体验](https://demos.anyrtc.io/ar-meet/)

### SDK集成
# > 方式一（推荐）[ ![Download](https://api.bintray.com/packages/dyncanyrtc/ar_dev/meet/images/download.svg) ](https://bintray.com/dyncanyrtc/ar_dev/meet/_latestVersion)

添加Jcenter仓库 Gradle依赖：

```
dependencies {
compile 'org.ar:meet_kit:3.1.4'
}
```

或者 Maven
```
<dependency>
  <groupId>org.ar</groupId>
  <artifactId>meet_kit</artifactId>
  <version>3.1.4</version>
  <type>pom</type>
</dependency>
```

### 安装

##### 编译环境

AndroidStudio

##### 运行环境

Android API 15+
真机运行

### 如何使用

##### 注册开发者信息

>如果您还未注册anyRTC开发者账号，请登录[anyRTC官网](http://www.anyrtc.io)注册及获取更多的帮助。

##### 替换开发者账号
在[anyRTC官网](http://www.anyrtc.io)获取了应用ID,应用Token等信息后，替换DEMO中
**DeveloperInfo**类中的信息即可

### 操作步骤

1、一台手机启动app，选择进入会议房间；

2、另一台手机进入相同会议房间，实时会议开始。

### 完整文档
SDK集成，API介绍，详见官方完整文档：[点击查看](https://docs.anyrtc.io/v1/MEET/android.html)

### iOS版 视频会议

[AR-Meeting-iOS](https://github.com/AnyRTC/anyRTC-Meeting-iOS)

### Web版 视频会议

[AR-Meeting-Web](https://github.com/anyRTC/anyRTC-Meeting-Web)


### 支持的系统平台
**Android** 4.0及以上

### 支持的CPU架构
**Android** arm64-v8a  armeabi armeabi-v7a


### 注意事项
1. Meeting SDK所有回调均在子线程中，所以在回调中操作UI等，应切换主线程。
2. 注意安卓6.0+动态权限处理。
3. 常见错误代码请参考[错误码查询](https://www.anyrtc.io/resoure)

### 技术支持
- anyRTC官方网址：[https://www.anyrtc.io](https://www.anyrtc.io/resoure)
- QQ技术咨询群：554714720
- 联系电话:021-65650071-816
- Email:hi@dync.cc


### 关于直播

本公司有一整套完整直播解决方案。本公司开发者平台www.anyrtc.io。除了基于RTMP协议的直播系统外，我公司还有基于WebRTC的时时交互直播系统、P2P呼叫系统、会议系统等。快捷集成SDK，便可让你的应用拥有时时通话功能。欢迎您的来电~

### License

- RTCMeetingEngine is available under the MIT license. See the LICENSE file for more info.





   



 
