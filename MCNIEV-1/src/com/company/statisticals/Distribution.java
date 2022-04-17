package com.company.statisticals;

public class Distribution {
    public static float normalpdf(float x, float mean, float std) {
        return (float) (Math.pow(Math.E, -Math.pow(x - mean, 2) / (2 * Math.pow(std, 2))) / (std * Math.sqrt(2 * Math.PI)));
    }
}
