package org.uacr.utilities;

/**
 * PIDFProfile keeps track of PIDF values for motion magic
 *
 * @author Matthew Oates
 */

public class PIDFProfile {

    private final double fP;
    private final double fI;
    private final double fD;
    private final double fF;

    public PIDFProfile(double p, double i, double d, double f) {
        fP = p;
        fI = i;
        fD = d;
        fF = f;
    }

    public PIDFProfile(double p, double i, double d) {
        this(p, i, d, 0);
    }

    public PIDFProfile() {
        this(0, 0, 0);
    }

    /**
     * @return the P value for this profile
     */
    public double getP() {
        return fP;
    }

    /**
     * @return the I value for this profile
     */
    public double getI() {
        return fI;
    }

    /**
     * @return the D value for this profile
     */
    public double getD() {
        return fD;
    }

    /**
     * @return the F value for this profile
     */
    public double getF() {
        return fF;
    }

    /**
     * @return if the profile that is passed in has the same values as this profile
     */
    public boolean equals(PIDFProfile profile) {
        return fP == profile.getP() && fI == profile.fI && fD == profile.getD() && fF == profile.getF();
    }
}
