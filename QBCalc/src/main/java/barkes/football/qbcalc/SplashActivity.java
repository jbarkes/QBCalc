/**
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * November 11, 2013
 */
package barkes.football.qbcalc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class SplashActivity extends Activity {
    private long mElapsed = 0;
    private long mSplashTime = 2000;
    private boolean mSplashActive = true;
    private boolean mPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** Hide the titlebar */
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splash);

        Thread splashView = new Thread() {
            public void run() {
                try {
                    while (mSplashActive && mElapsed < mSplashTime) {
                        if (!mPaused)
                            mElapsed = mElapsed + 100;
                        sleep(100);
                    }
                }
                catch(Exception e) {
                    /** Do nothing */
                }
                finally {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        };

        splashView.start();
    }
}
