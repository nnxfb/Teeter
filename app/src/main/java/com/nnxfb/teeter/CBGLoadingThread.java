package com.nnxfb.teeter;

import android.app.Activity;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.nnxfb.teeter.utils.TTConstants;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

/* loaded from: classes3.dex */
public class CBGLoadingThread extends Thread {
    private static Bitmap mBmpEnd;
    private static Bitmap mBmpFacet;
    private static Bitmap mBmpHole;
    private static Bitmap mBmpMaze;
    private static Bitmap mBmpShadow;
    public static int sw = 13;
    private Activity mActivity;
    private int mLevel;

    CBGLoadingThread(Activity aActivity, int nLevel) {
        this.mActivity = aActivity;
        this.mLevel = nLevel;
        System.gc();
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inPreferredConfig = Bitmap.Config.ARGB_4444;
        if (this.mLevel > 16 && mBmpMaze != null) {
            mBmpMaze.recycle();
            mBmpMaze = null;
        }
        if (this.mLevel <= 16 && mBmpMaze == null) {
            mBmpMaze = BitmapFactory.decodeResource(aActivity.getResources(), R.drawable.maze);
        } else if (this.mLevel > 16 && mBmpFacet == null) {
            mBmpFacet = BitmapFactory.decodeResource(aActivity.getResources(), R.drawable.facet);
        }
        if (mBmpEnd == null) {
            mBmpEnd = BitmapFactory.decodeResource(aActivity.getResources(), R.drawable.end);
        }
        if (mBmpHole == null) {
            mBmpHole = BitmapFactory.decodeResource(aActivity.getResources(), R.drawable.hole);
        }
        if (mBmpShadow == null) {
            mBmpShadow = BitmapFactory.decodeResource(aActivity.getResources(), R.drawable.shadow, option);
        }
        if (mBmpEnd.isRecycled()) {
            mBmpEnd = BitmapFactory.decodeResource(aActivity.getResources(), R.drawable.end);
            Log.w(TTConstants.TEETER, "BmpEnd is recycled.");
        }
        if (mBmpHole.isRecycled()) {
            mBmpHole = BitmapFactory.decodeResource(aActivity.getResources(), R.drawable.hole);
            Log.w(TTConstants.TEETER, "BmpHole is recycled.");
        }
        if (mBmpShadow.isRecycled()) {
            mBmpShadow = BitmapFactory.decodeResource(aActivity.getResources(), R.drawable.shadow, option);
            Log.w(TTConstants.TEETER, "BmpShadow is recycled.");
        }
    }

    public static void clearMemory() {
        if (mBmpHole != null) {
            mBmpHole.recycle();
        }
        mBmpHole = null;
        if (mBmpEnd != null) {
            mBmpEnd.recycle();
        }
        mBmpEnd = null;
        if (mBmpMaze != null) {
            mBmpMaze.recycle();
        }
        mBmpMaze = null;
        if (mBmpFacet != null) {
            mBmpFacet.recycle();
        }
        mBmpFacet = null;
        if (mBmpShadow != null) {
            mBmpShadow.recycle();
        }
        mBmpShadow = null;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        fnResetLevel();
        try {
            fnParseLevelFile(this.mLevel);
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }
        Bitmap oldBitmap = CU.BG_BMP;
        CU.BG_BMP = fnCreateBG();
        if (oldBitmap != null) {
            oldBitmap.recycle();
        }
        this.mActivity = null;
    }

    private void fnResetLevel() {
        if (CL.begin != null) {
            CL.begin.set(0, 0);
        } else {
            CL.begin = new Point(0, 0);
        }
        CL.end = null;
        CL.holes = new Point[0];
        CL.walls = new Rect[0];
        CL.walls_big = new Rect[0];
        CL.mIsFacet = false;
    }

