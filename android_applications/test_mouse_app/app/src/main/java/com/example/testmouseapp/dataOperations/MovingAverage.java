package com.example.testmouseapp.dataOperations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage {

    private final Queue<BigDecimal> window = new LinkedList<BigDecimal>();
    private final int period;
    private BigDecimal sum = BigDecimal.ZERO;

    public MovingAverage(int period) {
        assert period > 0 : "Period must be a positive integer";
        this.period = period;
    }

    public void addToWindow(double value) {
        BigDecimal BGval = BigDecimal.valueOf(value);
        sum = sum.add(BGval);
        window.add(BGval);

        if (window.size() > period) {
            sum = sum.subtract(window.remove());
        }
    }

    //TODO: fix weird float-double-BigDecimal disparity across code
    public float calculateAverage() {
        if (window.isEmpty()) {
            return 0;
        }

        BigDecimal divisor = BigDecimal.valueOf(window.size());
        BigDecimal BGrslt =  sum.divide(divisor, 2, RoundingMode.HALF_UP);
        return BGrslt.floatValue();
    }



}
