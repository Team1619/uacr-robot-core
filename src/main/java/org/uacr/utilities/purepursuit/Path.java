package org.uacr.utilities.purepursuit;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * PursuitPath is a class which stores points and calculates values to run pure pursuit
 *
 * @author Matthew Oates
 */

public class Path {

    /**PursuitPath specific creation and following data*/

    /**
     * Distance between each point (inches)
     */
    private double mPointSpacing = 1;

    /**
     * The amount of smoothing to be done on the path (larger number = more smoothing)
     */
    private double mPathSmoothing = 0.5;

    /**
     * Speed reduction through turns (larger number = faster turns)
     */
    private double mTurnSpeed = 1.0;

    /**
     * Scales following speed based on tracking error (smaller number = better tracking, larger number = faster tracking)
     */
    private double mTrackingErrorSpeed = 5.0;

    /**
     * Scales how much turn error is added into total error
     */
    private double mTurnErrorScalar = 0.125;

    /**
     * The max acceleration (total output/point)
     */
    private double mMaxAcceleration = 0.01;

    /**
     * The max deceleration (total output/point)
     */
    private double mMaxDeceleration = 0.005;

    /**
     * Minimum follow speed
     */
    private double mMinSpeed = 0.2;

    /**
     * Maximum follow speed
     */
    private double mMaxSpeed = 1.5;

    /**
     * Average look ahead distance
     */
    private double mLookAheadDistance = 5;

    /**
     * Look ahead distance for velocity calculations
     */
    private int mVelocityLookAheadPoints = 1;

    /**
     * Run specific data, gets reset with reset() method
     */

    private int mLastPointIndex = 0;
    private int mLastCurrentPointIndex = 0;
    private double mTargetAngle = 0;
    private double mDeltaAngle = 0;
    private double mCurvature = 0.000001;

    @Nullable
    private ValueInterpolator turnSpeedInterpolator;
    @Nullable
    private ValueInterpolator maxAccelerationInterpolator;
    @Nullable
    private ValueInterpolator maxDecelerationInterpolator;
    @Nullable
    private ValueInterpolator minSpeedInterpolator;
    @Nullable
    private ValueInterpolator maxSpeedInterpolator;
    @Nullable
    private ValueInterpolator lookAheadDistanceInterpolator;
    @Nullable
    private ValueInterpolator velocityLookAheadPointsInterpolator;

    /**
     * Waypoints along path specified by behavior
     */
    protected final ArrayList<Point> mPoints;

    protected final ArrayList<PathDeviationConfig> deviations;

    @Nullable
    protected ArrayList<Point> mGeneratedPoints;

    /**
     * All the points along the path, created from the waypoints (fPoints)
     */
    @Nullable
    private ArrayList<PathPoint> mPath;

    /**
     * Pass in an ArrayList of waypoints
     */
    public Path(ArrayList<Point> points) {
        mPoints = points;
        deviations = new ArrayList<>();
    }

    /**
     * Pass in a comma separated list or array of waypoints
     */
    public Path(Point... points) {
        this(new ArrayList<>(Arrays.asList(points)));
    }

    /**
     * Getters for path specific creation and following data
     */

    public double getPointSpacing() {
        return mPointSpacing;
    }

    /**
     * Setters for path specific creation and following data
     */

    public void setPointSpacing(double pointSpacing) {
        mPointSpacing = pointSpacing;
    }

    public double getPathSmoothing() {
        return mPathSmoothing;
    }

    public void setPathSmoothing(double pathSmoothing) {
        mPathSmoothing = pathSmoothing;
    }

    public double getTurnSpeed() {
        return mTurnSpeed;
    }

    public double getTurnSpeed(double distance) {
        if(null != turnSpeedInterpolator) {
            return turnSpeedInterpolator.getValue(distance);
        }
        return getTurnSpeed();
    }

    public void setTurnSpeed(double turnSpeed) {
        mTurnSpeed = turnSpeed;
    }

    public double getTrackingErrorSpeed() {
        return mTrackingErrorSpeed;
    }

    public void setTrackingErrorSpeed(double trackingErrorSpeed) {
        mTrackingErrorSpeed = trackingErrorSpeed;
    }

