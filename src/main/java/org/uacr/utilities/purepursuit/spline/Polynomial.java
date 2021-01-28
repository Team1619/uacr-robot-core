package org.uacr.utilities.purepursuit.spline;

public class Polynomial {
        private int order;
        private double[] coefficients;

        private double lowerBound;
        private double upperBound;
        private double offset;

        public Polynomial(int order, double lowerBound, double upperBound, double offset, double... coefficients) {
            if (coefficients.length != order + 1) {
                throw new IllegalArgumentException("Polynomial must have the same number of coefficients as its order");
            }
            this.order = order;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            this.offset = offset;
            this.coefficients = coefficients;
        }

        public double getLowerBound() {
            return lowerBound;
        }

        public double getUpperBound() {
            return upperBound;
        }

        public double eval(double x) {
            double result = 0;
            for (int i = 0; i < coefficients.length; i++) {
                result += coefficients[i] * Math.pow(x - offset, i);
            }
            return result;
        }

        public Polynomial getDerivative() {
            double[] newCoefficients = new double[order];
            for (int i = 1; i < coefficients.length; i++) {
                newCoefficients[i - 1] = coefficients[i] * (i + 1);
            }
            return new Polynomial(order - 1, lowerBound, upperBound, offset, newCoefficients);
        }

        public String toString() {
            String result = "Polynomial of order " + order + ": ";
            for (int i = 0; i < coefficients.length; i++) {
                result += coefficients[i] + "(x-" + offset + ")^" + i + "+ ";
            }
            return result + "\b\b";
        }
    }