attribute vec2 aPosition;
attribute vec2 aTexCoord;
attribute vec4 aRotVector;
varying vec2 vTexCoord;
void main(){
   mat2 rotMat = mat2(aRotVector.x,aRotVector.y,aRotVector.z,aRotVector.w);
   vTexCoord = aTexCoord;
   gl_Position = vec4(aPosition * rotMat,1,1);
}