    public void setTurnErrorScalar(double turnErrorScalar) {
        mTurnErrorScalar = turnErrorScalar;
    }

    public double getTurnErrorScalar() {
        return mTurnErrorScalar;
    }

    public double getMaxAcceleration() {
        return mMaxAcceleration;
    }

    public double getMaxAcceleration(double distance) {
        if(null != maxAccelerationInterpolator) {
            return maxAccelerationInterpolator.getValue(distance);
        }
        return getMaxAcceleration();
    }

    public void setMaxAcceleration(double maxAcceleration) {
        mMaxAcceleration = maxAcceleration;
    }

    public double getMaxDeceleration() {
        return mMaxDeceleration;
    }

    public double getMaxDeceleration(double distance) {
        if(null != maxDecelerationInterpolator) {
            return maxDecelerationInterpolator.getValue(distance);
        }
        return getMaxDeceleration();
    }

    public void setMaxDeceleration(double maxDeceleration) {
        mMaxDeceleration = maxDeceleration;
    }

    public double getMinSpeed() {
        return mMinSpeed;
    }

    public double getMinSpeed(double distance) {
        if(null != minSpeedInterpolator) {
            return minSpeedInterpolator.getValue(distance);
        }
        return getMinSpeed();
    }

    public void setMinSpeed(double minSpeed) {
        mMinSpeed = minSpeed;
    }

    public double getMaxSpeed() {
        return mMaxSpeed;
    }

    public double getMaxSpeed(double distance) {
        if(null != maxSpeedInterpolator) {
            return maxSpeedInterpolator.getValue(distance);
        }
        return getMaxSpeed();
    }

    public void setMaxSpeed(double maxSpeed) {
        mMaxSpeed = maxSpeed;
    }

    public double getLookAheadDistance() {
        return mLookAheadDistance;
    }

    public double getLookAheadDistance(double distance) {
        if(null != lookAheadDistanceInterpolator) {
            return lookAheadDistanceInterpolator.getValue(distance);
        }
        return getLookAheadDistance();
    }

    public void setLookAheadDistance(double lookAheadDistance) {
        mLookAheadDistance = lookAheadDistance;
    }

    public int getVelocityLookAheadPoints() {
        return mVelocityLookAheadPoints;
    }

    public double getVelocityLookAheadPoints(double distance) {
        if(null != velocityLookAheadPointsInterpolator) {
            return velocityLookAheadPointsInterpolator.getValue(distance);
        }
        return getVelocityLookAheadPoints();
    }

    public void setVelocityLookAheadPoints(int lookAheadPoints) {
        mVelocityLookAheadPoints = lookAheadPoints;
    }

    public ArrayList<PathDeviationConfig> getDeviations() {
        return deviations;
    }

    public void addDeviation(PathDeviationConfig deviation) {
        deviations.add(deviation);
    }

    /**Methods for path following*/

    /**
     * Returns a single PathPoint from fPath
     *
     * @param index the index of the PathPoint
     * @return a PathPoint
     */
    public PathPoint getPoint(int index) {
        return getPathPoint(index);
    }

    /**
     * Returns all points in path (fPath).
     *
     * @return a PathPoint ArrayList
     */
    public ArrayList<PathPoint> getPoints() {
        if (mPath != null) {
            return (ArrayList<PathPoint>) mPath.clone();
        }
        build();
        return getPoints();
    }

    /**
     * Returns all points in path (fPath).
     *
     * @return a PathPoint ArrayList
     */
    public ArrayList<Point> getWayPoints() {
        if (mPoints != null) {
            return (ArrayList<Point>) mPoints.clone();
        }
        build();
        return getWayPoints();
    }

    /**
     * Calculates and returns the angle from the robots current pose to a specified point.
     *
     * @param index           the index of the point, usually the look ahead point
     * @param currentLocation the current Pose2d of the robot
     * @return the angle in degrees
     */
    public double getAngleFromPathPoint(int index, Pose2d currentLocation) {
        if (mPath == null || mPath.size() == 0) return 0.0;

        Vector delta = new Vector(getPathPoint(index).subtract(currentLocation));

        double angle = Math.toDegrees(Math.atan2(delta.getY(), Math.abs(delta.getX()) > 0.3 ? delta.getX() : 0.3 * Math.signum(delta.getX())));

        mTargetAngle = angle;

        return angle;
    }


