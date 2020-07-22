package org.uacr.utilities.purepursuit.controller;

import org.uacr.utilities.purepursuit.Path;
import org.uacr.utilities.purepursuit.Pose2d;

import javax.annotation.Nullable;

public abstract class WestCoastPurePursuitController extends PurePursuitController {

    private final double fTrackWidth;

    @Nullable
    private Path mCurrentPath;
    private Pose2d mCurrentPose;
    private Pose2d mFollowPose;
    private FollowDirection mFollowDirection;
    private boolean mIsFollowing;

    public WestCoastPurePursuitController(double trackWidth) {
        fTrackWidth = trackWidth;

        mCurrentPath = null;
        mCurrentPose = new Pose2d();
        mFollowPose = new Pose2d();
        mFollowDirection = FollowDirection.FORWARD;
        mIsFollowing = false;
    }

    public double getTrackWidth() {
        return fTrackWidth;
    }

    public void followPath(Path path) {
        mCurrentPath = path;
        mCurrentPath.reset();
        reset();
        mIsFollowing = true;
    }

    public boolean isFollowing() {
        return mIsFollowing;
    }

    public boolean isPathFinished() {
        return !isFollowing();
    }

    public FollowDirection getFollowDirection() {
        return mFollowDirection;
    }

    public void setFollowDirection(FollowDirection followDirection) {
        mFollowDirection = followDirection;
    }

    public void reset() {
        mCurrentPose = new Pose2d();
        mFollowPose = new Pose2d();
    }

    public void update() {
        if (mCurrentPath == null || !mIsFollowing) {
            stopDrive();

            return;
        }

        mCurrentPose = getCurrentPose();

        mFollowPose = mCurrentPose.clone();

        if (mFollowDirection == FollowDirection.REVERSE) {
            mFollowPose = new Pose2d(mFollowPose.getX(), mFollowPose.getY(), ((mFollowPose.getHeading() + 360) % 360) - 180);
        }

        int lookahead = mCurrentPath.getLookAheadPointIndex(mFollowPose);
        int closest = mCurrentPath.getClosestPointIndex(mFollowPose);

        if (lookahead == -1) {
            stopDrive();

            mIsFollowing = false;
            return;
        }

        // Uses the path object to calculate curvature and velocity values
        double velocity = mCurrentPath.getPathPointVelocity(closest, mFollowPose);
        double curvature = mCurrentPath.getCurvatureFromPathPoint(lookahead, mFollowPose);

        updateDriveVelocities(velocity, curvature);
    }

    protected void updateDriveVelocities(double velocity, double curvature) {
        if (mFollowDirection == FollowDirection.REVERSE) {
            setDriveVelocities(-(velocity * ((1.5 - curvature * fTrackWidth) / 1.5)),
                    -(velocity * ((1.5 + curvature * fTrackWidth) / 1.5)));
        } else {
            setDriveVelocities(velocity * ((1.5 + curvature * fTrackWidth) / 1.5),
                    velocity * ((1.5 - curvature * fTrackWidth) / 1.5));
        }
    }

    public void stopDrive() {
        setDriveVelocities(0.0, 0.0);
    }

    public abstract void setDriveVelocities(double leftVelocity, double rightVelocity);

    public abstract Pose2d getCurrentPose();

    public enum FollowDirection {
        FORWARD,
        REVERSE
    }
}
