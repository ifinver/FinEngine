# Fine Engine & Fin Render 
[![License](LICENSES/GPL-blue.svg)](LICENSES/gpl-2.0.md)

Keywords: Android, Android ndk jni, OpenGL, GLSL shader, Unity, Cocos2d, OpenCV, Video Filter, Face swap, Face beauty, Video recorder, Face map.

与游戏引擎进行交互的模块，特别是视频数据的处理与交互。不仅适用于Unity，还可以接入Cocos2d等图形引擎。

涉及OpenGL、OpenCV、视频录制、滤镜、美颜、人脸交换、人脸贴图等技术。

并包含一个简单的OpenGL引擎“FinEngine”

关联项目：[FinEngineUnity](https://github.com/ifinver/FinEngineUnity), 包含了本项目里面Unity内部的代码。

如有帮助请star

### 参考
1. 人脸交换：https://github.com/hrastnik/FaceSwap/blob/master/main.cpp 、 https://yq.aliyun.com/articles/62520
1. OpenGL : [OpenGL® ES 2.0 Programming Guide](https://github.com/ifinver/FinEngine/blob/master/OpenGL%20ES%202.0%20Programming%20Guide.pdf)
1. OpenGL : [OpenGL® ES 2.0 Programming Guide 中文版](https://github.com/ifgyong/iOSDataFactory/blob/master/OpenGL%20ES%202.0%20编程指南%20中文版.pdf)
### 环境
ndk-bundle : r16b (更高的版本未测试)
targetSdkVersion : 23 (升级这个Unity模块会报错)
### 注意
1. 接入Opencv Face Detector，在主分支上。运行起来可以看效果（需要横屏）。
1. 人脸交换需要多关键点采样的人脸检测模块支持，Opencv并不支持这种效果，见face_swap分支。
1. 采用CMake编译C/C++，项目大部分的代码都在C++ native层
1. 多种GLSL视频渲染滤镜，在finengine的assets下。
1. UnityTransfer模块，主要利用java nio和共享指针传输视频帧数据，中间有一些坑已填平。此方案同样适用于Cocos2d
1. 为了更好地理解本项目，可以参考[Android传输摄像头视频数据到U3D的优化实战](https://www.jianshu.com/p/0df7700b9fb7)，时间有限只写过这一个，后面会慢慢补充。
1. 本项目的核心Key Point还有：
    - Unity里面没有双通道纹理，如何将opengl双通道纹理映射成Unity支持的纹理，并实现渲染的。
    - 与Unity共享Open Context的处理。（代码主要在UnityTransfer里面）
    - SurfaceView在native C/C++代码中如何操作与维护
    - 如何录制游戏引擎的视频帧
    - 多屏、多处实时渲染
    - 人脸贴图、人脸交换的opengl实现
## Work Flow
![Work Flow](/workflow.png)