    /**
     * Calculates and returns the curvature from the robots current pose to a specified point,
     * used by the follower to steer the robot.
     *
     * @param index           the index of the point, usually the look ahead point
     * @param currentLocation the current Pose2d of the robot
     * @return the curvature represent as 1 / radius of the circle made by the amount of curvature
     */
    public double getCurvatureFromPathPoint(int index, Pose2d currentLocation) {
        if (mPath == null || mPath.size() == 0) return 0.0;

        Vector delta = new Vector(index < getPath().size() - 1 ? getPathPoint(index).subtract(currentLocation) : getPathPoint(getPath().size() - 1).add(new Vector(getPathPoint(getPath().size() - 1).subtract(getPathPoint(getPath().size() - 2))).normalize().scale(mLookAheadDistance - currentLocation.distance(getPathPoint(getPath().size() - 1)))).subtract(currentLocation));

        double angle = Math.toDegrees(Math.atan2(delta.getY(), Math.abs(delta.getX()) > 0.3 ? delta.getX() : 0.3 * Math.signum(delta.getX())));

        mTargetAngle = angle;

        mDeltaAngle = currentLocation.getHeading() - angle;

        if (Math.abs(mDeltaAngle) > 180) mDeltaAngle = -Math.signum(mDeltaAngle) * (360 - Math.abs(mDeltaAngle));

        double curvature = (Math.abs(mDeltaAngle) > 90 ? Math.signum(mDeltaAngle) : Math.sin(Math.toRadians(mDeltaAngle))) / (delta.magnitude() / 2);

        if (Double.isInfinite(curvature) || Double.isNaN(curvature)) return 0.0;

        mCurvature = curvature;

        return curvature;
    }

    /**
     * Returns the last target angle of the robot,
     * calculated by getCurvatureFromPathPoint.
     * Used mainly for debugging.
     *
     * @return the last target angle
     */
    public double getTargetAngle() {
        return mTargetAngle;
    }

    /**
     * Calculates and returns the optimal velocity of the robot at its current position,
     * used by the follower to drive the robot.
     * Calculates the speed of upcoming points using curvature and tracking error, then picks the slowest.
     * This ensures we slow down in advance for turns, preventing overshoot.
     *
     * @param index           the index of the point closest to the robot
     * @param currentLocation the current Pose2d of the robot
     * @return the velocity
     */
    public double getPathPointVelocity(int index, Pose2d currentLocation) {
        double speed = mMaxSpeed;
        double pathDistance = getPathPoint(index).getDistance();
        double velocityLookAheadPoints = getVelocityLookAheadPoints(pathDistance);
        for (int i = index; i < index + velocityLookAheadPoints && i < getPoints().size(); i++) {
            speed = Math.min(speed, range(getPathPoint(i).getVelocity() / range(getTrackingError(currentLocation) / mTrackingErrorSpeed, 1, 3), getMinSpeed(pathDistance), getMaxSpeed(pathDistance)));
        }
        return speed;
    }

    /**
     * Calculates and returns the index of the point on the path (fPath) closest to the robots current position,
     * can then be passed into other methods to calculate following values.
     *
     * @param currentPosition the current Point of the robot
     * @return the index of the closest point
     */
    public int getClosestPointIndex(Point currentPosition) {
        if (mPath == null || mPath.size() == 0) return -1;

        double distance = Double.POSITIVE_INFINITY;
        int index = -1;

        for (int i = mLastCurrentPointIndex; i < getPath().size(); i++) {
            if(getPathPoint(i).getDistance() - getPathPoint(mLastCurrentPointIndex).getDistance() > getLookAheadDistance(getPathPoint(mLastCurrentPointIndex).getDistance())) {
                break;
            }

            double distanceFromCurrentPosition = currentPosition.distance(getPathPoint(i));
            if (distanceFromCurrentPosition < distance) {
                index = i;
                distance = distanceFromCurrentPosition;
            }
        }

        mLastCurrentPointIndex = index;

        if (mLastCurrentPointIndex < 0) {
            mLastCurrentPointIndex = 0;
        }

        return index;
    }

