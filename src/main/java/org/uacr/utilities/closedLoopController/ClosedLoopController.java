package org.uacr.utilities.closedLoopController;

import org.uacr.models.exceptions.ConfigurationInvalidTypeException;
import org.uacr.utilities.Config;
import org.uacr.utilities.YamlConfigParser;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ClosedLoopController {

    private static final Map<String, Double> sProfileDefaults = new HashMap<>();

    static {
        sProfileDefaults.put("integral_range", -1.0);
        sProfileDefaults.put("max_integral", -1.0);
        sProfileDefaults.put("idle_output", 0.0);
    }

    private final YamlConfigParser fYamlConfigParser;
    private final String fType;
    private final String fName;
    private final Map<String, ClosedLoopControllerProfile> fProfiles = new HashMap<>();
    @Nullable
    private ClosedLoopControllerProfile mCurrentClosedLoopControllerProfile;
    private double mSetpoint = 0.0;
    private double mIntegral = 0.0;
    private double mPreviousError = 0.0;
    private long mPreviousTime = -1;

    public ClosedLoopController(String name) {
        fName = name;
        fYamlConfigParser = new YamlConfigParser();
        fYamlConfigParser.loadWithFolderName("closed-loop-profiles.yaml");

        Config config = fYamlConfigParser.getConfig(name);
        fType = config.getType();
        Object configProfiles = config.get("profiles");

        if (!(configProfiles instanceof Map)) {
            throw new ConfigurationInvalidTypeException("Map", "profiles", configProfiles);
        }

        Map<String, Map<String, Double>> pidProfiles = (Map<String, Map<String, Double>>) configProfiles;

        for (Map.Entry<String, Map<String, Double>> profile : pidProfiles.entrySet()) {
            Map<String, Double> PIDValues = new HashMap<>(sProfileDefaults);
            for (Map.Entry<String, Double> parameter : pidProfiles.get(profile.getKey()).entrySet()) {
                PIDValues.put(parameter.getKey(), parameter.getValue());
            }
            fProfiles.put(profile.getKey(), new ClosedLoopControllerProfile(profile.getKey(), PIDValues));
        }

    }

    public void setProfile(String name) {
        if (!fProfiles.containsKey(name)) {
            throw new RuntimeException(fName + "does not contain a profile named '" + name + "'");
        }
        mCurrentClosedLoopControllerProfile = fProfiles.get(name);
    }

    public void set(double setpoint) {
        mSetpoint = setpoint;
    }

    public void reset() {
        mIntegral = 0.0;
        mPreviousError = 0.0;
        mPreviousTime = -1;
    }

    public double getSetpoint() {
        return mSetpoint;
    }

    public double getIntegral() {
        return mIntegral;
    }

    public double getError(double measuredValue) {
        return mSetpoint - measuredValue;
    }

    public double getWithPID(double measuredValue) {
        assert mCurrentClosedLoopControllerProfile != null;
        long time = System.currentTimeMillis();

        if (mPreviousTime == -1) {
            mPreviousTime = time;
        }

        double deltaTime = (time - mPreviousTime) / 1000.0;

        double error = mSetpoint - measuredValue;

        boolean insideIntegralRange = (mCurrentClosedLoopControllerProfile.getIntegralRange() == -1 || Math.abs(error) <= mCurrentClosedLoopControllerProfile.getIntegralRange());

        if (insideIntegralRange) {
            mIntegral += deltaTime * error;

            if (mCurrentClosedLoopControllerProfile.getMaxIntegral() != -1) {
                if (mIntegral < 0.0) {
                    mIntegral = Math.max(mIntegral, -mCurrentClosedLoopControllerProfile.getMaxIntegral());
                } else {
                    mIntegral = Math.min(mIntegral, mCurrentClosedLoopControllerProfile.getMaxIntegral());
                }
            }
        } else {
            mIntegral = 0.0;
        }

        double deltaError = error - mPreviousError;
        double derivative = deltaTime != 0.0 ? deltaError / deltaTime : Double.MAX_VALUE;

        if (derivative == Double.MAX_VALUE) {
            //	sLogger.warn("Derivative is at max value (no delta time) and will be multiplied by {}", fCurrentProfile.d);
        }

        double p = mCurrentClosedLoopControllerProfile.getP() * error;
        double i = mCurrentClosedLoopControllerProfile.getI() * mIntegral;
        double d = mCurrentClosedLoopControllerProfile.getD() * derivative;

        double output = mCurrentClosedLoopControllerProfile.getF() * mSetpoint + p + i + d + mCurrentClosedLoopControllerProfile.getIdleOutput();

        mPreviousTime = time;
        mPreviousError = error;

        if (mCurrentClosedLoopControllerProfile.hasForceCompensation()) {
            return Math.signum(output) * Math.min(Math.abs(output), mCurrentClosedLoopControllerProfile.getMaxOutput()) + (mCurrentClosedLoopControllerProfile.getKForceCompensation() * Math.sin(measuredValue));
        } else {
            return Math.signum(output) * Math.min(Math.abs(output), mCurrentClosedLoopControllerProfile.getMaxOutput());
        }
    }

    public double get(double measuredValue, double acceleration) {
        assert mCurrentClosedLoopControllerProfile != null;
        if (!mCurrentClosedLoopControllerProfile.hasFeedForward()) {
            throw new RuntimeException("The profile provided must include feedforward constants 'ka' and 'kv'");
        }

        double pidValue = getWithPID(measuredValue);
        return mCurrentClosedLoopControllerProfile.getKv() * getSetpoint() + mCurrentClosedLoopControllerProfile.getka() * acceleration + pidValue;
    }

}