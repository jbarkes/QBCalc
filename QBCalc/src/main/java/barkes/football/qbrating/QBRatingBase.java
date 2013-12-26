/*************************************************************************************************
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * November 11, 2013
 *************************************************************************************************/
package barkes.football.qbrating;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import barkes.football.qbcalc.BuildConfig;
import barkes.util.StaticStrings;
import barkes.util.FormatHelper;
import barkes.util.SmartLog;

/**
 * Base QBRating class.
 * Created by Jason Barkes on 11/16/2013.
 */
public class QBRatingBase {
    /**
     * Public properties.
     */
    public int completions;
    public int attempts;
    public int yards;
    public int tds;
    public int interceptions;
    public String date;
    public String team;
    public String player;

    /**
     * Protected properties.
     */
    protected double completionPercentage;
    protected double yardsPerAttempt;
    protected double touchdownPercentage;
    protected double interceptionPercentage;
    protected double rating;
    protected LEAGUE league;

    /**
     * League (calculation method) enumeration.
     */
    public enum LEAGUE {
        NFL, NCAA, AFL, CFL, HS
    }

    public static String[] LEAGUES = {
        "NFL",
        "NCAA",
        "AFL",
        "CFL",
        "HS"
    };

    /**
     * CSV headers.
     */
    public static enum CSV_HEADER {
        League, Team, Player, CMP, ATT, CMP_Percentage, YDS, YPA,
        TD, TD_Percentage, INT, INT_Percentage, QB_Rating
    }

    public static double getPerfectRating(LEAGUE league) {
        double perfect = 0;

        switch (league) {
            case AFL:
            case CFL:
            case NFL:
                perfect = 158.3;
                break;
            case HS:
            case NCAA:
                perfect = 1261.6;
                break;
        }

        return perfect;
    }

    /**
     * Initialization block.
     */
    {
        completions = 0;
        attempts = 0;
        yards = 0;
        tds = 0;
        interceptions = 0;
        completionPercentage = 0;
        yardsPerAttempt = 0;
        touchdownPercentage = 0;
        interceptionPercentage = 0;
        rating = 0;
        league = LEAGUE.NFL;
        date = "";
        team = "";
        player = "";
    }

    /**
     * Constructor.
     */
    public QBRatingBase(int completions, int attempts, int yards, int tds, int interceptions, LEAGUE league,
                        String date, String team, String player) {
        this.completions = completions;
        this.attempts = attempts;
        this.yards = yards;
        this.tds = tds;
        this.interceptions = interceptions;
        this.league = league;
        this.date = date;
        this.team = team;
        this.player = player;
    }

    /**
     * Completion Percentage getter.
     */
    public double getCompletionPercentage() {
        completionPercentage = FormatHelper.quickRound(((double) completions / (double) attempts) * 100, StaticStrings.FMT_DBL_PREC);
        return completionPercentage;
    }

    /**
     * Yards Per Attempt getter.
     */
    public double getYardsPerAttempt() {
        yardsPerAttempt = FormatHelper.quickRound((double) yards / (double) attempts, StaticStrings.FMT_DBL_PREC);
        return yardsPerAttempt;
    }

    /**
     * Touchdown Percentage getter.
     */
    public double getTouchdownPercentage() {
        touchdownPercentage = FormatHelper.quickRound(((double) tds / (double) attempts) * 100, StaticStrings.FMT_DBL_PREC);
        return touchdownPercentage;
    }

    /**
     * Interception Percentage getter.
     */
    public double getInterceptionPercentage() {
        interceptionPercentage = FormatHelper.quickRound(((double) interceptions / (double) attempts) * 100, StaticStrings.FMT_DBL_PREC);
        return interceptionPercentage;
    }

    /**
     * QB Rating getter.
     */
    public double getRating() {
        return rating;
    }

    /**
     * League (calculation formula) getter.
     */
    public LEAGUE getLeague() {
        return league;
    }

    /**
     * League (calculation formula) setter.
     */
    public void setLeague(LEAGUE league) {
        this.league = league;
    }

    /**
     * Date getter.
     */
    public String getDate() {
        return date;
    }

    /**
     * Date setter.
     */
    public void setDate(Date date) {
        this.date = new SimpleDateFormat(StaticStrings.FMT_SHORT_DT).format(date);
    }

    /**
     * Date setter.
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Helper method to return the calculation method (league).
     */
    public String getDispName() {
        return league.name();
    }

    /**
     * Helper method to return a typical formatted QB stat line.
     */
    public String getStatLine() {
        return completions + "-"
             + attempts + ", "
             + yards + " YDS, "
             + tds + " TD, "
             + interceptions + " INT";
    }

    /**
     * Return a QBRatingBase object from a JSONObject.
     */
    public static QBRatingBase fromJSONObject(final JSONObject jsonObject) {

        QBRatingBase QB = null;
        try {
            QB = new QBRatingBase(
                jsonObject.getInt(StaticStrings.KEY_COMPLETIONS),
                jsonObject.getInt(StaticStrings.KEY_ATTEMPTS),
                jsonObject.getInt(StaticStrings.KEY_YARDS),
                jsonObject.getInt(StaticStrings.KEY_TDS),
                jsonObject.getInt(StaticStrings.KEY_INTERCEPTIONS),
                LEAGUE.valueOf(jsonObject.getString(StaticStrings.KEY_LEAGUE)),
                jsonObject.getString(StaticStrings.KEY_DATE),
                jsonObject.getString(StaticStrings.KEY_TEAM),
                jsonObject.getString(StaticStrings.KEY_PLAYER)
            );

            QB.completionPercentage = jsonObject.getDouble(StaticStrings.KEY_COMPLETION_PERCENTAGE);
            QB.yardsPerAttempt = jsonObject.getDouble(StaticStrings.KEY_YARDS_PER_ATTEMPT);
            QB.touchdownPercentage = jsonObject.getDouble(StaticStrings.KEY_TD_PERCENTAGE);
            QB.interceptionPercentage = jsonObject.getDouble(StaticStrings.KEY_INTERCEPTION_PERCENTAGE);
            QB.rating = jsonObject.getDouble(StaticStrings.KEY_RATING);
        }
        catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                SmartLog.w(StaticStrings.LOG_TAG, "Exception caught: " + e.getMessage());
                SmartLog.d(StaticStrings.LOG_TAG, Log.getStackTraceString(e));
            }
        }

        return QB;
    }
}
