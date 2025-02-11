package com.nnxfb.teeter;

/* loaded from: classes3.dex */
public class Triangle {
    public Point3D a;
    public Point3D b;
    public Point3D c;
    private double dot00;
    private double dot01;
    private double dot02;
    private double dot11;
    private double dot12;
    private double invDenom;
    private Vector vAB;
    private Vector vAC;
    private Vector vAP = new Vector();

    public Triangle(Point3D A, Point3D B, Point3D C) {
        this.a = A;
        this.b = B;
        this.c = C;
        this.vAC = new Vector(this.c.x - this.a.x, this.c.y - this.a.y);
        this.vAB = new Vector(this.b.x - this.a.x, this.b.y - this.a.y);
        this.dot00 = this.vAC.dot(this.vAC);
        this.dot01 = this.vAC.dot(this.vAB);
        this.dot11 = this.vAB.dot(this.vAB);
        this.invDenom = 1.0d / ((this.dot00 * this.dot11) - (this.dot01 * this.dot01));
    }

    public boolean isPointInTriangle(int px, int py) {
        this.vAP.set(px - this.a.x, py - this.a.y);
        this.dot02 = this.vAC.dot(this.vAP);
        this.dot12 = this.vAB.dot(this.vAP);
        double u = ((this.dot11 * this.dot02) - (this.dot01 * this.dot12)) * this.invDenom;
        double v = ((this.dot00 * this.dot12) - (this.dot01 * this.dot02)) * this.invDenom;
        return u > 0.0d && v > 0.0d && u + v < 1.0d;
    }
}
