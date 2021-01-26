package org.uacr.utilities.purepursuit;

import org.uacr.utilities.purepursuit.spline.ParametricSpline;

import java.util.ArrayList;
import java.util.Arrays;

public class SplinePath extends Path {

    private boolean isNaturalSpline;
    private Double startingAngle = null;
    private Double endingAngle = null;

    /**
     * Pass in an ArrayList of waypoints
     */
    public SplinePath(ArrayList<Point> points) {
        mPoints = points;
        isNaturalSpline = true;
    }

    /**
     * Pass in a comma separated list or array of waypoints
     */
    public SplinePath(Point... points) {
        this(new ArrayList<>(Arrays.asList(points)));
    }

    public SplinePath(Double startingAngle, Double endingAngle, ArrayList<Point> points) {
        mPoints = points;
        this.startingAngle = startingAngle;
        this.endingAngle = endingAngle;
        isNaturalSpline = false;
    }

    public SplinePath(Double startingAngle, Double endingAngle, Point... points) {
        this(startingAngle, endingAngle, new ArrayList<>(Arrays.asList(points)));
    }

    @Override
    protected void fill() {
        ParametricSpline spline;
        if (isNaturalSpline) {
            spline = new ParametricSpline(mPoints);
        } else {
            spline = new ParametricSpline(mPoints, startingAngle, endingAngle);
        }
        mPoints = spline.getPoints(getPointSpacing());
    }

    @Override
    protected void smooth() {
        // No smoothing needed with spline path
    }
}
