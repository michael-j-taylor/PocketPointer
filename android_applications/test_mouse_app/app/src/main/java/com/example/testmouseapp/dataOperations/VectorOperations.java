package com.example.testmouseapp.dataOperations;

public class VectorOperations
{
    public static double getDirection(double x, double y)
    {
        return Math.tan(x/y);
    }

    public static boolean oppositeDirection(double aX, double aY, double vX, double vY)
    {
        double direction1 = getDirection(aX, aY);
        double direction2 = getDirection(vX, vY);
        if (Math.abs(direction1 - direction2) <= Math.sqrt(3)/4)
            return true;
        else
            return false;
    }
}
