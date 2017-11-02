RotateCircleImageView
=====================
[![Build Status](https://api.travis-ci.org/yangwencan2002/RotateCircleImageView.svg?branch=master)](https://travis-ci.org/yangwencan2002/RotateCircleImageView/) [ ![Download](https://api.bintray.com/packages/yangwencan2002/maven/RotateCircleImageView/images/download.svg) ](https://bintray.com/yangwencan2002/maven/RotateCircleImageView/_latestVersion)[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

Introduction
------

A fast rotating circular ImageView perfect for profile images. This is based on [CircleImageView from Henning Dodenhof](https://github.com/hdodenhof/CircleImageView) which itself is based on [RoundedImageView from Vince Mi](https://github.com/vinc3m1/RoundedImageView).

![RotateCircleImageView](https://raw.github.com/yangwencan2002/RotateCircleImageView/master/screenshot.gif)

Gradle
------
```
dependencies {
    ...
    compile 'com.vincan:rotatecircleimageview:1.0.0'
}
```

Usage
-----
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
rotateCircleImageView.setBorderWidth(2);
rotateCircleImageView.setBorderPadding(2);
rotateCircleImageView.setBorderColors(new int[]{Color.BLUE, Color.LTGRAY});
rotateCircleImageView.setBorderStyle(BorderStyle.ROTATE);
```

Sample
------
See `sample` project.

Release Notes
------
[here](https://github.com/yangwencan2002/RotateCircleImageView/releases)

## Where released?
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
