package com.nnxfb.teeter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.XmlResourceParser;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.nnxfb.teeter.utils.TTConstants;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

public class CTeeterActivity extends Activity {
    static final boolean $assertionsDisabled = true;
    private static final int MSG_OFF = 1;
    private static final int MSG_ON = 0;
    private AudioManager mAudioManager;
    private CGameModel mGame;
    private LightHandler mHandler;
//    private PowerManager mPowerManager;
    private final String TAG = TTConstants.TEETER;
    private final int DIALOG_PAUSE_DEFAULT_OPTION = 291;
    private final int DIALOG_RESTART_DEFAULT_OPTION = 1110;
    private final int DIALOG_RESTART_OPTION = 1929;
    private int password=0;
    @SuppressLint("HandlerLeak")
    private Handler mDialogHandler = new Handler() {
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DIALOG_PAUSE_DEFAULT_OPTION:
                    CTeeterActivity.this.mGame.fnStart(CU.LEVEL, 3);
                    Log.e(TTConstants.TEETER, "DIALOG_PAUSE_DEFAULT_OPTION");
                    break;
                case DIALOG_RESTART_DEFAULT_OPTION:
                    CTeeterActivity.this.mGame.fnStart(CU.LEVEL, 3);
                    Log.e(TTConstants.TEETER, "DIALOG_RESTART_DEFAULT_OPTION");
                    break;
                case DIALOG_RESTART_OPTION:
                    CU.LEVEL = 1;
                    CU.GAME_OVER = CTeeterActivity.$assertionsDisabled;
                    CU.TIMER_GO = true;
                    CTeeterActivity.this.mGame.fnStart(CU.LEVEL, 2);
                    break;
            }
        }
    };

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        CU.GAME_OVER = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("GAME_OVER", $assertionsDisabled);
        System.gc();

        try {
            fnLoadConfig();
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }
        this.mGame = new CGameModel(this);
        this.mAudioManager = (AudioManager) getSystemService(Activity.AUDIO_SERVICE);
        this.mGame.fnInitialize();
        this.mHandler = new LightHandler();
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        finish();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
        edit.putBoolean("GAME_OVER", CU.GAME_OVER);
        edit.commit();
        this.mGame.clearMemory();
        this.mGame = null;
        this.mAudioManager = null;
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(0);
        }
        this.mHandler = null;
        CL.clear();
        if (CU.BG_BMP != null) {
            CU.BG_BMP.recycle();
        }
        CU.BG_BMP = null;
        CBGLoadingThread.clearMemory();
        super.onDestroy();
        new Thread() {
            @Override
            public void run() {
                Process.killProcess(Process.myPid());
            }
        }.start();
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(mGame.fnIsSavedFileExisted()) {
            mGame.fnExtractSavedFile();
        }
        this.mGame.unlockTimer("Activity-onResume");
        if (this.mGame.fnGetGameState() == 2) {
            this.mGame.fnStart(CU.LEVEL, 1);
            return;
        }
        if (this.mGame.fnGetGameState() == 6) {
            this.mGame.fnStart(CU.LEVEL, 4);
            return;
        }
        if (this.mGame.fnGetGameState() == 4 || this.mGame.fnGetGameState() == 3 || this.mGame.fnGetGameState() != 8) {
            showDialog(3);
        } else if (!$assertionsDisabled) {
            throw new AssertionError();
        }
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        this.mGame.lockTimer("Activity-onPause");
        this.mGame.stopSensor();
        this.mGame.fnStop();
        this.mGame.fnSaveGameState();
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
    }

    @Override // android.app.Activity
    protected Dialog onCreateDialog(int id) {
        Message defaultOption = new Message();
        switch (id) {
            case 1:
                AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
                aBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                aBuilder.setTitle(R.string.htc_private_app);
                aBuilder.setMessage(R.string.str_msg_quit);
                aBuilder.setPositiveButton(R.string.str_btn_yes, (dialogInterface, i) -> finish());
//                if (CU.DEBUG) {
                    aBuilder.setNeutralButton(R.string.str_btn_jump, (dialogInterface, i) -> {
                        CU.TIMER_GO = true;
                        CGameModel cGameModel = this.mGame;
                        int l = CU.LEVEL + 1;
                        CU.LEVEL = l;
                        cGameModel.fnStart(l, 2);
                    });
//                }
                aBuilder.setNegativeButton(R.string.str_btn_no, (dialogInterface, i) -> this.mGame.fnStart(CU.LEVEL, 3));
                AlertDialog create = aBuilder.create();
                defaultOption.what = DIALOG_PAUSE_DEFAULT_OPTION;
                defaultOption.setTarget(this.mDialogHandler);
                create.setCancelMessage(defaultOption);
                return create;
            case 2:
                return null;
            case 3:
                AlertDialog.Builder aBuilder2 = new AlertDialog.Builder(this);
                aBuilder2.setIcon(android.R.drawable.ic_dialog_alert);
                aBuilder2.setTitle(R.string.htc_private_app);
                aBuilder2.setMessage(R.string.str_msg_continue);
                aBuilder2.setPositiveButton(R.string.str_btn_resume,
                        (dialog, whichButton) -> CTeeterActivity.this.mGame.fnStart(CU.LEVEL, 3));
                aBuilder2.setNegativeButton(R.string.str_btn_restart, (dialog, whichButton) -> {
                    CU.LEVEL = 1;
                    CU.GAME_OVER = CTeeterActivity.$assertionsDisabled;
                    Log.d("FileIO", "(onCreateDlg3) CU.GAME_OVER = "+CU.GAME_OVER);
                    CU.TIMER_GO = true;
                    CTeeterActivity.this.mGame.fnStart(CU.LEVEL, 2);
                });
                AlertDialog create2 = aBuilder2.create();
                if (CU.GAME_OVER) {
                    defaultOption.what = DIALOG_RESTART_OPTION;
                } else {
                    defaultOption.what = DIALOG_RESTART_DEFAULT_OPTION;
                }
                defaultOption.setTarget(this.mDialogHandler);
                create2.setCancelMessage(defaultOption);
                return create2;
            default: return null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (id == 1) {
            AlertDialog alertDialog = (AlertDialog) dialog;
            if (CU.DEBUG) {
                alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
            } else {
                alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.GONE);
            }
        }
        super.onPrepareDialog(id, dialog);
    }

    @Override // android.app.Activity
    public boolean onTouchEvent(MotionEvent event) {
        if (CU.TOUCHABLE) {
            switch (event.getAction()) {
                case 0:
                    float touchX =  event.getX();
                    float touchY =  event.getY();
                    if(touchX > CU.PWD_LL && touchY > CU.PWD_TT && touchX < CU.PWD_LR && touchY < CU.PWD_TB) {
                        password = 1;
                        Toast.makeText(this, "Teeter", Toast.LENGTH_SHORT).show();
                    } else if (touchX > CU.PWD_RL && touchY > CU.PWD_BT && touchX < CU.PWD_RR && touchY < CU.PWD_BB) {
                        if(password != 1) password=0;
                        else password++;
                    } else if (touchX > CU.PWD_RL && touchY > CU.PWD_TT && touchX < CU.PWD_RR && touchY < CU.PWD_TB) {
                        if(password != 2) password=0;
                        else password++;
                    } else if (touchX > CU.PWD_LL && touchY > CU.PWD_BT && touchX < CU.PWD_LR && touchY < CU.PWD_BB) {
                        if(password != 3) password=0;
                        else password++;
                        if(password == 4){
                            CU.DEBUG = !CU.DEBUG;
                            if (CU.DEBUG) {
                                Toast.makeText(this, "Debug ON", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Debug OFF", Toast.LENGTH_SHORT).show();
                            }
                            password=0;
                        }
                    } else if (CU.DEBUG && touchX > CU.HOLE_DBG_L && touchY > CU.HOLE_DBG_T && touchX < CU.HOLE_DBG_R && touchY < CU.HOLE_DBG_B ) {
                        password=0;
                        CU.HOLE_ON = !CU.HOLE_ON;
                        if (CU.HOLE_ON) {
                            Toast.makeText(this, "Hole ON", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Hole OFF", Toast.LENGTH_SHORT).show();
                        }
                    } else if (CU.DEBUG && touchX > CU.END_DBG_L && touchY > CU.END_DBG_T && touchX < CU.END_DBG_R && touchY < CU.END_DBG_B ) {
                        password=0;
                        CU.END_ON = !CU.END_ON;
                        if (CU.END_ON) {
                            Toast.makeText(this, "End ON", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "End OFF", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        password=0;
                        this.mGame.fnStop();
                        this.mGame.lockTimer("Activity-onTouchEvent");
                        this.mGame.gamePause();
                        showDialog(1);
                    }
                    break;
            }
        }
        return CU.TOUCHABLE;
    }

    private class LightHandler extends Handler {
        private LightHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    CTeeterActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    break;
                case 1:
                    CTeeterActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    break;
            }
        }
    }

    public void turnLight(boolean on) {
        Message msg = Message.obtain(this.mHandler);
        if (on) {
            msg.what = 0;
        } else {
            msg.what = 1;
        }
        msg.sendToTarget();
    }

    public void fnExternalGameFlow(int nCase) {
        switch (nCase) {
            case 2:
                CU.LEVEL = 1;
                CU.GAME_OVER = $assertionsDisabled;
                Log.d("FileIO", "(externGameFlow2) CU.GAME_OVER = "+CU.GAME_OVER);
                CU.TIMER_GO = true;
                this.mGame.fnStart(CU.LEVEL, 2);
                return;
            case 3:
                CU.LEVEL = 1;
                CU.GAME_OVER = $assertionsDisabled;
                Log.d("FileIO", "(externGameFlow3) CU.GAME_OVER = "+CU.GAME_OVER);
                this.mGame.gameFinish();
                CS.sfnReset();
                finish();
                return;
            case 4:
                this.mGame.fnStart(CU.LEVEL, 4);
                return;
            default:
                if (!$assertionsDisabled) {
                    throw new AssertionError();
                }
                return;
        }
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d("FileIO", "focused:"+hasFocus);
        if (hasFocus) {
            if (this.mGame.fnGetGameState() == 3 || this.mGame.fnGetGameState() == 4) {
                showDialog(3);
            } else if (this.mGame.fnGetGameState() == 8) {
                showDialog(1);
            }
        }
        super.onWindowFocusChanged(hasFocus);
    }

    private void fnLoadConfig() throws IOException, XmlPullParserException {

        XmlResourceParser xrp = getResources().getXml(R.xml.config_flyer_wwe);
        while (xrp.next() != 2) {
        }
        xrp.next();
        while (xrp.getEventType() != 3) {
            while (xrp.getEventType() != 2) {
                if (xrp.getEventType() != 1) {
                    xrp.next();
                } else {
                    return;
                }
            }
            if (xrp.getName().equals("level")) {
                CU.LEVEL_COUNT = xrp.getAttributeIntValue(0, 0);
            } else if (xrp.getName().equals("ball")) {
                CU.BALL_RADIUS = xrp.getAttributeIntValue(0, 0);
                CU.BALL_RADIUS_BIG = CU.S2B(CU.BALL_RADIUS);
            } else if (xrp.getName().equals("hole")) {
                CU.HOLE_RADIUS = xrp.getAttributeIntValue(0, 0);
            } else if (xrp.getName().equals("ending")) {
                CU.END_RADIUS = xrp.getAttributeIntValue(0, 0);
            } else if (xrp.getName().equals("gravity")) {
                CU.GRAVITY_FACTOR = xrp.getAttributeIntValue(0, 0);
            } else if (xrp.getName().equals("friction")) {
                CU.FRICTION_FACTOR = xrp.getAttributeIntValue(0, 0);
            } else if (xrp.getName().equals("bounce_rate")) {
                CU.BOUNCE_RATE = xrp.getAttributeFloatValue(0, 0.0f);
            } else if (xrp.getName().equals("speed_limie")) {
                CU.MAX_SPEED = xrp.getAttributeIntValue(0, 0);
            } else if (xrp.getName().equals("vibrate_speed")) {
                CU.VIBRATION_ACTIVE_SPEED = xrp.getAttributeIntValue(0, 0);
                CU.VIBRATION_ACTIVE_SPEED_GROUND = CU.VIBRATION_ACTIVE_SPEED / 2;
            } else if (xrp.getName().equals("v_hit")) {
                CU.VIBRATION_DURATION = xrp.getAttributeIntValue(0, 0);
            } else if (xrp.getName().equals("v_hole")) {
                int count = xrp.getAttributeIntValue(0, 0);
                CU.VIBRATION_HOLE = new long[count];
                xrp.next();
                for (int i = 0; i < count; i++) {
                    if (xrp.getName().equals("pattern")) {
                        CU.VIBRATION_HOLE[i] = xrp.getAttributeIntValue(0, 0);
                        xrp.next();
                        xrp.next();
                    }
                }
            } else if (xrp.getName().equals("v_ending")) {
                int count2 = xrp.getAttributeIntValue(0, 0);
                CU.VIBRATION_END = new long[count2];
                xrp.next();
                for (int i2 = 0; i2 < count2; i2++) {
                    if (xrp.getName().equals("pattern")) {
                        CU.VIBRATION_END[i2] = xrp.getAttributeIntValue(0, 0);
                        xrp.next();
                        xrp.next();
                    }
                }
            }
            while (xrp.getEventType() != 3) {
                xrp.next();
            }
            xrp.next();
        }
        xrp.close();
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean onSearchRequested() {
        return $assertionsDisabled;
    }
}
