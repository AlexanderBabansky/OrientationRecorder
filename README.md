# Orientation Recorder

Android app which records orientation (rotation) of Android device

<a href="https://play.google.com/store/apps/details?id=ua.alexanderbabansky.orientationrecorder"><img height="80dp" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png"/></a>

## Getting Started

### Current status

At this moments app was tested on *Android 8.0*, *arm64-v8a*, built in *Android Studio 3.6.1*. It uses native C++ [Cardboard SDK](https://github.com/googlevr/cardboard) by Google LLC, so CPU architecture is important.

### Prerequisites

To build app you need:

* Android SDK
* Android NDK
* Gradle 6.2.2
* Android Gradle Plugin 3.6.1

### Installing

You can build app, download prebuild APK for *Android 4.4+*, *arm64-v8*, *armeabi-v7a*, or get it on [Google Play](https://play.google.com/store/apps/details?id=ua.alexanderbabansky.orientationrecorder)

## Usage

### General
App can write out in *binary* or *text* format.

It gets orientation data of device in quaternion (w,x,y,z) with *intervals* specified in preferences, or *as fast as possible* (AFAP), if appropriate option is enabled. *AFAP is not recommended*, because device sensors update interval often is slowly (longer) then AFAP interval, so it wastes space. Usually, data interval is *slightly slowly*.

App *doesn't require Power Saving exception* and can record for a long time in background.
App uses *Storage Access Framework* to safely write to file.
Recording starts with a *coundown timer*, for syncing with video capturing devices.

### File format

**text**

\[TIMESTAMP\] \[W\] \[X\] \[Y\] \[Z\]*\\n*

**binary**

First 4 bytes of file are Integer with value 1. It is for detecting ENDIANESS of CPU. Data binary format

**TIMESTAMP|W|X|Y|Z**

TIMESTAMP: 8 bytes, long
* Timestamp in milliseconds from starting recording timer.
	
W,X,Y,Z: 4 bytes*4, float
* Quaternion orientation data
	
## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

Project uses [Cardboard SDK](https://github.com/googlevr/cardboard) from Google LLC, which is under [Apache 2.0 License](https://github.com/googlevr/cardboard/blob/master/LICENSE).
