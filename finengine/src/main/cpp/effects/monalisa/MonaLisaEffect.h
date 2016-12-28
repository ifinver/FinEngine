//
// Created by iFinVer on 2016/12/28.
//

#ifndef FINENGINE_MONALISAEFFECT_H
#define FINENGINE_MONALISAEFFECT_H

#include <opencv2/opencv.hpp>
using namespace cv;
using namespace std;

class MonaLisaEffect {
public:

    void monaLisa(Mat lisaFrame,Rect2i lisaRect,vector<cv::Point2i> lisaPoints,Mat vinciFrame,Rect2i vinciRect,vector<cv::Point2i> vinciPoints);

private:

};


#endif //FINENGINE_MONALISAEFFECT_H
