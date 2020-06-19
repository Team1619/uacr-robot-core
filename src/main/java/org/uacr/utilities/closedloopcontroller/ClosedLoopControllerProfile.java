package org.uacr.utilities.closedloopcontroller;

import org.uacr.models.exceptions.ConfigurationException;

import java.util.Map;

public class ClosedLoopControllerProfile {

    private String fName;

    private final double fF;
    private final double fP;
    private final double fI;
    private final double fD;
    private final double fMaxIntegral;
    private final double fIntegralRange;
    private final double fMaxOutput;
    private final double fIdleOutput;

    private double mKv;
    private double mKa;
    private double mKForceCompensation;
    private boolean mHasFeedForward;
    private boolean mHasForceCompensation;

    public ClosedLoopControllerProfile(String name, Map<String, Double> values) {
        fName = name;
        if (values.containsKey("f") && values.containsKey("p") && values.containsKey("i") && values.containsKey("d") &&
                values.containsKey("max_integral") && values.containsKey("integral_range") && values.containsKey("max_output") && values.containsKey("idle_output")) {
            fF = values.get("f");
            fP = values.get("p");
            fI = values.get("i");
            fD = values.get("d");

            fMaxIntegral = values.get("max_integral");
            fIntegralRange = values.get("integral_range");
            fMaxOutput = values.get("max_output");
            fIdleOutput = values.get("idle_output");

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
        return fF;
    }

    public double getP() {
        return fP;
    }

    public double getI() {
        return fI;
    }

    public double getD() {
        return fD;
    }

    public double getMaxIntegral() {
        return fMaxIntegral;
    }

    public double getIntegralRange() {
        return fIntegralRange;
    }

    public double getMaxOutput() {
        return fMaxOutput;
    }

    public double getIdleOutput() {
        return fIdleOutput;
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