    private void fnParseLevelFile(int nLevel) throws IOException, XmlPullParserException {
        XmlResourceParser xrp = this.mActivity.getResources().getXml((R.xml.level001 + nLevel) - 1);
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
            if (xrp.getName().equals("begin")) {
                CL.begin.set(xrp.getAttributeIntValue(null, "x", -1), xrp.getAttributeIntValue(null, "y", -1));
                Log.d("parse", "begin");
            } else if (xrp.getName().equals("end")) {
                CL.end = new Point(xrp.getAttributeIntValue(null, "x", -1), xrp.getAttributeIntValue(null, "y", -1));
            } else if (xrp.getName().equals("walls")) {
                int count = xrp.getAttributeIntValue(null, "count", 0);
                CL.walls = new Rect[count];
                CL.walls_big = new Rect[count];
                xrp.next();
                for (int i = 0; i < count; i++) {
                    if (xrp.getName().equals("wall")) {
                        CL.walls[i] = new Rect(xrp.getAttributeIntValue(null, "left", -1), xrp.getAttributeIntValue(null, "top", -1), xrp.getAttributeIntValue(null, "right", -1), xrp.getAttributeIntValue(null, "bottom", -1));
                        CL.walls_big[i] = CL.modifyRect(CL.walls[i]);
                        xrp.next();
                        xrp.next();
                    }
                }
            } else if (xrp.getName().equals("holes")) {
                int count2 = xrp.getAttributeIntValue(null, "count", 0);
                CL.holes = new Point[count2];
                xrp.next();
                for (int i2 = 0; i2 < count2; i2++) {
                    if (xrp.getName().equals("hole")) {
                        CL.holes[i2] = new Point(xrp.getAttributeIntValue(null, "x", -1), xrp.getAttributeIntValue(null, "y", -1));
                        xrp.next();
                        xrp.next();
                    }
                }
            } else if (xrp.getName().equals("background")) {
                CL.mIsFacet = xrp.getAttributeIntValue(0, -1) == 0;
            }
            while (xrp.getEventType() != 3) {
                xrp.next();
            }
            xrp.next();
        }
        xrp.close();
        CBall.updateWallInfo();
    }

    private Bitmap fnCreateBG() {
        Bitmap mazeBmp;
        Bitmap wallBmp;
        System.gc();
        Matrix scale = new Matrix();
        scale.setScale(CU.RATIO, CU.RATIO);

        Bitmap viewBmp = Bitmap.createBitmap(CU.SCREEN_WIDTH, CU.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
        viewBmp.eraseColor(Color.BLACK);
        Canvas canvas = new Canvas(viewBmp);
        if (CL.mIsFacet) {
            mazeBmp = mBmpFacet;
        } else {
            Bitmap mazeBmp2 = mBmpMaze;
            mazeBmp = mazeBmp2;
        }
        Log.d("layout", " "+mazeBmp.getWidth()+"x"+mazeBmp.getHeight());

        int targetWidth = (int) (CU.DESIGN_WIDTH * CU.RATIO);
        int originalWidth = mazeBmp.getWidth();
        int originalHeight = mazeBmp.getHeight();
        float scale2 = (float) targetWidth / originalWidth;
        int targetHeight = (int) (originalHeight * scale2);
        Bitmap scaledMazeBmp = Bitmap.createScaledBitmap(mazeBmp, targetWidth, targetHeight, true);
        canvas.drawBitmap(scaledMazeBmp, CU.OFFSET_X, CU.OFFSET_Y, null);
        
        if (CL.mIsFacet && CL.isDiamondNull()) {
            CL.initAcceleration(this.mActivity.getResources());
        }
        Point pt = new Point();
        pt.x = (int) (CL.end.x * CU.RATIO) + CU.OFFSET_X;
        pt.y = (int) (CL.end.y * CU.RATIO) + CU.OFFSET_Y;
        fnDrawHoleOnBG(canvas, pt, CU.END_RADIUS * CU.END_RATIO * CU.RATIO * 2.0f, mBmpEnd);
        for (int i = 0; i < CL.holes.length; i++) {
            pt.x = (int) (CL.holes[i].x * CU.RATIO) + CU.OFFSET_X;
            pt.y = (int) (CL.holes[i].y * CU.RATIO) + CU.OFFSET_Y;
            fnDrawHoleOnBG(canvas, pt, CU.HOLE_RADIUS * CU.HOLE_RATIO * CU.RATIO * 2.0f, mBmpHole);
        }
        Rect rSrc = new Rect();
        Rect rDst = new Rect();
        int safeCounter = 0;
        for (int i2 = 0; safeCounter < 5 && i2 < CL.walls.length; i2++) {
            try {
                fnGetLeftSRects(CL.walls[i2], rSrc, rDst);
                Bitmap divBmp1 = Bitmap.createBitmap(mBmpShadow, rSrc.left, rSrc.top, rSrc.width(), rSrc.height(), scale, false);
                canvas.drawBitmap(divBmp1, rDst.left * CU.RATIO + CU.OFFSET_X, rDst.top * CU.RATIO + CU.OFFSET_Y, (Paint) null);
                fnGetTopSRects(CL.walls[i2], rSrc, rDst);
                Bitmap divBmp2 = Bitmap.createBitmap(mBmpShadow, rSrc.left, rSrc.top, rSrc.width(), rSrc.height(), scale, false);
                canvas.drawBitmap(divBmp2, rDst.left * CU.RATIO + CU.OFFSET_X, rDst.top * CU.RATIO + CU.OFFSET_Y, (Paint) null);
                fnGetRightSRects(CL.walls[i2], rSrc, rDst);
                Bitmap divBmp3 = Bitmap.createBitmap(mBmpShadow, rSrc.left, rSrc.top, rSrc.width(), rSrc.height(), scale, false);
                canvas.drawBitmap(divBmp3, rDst.left * CU.RATIO + CU.OFFSET_X, rDst.top * CU.RATIO + CU.OFFSET_Y, (Paint) null);
                fnGetBottomSRects(CL.walls[i2], rSrc, rDst);
                Bitmap divBmp4 = Bitmap.createBitmap(mBmpShadow, rSrc.left, rSrc.top, rSrc.width(), rSrc.height(), scale, false);
                canvas.drawBitmap(divBmp4, rDst.left * CU.RATIO + CU.OFFSET_X, rDst.top * CU.RATIO + CU.OFFSET_Y, (Paint) null);
                divBmp1.recycle();
                divBmp2.recycle();
                divBmp3.recycle();
                divBmp4.recycle();
            } catch (IllegalArgumentException e) {
                safeCounter++;
            }
        }
//        if (CL.mIsFacet) {
//            wallBmp = mBmpFacet;
//        } else {
//            wallBmp = mBmpMaze;
//        }
        wallBmp = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.wall);
        Rect[] walls = new Rect[CL.walls.length];
        for (int k = 0; k < CL.walls.length; k++) {
            walls[k] = new Rect(CL.walls[k]);
            int wWidth = walls[k].width();
            int wHeight = walls[k].height();
            Bitmap wallDivBmp = Bitmap.createBitmap(wallBmp, walls[k].left, walls[k].top, wWidth, wHeight, scale, false);
            canvas.drawBitmap(wallDivBmp, walls[k].left * CU.RATIO + CU.OFFSET_X, walls[k].top * CU.RATIO + CU.OFFSET_Y, (Paint) null);
            wallDivBmp.recycle();
        }

        if (CU.DEBUG) {
            Paint p = new Paint();
            p.setColor(Color.argb(70, 0, 255, 0));
            canvas.drawRect(new Rect(CU.HOLE_DBG_L, CU.HOLE_DBG_T, CU.HOLE_DBG_R, CU.HOLE_DBG_B), p);
            p.setColor(Color.argb(130, 255, 0, 0));
            canvas.drawRect(new Rect(CU.END_DBG_L, CU.END_DBG_T, CU.END_DBG_R, CU.END_DBG_B), p);
            p.setColor(Color.argb(70, 0, 0, 255));
            p.setTextSize(40.0f);
            canvas.drawText("HOLE", CU.HOLE_DBG_TEXT_X, CU.HOLE_DBG_TEXT_Y, p);
            canvas.drawText("END", CU.END_DBG_TEXT_X, CU.END_DBG_TEXT_Y, p);
            p.setColor(Color.argb(255, 0, 0, 255));
            canvas.drawText("Lv:" + CU.LEVEL, CU.LV_DBG_X, CU.LV_DBG_Y, p);
            p.setColor(Color.argb(80, 0, 0, 255));
            canvas.drawRect(new Rect(CU.PWD_LL,CU.PWD_TT,CU.PWD_LR,CU.PWD_TB), p);
            canvas.drawRect(new Rect(CU.PWD_RL,CU.PWD_BT,CU.PWD_RR,CU.PWD_BB), p);
            canvas.drawRect(new Rect(CU.PWD_LL,CU.PWD_BT,CU.PWD_LR,CU.PWD_BB), p);
            canvas.drawRect(new Rect(CU.PWD_RL,CU.PWD_TT,CU.PWD_RR,CU.PWD_TB), p);
        }
        return viewBmp;
    }

    private void fnGetLeftSRects(Rect rOrg, Rect rSrc, Rect rDst) {
        Bitmap shadowBmp = mBmpShadow;
        Rect r = new Rect(rOrg);
        if (r.left > 0) {
            rDst.left = r.left - sw;
            rDst.right = r.left;
            rDst.top = r.top - sw;
            rDst.bottom = r.bottom;
            if (rDst.left < 0) {
                rDst.left = 0;
            }
            if (rDst.top < 0) {
                rDst.top = 0;
            }
            rSrc.right = sw;
            rSrc.left = rSrc.right - rDst.width();
            rSrc.bottom = r.height() + sw;
            rSrc.top = rSrc.bottom - rDst.height();
        }
    }

    private void fnGetTopSRects(Rect rOrg, Rect rSrc, Rect rDst) {
        Bitmap shadowBmp = mBmpShadow;
        Rect r = new Rect(rOrg);
        if (r.top > 0) {
            rDst.top = r.top - sw;
            rDst.bottom = r.top;
            rDst.right = r.right + sw;
            rDst.left = r.left;
            if (rDst.top < 0) {
                rDst.top = 0;
            }
            if (rDst.right > CU.DESIGN_WIDTH) {
                rDst.right = CU.DESIGN_WIDTH;
            }
            rSrc.bottom = sw;
            rSrc.top = rSrc.bottom - (rDst.bottom - rDst.top);
            rSrc.left = (shadowBmp.getWidth() - sw) - (r.right - r.left);
            rSrc.right = rSrc.left + (rDst.right - rDst.left);
        }
    }

    private void fnGetRightSRects(Rect rOrg, Rect rSrc, Rect rDst) {
        Bitmap shadowBmp = mBmpShadow;
        Rect r = new Rect(rOrg);
        if (r.right < CU.DESIGN_WIDTH) {
            rDst.right = r.right + sw;
            rDst.left = r.right;
            rDst.bottom = r.bottom + sw;
            rDst.top = r.top;
            if (rDst.right > CU.DESIGN_WIDTH) {
                rDst.right = CU.DESIGN_WIDTH;
            }
            if (rDst.bottom > CU.DESIGN_HEIGHT) {
                rDst.bottom = CU.DESIGN_HEIGHT;
            }
            rSrc.left = shadowBmp.getWidth() - sw;
            rSrc.right = rSrc.left + (rDst.right - rDst.left);
            rSrc.top = (shadowBmp.getHeight() - sw) - (r.bottom - r.top);
            rSrc.bottom = rSrc.top + (rDst.bottom - rDst.top);
        }
    }

    private void fnGetBottomSRects(Rect rOrg, Rect rSrc, Rect rDst) {
        Bitmap shadowBmp = mBmpShadow;
        Rect r = new Rect(rOrg);
        if (r.bottom < CU.DESIGN_HEIGHT) {
            rDst.bottom = r.bottom + sw;
            rDst.top = r.bottom;
            rDst.left = r.left - sw;
            rDst.right = r.right;
            if (r.bottom > CU.DESIGN_HEIGHT) {
                r.bottom = CU.DESIGN_HEIGHT;
            }
            if (r.left < 0) {
                r.left = 0;
            }
            rSrc.top = shadowBmp.getHeight() - sw;
            rSrc.bottom = rSrc.top + (rDst.bottom - rDst.top);
            rSrc.right = (r.right - r.left) + sw;
            rSrc.left = rSrc.right - (rDst.right - rDst.left);
        }
    }

    public static void fnDrawHoleOnBG(Canvas canvas, Point dstP, float radius, Bitmap srcBmp) {
        int w = srcBmp.getWidth();
        int h = srcBmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.setScale(radius / w, radius / h);
        Bitmap scaledBmp = Bitmap.createBitmap(srcBmp, 0, 0, w, h, matrix, true);
        canvas.drawBitmap(scaledBmp, dstP.x - (radius / 2.0f), dstP.y - (radius / 2.0f), (Paint) null);
        scaledBmp.recycle();
    }
}
