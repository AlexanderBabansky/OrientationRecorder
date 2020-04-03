//
// Created by Alexander Babansky on 25.03.2020.
//
#include <time.h>
#include <unistd.h>
#include <array>
#include <cmath>

#include "native-lib.h"
#include <jni.h>
#include "head_tracker.h"

static constexpr uint64_t kNanosInSeconds = 1000000000;
constexpr uint64_t kPredictionTimeWithoutVsyncNanos = 50000000;

cardboard::HeadTracker* head_tracker_;

extern "C"{

JNIEXPORT void JNICALL Java_ua_alexanderbabansky_orientationrecorder_RecordService_InitNative( JNIEnv* env,jobject thiz ){
    head_tracker_ = new cardboard::HeadTracker();
    head_tracker_->Resume();
}

JNIEXPORT void JNICALL Java_ua_alexanderbabansky_orientationrecorder_RecordService_DisposeNative( JNIEnv* env,jobject thiz ){
    head_tracker_->Pause();
    delete head_tracker_;
}

JNIEXPORT jfloatArray JNICALL Java_ua_alexanderbabansky_orientationrecorder_RecordService_GetOrientation( JNIEnv* env,jobject thiz )
{
    struct timespec res;
    clock_gettime(CLOCK_MONOTONIC, &res);
    long monotonic_time_nano = (res.tv_sec * kNanosInSeconds) + res.tv_nsec;
    monotonic_time_nano += kPredictionTimeWithoutVsyncNanos;

    std::array<float, 4> out_orientation;
    jfloat jout_orientation[4];
    std::array<float, 3> out_position;
    float test[3] = {1,2,3};
    head_tracker_->GetPose(monotonic_time_nano,out_position, out_orientation);

    for (int a=0;a<4;a++){
        jout_orientation[a] = out_orientation[a];
    }

    jfloatArray jfa_out_orientation = env->NewFloatArray(4);
    env->SetFloatArrayRegion(jfa_out_orientation,0,4,jout_orientation);
    return jfa_out_orientation;
}
}