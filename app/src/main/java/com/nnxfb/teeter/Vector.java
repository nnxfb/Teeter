package com.nnxfb.teeter;

import android.graphics.Point;

/* loaded from: classes3.dex */
public class Vector extends Point {
    public Vector() {
        super(0, 0);
    }

    public Vector(int x, int y) {
        super(x, y);
    }

    public Vector(Vector v) {
        this.x = v.x;
        this.y = v.y;
    }

    public int length() {
        return (int) Math.sqrt((this.x * this.x) + (this.y * this.y));
    }

    public int dot(Vector ballPos) {
        return (this.x * ballPos.x) + (this.y * ballPos.y);
    }

    public Vector mul(float target) {
        return new Vector((int) (this.x * target), (int) (this.y * target));
    }

    public Vector add(Vector target) {
        return new Vector(this.x + target.x, this.y + target.y);
    }

    @Override // android.graphics.Point
    public String toString() {
        return "Vector(" + this.x + "," + this.y + ")";
    }

    public void decrease(int friction) {
        int length = length();
        this.x -= (this.x * friction) / length;
        this.y -= (this.y * friction) / length;
    }

    public boolean equals(Vector compare) {
        return this.x == compare.x && this.y == compare.y;
    }
}
