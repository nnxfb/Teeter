package com.nnxfb.teeter;

/* loaded from: classes3.dex */
public class Point3D {
    public int x;
    public int y;
    public int z;

    public Point3D(int X, int Y, int Z) {
        this.x = X;
        this.y = Y;
        this.z = Z;
    }

    public String toString() {
        return "Point3D (" + this.x + "," + this.y + "," + this.z + ")";
    }
}
