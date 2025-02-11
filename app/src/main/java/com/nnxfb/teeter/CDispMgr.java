package com.nnxfb.teeter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;

/* loaded from: classes3.dex */
public class CDispMgr {
    private static final int EFF_AT_END = 201;
    private static final int EFF_AT_HOLE = 200;
    public static final int GAME_VIEW = 0;
    private static final int MSG_EFFECT = 100;
    public static final int RANK_VIEW = 2;
    public static final int SCORE_VIEW = 1;
    private Activity mActivity;
    private CScoreHandler mCSHandler;
    private EffectHandler mEffHandler;
    private CGamePage mGamePage;
    private CRankPage mRankPage;
    private CScorePage mScorePage;

    public CDispMgr(Activity activity) {
        this.mActivity = activity;
        this.mScorePage = new CScorePage(this.mActivity);
        this.mRankPage = new CRankPage(this.mActivity);
        this.mGamePage = new CGamePage(this.mActivity);
        this.mEffHandler = new EffectHandler();
    }

    public void fnShowGamePage() {
        fnAddViewIntoSwitcher(this.mGamePage.fnCreateView());
    }

    public void fnInvalidate() {
        this.mGamePage.fnInvalidate();
    }

    public void fnShowScorePage() {
        fnAddViewIntoSwitcher(this.mScorePage.fnCreateView());
        new Handler().postDelayed(() -> CDispMgr.this.mScorePage.fnInvalidate(), 100L);
        CBGLoadingThread cBGLoadingThread = null;
        if (CU.LEVEL < CU.LEVEL_COUNT) {
            Activity activity = this.mActivity;
            int i = CU.LEVEL + 1;
            CU.LEVEL = i;
            cBGLoadingThread = new CBGLoadingThread(activity, i);
            cBGLoadingThread.start();
        }
        this.mCSHandler = new CScoreHandler(cBGLoadingThread, this.mActivity);
        Message msg = Message.obtain(this.mCSHandler);
        this.mCSHandler.sendMessageDelayed(msg, 3000L);
    }

    public void fnPlayEndingEffect(Message endAnimEndMsg) {
        this.mGamePage.fnPlayEndingAnimation(endAnimEndMsg);
        Message endMsg = Message.obtain(this.mEffHandler, 100);
        endMsg.arg1 = EFF_AT_END;
        endMsg.sendToTarget();
    }

    public void fnAttachBall(CBall ball) {
        this.mGamePage.fnAttachBall(ball);
    }

    public void fnPlayHoleEffect(Message holeAnimEndMsg) {
        this.mGamePage.fnShowBall(View.INVISIBLE);
        Point HolePos = CL.HOLE_INDEX >= 0 ? CL.holes[CL.HOLE_INDEX] : null;
        this.mGamePage.fnPlayHoleAnimation(HolePos, holeAnimEndMsg);
        Message holeMsg = Message.obtain(this.mEffHandler, 100);
        holeMsg.arg1 = 200;
        holeMsg.sendToTarget();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fnAddViewIntoSwitcher(View aView) {
        this.mActivity.setContentView(aView);
    }

    private class EffectHandler extends Handler {
        private MediaPlayer mEndPlayer;
        private MediaPlayer mFinishPlayer;
        private MediaPlayer mHolePlayer;
        private Vibrator mVibrator;

        public EffectHandler() {
            this.mEndPlayer = MediaPlayer.create(CDispMgr.this.mActivity, R.raw.level_complete);
            this.mHolePlayer = MediaPlayer.create(CDispMgr.this.mActivity, R.raw.hole);
            this.mFinishPlayer = MediaPlayer.create(CDispMgr.this.mActivity, R.raw.game_complete);
            this.mVibrator = (Vibrator) CDispMgr.this.mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        }

        protected void finalize() {
            if (this.mEndPlayer != null) {
                this.mEndPlayer.release();
                this.mEndPlayer = null;
            }
            if (this.mHolePlayer != null) {
                this.mHolePlayer.release();
                this.mHolePlayer = null;
            }
            if (this.mFinishPlayer != null) {
                this.mFinishPlayer.release();
                this.mHolePlayer = null;
            }
            try {
                super.finalize();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    switch (msg.arg1) {
                        case 200:
                            this.mVibrator.vibrate(CU.VIBRATION_HOLE, -1);
                            this.mHolePlayer.start();
                            break;
                        case CDispMgr.EFF_AT_END /* 201 */:
                            this.mVibrator.vibrate(CU.VIBRATION_END, -1);
                            if (CU.LEVEL >= CU.LEVEL_COUNT) {
                                this.mFinishPlayer.start();
                                CU.GAME_OVER = true;
                                break;
                            } else {
                                this.mEndPlayer.start();
                                break;
                            }
                    }
            }
        }

        public void clearMemory() {
            this.mEndPlayer = null;
            this.mHolePlayer = null;
            this.mFinishPlayer = null;
            this.mVibrator = null;
        }
    }

    private class CScoreHandler extends Handler {
        private Activity mActivity;
        private Thread mThread;

        public CScoreHandler(Thread thread, Activity activity) {
            this.mThread = thread;
            this.mActivity = activity;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (this.mThread == null) {
                CDispMgr.this.fnAddViewIntoSwitcher(CDispMgr.this.mRankPage.fnCreateView());
                return;
            }
            try {
                this.mThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (this.mActivity instanceof CTeeterActivity) {
                CTeeterActivity teeterAct = (CTeeterActivity) this.mActivity;
                teeterAct.fnExternalGameFlow(4);
            }
        }
    }

    public void clearMemory() {
        if (this.mEffHandler != null) {
            this.mEffHandler.removeMessages(100);
            this.mEffHandler.clearMemory();
        }
        if (this.mCSHandler != null) {
            this.mCSHandler.removeMessages(0);
        }
        this.mGamePage.clearMemory();
        this.mScorePage.clearMemory();
        this.mRankPage.clearMemory();
        this.mActivity = null;
        this.mGamePage = null;
        this.mScorePage = null;
        this.mRankPage = null;
        this.mEffHandler = null;
        this.mCSHandler = null;
    }
}
