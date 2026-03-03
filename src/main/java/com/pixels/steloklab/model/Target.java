package com.pixels.steloklab.model;

public class Target {
    public enum Type { NEAR, FAR }

    private final Type type;
    private double y;
    private final double width;
    private final double height;
    private final double speed;
    private final double x;

    public Target(Type type, double x, double y, double baseSize, double baseSpeed) {
        this.type = type;
        this.x = x;
        this.y = y;
        if (type == Type.NEAR) {
            this.width = baseSize;
            this.height = baseSize;
            this.speed = baseSpeed;
        } else {
            this.width = baseSize / 2;
            this.height = baseSize / 2;
            this.speed = baseSpeed * 2;
        }
    }

    public Type getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getSpeed() { return speed; }

    public double getCenterY() { return y + height / 2; }
    public double getMinY() { return y; }
    public double getMaxY() { return y + height; }
    public double getMinX() { return x; }
    public double getMaxX() { return x + width; }
}
