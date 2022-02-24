package com.harjeet.trackerever.MyUtils;

public class DistanceAlgorithm
{
    public static double PIx = 3.141592653589793;
    public static double RADIUS = 6378.16;

    public static double Radians(double x)
    {
        return x * PIx / 180;
    }

    public static double DistanceBetweenPlaces(
            double lat1,
            double lon1,
            double lat2,
            double lon2
            )
    {
        double dlon = Radians(lon2 - lon1);
        double dlat = Radians(lat2 - lat1);

        double a = (Math.sin(dlat / 2) * Math.sin(dlat / 2)) + Math.cos(Radians(lat1)) * Math.cos(Radians(lat2)) * (Math.sin(dlon / 2) * Math.sin(dlon / 2));
        double angle = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return angle * RADIUS;
    }

}