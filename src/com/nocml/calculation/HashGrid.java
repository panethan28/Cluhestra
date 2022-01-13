package com.nocml.calculation;

import java.io.Serializable;

import static com.nocml.calculation.Config.*;

public class HashGrid implements Serializable {
    private static final long serialVersionUID = 7887702906028551410L;
    private final long hashcode;
    public static final double maxlat = max_lat;
    public static final double maxlon = max_lon;
    public static final double minlat = min_lat;
    public static final double minlon = min_lon;
    public static final long width = new Double(maxlat*1000000 - minlat*1000000).longValue();
    public static final long length = new Double(maxlon*1000000 - minlon*1000000).longValue();
    public static final long gridlength = length/girds;
    public static final long gridwidth = width/girds;

    public HashGrid(Double lon, Double lat) {
        long hashcode1;

        hashcode1 = (new Double(lat*1000000-minlat*1000000).longValue()/gridwidth + 1)*(new Double(lon*1000000-minlon*1000000).longValue()/gridlength + 1);
        if (hashcode1 >100L){
//            System.out.println("warning!!!"+hashcode+" gps ("+lon+","+lat + ")");
            hashcode1 = -1;
        }
        hashcode = hashcode1;
    }

    @Override
    public int hashCode() {

        return (int)hashcode;
    }
}
