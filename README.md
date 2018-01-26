RotateCircleImageView
=====================
[![Build Status](https://api.travis-ci.org/yangwencan2002/RotateCircleImageView.svg?branch=master)](https://travis-ci.org/yangwencan2002/RotateCircleImageView/)
[![Download](https://api.bintray.com/packages/yangwencan2002/maven/RotateCircleImageView/images/download.svg)](https://bintray.com/yangwencan2002/maven/RotateCircleImageView/_latestVersion)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![QQ Approved](https://img.shields.io/badge/QQ_Approved-1.0.0-red.svg)](https://github.com/yangwencan2002/RotateCircleImageView)

Introduction
------------
A fast circular ImageView that supports a piecewise, rotatable border perfect for profile images. This is based on [CircleImageView from Henning Dodenhof](https://github.com/hdodenhof/CircleImageView) which itself is based on [RoundedImageView from Vince Mi](https://github.com/vinc3m1/RoundedImageView).

![RotateCircleImageView](https://raw.github.com/yangwencan2002/RotateCircleImageView/master/screencap.gif)

There are many ways to create circular image in android, but BitmapShader which it uses is the fastest and best one that I know of because it:

- does not create a copy of the original bitmap
- does not use a clipPath which is not hardware accelerated and not anti-aliased.
- does not use setXfermode to clip the bitmap and draw twice to the canvas.

Gradle
------
```
dependencies {
    ...
    compile 'com.vincan:rotatecircleimageview:1.0.0'
}
```

Usage
------
XML
```xml
<com.vincan.rotatecircleimageview.RotateCircleImageView
    android:layout_width="100dp"
    android:layout_height="100dp"
    android:src="@drawable/penguin"
    app:rciv_border_width="2dp"
    app:rciv_border_padding="2dp"
    app:rciv_border_colors="@array/border_colors"
    app:rciv_border_style="rotate"/>
```

Java

```java
rotateCircleImageView.setBorderWidth(2);//border width
rotateCircleImageView.setBorderPadding(2);//border padding
rotateCircleImageView.setBorderColors(new int[]{Color.BLUE, Color.LTGRAY});//border colors starting at 12 o'clock and going clockwise
rotateCircleImageView.setBorderStyle(BorderStyle.ROTATE);//border style
```

Sample
------
See `sample` project.

Approved app
------
#### QQ
![QQ](https://raw.github.com/yangwencan2002/RotateCircleImageView/master/screencap_QQ.gif)

Release notes
------
[here](https://github.com/yangwencan2002/RotateCircleImageView/releases)

Where released
------
[bintray.com](https://bintray.com/yangwencan2002/maven/RotateCircleImageView)

License
------

    Copyright 2016-2017 Vincan Yang

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
