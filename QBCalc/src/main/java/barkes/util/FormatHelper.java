/**
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * November 11, 2013
 */
package barkes.util;

import java.text.DecimalFormat;

/**
 * Created by Jason Barkes on 12/1/1013.
 */
public class FormatHelper {

    /**
     * Helper method to round a double to a given number of decimal places.
     */
    public static double quickRound(double d, String fmt) {
        DecimalFormat twoPlaces = new DecimalFormat(fmt);
        return Double.valueOf(twoPlaces.format(d));
    }
}