    /**
     * Calculates and returns the index of the pure pursuit look ahead point,
     * can then be passed into other methods to calculate following values.
     *
     * @param currentPosition the current Point of the robot
     * @return the index of the look ahead point
     */
    public int getLookAheadPointIndex(Pose2d currentPosition) {
        return getLookAheadPointIndex(currentPosition, getClosestPointIndex(currentPosition));
    }

    public int getLookAheadPointIndex(Pose2d currentPosition, int closest) {
        if (mPath == null || mPath.size() == 0) return -1;

        if(0 <= closest && closest < mPath.size()) {
            PathPoint closestPoint = getPathPoint(closest);
            double pathDistance = closestPoint.getDistance();

            for (int i = closest; i < getPath().size(); i++) {
                mCurvature = Math.abs(getCurvatureFromPathPoint(i, currentPosition));

                double curvature = 0;

                for (int p = closest; p <= i; p++) {
                    curvature += getPathPoint(p).getCurvature();
                }

                if (getPathPoint(i).getDistance() - pathDistance > getLookAheadDistance(pathDistance) / range(curvature / 3, 1, 2)) {
                    mLastPointIndex = i;
                    return i;
                }
            }

            if (closest != getPath().size() - 1) return getPath().size() - 1;
        }

        return -1;
    }

    /**
     * Calculates and returns straight line distance between the robot and the closest point on the path (fPath),
     * used by getPathPointVelocity to slow the following speed if the robot is far off the path.
     *
     * @param currentPosition the current Point of the robot
     * @return the index of the look ahead point
     */
    public double getTrackingError(Point currentPosition) {
        return getPathPoint(getClosestPointIndex(currentPosition)).distance(currentPosition) + Math.abs(mDeltaAngle * mTurnErrorScalar);
    }

    /**Methods creating and clearing the path*/

    /**
     * Resets all the run specific data, so a single path can be run more than once.
     */
    public void reset() {
        mLastPointIndex = 0;
        mLastCurrentPointIndex = 0;
        mTargetAngle = 0;
        mDeltaAngle = 0;
        mCurvature = 0.000001;
    }

    /**
     * Returns a single PathPoint from fPath
     *
     * @param point the index of the PathPoint
     * @return a PathPoint
     */
    private PathPoint getPathPoint(int point) {
        if (mPath != null) {
            return mPath.get(point);
        }
        build();
        return getPathPoint(point);
    }

    /**
     * Returns all points in path (fPath).
     *
     * @return a PathPoint ArrayList
     */
    private ArrayList<PathPoint> getPath() {
        if (mPath != null) {
            return mPath;
        }
        build();
        return getPath();
    }

    /**
     * Turns all the waypoints (fPoints) into a path (fPath).
     */
    public void build() {
        turnSpeedInterpolator = new ValueInterpolator(getTurnSpeed(), getDeviations().stream().filter(PathDeviationConfig::hasTurnSpeed).map(deviation -> new ValueInterpolator.ValueDeviation(deviation.getTurnSpeed(), deviation.getStart(), deviation.getEnd(), deviation.getStartRamp(), deviation.getEndRamp())).collect(Collectors.toList()));
        maxAccelerationInterpolator = new ValueInterpolator(getMaxAcceleration(), getDeviations().stream().filter(PathDeviationConfig::hasMaxAcceleration).map(deviation -> new ValueInterpolator.ValueDeviation(deviation.getMaxAcceleration(), deviation.getStart(), deviation.getEnd(), deviation.getStartRamp(), deviation.getEndRamp())).collect(Collectors.toList()));
        maxDecelerationInterpolator = new ValueInterpolator(getMaxDeceleration(), getDeviations().stream().filter(PathDeviationConfig::hasMaxDeceleration).map(deviation -> new ValueInterpolator.ValueDeviation(deviation.getMaxDeceleration(), deviation.getStart(), deviation.getEnd(), deviation.getStartRamp(), deviation.getEndRamp())).collect(Collectors.toList()));
        minSpeedInterpolator = new ValueInterpolator(getMinSpeed(), getDeviations().stream().filter(PathDeviationConfig::hasMinSpeed).map(deviation -> new ValueInterpolator.ValueDeviation(deviation.getMinSpeed(), deviation.getStart(), deviation.getEnd(), deviation.getStartRamp(), deviation.getEndRamp())).collect(Collectors.toList()));
        maxSpeedInterpolator = new ValueInterpolator(getMaxSpeed(), getDeviations().stream().filter(PathDeviationConfig::hasMaxSpeed).map(deviation -> new ValueInterpolator.ValueDeviation(deviation.getMaxSpeed(), deviation.getStart(), deviation.getEnd(), deviation.getStartRamp(), deviation.getEndRamp())).collect(Collectors.toList()));
        lookAheadDistanceInterpolator = new ValueInterpolator(getLookAheadDistance(), getDeviations().stream().filter(PathDeviationConfig::hasLookAheadDistance).map(deviation -> new ValueInterpolator.ValueDeviation(deviation.getLookAheadDistance(), deviation.getStart(), deviation.getEnd(), deviation.getStartRamp(), deviation.getEndRamp())).collect(Collectors.toList()));
        velocityLookAheadPointsInterpolator = new ValueInterpolator(getVelocityLookAheadPoints(), getDeviations().stream().filter(PathDeviationConfig::hasVelocityLookAheadPoints).map(deviation -> new ValueInterpolator.ValueDeviation(deviation.getVelocityLookAheadPoints(), deviation.getStart(), deviation.getEnd(), deviation.getStartRamp(), deviation.getEndRamp())).collect(Collectors.toList()));

        if (mPath != null) {
            return;
        }

        if (mPoints.size() == 0) {
            mPath = new ArrayList<>();
            return;
        }

        fill();

        smooth();

        createPath();
    }

