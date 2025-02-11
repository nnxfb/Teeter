package com.nnxfb.teeter;

import android.graphics.Bitmap;

/* loaded from: classes3.dex */
public class CU {
    public static float RATIO;
    public static final int DESIGN_WIDTH = 960;
    public static final int DESIGN_HEIGHT = 540;
    public static int OFFSET_X;
    public static int OFFSET_Y;


    public static final int BALL_POS_DURATION = 30;
    public static final float BALL_RATIO = 1.53f;
    public static final int CONTACT_BOTTOM = 8;
    public static final int CONTACT_CORNER = 16;
    public static final int CONTACT_LEFT = 1;
    public static final int CONTACT_RIGHT = 2;
    public static final int CONTACT_TOP = 4;
    public static final int DELTA_TIME = 2;
    public static final int DLG_CONTINUE_ABORT = 1;
    public static final int DLG_FINISH = 2;
    public static final int DLG_NEWGAME_CONTINUE = 3;
    public static final float END_ANIM_RATIO = 2.13f;
    public static final float END_RATIO = 1.3f;
    public static final int GAME_INIT_NEXT_LEVEL = 2;
    public static final int GAME_NOT_START = 0;
    public static final int GAME_RUNNING = 1;
    public static final float HOLE_ANIM_RATIO = 1.333f;
    public static final float HOLE_RATIO = 1.1f;
    public static final int INTENT_REQ_SHOW_RANK = 0;
    public static final int MIN_ANGLE = 90;
    public static final int MSG_ANIM_END = 2;
    public static final int MSG_ANIM_HOLE = 4;
    public static final int MSG_EFFECT = 3;
    public static final int MSG_REDRAW = 1;
    public static final int REDRAW_DURATION = 30;
    public static final int RESTART_DELAY = 1000;
    public static final int RESULT_FINISH_LEVEL = 4;
    public static final int RESULT_LEAVE_GAME = 3;
    public static final int RESULT_NEWGAME_NEED_INITIALIZE = 2;
    public static final int SCALE_BIT = 10;
    public static final int STATE_AT_END = 2;
    public static final int STATE_AT_HOLE = 1;
    public static final int STATE_NORMAL = 0;
    public static final int STATE_PAUSE = 3;
    public static final int VIBRATION_ACTIVE_DELAY = 100;
    public static int VIBRATION_ACTIVE_SPEED;
    public static int VIBRATION_ACTIVE_SPEED_GROUND;
    public static int VIBRATION_DURATION;
    public static long[] VIBRATION_END;
    public static long[] VIBRATION_HOLE;
    public static int BALL_RADIUS = 0;
    public static int BALL_RADIUS_BIG = 0;
    public static Bitmap BG_BMP = null;
    public static float BOUNCE_RATE = 0.0f;
    public static int END_RADIUS = 0;
    public static int FRICTION_FACTOR = 0;
    public static int GRAVITY_FACTOR = 0;
    public static int HOLE_RADIUS = 0;
    public static int LEVEL_COUNT = 0;
    public static int MAX_SPEED = 0;
    public static int SCREEN_HEIGHT = 0;
    public static int SCREEN_WIDTH = 0;
    public static boolean DEBUG = false;
    public static boolean GAME_OVER = false;
    public static boolean TOUCHABLE = true;
    public static boolean TIMER_GO = false;
    public static boolean HOLE_ON = true;
    public static boolean END_ON = true;
    public static int LEVEL = 1;

    public static int PWD_X = 5;
    public static int PWD_Y = 240;
    public static int PWD_SIZE = 140;
    public static int PWD_SEP = 30;
    public static int PWD_LL = PWD_X;
    public static int PWD_TT = PWD_Y;
    public static int PWD_LR = PWD_LL + PWD_SIZE;
    public static int PWD_TB = PWD_TT + PWD_SIZE;
    public static int PWD_RL = PWD_LR + PWD_SEP;
    public static int PWD_RR = PWD_RL + PWD_SIZE;
    public static int PWD_BT = PWD_TB + PWD_SEP;
    public static int PWD_BB = PWD_BT + PWD_SIZE;

    public static int HOLE_DBG_X = 0;
    public static int HOLE_DBG_Y = 100;
    public static int HOLE_DBG_SIZE = 100;
    public static int HOLE_DBG_TEXT_X_OFFSET = 0;
    public static int HOLE_DBG_TEXT_Y_OFFSET = 70;
    public static int HOLE_DBG_L = HOLE_DBG_X;
    public static int HOLE_DBG_T = HOLE_DBG_Y;
    public static int HOLE_DBG_R = HOLE_DBG_L + HOLE_DBG_SIZE;
    public static int HOLE_DBG_B = HOLE_DBG_T + HOLE_DBG_SIZE;
    public static int HOLE_DBG_TEXT_X = HOLE_DBG_X + HOLE_DBG_TEXT_X_OFFSET;
    public static int HOLE_DBG_TEXT_Y = HOLE_DBG_Y + HOLE_DBG_TEXT_Y_OFFSET;

    public static int END_DBG_X = 220;
    public static int END_DBG_Y = 100;
    public static int END_DBG_SIZE = 100;
    public static int END_DBG_TEXT_X_OFFSET = 15;
    public static int END_DBG_TEXT_Y_OFFSET = 70;
    public static int END_DBG_L = END_DBG_X;
    public static int END_DBG_T = END_DBG_Y;
    public static int END_DBG_R = END_DBG_L + END_DBG_SIZE;
    public static int END_DBG_B = END_DBG_T + END_DBG_SIZE;
    public static int END_DBG_TEXT_X = END_DBG_X + END_DBG_TEXT_X_OFFSET;
    public static int END_DBG_TEXT_Y = END_DBG_Y + END_DBG_TEXT_Y_OFFSET;

    public static int LV_DBG_X = 375;
    public static int LV_DBG_Y = 50;


    public static final int UNIT_BIG = S2B(1) / 2;

    public static int S2B(int num) {
        return num << 10;
    }

    public static int B2S(int num) {
        return num >> 10;
    }
}
