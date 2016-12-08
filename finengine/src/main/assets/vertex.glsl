precision highp float;
attribute vec2 aPosition;
attribute vec2 aTexCoord;
attribute float aScaleX;
attribute float aScaleY;
varying vec2 vTexCoord;

void main(){
    vTexCoord = aTexCoord;
    mat2 aScaleMtx = mat2(aScaleX,0,0,aScaleY);
    gl_Position = vec4(aScaleMtx * aPosition,1.0,1.0);
}