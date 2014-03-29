package com.amusementlabs.whatsthescore;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.amusementlabs.whatsthescore.core.Game;
import com.amusementlabs.whatsthescore.core.User;
import com.amusementlabs.whatsthescore.util.Constants;
import com.amusementlabs.whatsthescore.util.GameDataManager;
import org.androidannotations.annotations.*;

import java.util.ArrayList;

import static com.amusementlabs.whatsthescore.util.Constants.NO_TEAM;
//import org.holoeverywhere.LayoutInflater;
//import org.holoeverywhere.app.Fragment;
//import org.holoeverywhere.widget.ArrayAdapter;
//import org.holoeverywhere.widget.LinearLayout;
//import org.holoeverywhere.widget.ListView;
//import org.holoeverywhere.widget.ViewPager;


@EFragment(R.layout.frag_stats)
@OptionsMenu(R.menu.frag_stats)
public class FragStats extends SherlockFragment {

    private static final int SCORE_STRING_THRESHOLD_LENGTH_FOR_SMALL = 2;
    private static final int SCORE_STRING_THRESHOLD_LENGTH_FOR_SMALLER = 3;
    private static final int SCORE_STRING_THRESHOLD_LENGTH_FOR_SMALLEST = 4;

    private static final int NAME_STRING_THRESHOLD_LENGTH_FOR_SMALL = 5;
    private static final int NAME_STRING_THRESHOLD_LENGTH_FOR_SMALLER = 10;

    //constants for scaled down font sizes, programmatic changes to font size dont take dimen values
    private static final int FONT_SIZE_SMALL = 16;
    private static final int FONT_SIZE_SMALLER = 12;
    private static final int FONT_SIZE_SMALLEST = 10;

    //number of columns view pager should show at one time, changing may require a change to layout_weight of pager
    private static final int ONSCREEN_PAGES = 5;

    private static final int RESET_DIALOG_REQUEST_CODE = 1;

    private Context mContext;

    @ViewById(R.id.frag_stats_pager)
    ViewPager vPager;

    @ViewById(R.id.frag_stats_list_labels)
    ListView vLabelListView;

    @ViewById(R.id.frag_stats_parent)
    LinearLayout vPagerParent;

    private Game mGame;
    private LabelListAdapter mLabelList;
    private ArrayList<PageListAdapter> mValuesAdapterArray;


    @AfterViews
    public void setUpViews() {

        mContext = getActivity();
        vPager.setOffscreenPageLimit(ONSCREEN_PAGES * 2);


        //disable hardware acceleration on pager, needed for a bug in <4.0 android
        vPagerParent.setClipChildren(false);
        vPagerParent.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        vPager.setClipChildren(false);
        vPager.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        //prevent 1px space between each set of pages
        vPager.setPageMargin(-1);

        mLabelList = new LabelListAdapter(mContext, R.layout.frag_stats_list_item_labels);
        mValuesAdapterArray = new ArrayList<PageListAdapter>();

        vLabelListView.setAdapter(mLabelList);
        vLabelListView.setOnTouchListener(touchForwarder);
        //vPager.setAdapter(new GridpagerAdapter());
    }


    @Override
    public void onPause() {
        super.onPause();
        GameDataManager.storeGame(mContext, mGame);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();

    }

    private void updateData() {
        mGame = GameDataManager.getGame(mContext);
        mLabelList.clear();
        mValuesAdapterArray.clear();


        //add a column for each round (plus one for total), but always add at least a screen's worth to color in header row
        for (int i = 1; i <= ONSCREEN_PAGES || i <= mGame.getRoundNum(); i++) {
            mValuesAdapterArray.add(new PageListAdapter(mContext, R.layout.frag_stats_list_item));
        }

        addHeaderRow();

        for (String name : mGame.getUserNames()) {
            User u = mGame.getUser(name);
            addRow(u);
        }

        vPager.setAdapter(new GridpagerAdapter());
        vPager.getAdapter().notifyDataSetChanged();
    }


