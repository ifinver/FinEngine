#Fine Engine & Fin Render
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

##FinEngine
视频处理引擎。

接管了摄像头，摄像机每一帧数据生成时，引擎在GPU上直接获取摄像机纹理做处理，经过滤镜+旋转+格式转换处理之后，把每一帧RGBA格式的视频数据从GPU下载到CPU，回调给业务端。
优势：
1. 高效。全程硬件级别，只有下载每一帧数据的时候耗时，需要业务侧等待。
2. Easy to use. 接口封装简单，只要初始化好引擎注册监听就可以了。
3. 实时滤镜。滤镜热切换的接口已经留好，就差时间写几个好玩的滤镜了。

##FinRender
独立的渲染组件。

可以在一个完全的独立的线程中使用open gl进行高效的渲染。
优势：
1. 线程隔离。不会被其他线程的渲染所干扰。(比如U3D)
1. 录制视频。有录制视频的需求就可以把MediaCodec的Surface传给FinRender，FinRender会自动把视频绘制到上面
1. 格式兼容。接口已留好，但是目前可以渲染RGBA的数据，后期其他格式的都可以支持。


##Work Flow
![Work Flow](https://github.com/ifinver/FinEngine/blob/master/workflow.png)
Only download from GPU to CPU and upload back 比较耗时。

##Next
1. 视频裁剪问题
1. 使用GraphicBuffer加速顶点上传
1. 使用oes扩展加速每一帧图像数据的下载和上传
1. 裁剪问题，当前是拉伸的，这里可以暂时让上层对渲染组件处理的(重写TextureView的onMeasure方法).
