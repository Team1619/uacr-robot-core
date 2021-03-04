package org.uacr.utilities.closedloopcontroller;

import java.util.Locale;

public class PIDController {
    double sensorValue;
    double setPoint;

    double proportional;
    double integral = 0;
    double derivative;
    double controlValue;
    double error;

    double previousTimeMs = Double.NaN;
    double elapsedTimeSec;
    double previousSensorValue = Double.NaN;

    //Gain values: change to increase/decrease the influence each part of the PID controller has on the output
    double proportionalGain = 1.0;
    double integralGain = 1.0;
    double derivativeGain = 0.1;

    public void setSetPoint(double setPoint) {
        this.setPoint = setPoint;
    }

    public double updateControlValue(double sensorValue, double currentTimeMs) {

        //Bounds the received sensorValue to between -1.0 and 1.0
        this.sensorValue = bounds(-1.0, 1.0, sensorValue);

        //The error term: the difference between the setPoint and the current position (sensorValue)
        error = setPoint - this.sensorValue;
        if (Double.isNaN(previousSensorValue)) {
            previousSensorValue = sensorValue;
        }
        if (Double.isNaN(previousTimeMs)) {
            previousTimeMs = currentTimeMs - 30;
        }

        //The proportional component: the maximum speed from the proportional component is 1.0 * proportionalGain
        proportional = error / 2.0;

        //The integral component: gets the change in time in seconds, multiplies it by the error (to scale the value), and adds it to the running total of the integral.
        elapsedTimeSec = (currentTimeMs - previousTimeMs) / 1000;
        double integralDelta = error * elapsedTimeSec;

        //The derivative component: Applies derivative to sensorValue rather than the error, bypassing derivative kick
        //Learn more about derivative kick here: https://apmonitor.com/pdc/index.php/Main/ProportionalIntegralDerivative
        double deltaSensorValue = this.sensorValue - previousSensorValue;
        if (elapsedTimeSec == 0.0) {
            if (deltaSensorValue < 0.0) {
                derivative = Double.MAX_VALUE;
            } else if (deltaSensorValue > 0.0) {
                derivative = Double.MIN_VALUE;
            } else {
                derivative = 0.0;
            }
        } else {
            derivative = - deltaSensorValue / elapsedTimeSec;
        }

        //Sums up all components of the PID controller and outputs the raw value
        double preclampControlValue = proportional * proportionalGain + (integral + integralDelta) * integralGain + derivative * derivativeGain;

        //Running checks for the integral component to see if:
        //A) The value is saturating
        //B) The total integral and the added integral are the same sign
        if (Math.abs(preclampControlValue) > 1.0 && Math.signum(error) == Math.signum(preclampControlValue)) {
            integralDelta = 0;
        }

        integral += integralDelta;
        previousTimeMs = currentTimeMs;
        previousSensorValue = sensorValue;
        controlValue = bounds(-1.0, 1.0, preclampControlValue);
        return controlValue;
    }

    private static double bounds(double min, double max, double value) {
        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }
        return value;
    }

    public String toString () {
        return String.format(Locale.ENGLISH,
                "PID: S:%.2f E:%.2f C:%.2f P:%.2f I:%.2f D:%.2f dT:%.2f",
                sensorValue,
                error,
                controlValue,
                proportional,
                integral,
                derivative,
                elapsedTimeSec
        );
    }
}
