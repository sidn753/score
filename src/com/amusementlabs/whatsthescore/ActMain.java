
package com.amusementlabs.whatsthescore;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.amusementlabs.whatsthescore.nav.ActPrefs;
import com.amusementlabs.whatsthescore.nav.MenuItemHelper;
import com.amusementlabs.whatsthescore.nav.NavDrawer;
import com.amusementlabs.whatsthescore.util.PrefsHelper;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import static com.amusementlabs.whatsthescore.util.Constants.EDIT_PREFS_REQUEST_CODE;
import static com.amusementlabs.whatsthescore.util.Constants.OPENED_FROM_TIMER_NOTIFICATION;

//TODO enable or disable ads here
//@EActivity(R.layout.act_main)
@EActivity(R.layout.act_main_ad)
public class ActMain extends SherlockFragmentActivity {


    private static final String CURRENT_FRAGMENT = "currentFragment";
    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.left_drawer)
    ListView mDrawerList;


    private NavDrawer mNavDrawer;
    private CharSequence mTitle;


    CharSequence mStoredTitle;
    String mCurrentFragment;
    private TextView mActionBarTitle;
    private LinearLayout mActionBarContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //drawer title is set to app title, currently does not get changed
        mTitle = getTitle();

        ActionBar ab = getSupportActionBar();

        // enable ActionBar app icon to behave as action to toggle nav drawer
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);


        ab.setDisplayShowTitleEnabled(false);
        LayoutInflater inflater = LayoutInflater.from(this);

        View mCustomView = inflater.inflate(R.layout.actionbar_title, null);
        mActionBarTitle = (TextView) mCustomView.findViewById(R.id.actionbar_title);
        mActionBarContainer = (LinearLayout) mCustomView.findViewById(R.id.actionbar_container);
        mActionBarTitle.setText("What's the Score");
        ab.setCustomView(mCustomView);
        ab.setDisplayShowCustomEnabled(true);


        //set default prefs and fake return from prefs activity to update local vars
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        PrefsHelper.updatePrefs(this);

    }


    @AfterViews
    protected void setUpViews() {
        //create NavDrawer helper
        mNavDrawer = new NavDrawer(this, mTitle, mDrawerLayout, mDrawerList);


        //if app is being opened from timer notif click, navigate directly to timer
        if (getIntent().getBooleanExtra(OPENED_FROM_TIMER_NOTIFICATION, false)) {
            changeFragment(getString(R.string.nav_timer_title));
        } else {

            changeFragment(mCurrentFragment);
        }

    }


    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        boolean drawerOpen = mNavDrawer.isNavDrawerOpen();

        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return mNavDrawer.getDrawerToggle().onOptionsItemSelected(MenuItemHelper.getMenuItem(item)) || super.onOptionsItemSelected(item);

    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the toggle state after onRestoreInstanceState has occurred.
        mNavDrawer.getDrawerToggle().syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Pass any configuration change to the drawer toggle
        mNavDrawer.getDrawerToggle().onConfigurationChanged(newConfig);
    }

    @Override
    public void setTitle(CharSequence title) {
        ActionBar ab = getSupportActionBar();

        mTitle = mStoredTitle = title;
        //ab.setTitle(mTitle);

        mActionBarTitle.setText(title);
        mActionBarTitle.invalidate();
        mActionBarTitle.invalidate();


    }


    public void changeFragment(String name) {

        if (name == null)
            name = getString(R.string.nav_scores_title);


        //start activity for settings
        if (name.equals(getString(R.string.nav_prefs_title))) {
            mStoredTitle = getTitle();
            Intent intent = new Intent(this, ActPrefs.class);
            startActivityForResult(intent, EDIT_PREFS_REQUEST_CODE);

        } else { //else change content view

            mCurrentFragment = name;

            Fragment nextContentFragment;
            if (name.equals(getString(R.string.nav_stats_title))) {
                nextContentFragment = new FragStats_();

            } else if (name.equals(getString(R.string.nav_timer_title))) {
                nextContentFragment = new FragTimer_();

            } else if (name.equals(getString(R.string.nav_dice_title))) {
                nextContentFragment = new FragDice_();

            } else if (name.equals(getString(R.string.nav_about_title))) {
                nextContentFragment = new FragAbout_();
            } else if (name.equals(getString(R.string.nav_upgrade_title))) {
                nextContentFragment = new FragUpgrade();
            } else {
                nextContentFragment = new FragScores_();
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, nextContentFragment).commit();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if we just changed preferences update local prefs vars to save calls to getDefaultSharedPrefs
        if (requestCode == EDIT_PREFS_REQUEST_CODE) {
            //restore title
            setTitle(mStoredTitle);

            PrefsHelper.updatePrefs(this);

            //reload current fragment, due to possible changes
            changeFragment(mCurrentFragment);
        }
    }


}