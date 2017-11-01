/*
 * Copyright 2016 - 2017 Vincan Yang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vincan.rotatecircleimageview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.Arrays;

/**
 * A fast rotating circular ImageView perfect for profile images.
 *
 * @author vincanyang
 */
public class RotateCircleImageView extends ImageView {

    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 2;

    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_CIRCLE_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final boolean DEFAULT_BORDER_OVERLAY = false;

    private final RectF mDrawableRect = new RectF();
    private final RectF mBorderRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();
    private final Paint mCircleBackgroundPaint = new Paint();

    private int mBorderWidth = DEFAULT_BORDER_WIDTH;
    private int mCircleBackgroundColor = DEFAULT_CIRCLE_BACKGROUND_COLOR;

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBitmapHeight;

    private float mDrawableRadius;

    private ColorFilter mColorFilter;

    private boolean mReady;
    private boolean mSetupPending;
    private boolean mBorderOverlay;
    private boolean mDisableCircularTransformation;

    private static final int DEFAULT_BORDER_TRACK_START_COLOR = Color.parseColor("#4799FE");
    private static final int DEFAULT_BORDER_TRACK_END_COLOR = Color.parseColor("#19D7FD");
    private static final int DEFAULT_BORDER_ROTATE_DURATION = 1200;
    private static final int DEFAULT_BORDER_TRACK_DURATION = 2000;
    private static final int DEFAULT_CIRCLE_COLOR = DEFAULT_BORDER_TRACK_START_COLOR;
    private static final int DEFAULT_BORDER_PADDING = 0;
    private static final int DEFAULT_BORDER_COLORS_LENGTH = 50;
    private static final float SOLID_ARC_MARGIN_ANGLE = 6f;

    public enum BorderStyle {
        STILL,
        ROTATE
    }

    private final static SparseArray<BorderStyle> sBorderStyleArray = new SparseArray<BorderStyle>(2) {
        {
            append(0, BorderStyle.STILL);
            append(1, BorderStyle.ROTATE);
        }
    };
    private BorderStyle mBorderStyle = BorderStyle.STILL;

    private float mRotateAngle;
    private ValueAnimator mRotateValueAnimator;
    private int mBorderRotateDuration = DEFAULT_BORDER_ROTATE_DURATION;

    private int mBorderTrackDuration = DEFAULT_BORDER_TRACK_DURATION;
    private int mBorderTrackStartColor = DEFAULT_BORDER_TRACK_START_COLOR;
    private int mBorderTrackEndColor = DEFAULT_BORDER_TRACK_END_COLOR;

    private ValueAnimator mFirstSolidTrackValueAnimator;//0=>216
    private ValueAnimator mSecondSolidValueAnimator;//216<=>36
    private float mSolidTrackAngle;
    private Paint mSolidTrackPaint = new Paint();

    private ValueAnimator mFirstDottedTrackValueAnimator;//0=>360
    private ValueAnimator mSencondDottedTrackValueAnimator;//360<=>108
    private float mDottedTrackAngle;
    private Paint mDottedTrackPaint = new Paint();
    private DashPathEffect mDashPathEffect = new DashPathEffect(new float[]{1f, 15f}, 0f);

    private int[] mBorderColors = new int[]{DEFAULT_CIRCLE_COLOR};
    private int mBorderPadding = DEFAULT_BORDER_PADDING;

    public RotateCircleImageView(Context context) {
        super(context);
        init();
    }

