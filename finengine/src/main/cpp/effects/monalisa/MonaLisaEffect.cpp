//
// Created by iFinVer on 2016/12/28.
//

#include "MonaLisaEffect.h"
#include "../../log.h"

#define LOG_TAG "Mona Lisa"

void MonaLisaEffect::monaLisa(Mat lisaFrame, Rect2i lisaRect, vector<cv::Point2i> lisaPoints, Mat vinciFrame, Rect2i vinciRect,
                              vector<cv::Point2i> vinciPoints) {
    //lisa face roi
    mFrameSize.width = (lisaRect.width > vinciRect.width ? lisaRect.width : vinciRect.width);
    mFrameSize.height = (lisaRect.height > vinciRect.height ? lisaRect.height : vinciRect.height);

    Rect lisaBorder = lisaRect - Point((mFrameSize.width - lisaRect.width) / 2, (mFrameSize.height - lisaRect.height) / 2);
    lisaBorder.width = mFrameSize.width;
    lisaBorder.height = mFrameSize.height;
    Rect temp = lisaBorder & cv::Rect(0, 0, lisaFrame.cols, lisaFrame.rows);
    if(temp != lisaBorder){
        //mFrameSize和mSmallFrameLisa和mSmallFrameVinci必须相等，不相等不处理
        return;
    }
    mSmallFrameLisa = lisaFrame(lisaBorder);

    Rect vinciBorder = vinciRect - Point((mFrameSize.width - vinciRect.width) / 2, (mFrameSize.height - vinciRect.height) / 2);
    vinciBorder.width = mFrameSize.width;
    vinciBorder.height = mFrameSize.height;
    Rect temp2 = vinciBorder & cv::Rect(0, 0, vinciFrame.cols, vinciFrame.rows);
    if(temp2 != vinciBorder){
        //mFrameSize和mSmallFrameLisa和mSmallFrameVinci必须相等，不相等不处理
        return;
    }
    mSmallFrameVinci = vinciFrame(vinciBorder);


    Point2i vinciPointTl = vinciBorder.tl();
    mVinciRect = vinciRect - vinciPointTl;
    //format face points to face rect
    mPointsLisa.clear();
    mPointsVinci.clear();
    Point2i lisaPointTl = lisaBorder.tl();
    for (int i = 0; i < lisaPoints.size(); i++) {
        mPointsLisa.push_back(Point2i(lisaPoints[i] - lisaPointTl));
        mPointsVinci.push_back(Point2i(vinciPoints[i] - vinciPointTl));
    }
    //calculate affine transform mat
    mTransformKeyPointsLisa[0] = mPointsLisa[9];
    mTransformKeyPointsLisa[1] = mPointsLisa[21];
    mTransformKeyPointsLisa[2] = mPointsLisa[22];
    mTransformKeyPointsVinci[0] = mPointsVinci[9];
    mTransformKeyPointsVinci[1] = mPointsVinci[21];
    mTransformKeyPointsVinci[2] = mPointsVinci[22];
    mTransformVinciToLisa = getAffineTransform(mTransformKeyPointsVinci, mTransformKeyPointsLisa);
    //calculate mask
    mMask.create(mFrameSize, CV_8UC1);
    mMask.setTo(cv::Scalar::all(0));
    fillConvexPoly(mMask, mPointsVinci, cv::Scalar(255));
//    outputMat("mMask",mMask);
    warpAffine(mMask, mWarpedMask, mTransformVinciToLisa, mFrameSize, INTER_NEAREST, BORDER_CONSTANT);
//    bitwise_and(mMask, mWarpedMask, mRefineMask);
    //extract Vinci's face, and warp it
    mSmallFrameVinci.copyTo(mFaceVinci, mMask);
    warpAffine(mFaceVinci, mWarpedFaceVinci, mTransformVinciToLisa, mFrameSize, INTER_NEAREST, BORDER_CONSTANT);
    //specify color
    specifyHistogram(mSmallFrameLisa, mWarpedFaceVinci, mWarpedMask);
    mFeatherSize = Size(5,5);
    //feather
    featherMask(mWarpedMask);
    //blend pixels
    for (size_t i = 0; i < mSmallFrameLisa.rows; i++) {
        auto frame_pixel = mSmallFrameLisa.row(i).data;
        auto faces_pixel = mWarpedFaceVinci.row(i).data;
        auto masks_pixel = mWarpedMask.row(i).data;

        for (size_t j = 0; j < mSmallFrameLisa.cols; j++) {
            if (*masks_pixel != 0) {
                *frame_pixel = (uchar) (((255 - *masks_pixel) * (*frame_pixel) + (*masks_pixel) * (*faces_pixel)) >> 8); // divide by 256
                *(frame_pixel + 1) = (uchar) (((255 - *(masks_pixel + 1)) * (*(frame_pixel + 1)) + (*(masks_pixel + 1)) * (*(faces_pixel + 1))) >> 8);
                *(frame_pixel + 2) = (uchar) (((255 - *(masks_pixel + 2)) * (*(frame_pixel + 2)) + (*(masks_pixel + 2)) * (*(faces_pixel + 2))) >> 8);
            }else{
            }

            frame_pixel += 3;
            faces_pixel += 3;
            masks_pixel++;
        }
    }
    //done.
}

