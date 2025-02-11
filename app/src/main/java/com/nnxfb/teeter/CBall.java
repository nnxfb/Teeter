package com.nnxfb.teeter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/* loaded from: classes3.dex */
public class CBall implements SensorEventListener {
    private static Rect[] walls;
    private Vector currentDiamond;
    private int mFriction;
    private Activity mParent;
    private SensorManager mSensorManager;
    private Timer mTimer;
    private Vibrator mVibrator;
    private int nextZone;
    private Rect wall;
    private static final int top = CU.BALL_RADIUS_BIG;
    private static final int left = CU.BALL_RADIUS_BIG;
    private static final int right = CU.S2B( CU.DESIGN_WIDTH - CU.BALL_RADIUS);
    private static final int bottom = CU.S2B( CU.DESIGN_HEIGHT - CU.BALL_RADIUS);
//    private final int POWER_IDLE = 0;
//    private final int POWER_AWAKE = 1;
    private boolean firstTime = true;
    private boolean timerLock = false;
    private int ballZone = -1;
    private long hitStemp = 0;
    private long idleTimer = 0;
    private long idleStemp = 0;
    private boolean keep_awake = true;
    private int mPowerState = 1;
    float[] sensorValue = new float[3];
    private Point ballPos = translateCL(CL.begin);
    private Point nextPos = new Point(this.ballPos);
    private Vector velocity = new Vector(0, 0);
    private Vector acceleration = new Vector(0, 0);
    private int mState = 0;
    private float[] mSensorValue = {0.0f, 0.0f, 0.0f};
    private float[] mSensorValue_old = {0.0f, 0.0f, 0.0f};

    public CBall(Activity parent) {
        this.mParent = parent;
        this.mVibrator = (Vibrator) parent.getSystemService(Context.VIBRATOR_SERVICE);
        this.mSensorManager = (SensorManager) this.mParent.getSystemService(Context.SENSOR_SERVICE);
    }

    public void fnStart(Point begin, Vector v, Vector a) {
        if (!this.timerLock) {
            this.timerLock = true;
            fnReset(begin, v, a);
            this.mTimer = new Timer();
            this.mTimer.schedule(new UpdateTask(), 1000L, 30L);
            if (this.firstTime) {
                this.firstTime = false;
                this.mSensorManager.registerListener(this, this.mSensorManager.getDefaultSensor(1), 1);
            }
            this.mPowerState = 1;
        }
    }

    public void fnStop() {
        this.mState = 3;
        if (this.mTimer != null) {
            this.mTimer.cancel();
        }
        this.mTimer = null;
        this.timerLock = false;
    }

    public void stopSensor() {
        this.firstTime = true;
        this.mSensorManager.unregisterListener(this);
    }

    public void fnReset(Point begin, Vector v, Vector a) {
        if (begin != null) {
            this.ballPos = translateCL(begin);
            this.nextPos = new Point(this.ballPos);
        }
        if (v != null) {
            this.velocity.set(v.x, v.y);
        }
        if (a != null) {
            this.acceleration.set(a.x, a.y);
        }
        this.mState = 0;
    }

