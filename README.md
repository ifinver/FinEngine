#Fine Engine & Fin Render
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

##Work Flow
![Work Flow](https://github.com/ifinver/FinEngine/blob/master/workflow.png)
####耗时地方：
1. 使用CPU在内存中进行美颜和人脸识别。
2. 把数据从内存上传到gpu

##FinEngine
视频处理引擎。

输入NV21(YUV420)数据，，经过滤镜+旋转+格式转换处理之后，渲染到一个逻辑视窗或物理视窗上。
####优势：
1. 高效。硬件级别。
2. Easy to use. 接口封装简单，只要初始化好引擎注册监听就可以了。
3. 实时滤镜。支持热切换。

##FinRender
独立的渲染组件。

可以在一个完全的独立的线程中使用open gl进行高效的渲染。
初始化时会创建一个逻辑视窗，传给FinEngine进行数据处理，然后将逻辑视窗投射到物理视窗或另一个逻辑视窗
####优势：
1. 线程隔离。不会被其他线程的渲染所干扰。(比如U3D)。
1. 高效。逻辑视窗使用了OpenGL的纹理，数据直接在显存中交互。

##FinRecorder
独立的录制组件。

类似FinRender，但是输入不是逻辑视窗，而是一个纹理id和与纹理绑定的eglContext，然后将数据输出至逻辑视窗或物理视窗。
这里可以用Android的MediaCodec创建一个逻辑视窗，然后用此组件进行投射，即可完成录制功能。
####优势：
1. 线程隔离。不会被其他线程的渲染所干扰。(比如U3D)。
1. 高效。逻辑视窗使用了OpenGL的纹理，数据直接在显存中交互。



##Next
1. 使用GraphicBuffer加速顶点和纹理坐标的上传
1. 使用oes扩展加速每一帧图像数据的上传(不重要)。当前的帧率在低端机已经可以达到20了。