    //Adds header row to grid- Round:, 1, 2 , 3...
    public void addHeaderRow() {

        //add dummy user for "Round:" label
        mLabelList.add(new User(getString(R.string.stats_rounds_header_text), NO_TEAM, NO_TEAM));

        int roundNum = mGame.getRoundNum();
        int rowsFilled = 0; //counter

        //add 1, 2 3...
        for (int i = 0; i < roundNum - 1; i++) {
            mValuesAdapterArray.get(i).add("" + (i + 1));
            rowsFilled++;
        }

        mValuesAdapterArray.get(roundNum - 1).add("Total");
        rowsFilled++;


        //if there are fewer rounds than minimum onscreen pages, add blanks to color in header row
        for (int i = rowsFilled; i < ONSCREEN_PAGES; i++) {
            //add blank value
            mValuesAdapterArray.get(i).add(" ");
        }

    }

    //add a row of data to the grid
    public void addRow(User u) {
        ArrayList scoreArray = u.getScores();

        int roundNum = mGame.getRoundNum();
        int rowsFilled = 0; //counter

        //add user name to list
        mLabelList.add(u);

        //loop for each round of game
        for (int i = 0; i < roundNum - 1; i++) {
            //add score values
            mValuesAdapterArray.get(i).add(scoreArray.get(i).toString());
            rowsFilled++;
        }

        mValuesAdapterArray.get(roundNum - 1).add("" + u.getTotalScore());
        rowsFilled++;


        //if there are fewer rounds than minimum onscreen pages, add blanks to draw lines properly

        for (int i = rowsFilled; i < ONSCREEN_PAGES; i++) {
            //add blank value
            mValuesAdapterArray.get(i).add(" ");
        }


    }

