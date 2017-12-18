package com.magicalrice.customview.magicalbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import java.security.InvalidParameterException;

/**
 * Created by Adolph on 2017/12/18.
 */

public class MagicalButton extends View implements DynamicAnimation.OnAnimationUpdateListener {

    private int mWidth, mHeight;
    private float mLength;
    private int mTextColor;//颜色
    private int mLabelColor;//标签颜色
    private int mBackgroundColor;//背景颜色
    private int mCircleColor;//按钮颜色
    private int count;//计数器文本
    private Paint mPaint;
    private VelocityTracker mTracker;
    private boolean canDownZero;
    private float mDownX;
    private float mCenterX;
    private SpringAnimation mAnimX;

    public MagicalButton(Context context) {
        this(context, null);
    }

    public MagicalButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MagicalButton);
        mTextColor = a.getColor(R.styleable.MagicalButton_text_color, Color.WHITE);
        mLabelColor = a.getColor(R.styleable.MagicalButton_enable_label_color, Color.WHITE);
        mLabelColor = a.getColor(R.styleable.MagicalButton_unable_label_color, Color.GRAY);
        mBackgroundColor = a.getColor(R.styleable.MagicalButton_background_color, Color.BLUE);
        mCircleColor = a.getColor(R.styleable.MagicalButton_circle_color, Color.DKGRAY);

        mPaint = new Paint();
        mAnimX = new SpringAnimation(this, SpringAnimation.TRANSLATION_X, 0f);
    }

    private void resetPaint() {
        mPaint.reset();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setBackgroundColor(Color.TRANSPARENT);
        mCenterX = 0f;
        mAnimX.addUpdateListener(this);
        mAnimX.getSpring().setStiffness(getStiffness());
        mAnimX.getSpring().setDampingRatio(getDamping());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawCenterCircle(canvas);
    }

    private void drawBackground(Canvas canvas) {
        resetPaint();
        RectF rectF = new RectF(0f, 0f, getWidth(), getHeight());
        canvas.drawRoundRect(rectF, getHeight() / 2, getHeight() / 2, mPaint);
        mPaint.setColor(mLabelColor);
        canvas.drawLine(mHeight / 2 - mLength, mHeight / 2f, mHeight / 2f + mLength, mHeight / 2f, mPaint);
        canvas.drawLine(mWidth - mHeight / 2 - mLength, mHeight / 2f, (mWidth - mHeight / 2f) + mLength, mHeight / 2f, mPaint);
        canvas.drawLine(mWidth / mHeight / 2f, mHeight / 2f - mLength, mWidth - mHeight / 2f, mHeight / 2f + mLength, mPaint);
    }

    private void drawCenterCircle(Canvas canvas) {
        int[] colors = {Color.GRAY, Color.TRANSPARENT};
        float r;
        RadialGradient gradient = new RadialGradient(mCenterX, mHeight / 2f, mHeight / 2f * 1.16f, colors, null, Shader.TileMode.REPEAT);
        mPaint.setShader(gradient);
        if (mCenterX > mWidth / 2) {
            r = Math.min(mWidth - mCenterX, mHeight / 2f * 1.15f);
        } else {
            r = Math.min(mCenterX, mHeight / 2f * 1.15f);
        }

        canvas.drawCircle(mCenterX, mHeight / 2f, r, mPaint);

        resetPaint();
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(mCenterX, mHeight / 2f, mHeight / 2f * 0.95f, mPaint);

        mPaint.setColor(mTextColor);
        canvas.drawText(count + "", mCenterX - mPaint.measureText(count + "") / 2f, (mHeight - mPaint.ascent() - mPaint.descent()), mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float translationX = 0;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getRawX();
                mTracker.addMovement(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                mAnimX.cancel();
                mCenterX = event.getRawX() - mDownX;
                translationX = event.getRawX() - mDownX;
                mTracker.addMovement(event);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTracker.computeCurrentVelocity(500);
                if (translationX != 0f){
                    mAnimX.setStartVelocity(mTracker.getXVelocity());
                    mAnimX.start();
                }
                if (event.getRawX() > mDownX){
                    count ++;
                } else {
                    if (count <= 0 && canDownZero)
                        count--;
                    if (count <= 0 && !canDownZero){
                        count = 0;
                    }
                    if (count > 0){
                        count--;
                    }
                }
                mTracker.clear();
                return true;
        }
        return false;
    }

    @Override
    public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
        mCenterX = value;
    }

    private float getStiffness() {
        return 50f;
    }

    private float getDamping() {
        return 0.4f;
    }
}
