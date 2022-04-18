package com.company.arithmetics;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FACT {
    private static double gamma(double x, double n) {
        return Math.pow((x / (1 - x)), n) * Math.pow(Math.E, (x / (x - 1))) / Math.pow(1 - x, 2);
    }

    public static double solve(Double target) {
        double result = 0;

        double interval = 1.0 / 5000;
        double factor = interval / 3;
        double[] x = new double[5000 + 1];

        for (int i = 0; i < x.length; i++) {
            double temp = interval * i;
            x[i] = temp == 1 ? 0.999999 : temp;
        }

        for (int i = 0; i < x.length; i++) {
            if (i == 0 || i == x.length - 1) result += gamma(x[i], target);
            else if (i % 2 == 0) result += 2 * gamma(x[i], target);
            else result += 4 * gamma(x[i], target);
        }

        result *= factor;
        return BigDecimal.valueOf(Math.abs(result)).setScale(6, RoundingMode.HALF_UP).doubleValue();
    }
}
