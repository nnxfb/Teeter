package com.nnxfb.teeter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

/* loaded from: classes3.dex */
public final class CGamePage {
    static final boolean $assertionsDisabled = false;
    private Point ballPos;
    private Activity mActivity;
    private Animation mAnimFadeIn;
    private CBall mBall;
    private AbsoluteLayout.LayoutParams mBallParam;
    private ImageView mBallView;
    private AbsoluteLayout mGameLayout;
    private ImageView mHoleAnimView;
    private ImageView mEndAnimView;
    private final int half = (int) (CU.BALL_RADIUS * CU.BALL_RATIO);
    private CAnimDrawable mEndAnimDr = new CAnimDrawable();
    private CAnimDrawable mHoleAnimDr = new CAnimDrawable();

    public CGamePage(Activity activity) {
        this.mActivity = activity;
        System.gc();
        for (int i = 0; i < 32; i++) {
            Bitmap aBmp = BitmapFactory.decodeResource(this.mActivity.getResources(), R.drawable.end_anim_001 + i);
            BitmapDrawable dr = new BitmapDrawable(this.mActivity.getResources(), aBmp);
            this.mEndAnimDr.addFrame(dr, 50);
        }
        for (int i2 = 0; i2 < 20; i2++) {
            Bitmap aBmp2 = BitmapFactory.decodeResource(this.mActivity.getResources(), R.drawable.hole_anim_001 + i2);
            this.mHoleAnimDr.addFrame(new BitmapDrawable(this.mActivity.getResources(), aBmp2), 45 - (i2 * 2));
        }
        this.mAnimFadeIn = AnimationUtils.loadAnimation(this.mActivity, android.R.anim.fade_in);
        this.ballPos = new Point();
    }

    public View fnCreateView() {
        this.mGameLayout = new AbsoluteLayout(this.mActivity);
        BitmapDrawable dr = new BitmapDrawable(this.mActivity.getResources(), Bitmap.createBitmap(CU.BG_BMP));
        this.mGameLayout.setBackground(dr);
        this.mBallView = new ImageView(this.mActivity);
        this.mBallView.setImageResource(R.drawable.ball);
        this.mBallParam = new AbsoluteLayout.LayoutParams(0, 0, 0, 0);
        this.mGameLayout.addView(this.mBallView, this.mBallParam);
        this.mBallView.setVisibility(View.GONE);

        this.mEndAnimView = new ImageView(this.mActivity);
        int radius = (int) (CU.BALL_RADIUS * CU.END_ANIM_RATIO * CU.RATIO);
        this.mGameLayout.addView(mEndAnimView, new AbsoluteLayout.LayoutParams(
                radius * 2,
                radius * 2,
                (int) (CL.end.x * CU.RATIO) - radius + CU.OFFSET_X,
                (int) (CL.end.y * CU.RATIO) - radius + CU.OFFSET_Y));
        this.mEndAnimView.setBackground(this.mEndAnimDr);
        this.mEndAnimView.setVisibility(View.INVISIBLE);

        this.mHoleAnimView = new ImageView(this.mActivity);
        int radius2 = (int) (CU.BALL_RADIUS * CU.HOLE_ANIM_RATIO * CU.RATIO);
        this.mGameLayout.addView(this.mHoleAnimView, new AbsoluteLayout.LayoutParams(
                radius2 * 2,
                radius2 * 2,
                (int) (CL.end.x * CU.RATIO) - radius2 + CU.OFFSET_X,
                (int) (CL.end.y * CU.RATIO) - radius2 + CU.OFFSET_Y));
        this.mHoleAnimView.setBackground(this.mHoleAnimDr);
        this.mHoleAnimView.setVisibility(View.INVISIBLE);

        return this.mGameLayout;
    }

    public void fnAttachBall(CBall ball) {
        this.mBall = ball;
        if (this.mBallView != null) {
            this.mBallView.setVisibility(View.VISIBLE);
            fnInvalidate();
            this.mBallView.startAnimation(this.mAnimFadeIn);
        }
    }

