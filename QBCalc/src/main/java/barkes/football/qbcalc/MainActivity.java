/*************************************************************************************************
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * 11/13/2013
 *************************************************************************************************/
package barkes.football.qbcalc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Locale;

import barkes.football.qbrating.QBRatingBase;

public class MainActivity extends ActionBarActivity
        implements ActionBar.TabListener,
                   RatingFrag.QBFragImpl.OnRatingCalculatedListener,
                   RecentsFrag.OnRecentsActionsListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         * Handle action bar item clicks here. The action bar will
         * automatically handle clicks on the Home/Up button, so long
         * as you specify a parent activity in AndroidManifest.xml.
         */
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.action_about:
                showAboutDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        /** When the given tab is selected, switch to the corresponding page in the ViewPager. */
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    private void showAboutDialog() {

        /**
         * DialogFragment.show() will take care of adding the fragment
         * in a transaction.  We also want to remove any currently showing
         * dialog, so make our own transaction and take care of that here.
         */
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        /** Create and show the dialog. */
        AboutDialogFrag newFragment = AboutDialogFrag.newInstance();
        newFragment.show(ft, "dialog");
    }

    public Fragment findFragmentByPosition(int position) {
        FragmentPagerAdapter fragmentPagerAdapter = mSectionsPagerAdapter;
        return getSupportFragmentManager().findFragmentByTag(
            "android:switcher:" + mViewPager.getId() + ":"
                + fragmentPagerAdapter.getItemId(position));
    }

    @Override
    public void onRatingCalculated(QBRatingBase qbRating) {
        /** A new QB Rating was calculated, so update the Recents list */
        RecentsFrag recentsFragment = (RecentsFrag)
            findFragmentByPosition(1);

        if(recentsFragment != null) {
            recentsFragment.updateData(qbRating);
        }
    }

    @Override
    public void onRecentsCleared() {
        /** Let the Calculator Fragment know the Recents history has been cleared */
        RatingFrag.QBFragImpl calcFragment = (RatingFrag.QBFragImpl)
            findFragmentByPosition(0);

        if(calcFragment != null) {
            calcFragment.resetRecentsHistory();
        }
    }

    @Override
    public void onRatingOpen(QBRatingBase qb) {
        mViewPager.setCurrentItem(0, true);

        RatingFrag.QBFragImpl calcFragment = (RatingFrag.QBFragImpl)
                findFragmentByPosition(0);

        if(calcFragment != null) {
            calcFragment.setRating(qb);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * getItem is called to instantiate the fragment for the given page.
         */
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return RatingFrag.QBFragImpl.newInstance();
                case 1:
                    return new RecentsFrag();
            }
            return null;
        }

        @Override
        public int getCount() {
            /** Show 2 fragments */
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section_nfl).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section_main).toUpperCase(l);
            }
            return null;
        }
    }
}
