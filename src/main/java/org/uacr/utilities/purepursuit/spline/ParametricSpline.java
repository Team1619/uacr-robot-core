package org.uacr.utilities.purepursuit.spline;

import org.uacr.utilities.purepursuit.Point;

import java.util.ArrayList;
import java.util.List;

public class ParametricSpline {
    private Spline splineX;
    private Spline splineY;

    public ParametricSpline(List<? extends Point> points, double startingHeading, double endingHeading) {
        double[] t = new double[points.size()];
        double[] x = new double[points.size()];
        double[] y = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            t[i] = i;
            x[i] = points.get(i).getX();
            y[i] = points.get(i).getY();
        }

        splineX = new Spline(t, x, startingHeading, endingHeading);
        splineY = new Spline(t, y, startingHeading, endingHeading);
    }

    public ParametricSpline(List<? extends Point> points) {
        double[] t = new double[points.size()];
        double[] x = new double[points.size()];
        double[] y = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            t[i] = i;
            x[i] = points.get(i).getX();
            y[i] = points.get(i).getY();
        }

        splineX = new Spline(t, x);
        splineY = new Spline(t, y);
    }

    public ArrayList<Point> getPoints(double spacing) {
        ArrayList<Point> result = new ArrayList<>();
        double length = 0;
        double target = 0;
        final double k = 0.001;
        for (double t = 0; t < splineX.getUpperBound(); t += k) {
            length += getInstantaneousArcLength(t) * k;
            if (length >= target) {
                result.add(new Point(splineX.eval(t), splineY.eval(t)));
                target += spacing * 2;
            }
        }
        result.add(new Point(splineX.eval(splineX.getUpperBound()), splineY.eval(splineX.getUpperBound())));
        return result;
    }

    private double getInstantaneousArcLength(double t) {
        Polynomial xDerivative = splineX.getCurve(t).getDerivative();
        Polynomial yDerivative = splineY.getCurve(t).getDerivative();
        return Math.sqrt(Math.pow(xDerivative.eval(t), 2) + Math.pow(yDerivative.eval(t), 2));
    }
}
