package com.nnxfb.teeter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.nnxfb.teeter.utils.LogUtils;
import com.nnxfb.teeter.utils.TTConstants;

public class CCoverActivity extends Activity {
    private static final String TAG = "CCoverActivity";
    private boolean hasSensor;
    private ImageView ivBG;
    private ImageView ivBall;
    private ConstraintLayout splashLayout;
    Thread mDataLoaderThread;
    private MediaPlayer mEndPlayer;
    private AnimationDrawable mHoleDrw;
//    private LinearLayout mMainView;
    private boolean onPause;
    private SensorManager sm;
    private boolean startGame;
    private final int NO_SENSOR = 1;
    private final int GAME_START = 1;
    private final int GAME_EXIT = 2;
    private final int GAME_ENTER = 3;

    private final int ANIM_LEFT_MARGIN = 237;
    private final int ANIM_TOP_MARGIN = 615;
    private final int ANIM_WIDTH = 220;
    private final int ANIM_HEIGHT = 128;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GAME_START:
                    if (CCoverActivity.this.mHoleDrw == null) {
                        Log.d(CCoverActivity.TAG, "mHoleDrw is null");
                    } else {
                        Log.d(TAG, "handleMessage: start");
                        ivBall.setVisibility(ImageView.VISIBLE);
                        mHoleDrw.start();
                        new Handler().postDelayed(new Runnable() {
                            @Override // java.lang.Runnable
                            public void run() {
                                if (mHoleDrw.getCurrent() == mHoleDrw.getFrame(30) && mEndPlayer != null) {
                                    mEndPlayer.start();
                                }
                                if (mHoleDrw.getCurrent() == mHoleDrw.getFrame(59)) {
                                    fadeout();
                                } else {
                                    new Handler().postDelayed(this, 25L);
                                }
                            }
                        }, 25L);
                    }
                    break;
                case GAME_EXIT:
                    CCoverActivity.this.finish();
                    break;
                case GAME_ENTER:
                    CCoverActivity.this.gameStart();
                    break;
            }
        }
    };
    private SensorEventListener sl = new SensorEventListener() {
        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent values) {
        }
    };

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        System.gc();
        this.hasSensor = checkHasSensor();
        if (!this.hasSensor) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            this.mHandler.sendEmptyMessageDelayed(GAME_EXIT, 5000L);
            showDialog(NO_SENSOR);
            return;
        }
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        int large,small;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        if (dm.widthPixels > dm.heightPixels) {
            large = dm.widthPixels;
            small = dm.heightPixels;
        } else {
            small = dm.widthPixels;
            large = dm.heightPixels;
        }
        CU.SCREEN_WIDTH = large;
        CU.SCREEN_HEIGHT = small;
        CU.RATIO = Math.min((float) large / CU.DESIGN_WIDTH, (float) small / CU.DESIGN_HEIGHT);
        CU.OFFSET_X = (int) ((large - CU.DESIGN_WIDTH * CU.RATIO) / 2);
        CU.OFFSET_Y = (int) ((small - CU.DESIGN_HEIGHT * CU.RATIO) / 2);

        setContentView(R.layout.splash);

        this.splashLayout = findViewById(R.id.splash);
        this.ivBG = findViewById(R.id.splash_bg);
        this.ivBall = findViewById(R.id.splash_anim);
        ConstraintLayout.LayoutParams animParams = (ConstraintLayout.LayoutParams) ivBall.getLayoutParams();
        animParams.width = (int) (ANIM_WIDTH * CU.RATIO);
        animParams.height = (int) (ANIM_HEIGHT * CU.RATIO);
        animParams.leftMargin = (int) (ANIM_LEFT_MARGIN * CU.RATIO);
        animParams.topMargin = (int) (ANIM_TOP_MARGIN * CU.RATIO);
        this.ivBall.setLayoutParams(animParams);
        this.mHoleDrw = (AnimationDrawable) ivBall.getDrawable();
        this.mEndPlayer = MediaPlayer.create(this, R.raw.level_complete);

        this.mDataLoaderThread = new Thread(() -> {
            if (hasSensor) {
                mHandler.sendEmptyMessageDelayed(1, 500L);
            }
        });
        this.mDataLoaderThread.start();
        this.onPause = false;
        this.startGame = false;
        LogUtils.toQualityBoard(TAG, true, "onCreate");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        this.onPause = false;
        LogUtils.toQualityBoard(TAG, true, "onResume");
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onWindowFocusChanged(boolean focus) {
        if (focus && this.startGame) {
            this.startGame = false;
            this.mHandler.sendEmptyMessageDelayed(3, 200L);
        } else if (!focus) {
            this.mHandler.removeMessages(3);
        }
        Log.d(TAG, "focus:"+focus+" started:"+this.startGame);
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        this.onPause = true;
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        this.sm.unregisterListener(this.sl);
        this.sm = null;
        this.sl = null;
        try {
            try {
                if (this.mDataLoaderThread != null) {
                    this.mDataLoaderThread.join();
                }
                if (this.hasSensor) {
                    this.splashLayout.removeAllViews();
//                    this.mMainView.removeAllViews();
                }
                this.splashLayout = null;
//                this.mMainView = null;
                this.ivBG = null;
                this.ivBall = null;
                this.mHoleDrw = null;
                this.mEndPlayer = null;
                this.mDataLoaderThread = null;
                super.onDestroy();
            } catch (InterruptedException e) {
                if (this.hasSensor) {
                    this.splashLayout.removeAllViews();
//                    this.mMainView.removeAllViews();
                }
                this.splashLayout = null;
//                this.mMainView = null;
                this.ivBG = null;
                this.ivBall = null;
                this.mHoleDrw = null;
                this.mEndPlayer = null;
                this.mDataLoaderThread = null;
                super.onDestroy();
                throw new RuntimeException(e);
            }
            LogUtils.toQualityBoard(TAG, true, "onCreate");
        } catch (Throwable th) {
            if (this.hasSensor) {
                if (this.splashLayout != null) {
                    this.splashLayout.removeAllViews();
                }
            }
            this.splashLayout = null;
            this.ivBG = null;
            this.ivBall = null;
            this.mHoleDrw = null;
            this.mEndPlayer = null;
            this.mDataLoaderThread = null;
            super.onDestroy();
            throw th;
        }
    }

    public void gameStart() {
        Intent next = new Intent();
        next.setClass(this, CTeeterActivity.class);
        startActivity(next);
        finish();
    }

    public class CAnimDra extends AnimationDrawable {
        private int mPlayingCount = 0;

        public CAnimDra() {
        }

        @Override // android.graphics.drawable.AnimationDrawable, java.lang.Runnable
        public void run() {
            super.run();
            if (this.mPlayingCount == 30 && CCoverActivity.this.mEndPlayer != null) {
                CCoverActivity.this.mEndPlayer.start();
            }
            if (this.mPlayingCount == 59) {
                CCoverActivity.this.fadeout();
            }
            this.mPlayingCount++;
        }
    }

    public void fadeout() {
        Animation an2 = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        an2.setDuration(1000L);
        an2.setStartOffset(500L);
        an2.setFillAfter(true);
        an2.setAnimationListener(new Animation.AnimationListener() {
            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
                if (!CCoverActivity.this.onPause) {
                    CCoverActivity.this.gameStart();
                } else {
                    CCoverActivity.this.startGame = true;
                }
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationRepeat(Animation animation) {
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationStart(Animation animation) {
            }
        });
        if (this.splashLayout != null) {
            this.splashLayout.startAnimation(an2);
        }
    }

    private boolean checkHasSensor() {
        this.sm = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        boolean sensorEnable = this.sm.registerListener(this.sl, this.sm.getDefaultSensor(1), 1);
        Log.e(TTConstants.TEETER, "registerListener return = " + sensorEnable);
        return sensorEnable;
    }

    @Override // android.app.Activity
    protected Dialog onCreateDialog(int id) {
        if (id != 1) {
            return null;
        }
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
        aBuilder.setTitle(R.string.htc_private_app);
        aBuilder.setCancelable(false);
        aBuilder.setMessage(R.string.str_no_sensor);
        aBuilder.setPositiveButton(R.string.str_btn_quit, (dialog, whichButton) -> CCoverActivity.this.finish());
        return aBuilder.create();
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == 4) {
            return super.dispatchKeyEvent(event);
        }
        return true;
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return true;
    }
}