    public void fnDetachBall() {
        this.mBall = null;
        if (this.mBallView != null) {
            this.mBallView.setVisibility(View.INVISIBLE);
        }
    }

    public void fnShowBall(int visibility) {
        if (this.mBallView != null) {
            this.mBallView.setVisibility(visibility);
        }
    }

    public void fnInvalidate() {
        fnAdjustBallPosToDrawPos();
        this.mGameLayout.updateViewLayout(this.mBallView, this.mBallParam);
    }

    public void fnPlayEndingAnimation(Message playEndMsg) {
        this.mBallView.setVisibility(View.INVISIBLE);
        this.mEndAnimView.setVisibility(View.VISIBLE);
        this.mEndAnimDr.fnSetPlayEndMsg(playEndMsg);
        this.mEndAnimDr.stop();
        this.mEndAnimDr.start();
    }

    public void fnPlayHoleAnimation(Point holePos, Message playEndMsg) {
        this.mBallView.setVisibility(View.INVISIBLE);
        if (holePos != null) {
            this.mHoleAnimDr.fnSetPlayEndMsg(playEndMsg);
            int radius = (int) (CU.BALL_RADIUS * CU.HOLE_ANIM_RATIO * CU.RATIO);
            this.mGameLayout.updateViewLayout(this.mHoleAnimView, new AbsoluteLayout.LayoutParams(
                    radius * 2,
                    radius * 2,
                    (int) (holePos.x * CU.RATIO) - radius + CU.OFFSET_X,
                    (int) (holePos.y * CU.RATIO) - radius + CU.OFFSET_Y));
            CRotateAnimation an = new CRotateAnimation(this.mHoleAnimView.getWidth() / 2, this.mHoleAnimView.getHeight() / 2, this.mBall.getInHoleDegree() + 1);
            an.setDuration(5L);
            an.setFillAfter(true);
            this.mHoleAnimView.setVisibility(View.VISIBLE);
            this.mHoleAnimView.startAnimation(an);
            this.mHoleAnimDr.stop();
            this.mHoleAnimDr.start();
        }
    }

    private void fnAdjustBallPosToDrawPos() {
        this.mBall.fnGetCenter(this.ballPos);
        this.mBallParam.x = (int) ((this.ballPos.x - this.half) * CU.RATIO) + CU.OFFSET_X;
        this.mBallParam.y = (int) ((this.ballPos.y - this.half) * CU.RATIO) + CU.OFFSET_Y;
        this.mBallParam.width = (int) (this.half * 2 * CU.RATIO);
        this.mBallParam.height = (int) (this.half * 2 * CU.RATIO);
    }

    public static final class CAnimDrawable extends AnimationDrawable {
        private Message mPlayEndMsg;
        private int mPlayingCount = 0;

        public void fnSetPlayEndMsg(Message msg) {
            this.mPlayEndMsg = msg;
        }

        @Override // android.graphics.drawable.AnimationDrawable, java.lang.Runnable
        public void run() {
            if (this.mPlayEndMsg == null || this.mPlayEndMsg.getTarget() == null || mPlayingCount > getNumberOfFrames()) {
                return;
            }
            if (this.mPlayingCount <= getNumberOfFrames() - 2) {
                super.run();
            }
            if (this.mPlayingCount >= getNumberOfFrames() - 1) {
                this.mPlayingCount = 0;
                this.mPlayEndMsg.sendToTarget();
            } else {
                this.mPlayingCount++;
            }
        }
    }

    public void clearMemory() {
        this.ballPos = null;
        this.mActivity = null;
        this.mAnimFadeIn = null;
        if (this.mBall != null) {
            this.mBall.clearMemory();
        }
        this.mBall = null;
        this.mBallParam = null;
        this.mBallView = null;
        this.mEndAnimDr = null;
        this.mGameLayout = null;
        this.mHoleAnimDr = null;
        this.mHoleAnimView = null;
        this.mEndAnimView =null;
    }
}
