package com.pixels.steloklab.model;

public class Arrow {
    private double x;
    private double y;
    private boolean visible;

    public Arrow() {
        this.visible = false;
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
}
