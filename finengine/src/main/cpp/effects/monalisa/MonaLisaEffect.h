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
    /**
     * Vinci's face will copy to Lisa's face
     */
    void monaLisa(Mat lisaFrame,Rect2i lisaRect,vector<cv::Point2i> lisaPoints,Mat vinciFrame,Rect2i vinciRect,vector<cv::Point2i> vinciPoints);

private:
    // Calculates source image histogram and changes target_image to match source hist
    void specifyHistogram(const cv::Mat source_image, cv::Mat target_image, cv::Mat mask);
    // Blurs edges of mask
    void featherMask(const cv::Mat &mask);

    Mat mSmallLisaFrame,mSmallVinciFrame;
    Rect mVinciRect,mBigVinciRect;
    vector<Point2i> mPointsLisa,mPointsVinci;
    Point2f mTransformKeyPointsLisa[3],mTransformKeyPointsVinci[3];
    Size mFeatherSize,mFrameSizeLisa,mFrameSizeVinci;
    Mat mTransformVinciToLisa;
    Mat mMask,mWarpedMask,mRefineMask;
    Mat mFaceVinci,mWarpedFaceVinci;

    //used to specify color
    uint8_t LUT[3][256];
    int source_hist_int[3][256];
    int target_hist_int[3][256];
    float source_histogram[3][256];
    float target_histogram[3][256];
};


#endif //FINENGINE_MONALISAEFFECT_H
