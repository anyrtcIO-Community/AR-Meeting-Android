### anyRTC-Meeting-Android SDK for Android
### 简介
anyRTC-Meeting-Android视频会议，基于RTMeetEngine SDK，支持视频、语音多人会议，适用于会议、培训、互动等多人移动会议。

### 项目展示
![image](https://github.com/AnyRTC/anyRTC-Meeting-Android/blob/master/images/meet1.jpg)
![image](https://github.com/AnyRTC/anyRTC-Meeting-Android/blob/master/images/meet2.jpg)
![image](https://github.com/AnyRTC/anyRTC-Meeting-Android/blob/master/images/meet3.jpg)
![image](https://github.com/AnyRTC/anyRTC-Meeting-Android/blob/master/images/meet4.jpg)


### app体验

##### 扫码下载
![image](https://github.com/AnyRTC/anyRTC-Meeting-Android/blob/master/images/demo_qrcode.png)
##### [点击下载](https://www.pgyer.com/anyRTC_Meeting)
##### [WEB在线体验](https://www.anyrtc.cc/demo/meeting)

### SDK集成
# > 方式一（推荐）[ ![Download](https://api.bintray.com/packages/dyncanyrtc/anyrtc_dev/anyRTC-Meeting-Android/images/download.svg) ](https://bintray.com/dyncanyrtc/anyrtc_dev/anyRTC-Meeting-Android/_latestVersion)

添加Jcenter仓库 Gradle依赖：

```
dependencies {
  compile 'org.anyrtc:meet_kit:2.4.3' //最新版见上面Download
}
```

或者 Maven
```
<dependency>
  <groupId>org.anyrtc</groupId>
  <artifactId>meet_kit</artifactId>
  <version>2.4.3</version>
  <type>pom</type>
</dependency>
```

>方式二

 [下载aar SDK](https://www.anyrtc.io/resoure)

>1. 将下载好的meet_kit-release-release.aar文件放入项目的libs目录中
>2. 在Model下的build.gradle文件添加如下代码依赖MEETING SDK

```
android
{

 repositories {
        flatDir {dirs 'libs'}
    }
    
 }
    
```
```
dependencies {
    compile(name: 'meet_kit-release', ext: 'aar')
}
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
在[anyRTC官网](http://www.anyrtc.io)获取了开发者账号，AppID等信息后，替换DEMO中
**AnyRTCApplication**类中的开发者信息即可

### 操作步骤

1、一台手机启动app，选择进入会议房间；

2、另一台手机进入相同会议房间，实时会议开始。

### 完整文档
SDK集成，API介绍，详见官方完整文档：[点击查看](https://www.anyrtc.io/resoure)

### Ios版anyRTC-Meeting视频会议

[anyRTC-Meeting-Ios](https://github.com/AnyRTC/anyRTC-Meeting-iOS)

### Web版anyRTC-Meeting视频会议在线体验

[anyRTC-Meeting-Web](https://www.anyrtc.cc/demo/meeting)


### 支持的系统平台
**Android** 4.0及以上

### 支持的CPU架构
**Android** arm64-v8a  armeabi armeabi-v7a


### 注意事项
1. Meeting SDK所有回调均在子线程中，所以在回调中操作UI等，应切换主线程。
2. 发布直播订阅直播注意安卓6.0+动态权限处理。
3. 常见错误代码请参考[错误码查询](https://www.anyrtc.io/resoure)

### 商业授权
程序发布需商用授权，业务咨询请联系 QQ:580477436

QQ交流群:554714720

联系电话:021-65650071-839

Email:hi@dync.cc

### 技术支持 
- anyRTC官方网址：[https://www.anyrtc.io](https://www.anyrtc.io/resoure)
- QQ技术咨询群：580477436
- 

### 关于直播

本公司有一整套完整直播解决方案。本公司开发者平台www.anyrtc.io。除了基于RTMP协议的直播系统外，我公司还有基于WebRTC的时时交互直播系统、P2P呼叫系统、会议系统等。快捷集成SDK，便可让你的应用拥有时时通话功能。欢迎您的来电~

### License

- RTCMeetingEngine is available under the MIT license. See the LICENSE file for more info.





   



 
