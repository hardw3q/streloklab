package com.pixels.steloklab.service;

import com.pixels.steloklab.model.Arrow;
import com.pixels.steloklab.model.GameModel;
import com.pixels.steloklab.model.HitResult;
import com.pixels.steloklab.model.Target;

public final class CollisionDetector {

    private CollisionDetector() {}

    public static HitResult checkHit(Arrow arrow, Target nearTarget, Target farTarget) {
        if (!arrow.isVisible()) return HitResult.NONE;
        double ax = arrow.getX();
        double ay = arrow.getY();
        double aw = GameModel.ARROW_LENGTH;
        double ah = GameModel.ARROW_HEIGHT;
        if (rectsOverlap(ax, ay, aw, ah, nearTarget.getMinX(), nearTarget.getMinY(), nearTarget.getWidth(), nearTarget.getHeight()))
            return HitResult.NEAR_HIT;
        if (rectsOverlap(ax, ay, aw, ah, farTarget.getMinX(), farTarget.getMinY(), farTarget.getWidth(), farTarget.getHeight()))
            return HitResult.FAR_HIT;
        return HitResult.NONE;
    }

    private static boolean rectsOverlap(double x1, double y1, double w1, double h1,
                                       double x2, double y2, double w2, double h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }
}