    @OptionsItem(R.id.menu_reset_game)
    boolean resetGame() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new DialogResetGame();
        dialog.show(getActivity().getSupportFragmentManager(), "DialogResetGame");
        dialog.setTargetFragment(this, RESET_DIALOG_REQUEST_CODE);
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //game has been reset, update data
        if (requestCode == RESET_DIALOG_REQUEST_CODE)
            updateData();
    }

    //adapter for the view pager
    //each page is a list to be used as a column of the grid of scores
    //wired to display the contents of mValuesAdapterArray
    private class GridpagerAdapter extends PagerAdapter {


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View page = getActivity().getLayoutInflater().inflate(R.layout.frag_stats_page, container, false);

            ListView pageList = (ListView) page.findViewById(R.id.page_list);

            //need to sync scrolling, so use touch forwarding
            pageList.setOnTouchListener(touchForwarder);

            pageList.setAdapter(mValuesAdapterArray.get(position));

            container.addView(page);

            return (page);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return (mValuesAdapterArray.size());
        }

        @Override
        public float getPageWidth(int position) {

            //calculate percentage of view each page should take up
            return ((float) 1.0 / ONSCREEN_PAGES);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }
    }


    //adapter for each score column in pager
    private class PageListAdapter extends ArrayAdapter<String> {
        Context mContext;
        int mLayoutResourceId;

        public PageListAdapter(Context context, int layoutResourceId) {
            super(context, layoutResourceId);
            mLayoutResourceId = layoutResourceId;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            TextView dataViewHandle;
            LinearLayout parentViewHandle;


            if (convertView == null) //if the view is new, created new ViewHolder
            {
                convertView = View.inflate(mContext, mLayoutResourceId, null);
                dataViewHandle = (TextView) convertView.findViewById(R.id.stats_list_item_view_name);
                parentViewHandle = (LinearLayout) convertView.findViewById(R.id.frag_stats_list_item_parent);

                viewHolder = new ViewHolder(dataViewHandle, parentViewHandle);
                convertView.setTag(viewHolder);
            } else //else the view is being recycled, get the tag so we can update the ViewHolder
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            String data = getItem(position);

            if (position == 0) { //if this is a header, color the background
                viewHolder.mParent.setBackgroundColor(mContext.getResources().getColor(R.color.stats_header_bg));
            } else {
                viewHolder.mParent.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
            }


            if (data.length() > SCORE_STRING_THRESHOLD_LENGTH_FOR_SMALL) { //if string is too long
                viewHolder.mData.setTextSize(FONT_SIZE_SMALL);
            }
            if (data.length() > SCORE_STRING_THRESHOLD_LENGTH_FOR_SMALLER) {
                viewHolder.mData.setTextSize(FONT_SIZE_SMALLER);
            }
            if (data.length() > SCORE_STRING_THRESHOLD_LENGTH_FOR_SMALLEST) {
                viewHolder.mData.setTextSize(FONT_SIZE_SMALLEST);
            }

            viewHolder.mData.setText(getItem(position));

            return convertView;
        }


        private class ViewHolder {
            public final TextView mData;
            public final LinearLayout mParent;

            public ViewHolder(TextView data, LinearLayout parent) {
                mData = data;
                mParent = parent;
            }
        }

    }


    //adapter for label column
    private class LabelListAdapter extends ArrayAdapter<User> {
        Context mContext;
        int mLayoutResourceId;


        public LabelListAdapter(Context context, int layoutResourceId) {
            super(context, layoutResourceId);
            mLayoutResourceId = layoutResourceId;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            LinearLayout parentViewHandle;
            LinearLayout colorTagHandle;
            TextView nameViewHandle;


            if (convertView == null) //if the view is new, created new ViewHolder
            {
                convertView = View.inflate(mContext, mLayoutResourceId, null);
                colorTagHandle = (LinearLayout) convertView.findViewById(R.id.stats_listitem_label_colortag);
                nameViewHandle = (TextView) convertView.findViewById(R.id.stats_listitem_label_name);
                parentViewHandle = (LinearLayout) convertView.findViewById(R.id.stats_listitem_label_parent);

                viewHolder = new ViewHolder(colorTagHandle, nameViewHandle, parentViewHandle);
                convertView.setTag(viewHolder);
            } else //else the view is being recycled, get the tag so we can update the ViewHolder
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            User u = getItem(position);
            String name = u.getName();

            if (position == 0) { //if this is a header
                viewHolder.mParent.setBackgroundColor(mContext.getResources().getColor(R.color.stats_header_bg));
            } else {
                viewHolder.mParent.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
            }

            if (name.length() > NAME_STRING_THRESHOLD_LENGTH_FOR_SMALL) { //if string is too long
                viewHolder.mName.setTextSize(16);
            }
            if (name.length() > NAME_STRING_THRESHOLD_LENGTH_FOR_SMALLER) {
                viewHolder.mName.setTextSize(12);
            }

            //set colortag to team color if there is one
            if (u.getTeam() != Constants.NO_TEAM)
                viewHolder.mColorTag.setBackgroundColor(u.getColor());
            else
                viewHolder.mColorTag.setBackgroundColor(getResources().getColor(R.color.transparent));


            viewHolder.mName.setText(name);

            return convertView;
        }


        private class ViewHolder {
            public final TextView mName;
            private final LinearLayout mColorTag;
            private final LinearLayout mParent;

            public ViewHolder(LinearLayout colorTag, TextView name, LinearLayout parentView) {
                mColorTag = colorTag;
                mName = name;
                mParent = parentView;
            }
        }

    }


    //forwards touch events to all lists in pager to sync scrolling
    View.OnTouchListener touchForwarder = new View.OnTouchListener() {
        View mTouched;
        boolean mMovingFlag;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            MotionEvent newEvent = MotionEvent.obtain(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (mTouched == null) {
                        mTouched = v;
                    }
                    mMovingFlag = true;
                    break;
                case MotionEvent.ACTION_UP:
                    if (mMovingFlag == false) {
                        newEvent.setAction(MotionEvent.ACTION_CANCEL);
                    }
                    mMovingFlag = false;
                    mTouched = null;
                    break;
                default:
                    mMovingFlag = false;
                    if (mTouched != null && mTouched.equals(v)) {

                    }
                    break;

            }
            if (mTouched == null || mTouched.equals(v)) {
                int items = vPager.getChildCount();

                for (int list = 0; list < items; list++) {
                    ListView listView = (ListView) vPager.getChildAt(list);
                    if (listView != v) {
                        listView.onTouchEvent(newEvent);
                    }
                }

                if (vLabelListView != v) {
                    vLabelListView.onTouchEvent(newEvent);
                }

            }
            return false;
        }
    };


}