    public RotateCircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateCircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RotateCircleImageView, defStyle, 0);
        mBorderWidth = typedArray.getDimensionPixelSize(R.styleable.RotateCircleImageView_rciv_border_width, DEFAULT_BORDER_WIDTH);
        mBorderOverlay = typedArray.getBoolean(R.styleable.RotateCircleImageView_rciv_border_overlay, DEFAULT_BORDER_OVERLAY);
        mCircleBackgroundColor = typedArray.getColor(R.styleable.RotateCircleImageView_rciv_circle_background_color,
                DEFAULT_CIRCLE_BACKGROUND_COLOR);

        final int borderColorsId = typedArray.getResourceId(R.styleable.RotateCircleImageView_rciv_border_colors, 0);
        mBorderPadding = typedArray.getDimensionPixelSize(R.styleable.RotateCircleImageView_rciv_border_padding, DEFAULT_BORDER_PADDING);
        mBorderTrackStartColor = typedArray.getColor(R.styleable.RotateCircleImageView_rciv_border_track_start_color, DEFAULT_BORDER_TRACK_START_COLOR);
        mBorderTrackEndColor = typedArray.getColor(R.styleable.RotateCircleImageView_rciv_border_track_start_color, DEFAULT_BORDER_TRACK_END_COLOR);
        mBorderTrackDuration = typedArray.getInt(R.styleable.RotateCircleImageView_rciv_border_track_duration, DEFAULT_BORDER_TRACK_DURATION);
        mBorderRotateDuration = typedArray.getInt(R.styleable.RotateCircleImageView_rciv_border_rotate_duration, DEFAULT_BORDER_ROTATE_DURATION);
        BorderStyle circleStyle = sBorderStyleArray.get(typedArray.getInt(R.styleable.RotateCircleImageView_rciv_border_style, BorderStyle.STILL.ordinal()));
        setBorderStyle(circleStyle);
        if (borderColorsId != 0) {
            mBorderColors = getResources().getIntArray(borderColorsId);
        }
        typedArray.recycle();
        init();
    }

    private void init() {
        super.setScaleType(SCALE_TYPE);
        mReady = true;

        if (mSetupPending) {
            setup();
            mSetupPending = false;
        }
    }

    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (adjustViewBounds) {
            throw new IllegalArgumentException("adjustViewBounds not supported.");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDisableCircularTransformation) {
            super.onDraw(canvas);
            return;
        }
        if (mBitmap != null) {
            if (mCircleBackgroundColor != Color.TRANSPARENT) {
                canvas.drawCircle(mDrawableRect.centerX(), mDrawableRect.centerY(), mDrawableRadius, mCircleBackgroundPaint);
            }
            canvas.drawCircle(mDrawableRect.centerX(), mDrawableRect.centerY(), mDrawableRadius, mBitmapPaint);
        }
        if (mBorderWidth > 0) {
            switch (mBorderStyle) {
                case ROTATE:
                    drawRotateBorder(canvas);
                    break;
                case STILL:
                    drawStillBorder(canvas);
                    break;
            }
        }
    }

    private void drawRotateBorder(Canvas canvas) {
        canvas.rotate(mRotateAngle, mBorderRect.centerX(), mBorderRect.centerY());//实线圆弧末端转动是通过旋转整个view来实现的
        canvas.drawArc(mBorderRect, 270f, mSolidTrackAngle, false, mSolidTrackPaint);
        canvas.drawArc(mBorderRect, 270f, mDottedTrackAngle, false, mDottedTrackPaint);
    }

    private void drawStillBorder(Canvas canvas) {
        for (int i = 0; i < mBorderColors.length; i++) {
            mBorderPaint.setColor(mBorderColors[i]);
            float startAngle = 270f + 360f / mBorderColors.length * i;
            float arcGap = mBorderColors.length > 1 ? SOLID_ARC_MARGIN_ANGLE : 0;
            canvas.drawArc(mBorderRect, startAngle, 360f / mBorderColors.length - arcGap, false, mBorderPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        setup();
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
        setup();
    }

    public int getCircleBackgroundColor() {
        return mCircleBackgroundColor;
    }

    public void setCircleBackgroundColor(int circleBackgroundColor) {
        if (circleBackgroundColor == mCircleBackgroundColor) {
            return;
        }
        mCircleBackgroundColor = circleBackgroundColor;
        mCircleBackgroundPaint.setColor(circleBackgroundColor);
        invalidate();
    }

    public void setCircleBackgroundColorResource(int circleBackgroundRes) {
        setCircleBackgroundColor(getContext().getResources().getColor(circleBackgroundRes));
    }

    public int getBorderWidth() {
        return mBorderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        if (borderWidth == mBorderWidth) {
            return;
        }
        mBorderWidth = borderWidth;
        setup();
    }

    public boolean isBorderOverlay() {
        return mBorderOverlay;
    }

    public void setBorderOverlay(boolean borderOverlay) {
        if (borderOverlay == mBorderOverlay) {
            return;
        }
        mBorderOverlay = borderOverlay;
        setup();
    }

    public boolean isDisableCircularTransformation() {
        return mDisableCircularTransformation;
    }

    public void setDisableCircularTransformation(boolean disableCircularTransformation) {
        if (mDisableCircularTransformation == disableCircularTransformation) {
            return;
        }
        mDisableCircularTransformation = disableCircularTransformation;
        initializeBitmap();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        initializeBitmap();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        initializeBitmap();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        initializeBitmap();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        initializeBitmap();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (cf == mColorFilter) {
            return;
        }
        mColorFilter = cf;
        applyColorFilter();
        invalidate();
    }

    @Override
    public ColorFilter getColorFilter() {
        return mColorFilter;
    }

    private void applyColorFilter() {
        if (mBitmapPaint != null) {
            mBitmapPaint.setColorFilter(mColorFilter);
        }
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initializeBitmap() {
        if (mDisableCircularTransformation) {
            mBitmap = null;
        } else {
            mBitmap = getBitmapFromDrawable(getDrawable());
        }
        setup();
    }

    private void setup() {
        if (!mReady) {
            mSetupPending = true;
            return;
        }

        if (getWidth() == 0 && getHeight() == 0) {
            return;
        }

        if (mBitmap != null) {
            mBitmapPaint.setAntiAlias(true);
            mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mBitmapPaint.setShader(mBitmapShader);
            mBitmapHeight = mBitmap.getHeight();
            mBitmapWidth = mBitmap.getWidth();
        }

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setStrokeCap(Paint.Cap.ROUND);

        mCircleBackgroundPaint.setStyle(Paint.Style.FILL);
        mCircleBackgroundPaint.setAntiAlias(true);
        mCircleBackgroundPaint.setColor(mCircleBackgroundColor);

        mBorderRect.set(calculateBounds());
        mBorderRect.inset(mBorderWidth / 2.0f, mBorderWidth / 2.0f);

        mDrawableRect.set(calculateBounds());
        if (mBorderPadding > 0) {
            mDrawableRect.inset(mBorderPadding, mBorderPadding);
        }
        if (!mBorderOverlay && mBorderWidth > 0) {
            mDrawableRect.inset(mBorderWidth - 1.0f, mBorderWidth - 1.0f);
        }
        mDrawableRadius = Math.min(mDrawableRect.height() / 2.0f, mDrawableRect.width() / 2.0f);

        Shader shader = new LinearGradient(0f, 0f, getWidth(),
                getHeight(), mBorderTrackStartColor, mBorderTrackEndColor, Shader.TileMode.CLAMP);
        mSolidTrackPaint.setShader(shader);
        mSolidTrackPaint.setStyle(Paint.Style.STROKE);
        mSolidTrackPaint.setAntiAlias(true);
        mSolidTrackPaint.setStrokeWidth(mBorderWidth);
        mSolidTrackPaint.setStrokeCap(Paint.Cap.ROUND);

        mDottedTrackPaint = new Paint(mSolidTrackPaint);
        mDottedTrackPaint.setPathEffect(mDashPathEffect);

        applyColorFilter();
        updateShaderMatrix();
        invalidate();
    }

    private RectF calculateBounds() {
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        int sideLength = Math.min(availableWidth, availableHeight);

        float left = getPaddingLeft() + (availableWidth - sideLength) / 2f;
        float top = getPaddingTop() + (availableHeight - sideLength) / 2f;

        return new RectF(left, top, left + sideLength, top + sideLength);
    }

    private void updateShaderMatrix() {
        if (mBitmap == null) {
            return;
        }
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / (float) mBitmapHeight;
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            scale = mDrawableRect.width() / (float) mBitmapWidth;
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + mDrawableRect.left, (int) (dy + 0.5f) + mDrawableRect.top);

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

    private void startValueAnimator() {
        if (mRotateValueAnimator == null) {
            mRotateValueAnimator = ValueAnimator.ofFloat(0f, 360f);
            mRotateValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mRotateAngle = (Float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mRotateValueAnimator.setInterpolator(new LinearInterpolator());
            mRotateValueAnimator.setDuration(mBorderRotateDuration);
            mRotateValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        }
        mRotateValueAnimator.start();

        if (mFirstSolidTrackValueAnimator == null) {
            mFirstSolidTrackValueAnimator = ValueAnimator.ofFloat(0f, 216f);
            mFirstSolidTrackValueAnimator.setDuration(mBorderTrackDuration);
            mFirstSolidTrackValueAnimator.setRepeatCount(0);
            mFirstSolidTrackValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSolidTrackAngle = (Float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mFirstSolidTrackValueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mSecondSolidValueAnimator.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }

        if (mSecondSolidValueAnimator == null) {
            mSecondSolidValueAnimator = ValueAnimator.ofFloat(216f, 36f);
            mSecondSolidValueAnimator.setDuration(mBorderTrackDuration);
            mSecondSolidValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mSecondSolidValueAnimator.setRepeatMode(ValueAnimator.REVERSE);
            mSecondSolidValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSolidTrackAngle = (Float) animation.getAnimatedValue();
                    invalidate();
                }
            });
        }
        mFirstSolidTrackValueAnimator.start();

        if (mFirstDottedTrackValueAnimator == null) {
            mFirstDottedTrackValueAnimator = ValueAnimator.ofFloat(0f, 360f);
            mFirstDottedTrackValueAnimator.setInterpolator(new LinearInterpolator());
            mFirstDottedTrackValueAnimator.setDuration(mBorderTrackDuration);
            mFirstDottedTrackValueAnimator.setRepeatCount(0);
            mFirstDottedTrackValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mDottedTrackAngle = (Float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mFirstDottedTrackValueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mSencondDottedTrackValueAnimator.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }

        if (mSencondDottedTrackValueAnimator == null) {
            mSencondDottedTrackValueAnimator = ValueAnimator.ofFloat(360f, 108f);
            mSencondDottedTrackValueAnimator.setInterpolator(new LinearInterpolator());
            mSencondDottedTrackValueAnimator.setDuration(mBorderTrackDuration);
            mSencondDottedTrackValueAnimator.setRepeatCount(-1);
            mSencondDottedTrackValueAnimator.setRepeatMode(ValueAnimator.REVERSE);
            mSencondDottedTrackValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mDottedTrackAngle = (Float) animation.getAnimatedValue();
                    invalidate();
                }
            });
        }
        mFirstDottedTrackValueAnimator.start();
    }

    private void endValueAnimator() {
        if (mRotateValueAnimator != null) {
            mRotateValueAnimator.end();
        }
        if (mFirstSolidTrackValueAnimator != null) {
            mFirstSolidTrackValueAnimator.end();
        }
        if (mSecondSolidValueAnimator != null) {
            mSecondSolidValueAnimator.end();
        }
        if (mFirstDottedTrackValueAnimator != null) {
            mFirstDottedTrackValueAnimator.end();
        }
        if (mSencondDottedTrackValueAnimator != null) {
            mSencondDottedTrackValueAnimator.end();
        }
    }


    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            if (mBorderStyle == BorderStyle.ROTATE) {
                startValueAnimator();
            }
        } else {
            if (mBorderStyle == BorderStyle.ROTATE) {
                endValueAnimator();//不可见时停止动画，避免过度绘制
            }
        }
    }

    public void setBorderColors(int[] borderColors) {
        if (borderColors != null && !Arrays.equals(mBorderColors, borderColors)) {
            if (borderColors.length > DEFAULT_BORDER_COLORS_LENGTH) {
                borderColors = Arrays.copyOfRange(borderColors, 0, DEFAULT_BORDER_COLORS_LENGTH);
            }
            mBorderColors = borderColors;
            invalidate();
        }
    }

    public void setBorderTrackDuration(int borderTrackDuration) {
        if (mBorderTrackDuration != borderTrackDuration) {
            mBorderTrackDuration = borderTrackDuration;
            mFirstSolidTrackValueAnimator.setDuration(mBorderTrackDuration);
        }
    }

    public void setBorderRotateDuration(int borderRotateDuration) {
        if (mBorderRotateDuration != borderRotateDuration) {
            mBorderRotateDuration = borderRotateDuration;
            mRotateValueAnimator.setDuration(mBorderRotateDuration);
        }
    }

    public void setBorderStyle(BorderStyle borderStyle) {
        if (mBorderStyle != borderStyle) {
            mBorderStyle = borderStyle;
            if (mBorderStyle == BorderStyle.ROTATE) {
                startValueAnimator();
            } else {
                endValueAnimator();
            }
            invalidate();
        }
    }

    public BorderStyle getBorderStyle() {
        return mBorderStyle;
    }

    public void setBorderTrackStartColor(int borderTrackStartColor) {
        if (borderTrackStartColor != mBorderTrackStartColor) {
            mBorderTrackStartColor = borderTrackStartColor;
            mSolidTrackPaint = null;
            mDottedTrackPaint = null;
        }
    }

    public void setBorderTrackEndColor(int borderTrackEndColor) {
        if (borderTrackEndColor != mBorderTrackEndColor) {
            mBorderTrackEndColor = borderTrackEndColor;
            mSolidTrackPaint = null;
            mDottedTrackPaint = null;
        }
    }

    public int getBorderPadding() {
        return mBorderPadding;
    }

    public void setBorderPadding(int borderPadding) {
        if (borderPadding == mBorderPadding) {
            return;
        }
        mBorderPadding = borderPadding;
        setup();
    }
}