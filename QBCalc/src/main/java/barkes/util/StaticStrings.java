/*************************************************************************************************
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * November 11, 2013
 *************************************************************************************************/
package barkes.util;

/**
 * Created by Jason Barkes on 11/26/2013.
 */
public class StaticStrings {

    /** JSON keys */
    public static final String JSON_KEY_RECENTS = "recents";
    public static final String KEY_COMPLETIONS = "completions";
    public static final String KEY_ATTEMPTS = "attempts";
    public static final String KEY_YARDS = "yards";
    public static final String KEY_TDS = "tds";
    public static final String KEY_INTERCEPTIONS = "interceptions";
    public static final String KEY_COMPLETION_PERCENTAGE = "completionPercentage";
    public static final String KEY_YARDS_PER_ATTEMPT = "yardsPerAttempt";
    public static final String KEY_TD_PERCENTAGE = "touchdownPercentage";
    public static final String KEY_INTERCEPTION_PERCENTAGE = "interceptionPercentage";
    public static final String KEY_RATING = "rating";
    public static final String KEY_LEAGUE = "league";
    public static final String KEY_DATE = "date";
    public static final String KEY_TEAM = "team";
    public static final String KEY_PLAYER = "player";

    /** Formats */
    public static final String FMT_DBL_PREC = "#.##";
    public static final String FMT_SGL_PREC = "#.#";
    public static final String FMT_SHORT_DT = "MM/dd/yyyy";
    public static final String MSG_TYPE_RFC822 = "message/rfc822";
    public static final String MSG_TYPE_UTF8 = "UTF-8";
    public static final String MSG_TYPE_CSV = "text/csv";
    public static final String CSV_FILENAME = "qb-ratings.csv";

    /** Errors */
    public static final String LOG_TAG = "QBCALC_LOG";
    public static final String ERR_CALC_REQUIRED = "Please calculate the QB rating first";

    /** Titles */
    public static final String DLG_TITLE_SELECT_APP = "Select App:";
}
