package com.nnxfb.teeter;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

/* loaded from: classes3.dex */
public final class CL {
    private static Point3D[] a_k;
    public static Triangle[] diamondZone;
    public static boolean mIsFacet;
    public static Point begin = new Point();
    public static Point end = new Point();
    public static Rect[] walls = new Rect[0];
    public static Rect[] walls_big = new Rect[0];
    public static Point[] holes = new Point[0];
    public static int HOLE_INDEX = -1;
    private static Vector[] diamond_acceleration = null;

    public static Rect modifyRect(Rect source) {
        return new Rect(CU.S2B(source.left), CU.S2B(source.top), CU.S2B(source.right), CU.S2B(source.bottom));
    }

    public static float destToPoint(int x1, int y1, int x2, int y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt((dx * dx) + (dy * dy));
    }

    public static boolean isAtHole(Point pos) {
        int bigRadius = CU.S2B(CU.HOLE_RADIUS);
        for (int i = 0; holes != null && i < holes.length; i++) {
            float dest = destToPoint(CU.S2B(holes[i].x), CU.S2B(holes[i].y), pos.x, pos.y);
            if (dest > 0.0f && dest < bigRadius) {
                HOLE_INDEX = i;
                return true;
            }
        }
        return false;
    }

    public static boolean isAtEnd(Point pos) {
        if (end != null) {
            float dest = destToPoint(CU.S2B(end.x), CU.S2B(end.y), pos.x, pos.y);
            if (dest > 0.0f && dest < CU.S2B(CU.END_RADIUS)) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static int atZone(int px, int py, int vx, int vy) {
        return atZoneInternal(px, py, vx, vy, 0, 10);
    }

    private static int atZoneInternal(int px, int py, int vx, int vy, int depth, int maxDepth) {
        if (depth >= maxDepth) {
            Log.w("Caution", "Max recursion depth reached, returning default value.");
            return 0;
        }
        for (int i = 0; i < diamondZone.length; i++) {
            if (diamondZone[i].isPointInTriangle(px, py)) {
                return i;
            }
        }
        Log.w("Caution", "Ball on the edge, recompute is needed pos(" + px + ", " + py + ") V(" + vx + ", " + vy + ")");
        int vx2 = CU.B2S(vx);
        int vy2 = CU.B2S(vy);
        if (vx2 == 0) {
            vx2 = 5;
        }
        if (vy2 == 0) {
            vy2 = 5;
        }
        if (depth % 10 == 0) {
            vx2 += (int) ((Math.random() * 10.0d) - 5.0d);
            vy2 += (int) ((Math.random() * 10.0d) - 5.0d);
        }
        return atZoneInternal(px + vx2, py + vy2, vx2, vy2, depth + 1, maxDepth);
    }

    public static void initAcceleration(Resources res) {
        if (diamond_acceleration == null) {
            a_k = new Point3D[11];
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 >= 11) {
                    break;
                }
                int[] face = res.getIntArray(R.array.a + i2);
                a_k[i2] = new Point3D(face[0], face[1], face[2]);
                i = i2 + 1;
            }
            diamondZone = new Triangle[]{new Triangle(a_k[3], a_k[10], a_k[5]), new Triangle(a_k[3], a_k[5], a_k[0]), new Triangle(a_k[3], a_k[0], a_k[1]), new Triangle(a_k[3], a_k[1], a_k[2]), new Triangle(a_k[3], a_k[2], a_k[4]), new Triangle(a_k[3], a_k[4], a_k[8]), new Triangle(a_k[3], a_k[8], a_k[10]), new Triangle(a_k[10], a_k[8], a_k[9]), new Triangle(a_k[10], a_k[9], a_k[7]), new Triangle(a_k[7], a_k[6], a_k[10]), new Triangle(a_k[10], a_k[6], a_k[5])};
            diamond_acceleration = new Vector[diamondZone.length];
            Vector3D v01 = new Vector3D(0, 0, 0);
            Vector3D v12 = new Vector3D(0, 0, 0);
            Vector3D v1 = new Vector3D(0, 0, 0);
            int i3 = 0;
            while (i3 < diamond_acceleration.length) {
                Triangle zone = diamondZone[i3];
                v01.x = zone.b.x - zone.a.x;
                v01.y = zone.b.y - zone.a.y;
                v01.z = zone.b.z - zone.a.z;
                v12.x = zone.c.x - zone.b.x;
                v12.y = zone.c.y - zone.b.y;
                v12.z = zone.c.z - zone.b.z;
                Vector3D normal = v01.cross(v12);
                normal.x /= 100;
                normal.y /= 100;
                normal.z /= 100;
                v1.x = normal.x;
                v1.y = normal.y;
                double cosV = (double) (v1.dot(normal) / v1.length()) / normal.length();
                int X = (int) ((((normal.x * CU.GRAVITY_FACTOR) * 9.80665f) * cosV) / normal.length());
                int Y = (int) ((((normal.y * CU.GRAVITY_FACTOR) * 9.80665f) * cosV) / normal.length());
                diamond_acceleration[i3] = new Vector(X, Y);
                int X2 = i3 + 1;
                i3 = X2;
            }
        }
    }

    public static Vector getDiamondAcceleration(int zone) {
        return diamond_acceleration[zone];
    }

    public static void clear() {
        begin = null;
        end = null;
        walls = null;
        walls_big = null;
        holes = null;
        diamond_acceleration = null;
        for (int i = 0; diamondZone != null && i < diamondZone.length; i++) {
            diamondZone[i] = null;
        }
        diamondZone = null;
        if (a_k != null) {
            for (int i2 = 0; i2 < 11; i2++) {
                a_k[i2] = null;
            }
            a_k = null;
        }
    }

    public static boolean isDiamondNull() {
        return diamond_acceleration == null;
    }
}
