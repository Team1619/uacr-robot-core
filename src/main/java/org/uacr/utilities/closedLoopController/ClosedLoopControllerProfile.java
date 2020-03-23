package org.uacr.utilities.closedLoopController;

import org.uacr.models.exceptions.ConfigurationException;

import java.util.Map;

public class ClosedLoopControllerProfile {

    private String fName;

    private double mF;
    private double mP;
    private double mI;
    private double mD;
    private double mMaxIntegral;
    private double mIntegralRange;
    private double mMaxOutput;
    private double mIdleOutput;
    private double mKv;
    private double mKa;
    private double mKForceCompensation;
    private boolean mHasFeedForward;
    private boolean mHasForceCompensation;

    public ClosedLoopControllerProfile(String name, Map<String, Double> values) {
        fName = name;
        if (values.containsKey("f") && values.containsKey("p") && values.containsKey("i") && values.containsKey("d") &&
                values.containsKey("max_integral") && values.containsKey("integral_range") && values.containsKey("max_output") && values.containsKey("idle_output")) {
            mF = values.get("f");
            mP = values.get("p");
            mI = values.get("i");
            mD = values.get("d");

            mMaxIntegral = values.get("max_integral");
            mIntegralRange = values.get("integral_range");
            mMaxOutput = values.get("max_output");
            mIdleOutput = values.get("idle_output");

            if (values.containsKey("ka") && values.containsKey("kv")) {
                mHasFeedForward = true;
                mKa = values.get("ka");
                mKv = values.get("kv");
            }

            if (values.containsKey("force_compensation")) {
                mHasForceCompensation = true;
                mKForceCompensation = values.get("force_compensation");

            }
        } else {
            throw new ConfigurationException("Must provide value for 'f', 'p', 'i', 'd', 'max_output'");
        }
    }

    public double getF() {
        return mF;
    }

    public double getP() {
        return mP;
    }

    public double getI() {
        return mI;
    }

    public double getD() {
        return mD;
    }

    public double getMaxIntegral() {
        return mMaxIntegral;
    }

    public double getIntegralRange() {
        return mIntegralRange;
    }

    public double getMaxOutput() {
        return mMaxOutput;
    }

    public double getIdleOutput() {
        return mIdleOutput;
    }

    public double getKv() {
        return mKv;
    }

    public double getka() {
        return mKa;
    }

    public double getKForceCompensation() {
        return mKForceCompensation;
    }

    public boolean hasFeedForward() {
        return mHasFeedForward;
    }

    public boolean hasForceCompensation() {
        return mHasForceCompensation;
    }
}
