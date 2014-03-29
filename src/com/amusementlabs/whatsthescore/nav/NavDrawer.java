package com.amusementlabs.whatsthescore.nav;


import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.amusementlabs.whatsthescore.ActMain_;
import com.amusementlabs.whatsthescore.R;


public class NavDrawer {

    private static final int SCORES_POSITION = 0;
    private final ActionBarDrawerToggle mDrawerToggle;
    private final CharSequence mDrawerTitle;
    private String[] mDrawerTitles;
    private ActMain_ mParent;


    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;


    public NavDrawer(SherlockFragmentActivity parent, CharSequence title, DrawerLayout drawerLayout, ListView drawerList) {
        mParent = (ActMain_) parent;
        mDrawerTitle = title;

        mDrawerTitles = mParent.getResources().getStringArray(R.array.nav_drawer_titles);

        mDrawerLayout = drawerLayout;
        mDrawerList = drawerList;


        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new NavDrawerListAdapter(mParent, R.layout.navdrawer_list_item, mDrawerTitles));
        mDrawerList.setOnItemClickListener(new NavDrawerItemClickListener());

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);


        //set up AB drawer toggle
        DrawerLayout mDrawerLayout = (DrawerLayout) mParent.findViewById(R.id.drawer_layout);
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                mParent,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.acc_drawer_open,  /* "open drawer" description for accessibility */
                R.string.acc_drawer_close  /* "close drawer" description for accessibility */
        ) {
            CharSequence savedTitle;

            public void onDrawerClosed(View view) {
                //mParent.getSupportActionBar().setTitle(savedTitle);
                mParent.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                savedTitle = mParent.getTitle();
                //mParent.getSupportActionBar().setTitle(mDrawerTitle);
                mParent.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);


    }


    public void selectItem(int position) {

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        mParent.changeFragment(mDrawerTitles[position]);

        if (position == SCORES_POSITION)
            mParent.setTitle(R.string.app_name); //scores fagment should have app name as title
        else
            mParent.setTitle(mDrawerTitles[position]); //set AB title to drawer item title


        mDrawerLayout.closeDrawer(mDrawerList);

    }

    public boolean isNavDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(mDrawerList);
    }

    public ActionBarDrawerToggle getDrawerToggle() {
        return mDrawerToggle;
    }


    /* The click listener for ListView in the navigation drawer */
    private class NavDrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

    }


    //The list adapter for the nav drawer
    private class NavDrawerListAdapter extends ArrayAdapter {
        Context mContext;
        int mLayoutResourceId;
        TypedArray mIconsArray;

        public NavDrawerListAdapter(Context context, int layoutResourceId, Object[] objects) {
            super(context, layoutResourceId, objects);
            mLayoutResourceId = layoutResourceId;
            mContext = context;

            //get handle to icons
            mIconsArray = context.getResources().obtainTypedArray(R.array.nav_drawer_icons);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            TextView textViewHandle;

            if (convertView == null) //if the view is new, created new ViewHolder
            {
                convertView = View.inflate(mContext, mLayoutResourceId, null);

                //get handles to each view in the list item
                textViewHandle = (TextView) convertView.findViewById(R.id.nav_drawer_list_item_text);

                viewHolder = new ViewHolder(textViewHandle);
                convertView.setTag(viewHolder);
            } else //else the view is being recycled, get the tag so we can update the ViewHolder
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            //get icon for list position and set it
            viewHolder.mText.setText((CharSequence) getItem(position)); //requires list to be Strings
            try {
                int icon = mIconsArray.getResourceId(position, 0);
                viewHolder.mText.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
            } catch (IndexOutOfBoundsException e) {
                //no icon for this item, do nothing
            }


            return convertView;
        }


        private class ViewHolder {
            public final TextView mText;

            public ViewHolder(TextView text) {
                mText = text;
            }
        }

    }
}