void MonaLisaEffect::specifyHistogram(const cv::Mat source_image, cv::Mat target_image, cv::Mat mask) {
    std::memset(source_hist_int, 0, sizeof(int) * 3 * 256);
    std::memset(target_hist_int, 0, sizeof(int) * 3 * 256);

    for (size_t i = 0; i < mask.rows; i++) {
        auto current_mask_pixel = mask.row(i).data;
        auto current_source_pixel = source_image.row(i).data;
        auto current_target_pixel = target_image.row(i).data;

        for (size_t j = 0; j < mask.cols; j++) {
            if (*current_mask_pixel != 0) {
                source_hist_int[0][*current_source_pixel]++;
                source_hist_int[1][*(current_source_pixel + 1)]++;
                source_hist_int[2][*(current_source_pixel + 2)]++;

                target_hist_int[0][*current_target_pixel]++;
                target_hist_int[1][*(current_target_pixel + 1)]++;
                target_hist_int[2][*(current_target_pixel + 2)]++;
            }

            // Advance to next pixel
            current_source_pixel += 3;
            current_target_pixel += 3;
            current_mask_pixel++;
        }
    }

    // Calc CDF
    for (size_t i = 1; i < 256; i++) {
        source_hist_int[0][i] += source_hist_int[0][i - 1];
        source_hist_int[1][i] += source_hist_int[1][i - 1];
        source_hist_int[2][i] += source_hist_int[2][i - 1];

        target_hist_int[0][i] += target_hist_int[0][i - 1];
        target_hist_int[1][i] += target_hist_int[1][i - 1];
        target_hist_int[2][i] += target_hist_int[2][i - 1];
    }

    // Normalize CDF
    for (size_t i = 0; i < 256; i++) {
        source_histogram[0][i] = (source_hist_int[0][i] ? (float) source_hist_int[0][i] / source_hist_int[0][255] : 0);
        source_histogram[1][i] = (source_hist_int[1][i] ? (float) source_hist_int[1][i] / source_hist_int[1][255] : 0);
        source_histogram[2][i] = (source_hist_int[2][i] ? (float) source_hist_int[2][i] / source_hist_int[2][255] : 0);

        target_histogram[0][i] = (target_hist_int[0][i] ? (float) target_hist_int[0][i] / target_hist_int[0][255] : 0);
        target_histogram[1][i] = (target_hist_int[1][i] ? (float) target_hist_int[1][i] / target_hist_int[1][255] : 0);
        target_histogram[2][i] = (target_hist_int[2][i] ? (float) target_hist_int[2][i] / target_hist_int[2][255] : 0);
    }

    // Create lookup table

    auto binary_search = [&](const float needle, const float haystack[]) -> uint8_t {
        uint8_t l = 0, r = 255, m = 0;
        while (l < r) {
            m = (uint8_t) ((l + r) / 2);
            if (needle > haystack[m])
                l = (uint8_t) (m + 1);
            else
                r = (uint8_t) (m - 1);
        }
        return m;
    };

    for (size_t i = 0; i < 256; i++) {
        LUT[0][i] = binary_search(target_histogram[0][i], source_histogram[0]);
        LUT[1][i] = binary_search(target_histogram[1][i], source_histogram[1]);
        LUT[2][i] = binary_search(target_histogram[2][i], source_histogram[2]);
    }

    // repaint pixels
    for (size_t i = 0; i < mask.rows; i++) {
        auto current_mask_pixel = mask.row(i).data;
        auto current_target_pixel = target_image.row(i).data;
        for (size_t j = 0; j < mask.cols; j++) {
            if (*current_mask_pixel != 0) {
                *current_target_pixel = LUT[0][*current_target_pixel];
                *(current_target_pixel + 1) = LUT[1][*(current_target_pixel + 1)];
                *(current_target_pixel + 2) = LUT[2][*(current_target_pixel + 2)];
            }

            // Advance to next pixel
            current_target_pixel += 3;
            current_mask_pixel++;
        }
    }
}

void MonaLisaEffect::featherMask(const cv::Mat &mask) {
    const Mat &kernel = getStructuringElement(cv::MORPH_RECT, mFeatherSize);
    cv::erode(mask, mask, kernel, cv::Point(-1, -1), 1, cv::BORDER_CONSTANT,
              cv::Scalar(0));
    cv::blur(mask, mask, mFeatherSize, cv::Point(-1, -1), cv::BORDER_CONSTANT);
}

void MonaLisaEffect::outputMat(const char *matName, Mat mat) {
    std::ostringstream cout;
    cout << mat;
    LOGI("%s\n%s",matName,cout.str().c_str());
}







