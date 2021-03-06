package org.uacr.utilities.purepursuit;

/**
 * PathPoint is an add on to the Point class,
 * allowing it to store additional values necessary for the pure pursuit Path class
 *
 * @author Matthew Oates
 */

public class PathPoint extends Point {

    private final double fDistance;
    private final double fCurvature;

    private double mVelocity;

    public PathPoint(double x, double y, double distance, double curvature) {
        super(x, y);

        fDistance = distance;
        fCurvature = curvature;
    }

    public PathPoint(Point point, double distance, double curvature) {
        this(point.getX(), point.getY(), distance, curvature);
    }

    public double getDistance() {
        return fDistance;
    }

    public double getCurvature() {
        return fCurvature;
    }

    public double getVelocity() {
        return mVelocity;
    }

    public void setVelocity(double velocity) {
        mVelocity = velocity;
    }
}
