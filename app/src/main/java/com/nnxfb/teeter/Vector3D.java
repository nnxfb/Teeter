package com.nnxfb.teeter;

/* loaded from: classes3.dex */
public class Vector3D {
    public int x;
    public int y;
    public int z;

    public Vector3D(int xx, int yy, int zz) {
        this.x = xx;
        this.y = yy;
        this.z = zz;
    }

    public Vector3D cross(Vector3D v) {
        return new Vector3D((this.y * v.z) - (this.z * v.y), (this.z * v.x) - (this.x * v.z), (this.x * v.y) - (this.y * v.x));
    }

    public String toString() {
        return "Vector3D (" + this.x + "," + this.y + "," + this.z + ")";
    }

    public int dot(Vector3D v) {
        return (this.x * v.x) + (this.y * v.y) + (this.z * v.z);
    }

    public int length() {
        return (int) Math.sqrt((this.x * this.x) + (this.y * this.y) + (this.z * this.z));
    }
}
