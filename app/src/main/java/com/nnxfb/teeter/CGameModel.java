package com.nnxfb.teeter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.nnxfb.teeter.utils.TTConstants;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/* loaded from: classes3.dex */
public class CGameModel {
    static final boolean $assertionsDisabled = false;
    public static final int BALL_INFO_CLEAR = 0;
    public static final int BALL_INFO_RELOAD = 1;
    public static final int START_CONTINUE = 3;
    public static final int START_NEWGAME = 1;
    public static final int START_NEWGAME_NEED_INIT = 2;
    public static final int START_NEXTLEVEL = 4;
    public static final int STATE_FINISH_GAME = 7;
    public static final int STATE_FINISH_LEVEL = 6;
    public static final int STATE_INITIALIZED = 2;
    public static final int STATE_NORMAL = 5;
    public static final int STATE_NOT_INITIALIZED = 1;
    public static final int STATE_PAUSED = 8;
    public static final int STATE_STOPPED = 4;
    public static final int STATE_UNDIFINED = 0;
    public static final int STATE_UNINITIALIZED = 3;
    private static final String STORAGE_FILENAME = "current.state";
    private CTeeterActivity mActivity;
    private CBall mBall;
    private CDispMgr mDispMgr;
    private CLoadingHandler mLHandler;
    private ReDrawTask mTTask;
    private CBGLoadingThread mThread;
    private Timer mTimer;
    private int locker = 3;
    private int mGameState = 1;
    private boolean mIsFileSaved = false;
    private Point mBallPos = new Point(0, 0);
    private Vector mVelocity = new Vector(0, 0);
    private Vector mAccelerate = new Vector(0, 0);
    private CGameHandler mHandler = new CGameHandler();

