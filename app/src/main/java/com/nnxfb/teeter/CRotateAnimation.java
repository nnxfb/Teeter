package com.nnxfb.teeter;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/* loaded from: classes3.dex */
public class CRotateAnimation extends Animation {
    private int mCenterX;
    private int mCenterY;
    private int mDegree;

    public CRotateAnimation(int centerX, int centerY, int degree) {
        this.mCenterX = 0;
        this.mCenterY = 0;
        this.mDegree = 0;
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        this.mDegree = degree;
    }

    @Override // android.view.animation.Animation
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override // android.view.animation.Animation
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        Matrix matrix = t.getMatrix();
        Camera mCamera = new Camera();
        mCamera.save();
        mCamera.rotateZ(this.mDegree);
        mCamera.getMatrix(matrix);
        mCamera.restore();
        matrix.preTranslate(-this.mCenterX, -this.mCenterY);
        matrix.postTranslate(this.mCenterX, this.mCenterY);
    }
}
