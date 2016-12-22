precision highp float;
attribute highp vec2 aPosition;
attribute highp vec2 aTexCoord;
uniform highp int uRotation;
uniform highp float uScaleX;
uniform highp float uScaleY;
varying highp vec2 vTexCoord;
void main(){
   vTexCoord = aTexCoord;
   vec2 rotPos = aPosition;
   if(uRotation == 1){
       rotPos = aPosition * mat2(0,-1,1,0);
   }else if(uRotation == 2){
       rotPos = aPosition * mat2(-1,0,0,-1);
   }else if(uRotation == 3){
       rotPos = aPosition * mat2(0,1,-1,0);
   }

   mat2 scaleMtx = mat2(uScaleX,0,0,uScaleY);
   gl_Position = vec4(scaleMtx * rotPos,1.0,1.0);
}