    /**
     * Fills the spaces between waypoints (fPoints) with a point fPointSpacing inches.
     */
    protected void fill() {
        ArrayList<Point> newPoints = new ArrayList<>();

        mGeneratedPoints = (ArrayList<Point>) mPoints.clone();

        for (int s = 1; s < mGeneratedPoints.size(); s++) {
            Vector vector = new Vector(mGeneratedPoints.get(s - 1), mGeneratedPoints.get(s));

            int numPointsFit = (int) Math.ceil(vector.magnitude() / mPointSpacing);

            vector = vector.normalize().scale(mPointSpacing);

            for (int i = 0; i < numPointsFit; i++) {
                newPoints.add(mGeneratedPoints.get(s - 1).add(vector.scale(i)));
            }
        }

        newPoints.add(mGeneratedPoints.get(mGeneratedPoints.size() - 1));

        mGeneratedPoints = newPoints;
    }

    /**
     * Smooths the straight lines of points into a curved path.
     */
    protected void smooth() {
        double change = 0.5;
        double changedPoints = 1;
        while (change / changedPoints >= 0.01) {
            change = 0;
            changedPoints = 0;

            ArrayList<Point> newPoints = (ArrayList<Point>) mGeneratedPoints.clone();

            for (int i = 1; i < mGeneratedPoints.size() - 1; i++) {
                Point point = mGeneratedPoints.get(i);

                Vector middle = new Vector(mGeneratedPoints.get(i + 1).subtract(mGeneratedPoints.get(i - 1)));

                middle = new Vector(mGeneratedPoints.get(i - 1).add(middle.normalize().scale(middle.magnitude() / 2)));

                Vector delta = new Vector(middle.subtract(point));

                Point newPoint = point.add(delta.normalize().scale(delta.magnitude() * mPathSmoothing));

                if (!Double.isNaN(newPoint.getX()) && !Double.isNaN(newPoint.getY())) {
                    newPoints.set(i, newPoint);
                    change += point.distance(newPoint);
                    changedPoints++;
                }
            }

            mGeneratedPoints = newPoints;
        }
    }

    /**
     * Calculates a target velocity and curvature for every point on the path.
     */
    private void createPath() {

        mPath = new ArrayList<>();

        for (int p = 0; p < mGeneratedPoints.size(); p++) {
            mPath.add(new PathPoint(mGeneratedPoints.get(p), getPointDistance(p), getPointCurvature(p)));
        }

        for (int p = 0; p < mGeneratedPoints.size(); p++) {
            getPathPoint(p).setVelocity(getPointVelocity(p));
        }

        for (int p = mGeneratedPoints.size() - 2; p >= 0; p--) {
            getPathPoint(p).setVelocity(getPointNewVelocity(p));
        }
    }