    private class UpdateTask extends TimerTask {
        private UpdateTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            CBall.this.mState = CBall.this.updateState();
            switch (CBall.this.mState) {
                case 0:
                    CBall.this.updateAcceleration();
                    CBall.this.updateVelocity();
                    CBall.this.nextPos.x += CBall.this.velocity.x;
                    CBall.this.nextPos.y += CBall.this.velocity.y;
                    CBall.this.wallContactFix();
                    CBall.this.screenContactFix();
                    CBall.this.wallContactControl();
                    CBall.this.screenContactControl();
                    int v = CBall.this.velocity.length();
                    if(v != 0){
                        if (v > CU.MAX_SPEED) {
                            float ratio = (float) CU.MAX_SPEED / v;
                            CBall.this.velocity.x = (int) (CBall.this.velocity.x * ratio);
                            CBall.this.velocity.y = (int) (CBall.this.velocity.y * ratio);
                        }
                        if (CBall.this.velocity.length() > CBall.this.mFriction) {
                            CBall.this.velocity.decrease(CBall.this.mFriction);
                        }
                    }
                    CBall.this.ballPos.x = CBall.this.nextPos.x;
                    CBall.this.ballPos.y = CBall.this.nextPos.y;
                    break;
            }
        }
    }

    public void screenContactFix() {
        if (this.nextPos.x < left) {
            if (Math.abs(this.velocity.x) > CU.VIBRATION_ACTIVE_SPEED) {
                hits();
            }
            this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
            this.nextPos.x += left - this.nextPos.x;
        }
        if (this.nextPos.x > right) {
            if (Math.abs(this.velocity.x) > CU.VIBRATION_ACTIVE_SPEED) {
                hits();
            }
            this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
            this.nextPos.x -= this.nextPos.x - right;
        }
        if (this.nextPos.y < top) {
            if (Math.abs(this.velocity.y) > CU.VIBRATION_ACTIVE_SPEED) {
                hits();
            }
            this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
            this.nextPos.y += top - this.nextPos.y;
        }
        if (this.nextPos.y > bottom) {
            if (Math.abs(this.velocity.y) > CU.VIBRATION_ACTIVE_SPEED) {
                hits();
            }
            this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
            this.nextPos.y -= this.nextPos.y - bottom;
        }
    }

    public void wallContactFix() {
        int baseline;
        int baseline2;
        int baseline3;
        int baseline4;
        for (Rect rect : walls) {
            this.wall = rect;
            if (this.nextPos.y >= this.wall.top && this.nextPos.y <= this.wall.bottom) {
                int baseline5 = this.wall.left - CU.BALL_RADIUS_BIG;
                if (Math.abs(baseline5 - this.nextPos.x) < CU.BALL_RADIUS_BIG && this.nextPos.x > baseline5) {
                    if (Math.abs(this.velocity.x) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
                    this.nextPos.x += baseline5 - this.nextPos.x;
                }
                if (this.ballPos.x < baseline5 && this.nextPos.x > baseline5) {
                    if (Math.abs(this.velocity.x) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
                    this.nextPos.x += baseline5 - this.nextPos.x;
                }
                int baseline6 = this.wall.right + CU.BALL_RADIUS_BIG;
                if (Math.abs(baseline6 - this.nextPos.x) < CU.BALL_RADIUS_BIG && this.nextPos.x < baseline6) {
                    if (Math.abs(this.velocity.x) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
                    this.nextPos.x += baseline6 - this.nextPos.x;
                }
                if (this.ballPos.x > baseline6 && this.nextPos.x < baseline6) {
                    if (Math.abs(this.velocity.x) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
                    this.nextPos.x += baseline6 - this.nextPos.x;
                }
            }
            if (this.nextPos.x >= this.wall.left && this.nextPos.x <= this.wall.right) {
                int baseline7 = this.wall.top - CU.BALL_RADIUS_BIG;
                if (Math.abs(baseline7 - this.nextPos.y) < CU.BALL_RADIUS_BIG && this.nextPos.y > baseline7) {
                    if (Math.abs(this.velocity.y) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
                    this.nextPos.y += baseline7 - this.nextPos.y;
                }
                if (this.ballPos.y < baseline7 && this.nextPos.y > baseline7) {
                    if (Math.abs(this.velocity.y) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
                    this.nextPos.y += baseline7 - this.nextPos.y;
                }
                int baseline8 = this.wall.bottom + CU.BALL_RADIUS_BIG;
                if (Math.abs(baseline8 - this.nextPos.y) < CU.BALL_RADIUS_BIG && this.nextPos.y < baseline8) {
                    if (Math.abs(this.velocity.y) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
                    this.nextPos.y += baseline8 - this.nextPos.y;
                }
                if (this.ballPos.y > baseline8 && this.nextPos.y < baseline8) {
                    if (Math.abs(this.velocity.y) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
                    this.nextPos.y += baseline8 - this.nextPos.y;
                }
            }

            // 左上
            if (this.nextPos.x <= this.wall.left && this.nextPos.x >= this.wall.left - CU.BALL_RADIUS_BIG &&
                    this.nextPos.y <= this.wall.top && this.nextPos.y >= this.wall.top - CU.BALL_RADIUS_BIG &&
                    (baseline4 = (int) CL.destToPoint(this.wall.left, this.wall.top, this.nextPos.x, this.nextPos.y)) < CU.BALL_RADIUS_BIG) {
                Log.d("ball motion", "in wall : "+baseline4+"<"+CU.BALL_RADIUS_BIG);
                Vector cornerForce = new Vector(this.wall.left - this.nextPos.x, this.wall.top - this.nextPos.y);
                float dot = cornerForce.dot(this.velocity);
                if (dot > 0.0f) {
                    Vector force = cornerForce.mul(dot / cornerForce.dot(cornerForce)); // 投影向量
                    this.velocity.x = (int) (this.velocity.x - (force.x * (CU.BOUNCE_RATE + 1.0f)));
                    this.velocity.y = (int) (this.velocity.y - (force.y * (CU.BOUNCE_RATE + 1.0f)));
                    Vector cornerForce2 = cornerForce.mul(((float) CU.BALL_RADIUS_BIG / baseline4) - 1.0f);
                    this.nextPos.x -= cornerForce2.x;
                    this.nextPos.y -= cornerForce2.y;
                    if (Math.abs(force.length()) > CU.VIBRATION_ACTIVE_SPEED / 3) {
                        hits();
                    }
                }
            }

            if (this.ballPos.x <= this.wall.left && this.ballPos.y <= this.wall.top &&
                    this.nextPos.x >= this.wall.left && this.nextPos.y >= this.wall.top
            ) {
                int cosV = (this.velocity.x * 1024) / this.velocity.length();
                Vector center = new Vector(this.wall.left - this.ballPos.x, this.wall.top - this.ballPos.y);
                int cosCenter = (center.x * 1024) / center.length();
                if (cosV > cosCenter) {
                    Log.d("ball motion", "cosV > cosCenter");
                    if (Math.abs(this.velocity.y) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
                    this.nextPos.y += (this.wall.top - CU.BALL_RADIUS_BIG) - this.nextPos.y;
                } else if (cosV < cosCenter) {
                    Log.d("ball motion", "cosV < cosCenter");
                    if (Math.abs(this.velocity.x) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
                    this.nextPos.x += (this.wall.left - CU.BALL_RADIUS_BIG) - this.nextPos.x;
                } else {
                    Log.d("ball motion", "cosV = cosCenter");
                    if (Math.abs(this.velocity.length()) > CU.VIBRATION_ACTIVE_SPEED / 3) {
                        hits();
                    }
                    this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
                    this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
                    this.nextPos.x += (this.wall.left - CU.BALL_RADIUS_BIG) - this.nextPos.x;
                    this.nextPos.y += (this.wall.top - CU.BALL_RADIUS_BIG) - this.nextPos.y;
                }
            }

            // 左下
            if (this.nextPos.x <= this.wall.left && this.nextPos.x >= this.wall.left - CU.BALL_RADIUS_BIG && this.nextPos.y >= this.wall.bottom && this.nextPos.y <= this.wall.bottom + CU.BALL_RADIUS_BIG && (baseline3 = (int) CL.destToPoint(this.wall.left, this.wall.bottom, this.nextPos.x, this.nextPos.y)) < CU.BALL_RADIUS_BIG) {
                Vector cornerForce3 = new Vector(this.wall.left - this.nextPos.x, this.wall.bottom - this.nextPos.y);
                float dot2 = cornerForce3.dot(this.velocity);
                if (dot2 > 0.0f) {
                    Vector force2 = cornerForce3.mul(dot2 / cornerForce3.dot(cornerForce3));
                    this.velocity.x = (int) (this.velocity.x - (force2.x * (CU.BOUNCE_RATE + 1.0f)));
                    this.velocity.y = (int) (this.velocity.y - (force2.y * (CU.BOUNCE_RATE + 1.0f)));
                    Vector cornerForce4 = cornerForce3.mul(((float) CU.BALL_RADIUS_BIG / baseline3) - 1.0f);
                    this.nextPos.x -= cornerForce4.x;
                    this.nextPos.y -= cornerForce4.y;
                    if (Math.abs(force2.length()) > CU.VIBRATION_ACTIVE_SPEED / 3) {
                        hits();
                    }
                }
            }
            if (this.ballPos.x <= this.wall.left && this.ballPos.y >= this.wall.bottom && this.nextPos.x >= this.wall.left && this.nextPos.y <= this.wall.bottom) {
                int cosV2 = (this.velocity.x * 1000) / this.velocity.length();
                Vector center2 = new Vector(this.wall.left - this.ballPos.x, this.wall.bottom - this.ballPos.y);
                int cosCenter2 = (center2.x * 1000) / center2.length();
                if (cosV2 > cosCenter2) {
                    if (Math.abs(this.velocity.y) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
                    this.nextPos.y += (this.wall.bottom + CU.BALL_RADIUS_BIG) - this.nextPos.y;
                } else if (cosV2 < cosCenter2) {
                    if (Math.abs(this.velocity.x) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
                    this.nextPos.x += (this.wall.left - CU.BALL_RADIUS_BIG) - this.nextPos.x;
                } else {
                    if (Math.abs(this.velocity.length()) > CU.VIBRATION_ACTIVE_SPEED / 3) {
                        hits();
                    }
                    this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
                    this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
                    this.nextPos.x += (this.wall.left - CU.BALL_RADIUS_BIG) - this.nextPos.x;
                    this.nextPos.y += (this.wall.bottom + CU.BALL_RADIUS_BIG) - this.nextPos.y;
                }
            }

            // 右上
            if (this.nextPos.x >= this.wall.right && this.nextPos.x <= this.wall.right + CU.BALL_RADIUS_BIG && this.nextPos.y <= this.wall.top && this.nextPos.y >= this.wall.top - CU.BALL_RADIUS_BIG && (baseline2 = (int) CL.destToPoint(this.wall.right, this.wall.top, this.nextPos.x, this.nextPos.y)) < CU.BALL_RADIUS_BIG) {
                Vector cornerForce5 = new Vector(this.wall.right - this.nextPos.x, this.wall.top - this.nextPos.y);
                float dot3 = cornerForce5.dot(this.velocity);
                if (dot3 > 0.0f) {
                    Vector force3 = cornerForce5.mul(dot3 / cornerForce5.dot(cornerForce5));
                    this.velocity.x = (int) (this.velocity.x - (force3.x * (CU.BOUNCE_RATE + 1.0f)));
                    this.velocity.y = (int) (this.velocity.y - (force3.y * (CU.BOUNCE_RATE + 1.0f)));
                    Vector cornerForce6 = cornerForce5.mul(((float) CU.BALL_RADIUS_BIG / baseline2) - 1.0f);
                    this.nextPos.x -= cornerForce6.x;
                    this.nextPos.y -= cornerForce6.y;
                    if (Math.abs(force3.length()) > CU.VIBRATION_ACTIVE_SPEED / 3) {
                        hits();
                    }
                }
            }
            if (this.ballPos.x >= this.wall.right && this.ballPos.y <= this.wall.top && this.nextPos.x <= this.wall.right && this.nextPos.y >= this.wall.top) {
                int cosV3 = ((-this.velocity.x) * 1000) / this.velocity.length();
                Vector center3 = new Vector(this.wall.right - this.ballPos.x, this.wall.top - this.ballPos.y);
                int cosCenter3 = ((-center3.x) * 1000) / center3.length();
                if (cosV3 > cosCenter3) {
                    if (Math.abs(this.velocity.y) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
                    this.nextPos.y += (this.wall.top - CU.BALL_RADIUS_BIG) - this.nextPos.y;
                } else if (cosV3 < cosCenter3) {
                    if (Math.abs(this.velocity.x) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
                    this.nextPos.x += (this.wall.right + CU.BALL_RADIUS_BIG) - this.nextPos.x;
                } else {
                    if (Math.abs(this.velocity.length()) > CU.VIBRATION_ACTIVE_SPEED / 3) {
                        hits();
                    }
                    this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
                    this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
                    this.nextPos.x += (this.wall.right + CU.BALL_RADIUS_BIG) - this.nextPos.x;
                    this.nextPos.y += (this.wall.top - CU.BALL_RADIUS_BIG) - this.nextPos.y;
                }
            }

            if (this.nextPos.x >= this.wall.right && this.nextPos.x <= this.wall.right + CU.BALL_RADIUS_BIG && this.nextPos.y >= this.wall.bottom && this.nextPos.y <= this.wall.bottom + CU.BALL_RADIUS_BIG && (baseline = (int) CL.destToPoint(this.wall.right, this.wall.bottom, this.nextPos.x, this.nextPos.y)) < CU.BALL_RADIUS_BIG) {
                Vector cornerForce7 = new Vector(this.wall.right - this.nextPos.x, this.wall.bottom - this.nextPos.y);
                float dot4 = cornerForce7.dot(this.velocity);
                if (dot4 > 0.0f) {
                    Vector force4 = cornerForce7.mul(dot4 / cornerForce7.dot(cornerForce7));
                    this.velocity.x = (int) (this.velocity.x - (force4.x * (CU.BOUNCE_RATE + 1.0f)));
                    this.velocity.y = (int) (this.velocity.y - (force4.y * (CU.BOUNCE_RATE + 1.0f)));
                    Vector cornerForce8 = cornerForce7.mul(( CU.BALL_RADIUS_BIG / (float) baseline) - 1.0f);
                    this.nextPos.x -= cornerForce8.x;
                    this.nextPos.y -= cornerForce8.y;
                    if (Math.abs(force4.length()) > CU.VIBRATION_ACTIVE_SPEED / 3) {
                        hits();
                    }
                }
            }
            if (this.ballPos.x >= this.wall.right && this.ballPos.y >= this.wall.bottom && this.nextPos.x <= this.wall.right && this.nextPos.y <= this.wall.bottom) {
                int cosV4 = ((-this.velocity.x) * 1000) / this.velocity.length();
                Vector center4 = new Vector(this.wall.right - this.ballPos.x, this.wall.bottom - this.ballPos.y);
                int cosCenter4 = ((-center4.x) * 1000) / center4.length();
                if (cosV4 > cosCenter4) {
                    if (Math.abs(this.velocity.y) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
                    this.nextPos.y += (this.wall.bottom + CU.BALL_RADIUS_BIG) - this.nextPos.y;
                } else if (cosV4 < cosCenter4) {
                    if (Math.abs(this.velocity.x) > CU.VIBRATION_ACTIVE_SPEED) {
                        hits();
                    }
                    this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
                    this.nextPos.x += (this.wall.right + CU.BALL_RADIUS_BIG) - this.nextPos.x;
                } else {
                    if (Math.abs(this.velocity.length()) > CU.VIBRATION_ACTIVE_SPEED / 3) {
                        hits();
                    }
                    this.velocity.x = (int) (this.velocity.x * (-CU.BOUNCE_RATE));
                    this.velocity.y = (int) (this.velocity.y * (-CU.BOUNCE_RATE));
                    this.nextPos.x += (this.wall.right + CU.BALL_RADIUS_BIG) - this.nextPos.x;
                    this.nextPos.y += (this.wall.bottom + CU.BALL_RADIUS_BIG) - this.nextPos.y;
                }
            }
        }
    }

    public void screenContactControl() {
        if (this.nextPos.x < left) {
            this.velocity.x *= 0;
            this.nextPos.x += left - this.nextPos.x;
        }
        if (this.nextPos.x > right) {
            this.velocity.x *= 0;
            this.nextPos.x -= this.nextPos.x - right;
        }
        if (this.nextPos.y < top) {
            this.velocity.y *= 0;
            this.nextPos.y += top - this.nextPos.y;
        }
        if (this.nextPos.y > bottom) {
            this.velocity.y *= 0;
            this.nextPos.y -= this.nextPos.y - bottom;
        }
    }

    public void wallContactControl() {
        int baseline;
        int baseline2;
        int baseline3;
        int baseline4;
        for (Rect rect : walls) {
            this.wall = rect;
            if (this.nextPos.y >= this.wall.top && this.nextPos.y <= this.wall.bottom) {
                int baseline5 = this.wall.left - CU.BALL_RADIUS_BIG;
                if (Math.abs(baseline5 - this.nextPos.x) < CU.BALL_RADIUS_BIG && this.nextPos.x > baseline5) {
                    this.velocity.x = 0;
                    this.nextPos.x += baseline5 - this.nextPos.x;
                }
                if (this.ballPos.x < baseline5 && this.nextPos.x > baseline5) {
                    this.velocity.x = 0;
                    this.nextPos.x += baseline5 - this.nextPos.x;
                }
                int baseline6 = this.wall.right + CU.BALL_RADIUS_BIG;
                if (Math.abs(baseline6 - this.nextPos.x) < CU.BALL_RADIUS_BIG && this.nextPos.x < baseline6) {
                    this.velocity.x = 0;
                    this.nextPos.x += baseline6 - this.nextPos.x;
                }
                if (this.ballPos.x > baseline6 && this.nextPos.x < baseline6) {
                    this.velocity.x = 0;
                    this.nextPos.x += baseline6 - this.nextPos.x;
                }
            }
            if (this.nextPos.x >= this.wall.left && this.nextPos.x <= this.wall.right) {
                int baseline7 = this.wall.top - CU.BALL_RADIUS_BIG;
                if (Math.abs(baseline7 - this.nextPos.y) < CU.BALL_RADIUS_BIG && this.nextPos.y > baseline7) {
                    this.velocity.y = 0;
                    this.nextPos.y += baseline7 - this.nextPos.y;
                }
                if (this.ballPos.y < baseline7 && this.nextPos.y > baseline7) {
                    this.velocity.y = 0;
                    this.nextPos.y += baseline7 - this.nextPos.y;
                }
                int baseline8 = this.wall.bottom + CU.BALL_RADIUS_BIG;
                if (Math.abs(baseline8 - this.nextPos.y) < CU.BALL_RADIUS_BIG && this.nextPos.y < baseline8) {
                    this.velocity.y = 0;
                    this.nextPos.y += baseline8 - this.nextPos.y;
                }
                if (this.ballPos.y > baseline8 && this.nextPos.y < baseline8) {
                    this.velocity.y = 0;
                    this.nextPos.y += baseline8 - this.nextPos.y;
                }
            }
            if (this.nextPos.x <= this.wall.left && this.nextPos.x >= this.wall.left - CU.BALL_RADIUS_BIG && this.nextPos.y <= this.wall.top && this.nextPos.y >= this.wall.top - CU.BALL_RADIUS_BIG && (baseline4 = (int) CL.destToPoint(this.wall.left, this.wall.top, this.nextPos.x, this.nextPos.y)) < CU.BALL_RADIUS_BIG) {
                Vector cornerForce = new Vector(this.wall.left - this.nextPos.x, this.wall.top - this.nextPos.y);
                float dot = cornerForce.dot(this.velocity);
                if (dot > 0.0f) {
                    this.velocity.x = 0;
                    this.velocity.y = 0;
                    Vector corneForce2 = cornerForce.mul((CU.BALL_RADIUS_BIG / baseline4) - 1.0f);
                    this.nextPos.x -= corneForce2.x;
                    this.nextPos.y -= corneForce2.y;
                }
            }
            if (this.nextPos.x <= this.wall.left && this.nextPos.x >= this.wall.left - CU.BALL_RADIUS_BIG && this.nextPos.y >= this.wall.bottom && this.nextPos.y <= this.wall.bottom + CU.BALL_RADIUS_BIG && (baseline3 = (int) CL.destToPoint(this.wall.left, this.wall.bottom, this.nextPos.x, this.nextPos.y)) < CU.BALL_RADIUS_BIG) {
                Vector cornerForce3 = new Vector(this.wall.left - this.nextPos.x, this.wall.bottom - this.nextPos.y);
                float dot2 = cornerForce3.dot(this.velocity);
                if (dot2 > 0.0f) {
                    this.velocity.x = 0;
                    this.velocity.y = 0;
                    Vector cornerForce4 = cornerForce3.mul((CU.BALL_RADIUS_BIG / baseline3) - 1.0f);
                    this.nextPos.x -= cornerForce4.x;
                    this.nextPos.y -= cornerForce4.y;
                }
            }
            if (this.nextPos.x >= this.wall.right && this.nextPos.x <= this.wall.right + CU.BALL_RADIUS_BIG && this.nextPos.y <= this.wall.top && this.nextPos.y >= this.wall.top - CU.BALL_RADIUS_BIG && (baseline2 = (int) CL.destToPoint(this.wall.right, this.wall.top, this.nextPos.x, this.nextPos.y)) < CU.BALL_RADIUS_BIG) {
                Vector cornerForce5 = new Vector(this.wall.right - this.nextPos.x, this.wall.top - this.nextPos.y);
                float dot3 = cornerForce5.dot(this.velocity);
                if (dot3 > 0.0f) {
                    this.velocity.x = 0;
                    this.velocity.y = 0;
                    Vector cornerForce6 = cornerForce5.mul((CU.BALL_RADIUS_BIG / baseline2) - 1.0f);
                    this.nextPos.x -= cornerForce6.x;
                    this.nextPos.y -= cornerForce6.y;
                }
            }
            if (this.nextPos.x >= this.wall.right && this.nextPos.x <= this.wall.right + CU.BALL_RADIUS_BIG &&
                    this.nextPos.y >= this.wall.bottom && this.nextPos.y <= this.wall.bottom + CU.BALL_RADIUS_BIG &&
                    (baseline = (int) CL.destToPoint(this.wall.right, this.wall.bottom, this.nextPos.x, this.nextPos.y)) < CU.BALL_RADIUS_BIG) {
                Vector cornerForce7 = new Vector(this.wall.right - this.nextPos.x, this.wall.bottom - this.nextPos.y);
                float dot4 = cornerForce7.dot(this.velocity);
                if (dot4 > 0.0f) {
                    this.velocity.x = 0;
                    this.velocity.y = 0;
                    Vector cornerForce8 = cornerForce7.mul((CU.BALL_RADIUS_BIG / baseline) - 1.0f);
                    this.nextPos.x -= cornerForce8.x;
                    this.nextPos.y -= cornerForce8.y;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateAcceleration() {
        int speed;
        this.acceleration.x = (int) (this.mSensorValue[1] * CU.GRAVITY_FACTOR);
        this.acceleration.y = (int) (this.mSensorValue[0] * CU.GRAVITY_FACTOR);
        if (CL.mIsFacet) {
            this.nextZone = CL.atZone(CU.B2S(this.nextPos.x), CU.B2S(this.nextPos.y), this.velocity.x, this.velocity.y);
            if (this.nextZone != this.ballZone) {
                if (this.currentDiamond == null) {
                    this.currentDiamond = CL.getDiamondAcceleration(this.nextZone);
                }
                if (this.currentDiamond.length() == 0) {
                    speed = 0;
                } else {
                    speed = this.velocity.dot(this.currentDiamond) / this.currentDiamond.length();
                }
                if (speed >= CU.VIBRATION_ACTIVE_SPEED_GROUND) {
                    hits();
                } else if ((-speed) >= CU.VIBRATION_ACTIVE_SPEED) {
                    hits();
                }
                this.currentDiamond = CL.getDiamondAcceleration(this.nextZone);
            }
            int speed2 = this.nextZone;
            this.ballZone = speed2;
            this.acceleration.x += this.currentDiamond.x;
            this.acceleration.y += this.currentDiamond.y;
        }
        this.mFriction = (int) Math.abs(this.mSensorValue[2] * CU.FRICTION_FACTOR);
    }

    public void updateVelocity() {
        this.velocity.x += this.acceleration.x * 2;
        this.velocity.y += this.acceleration.y * 2;
    }

    private void hits() {
        long now = System.currentTimeMillis();
//        Log.d("Vibration", "hits called" + now);
        if (now - this.hitStemp >= 100) {
            this.mVibrator.vibrate(CU.VIBRATION_DURATION);
            Log.d("Vibration", "duration= " + CU.VIBRATION_DURATION + " time="+now);
            this.hitStemp = now;
        }
    }

    public int updateState() {
        this.mSensorValue_old[0] = this.mSensorValue[0];
        this.mSensorValue_old[1] = this.mSensorValue[1];
        if (this.mState == 3) {
            return 3;
        }
        if (CU.END_ON && CL.isAtEnd(this.ballPos)) {
            return 2;
        }
        return (CU.HOLE_ON && CL.isAtHole(this.ballPos)) ? 1 : 0;
    }

    public int getInHoleDegree() {
        float acc = Math.max(this.acceleration.length(),0.001f);
        int degree = (int) (Math.toDegrees(Math.acos((double) this.acceleration.y / acc)) + 0.5d);
        if (this.acceleration.x < 0) {
            degree = 360 - degree;
        }
        return degree - 30;
    }

    public void clearMemory() {
        this.velocity = null;
        this.acceleration = null;
        this.ballPos = null;
        this.nextPos = null;
        this.mParent = null;
        if (this.mSensorManager != null) {
            this.mSensorManager.unregisterListener(this);
        }
        this.mSensorManager = null;
        this.mVibrator = null;
        if (this.mTimer != null) {
            this.mTimer.cancel();
        }
        this.mTimer = null;
        this.mSensorValue = null;
        this.mSensorValue_old = null;
        this.currentDiamond = null;
        this.wall = null;
        walls = null;
    }

    public int fnCheckStatus() {
        return this.mState;
    }

    public static void updateWallInfo() {
        walls = CL.walls_big;
    }

    public void fnGetCenter(Point V) {
        V.x = CU.B2S(this.ballPos.x);
        V.y = CU.B2S(this.ballPos.y);
    }

    public void fnGetVelocity(Vector V) {
        V.x = this.velocity.x;
        V.y = this.velocity.y;
    }

    public void fnGetAccelerate(Vector V) {
        V.x = this.acceleration.x;
        V.y = this.acceleration.y;
    }

    private Point translateCL(Point begin) {
        return new Point(CU.S2B(begin.x), CU.S2B(begin.y));
    }

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == 1) {
            System.arraycopy(event.values, 0, this.sensorValue, 0, 3);
            if (Math.abs(this.sensorValue[0]) <= 0.2f && Math.abs(this.sensorValue[1]) <= 0.2f) {
                float[] fArr = this.mSensorValue;
                this.mSensorValue[1] = 0.0f;
                fArr[0] = 0.0f;
                this.mSensorValue[2] = this.sensorValue[2];
                return;
            }
            float[] fArr2 = event.values;
            this.mSensorValue = fArr2;
        }
    }
}