    @SuppressLint("HandlerLeak")
    public class CGameHandler extends Handler {
        public CGameHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (CGameModel.this.mBall != null) {
                        int state = CGameModel.this.mBall.fnCheckStatus();
                        switch (state) {
                            case 0:
                                CGameModel.this.mDispMgr.fnInvalidate();
                                break;
                            case 1:
                                CGameModel.this.lockTimer("CGameModel-STATE_AT_HOLE");
                                CGameModel.this.fnStop();
                                Message holeAnimEndMsg = Message.obtain(this, 4);
                                CGameModel.this.mDispMgr.fnPlayHoleEffect(holeAnimEndMsg);
                                CS.sfnFallInHole();
                                break;
                            case 2:
                                CU.TOUCHABLE = CGameModel.$assertionsDisabled;
                                CGameModel.this.mGameState = 6;
                                CGameModel.this.lockTimer("CGameModel-STATE_AT_END");
                                CGameModel.this.fnStop();
                                Message playEndMsg = Message.obtain(this, 2);
                                CGameModel.this.mDispMgr.fnPlayEndingEffect(playEndMsg);
                                break;
                        }
                    }
                    break;
                case 2:
                    if (CGameModel.this.mBall != null) {
                        CGameModel.this.mBall.fnReset(CL.begin, new Vector(0, 0), new Vector(0, 0));
                        CGameModel.this.mDispMgr.fnShowScorePage();
                        break;
                    }
                    break;
                case 4:
                    CGameModel.this.fnResetBallInfo(1);
                    CGameModel.this.fnStart(CU.LEVEL, 3);
                    break;
            }
        }
    }

    private class ReDrawTask extends TimerTask {
        private Message mMsg;

        private ReDrawTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            this.mMsg = Message.obtain(CGameModel.this.mHandler, 1);
            this.mMsg.sendToTarget();
        }
    }

    public CGameModel(Activity activity) {
        this.mActivity = (CTeeterActivity) activity;
        this.mDispMgr = new CDispMgr(this.mActivity);
    }

    public boolean fnInitialize() {
        this.mGameState = 2;
        boolean isFileExisted = fnIsSavedFileExisted();
        Log.d("FileIO", " isFileExisted = "+isFileExisted);
        if (isFileExisted) {
            lockTimer("CGameModel-fnInitialize()-fnIsSavedFileExisted");
            fnExtractSavedFile();
            this.mIsFileSaved = false;
        }
        this.mThread = new CBGLoadingThread(this.mActivity, CU.LEVEL);
        this.mThread.start();
        this.mLHandler = new CLoadingHandler(this.mThread, true);
        unlockTimer("CGameModel-fnInitialize()");
        CU.TIMER_GO = true;
        Message msg = Message.obtain(this.mLHandler, 0);
        this.mLHandler.sendMessageAtFrontOfQueue(msg);
        try {
            this.mLHandler.mThread.join();
            return isFileExisted;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void clearMemory() {
        Log.d("FileIO", "GameModel.clearMemory called");
        Log.d("FileIO", "mGameState="+this.mGameState);
        if (this.mGameState == 7) {
            fnResetBallInfo(0);
            boolean exists = fnIsSavedFileExisted();
            if (exists) {
                this.mActivity.deleteFile(STORAGE_FILENAME);
            }
        } else {
            this.mGameState = 3;
            Log.d("FileIO", "mIsFileSaved="+this.mIsFileSaved);
            if (!this.mIsFileSaved) {
                this.mIsFileSaved = true;
                fnSaveGameState();
            }
        }
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
        if (this.mTTask != null) {
            this.mTTask.cancel();
            this.mTTask = null;
        }
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
        }
        if (this.mLHandler != null) {
            this.mLHandler.removeMessages(0);
        }
        if (this.mBall != null) {
            this.mBall.clearMemory();
        }
        this.mBall = null;
        if (this.mDispMgr != null) {
            this.mDispMgr.clearMemory();
        }
        this.mDispMgr = null;
        this.mActivity = null;
        this.mBallPos = null;
        this.mVelocity = null;
        this.mAccelerate = null;
        this.mHandler = null;
        this.mThread = null;
    }

    public boolean fnStart(int level, int nStartCode) {
        System.gc();
        switch (nStartCode) {
            case 1:
                fnResetBallInfo(1);
                CS.sfnReset();
                break;
            case 2:
                CS.sfnReset();
                this.mThread = new CBGLoadingThread(this.mActivity, CU.LEVEL);
                this.mThread.start();
                this.mLHandler = new CLoadingHandler(this.mThread, false);
                Message msg = Message.obtain(this.mLHandler, 0);
                this.mLHandler.sendMessageAtFrontOfQueue(msg);
                break;
            case 3:
                unlockTimer("CGameModel-START_CONTINUE()");
                fnStartInternal();
                break;
            case 4:
                fnResetBallInfo(1);
                this.mDispMgr.fnShowGamePage();
                unlockTimer("CGameModel-START_NEXTLEVEL()");
                fnStartInternal();
                break;
            default:
                throw new AssertionError();
        }
        CU.TOUCHABLE = true;
        return true;
    }

    public void lockTimer(String name) {
        this.locker++;
        Log.i(TTConstants.TEETER, "Locker = " + this.locker + " lock by " + name);
    }

    public void unlockTimer(String name) {
        this.locker--;
        Log.i(TTConstants.TEETER, "Locker = " + this.locker + " unlock by " + name);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean fnStartInternal() {
        if (this.locker > 0) {
            return true;
        }
        this.locker = 0;
        this.mGameState = 5;
        if (this.mTimer == null) {
            this.mTimer = new Timer();
            this.mTTask = new ReDrawTask();
            this.mTimer.schedule(this.mTTask, 1000L, 30L);
        }
        this.mBall.fnStart(this.mBallPos, this.mVelocity, this.mAccelerate);
        this.mDispMgr.fnAttachBall(this.mBall);
        CS.sfnBeginLevel();
        return true;
    }

    public boolean fnStop() {
        fnStopInternal();
        return true;
    }

    public void stopSensor() {
        if (this.mBall != null) {
            this.mBall.stopSensor();
        }
    }

    private boolean fnStopInternal() {
        if (this.mBall != null && this.mGameState != 3) {
            this.mBall.fnGetCenter(this.mBallPos);
            this.mBall.fnGetVelocity(this.mVelocity);
            this.mBall.fnGetAccelerate(this.mAccelerate);
            this.mBall.fnStop();
        }
        if (this.mGameState != 7 && this.mGameState != 6 && this.mGameState != 8) {
            this.mGameState = 4;
        }
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
            try {
                if (this.mHandler != null) {
                    this.mHandler.removeMessages(1);
                }
                if (this.mLHandler != null) {
                    this.mLHandler.removeMessages(0);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (this.mTTask != null) {
            this.mTTask.cancel();
            this.mTTask = null;
        }
        if (this.mGameState != 7 && this.mGameState != 6) {
            CS.sfnPauseRecord();
        } else {
            CS.sfnEndLevel();
        }
        return true;
    }

    public int fnGetGameState() {
        return this.mGameState;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fnResetBallInfo(int mode) {
        if (this.mBallPos == null) {
            this.mBallPos = new Point();
        }
        if (mode == 0) {
            this.mBallPos.set(0, 0);
        } else if (mode == 1) {
            this.mBallPos.set(CL.begin.x, CL.begin.y);
        }
        if (this.mVelocity == null) {
            this.mVelocity = new Vector();
        }
        this.mVelocity.set(0, 0);
        if (this.mAccelerate == null) {
            this.mAccelerate = new Vector();
        }
        this.mAccelerate.set(0, 0);
    }

    public void fnSaveGameState() {
        try {
            FileOutputStream fos = this.mActivity.openFileOutput(STORAGE_FILENAME, Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeInt(CU.LEVEL);
            Log.d("FileIO", "write successful ");
            dos.writeBoolean(CS.sfnGetPausedState());
            dos.writeLong(CS.sfnGetLevelTime());
            dos.writeLong(CS.sfnGetTotalTime());
            dos.writeInt(CS.sfnGetLevelAttempt());
            dos.writeInt(CS.sfnGetTotalAttempt());
            dos.writeInt(this.mBallPos.x);
            dos.writeInt(this.mBallPos.y);
            dos.writeInt(this.mVelocity.x);
            dos.writeInt(this.mVelocity.y);
            dos.writeInt(this.mAccelerate.x);
            dos.writeInt(this.mAccelerate.y);
            dos.writeInt(this.mGameState);
            dos.flush();
            dos.close();
            fos.close();
        } catch (IOException e) {
            Log.d("FileIO", "write error ");
            throw new RuntimeException(e);
        }
    }

    public boolean fnExtractSavedFile() {
        try {
            FileInputStream fis = this.mActivity.openFileInput(STORAGE_FILENAME);
            DataInputStream dis = new DataInputStream(fis);
            CU.LEVEL = dis.readInt();
            CS.sfnSetPausedState(dis.readBoolean());
            CS.sfnSetLevelTime(dis.readLong());
            CS.sfnSetTotalTime(dis.readLong());
            CS.sfnSetLevelAttempt(dis.readInt());
            CS.sfnSetTotalAttempt(dis.readInt());
            this.mBallPos.set(dis.readInt(), dis.readInt());
            this.mVelocity.set(dis.readInt(), dis.readInt());
            this.mAccelerate.set(dis.readInt(), dis.readInt());
            this.mGameState = dis.readInt();
            Log.d("FileIO", "read: successfully");
            return true;
        } catch (IOException e) {
            Log.e("FileIO", "read error: " + e);
            return false;
        }
    }

    public boolean fnIsSavedFileExisted() {
        try {
            FileInputStream fis = this.mActivity.openFileInput(STORAGE_FILENAME);
            fis.close();
            return true;
        } catch (IOException e) {
            Log.d("FileIO", "fnIsSavedFileExisted "+e);
            return false;
        }
    }

    private class CLoadingHandler extends Handler {
        private Thread mThread;
        private boolean newBall;

        public CLoadingHandler(Thread thread, boolean newBall) {
            this.mThread = thread;
            this.newBall = newBall;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (this.mThread != null) {
                try {
                    this.mThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                CGameModel.this.mDispMgr.fnShowGamePage();
                if (this.newBall && CGameModel.this.mBall == null) {
                    CGameModel.this.mBall = new CBall(CGameModel.this.mActivity);
                }
                if (!this.newBall) {
                    CGameModel.this.fnResetBallInfo(1);
                }
                CGameModel.this.unlockTimer("CGameModel-CLoadingHandler");
                if (CU.TIMER_GO) {
                    CU.TIMER_GO = CGameModel.$assertionsDisabled;
                    CGameModel.this.fnStartInternal();
                }
            }
        }
    }

    public void gameFinish() {
        this.mGameState = 7;
    }

    public void gamePause() {
        this.mGameState = 8;
    }
}