    /**
     * Returns the distance a point is along the path.
     *
     * @param p the index of the point
     * @return the distance the point is along the path
     */
    private double getPointDistance(int p) {
        if (p == 0) return 0.0;
        return mGeneratedPoints.get(p).distance(mGeneratedPoints.get(p - 1)) + getPathPoint(p - 1).getDistance();
    }

    /**
     * Returns the curvature of the path at a point,
     * uses the getCurvature method.
     * Used by the getPointVelocity method.
     *
     * @param p the index of the point
     * @return the curvature of the path at the point, represent as 1 / radius of the circle made by the amount of curvature
     */
    private double getPointCurvature(int p) {
        if (p <= 0 || p >= mGeneratedPoints.size() - 1) return 0.0;
        return getCurvature(mGeneratedPoints.get(p), mGeneratedPoints.get(p - 1), mGeneratedPoints.get(p + 1));
    }

    /**
     * Returns the curvature between three points,
     * by fitting a circle to the points.
     *
     * @param p1 the index of the first point
     * @param p2 the index of the second point
     * @param p3 the index of the third point
     * @return the curvature represent as 1 / radius of the circle made by the amount of curvature
     */
    private double getCurvature(Point p1, Point p2, Point p3) {
        double x1 = p1.getX(), y1 = p1.getY();
        double x2 = p2.getX(), y2 = p2.getY();
        double x3 = p3.getX(), y3 = p3.getY();
        if (x1 == x2) x1 += 0.0001;
        double k1 = 0.5 * (Math.pow(x1, 2) + Math.pow(y1, 2) - Math.pow(x2, 2) - Math.pow(y2, 2)) / (x1 - x2);
        double k2 = (y1 - y2) / (x1 - x2);
        double b = 0.5 * (Math.pow(x2, 2) - 2 * x2 * k1 + Math.pow(y2, 2) - Math.pow(x3, 2) + 2 * x3 * k1 - Math.pow(y3, 2)) / (x3 * k2 - y3 + y2 - x2 * k2);
        double a = k1 - k2 * b;
        double r = Math.sqrt(Math.pow(x1 - a, 2) + Math.pow(y1 - b, 2));
        double c = 1 / r;
        if (Double.isNaN(c)) {
            return 0.0;
        }
        return c;
    }

    /**
     * Returns the first calculation velocity of the path at a point,
     * calculated using the amount of curvature at the point.
     * Uses the getPointCurvature method.
     *
     * @param p the index of the point
     * @return the first calculation of velocity
     */
    private double getPointVelocity(int p) {
        PathPoint point = getPathPoint(p);

        if (p <= 0 || p >= mGeneratedPoints.size() - 2) return getMinSpeed(point.getDistance());

        return Math.max(Math.min(getPathPoint(p - 1).getVelocity() + 2 * getMaxAcceleration(point.getDistance()) * mGeneratedPoints.get(p).distance(mGeneratedPoints.get(p + 1)), Math.min(getTurnSpeed(point.getDistance()) / point.getCurvature(), getMaxSpeed(point.getDistance()))), getMinSpeed(point.getDistance()));
    }

    /**
     * Returns the second/final calculation of the velocity of the path at a point,
     * calculated using fMaxAcceleration, and the speed at nearby points,
     * to make smooth and consistent acceleration and deceleration.
     *
     * @param p the index of the point
     * @return the second/final calculation of velocity
     */
    private double getPointNewVelocity(int p) {
        PathPoint point = getPathPoint(p);

        if (p >= mGeneratedPoints.size() - 2) return getMinSpeed(point.getDistance());

        return Math.min(getPathPoint(p).getVelocity(), Math.min(getPathPoint(p + 1).getVelocity() + 2 * getMaxDeceleration(point.getDistance()) * mGeneratedPoints.get(p).distance(mGeneratedPoints.get(p + 1)), getMaxSpeed(point.getDistance())));
    }

    /**
     * Returns number in the bounds of min and max
     *
     * @param number the initial number
     * @param min    the lower bound of the return value
     * @param max    the upper bound of the return value
     * @return the bounded number
     */
    private double range(double number, double min, double max) {
        if (number < min) {
            return min;
        }
        if (number > max) {
            return max;
        }
        return number;
    }
}