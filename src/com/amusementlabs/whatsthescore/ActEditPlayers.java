
package com.amusementlabs.whatsthescore;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.amusementlabs.whatsthescore.core.Game;
import com.amusementlabs.whatsthescore.core.User;
import com.amusementlabs.whatsthescore.util.Constants;
import com.amusementlabs.whatsthescore.util.GameDataManager;
import com.amusementlabs.whatsthescore.util.Logr;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import org.androidannotations.annotations.*;

import java.util.Comparator;
import java.util.Random;

//import org.holoeverywhere.app.ListActivity;
//import org.holoeverywhere.widget.ArrayAdapter;
//import org.holoeverywhere.widget.TextView;
//import org.holoeverywhere.widget.Toast;


@EActivity(R.layout.act_edit_players)
@OptionsMenu(R.menu.act_edit_players)
public class ActEditPlayers extends SherlockFragmentActivity implements DialogEditPlayer.DialogCallbackListener {

    private CharSequence mTitle;
    private ArrayAdapter<User> mAdapter;
    private Game mGame;
    private Context mContext;


    private DragSortController mController;

    //Dragsort options
    public int dragStartMode = DragSortController.ON_DOWN;
    public boolean removeEnabled = false;
    public int removeMode = DragSortController.CLICK_REMOVE;
    public boolean sortEnabled = true;
    public boolean dragEnabled = true;

    @ViewById(R.id.edit_players_dragsort_list_view)
    DragSortListView mDslv;


    @AfterViews
    void setUpViews() {
        //manually set title
        setTitle(R.string.act_edit_players_title);

        mContext = this;

        mGame = GameDataManager.getGame(this);

        //set up dragsort list view, and set options
        mDslv = (DragSortListView) findViewById(R.id.edit_players_dragsort_list_view);
        mController = buildController(mDslv);
        mDslv.setFloatViewManager(mController);
        mDslv.setOnTouchListener(mController);
        mDslv.setDragEnabled(dragEnabled);
        mDslv.setDropListener(onDrop);


        //create and set adapter
        mAdapter = new EditPlayersListAdapter(this, R.layout.act_edit_players_list_item);


        //set listener to display edit player dialog when player is clicked
        mDslv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditPlayerDialog(mAdapter.getItem(position));
            }
        });

        mDslv.setAdapter(mAdapter);

        //always use user defined order in edit players, so user can change order
        mGame.sortUserNamesList(Game.SORT_BY_USER_DEFINED);

        //populate adapter
        mAdapter.clear();
        for (String name : mGame.getUserNames()) {
            mAdapter.add(mGame.getUser(name));
        }
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }


    @OptionsItem(R.id.menu_add_player)
    boolean openAddPlayerDiaglog() {
        showEditPlayerDialog(null);
        return true;
    }

    @OptionsItem(R.id.menu_shuffle_players)
    boolean shufflePlayers() {
        final Random rnd = new Random();
        Comparator<User> rndCompare = new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                if (rnd.nextBoolean())
                    return -1;
                else
                    return 1; // 50% chance of both positive and negative
            }
        };
        mAdapter.sort(rndCompare);
        mAdapter.notifyDataSetChanged();
        return true;
    }

    @OptionsItem
    boolean homeSelected() {
        finish();
        NavUtils.navigateUpFromSameTask(this);
        return true;
    }

    public void showEditPlayerDialog(User u) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new DialogEditPlayer(mGame, u);
        dialog.show(getSupportFragmentManager(), "DialogEditPlayer");
    }


    private void addUser(String name, int team, int color) {
        User u = new User(name, team, color, mGame.getRoundNum());
        mGame.addUser(u);
        mAdapter.add(u);
    }

    private void removeUser(User u) {
        final User fUser = u;

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle("Remove User")
                .setMessage("Are you sure you want to remove this player? All saved scores for this player will be lost.");

        // Add the buttons
        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mAdapter.remove(fUser);
                mGame.removeUser(fUser);
            }
        });
        builder.setNegativeButton(R.string.cancel_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }


    @Override
    public void onDialogPositiveClick(User u, String name, int team, int color, boolean addAnotherFlag) {
        if (u == null) { //new player
            addUser(name, team, color);
        } else {
            User temp = new User(name, team, color);
            mGame.updateUser(u.getName(), temp);

            mAdapter.notifyDataSetChanged();
        }

        //if we need to add another player
        if (addAnotherFlag)
            showEditPlayerDialog(null);
    }


    @Override
    public void finish() {

        setResult(RESULT_OK);
        GameDataManager.storeGame(this, mGame);

        super.finish();
    }


    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    if (from != to) {
                        User u = mAdapter.getItem(from);

                        //change position in adapter, sort order value updated in list adapter
                        mAdapter.remove(u);
                        mAdapter.insert(u, to);
                    }
                }
            };


    /**
     * Called in onCreateView. Override this to provide a custom
     * DragSortController.
     */
    public DragSortController buildController(DragSortListView dslv) {

        DragSortController controller = new DragSortController(dslv);
        controller.setDragHandleId(R.id.drag_handle);
        controller.setRemoveEnabled(removeEnabled);
        controller.setSortEnabled(sortEnabled);
        controller.setDragInitMode(dragStartMode);
        controller.setRemoveMode(removeMode);
        return controller;
    }


    private class EditPlayersListAdapter extends ArrayAdapter<User> {
        Context mContext;
        int mLayoutResourceId;


        public EditPlayersListAdapter(Context context, int layoutResourceId) {
            super(context, layoutResourceId);
            mLayoutResourceId = layoutResourceId;
            mContext = context;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            TextView textViewHandle;
            LinearLayout colorTagHandle;
            ImageButton removeButton;

            if (convertView == null) //if the view is new, created new ViewHolder
            {
                convertView = View.inflate(mContext, mLayoutResourceId, null);
                textViewHandle = (TextView) convertView.findViewById(R.id.edit_players_list_item_text);
                colorTagHandle = (LinearLayout) convertView.findViewById(R.id.drag_handle);
                removeButton = (ImageButton) convertView.findViewById(R.id.edit_players_button_remove);

                viewHolder = new ViewHolder(textViewHandle, colorTagHandle, removeButton);
                convertView.setTag(viewHolder);
            } else //else the view is being recycled, get the tag so we can update the ViewHolder
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            User u = getItem(position);
            String n = u.getName();
            viewHolder.mText.setText(n);

            //SORT ORDER VALUE IN USER IS UPDATED HERE
            //automatically update sort order by position for convience, will always be called when a change is made
            Logr.w("Setting " + u.getName() + " to pos: " + position);
            u.setSortOrder(position);


            if (u.getTeam() != Constants.NO_TEAM) {
                viewHolder.mColorTag.setBackgroundResource(R.color.transparent);
                viewHolder.mColorTag.setBackgroundColor(u.getColor());
            } else {
                viewHolder.mColorTag.setBackgroundColor(getResources().getColor(R.color.transparent));
                viewHolder.mColorTag.setBackgroundResource(R.drawable.ic_drag_handle);
            }

            final User thisUser = u; //final for inner classes
            viewHolder.mText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditPlayerDialog(thisUser);
                }
            });


            viewHolder.mRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeUser(thisUser);
                }
            });


            return convertView;
        }


        private class ViewHolder {
            public final TextView mText;
            public final LinearLayout mColorTag;
            private final ImageButton mRemoveButton;

            public ViewHolder(TextView text, LinearLayout colorTag, ImageButton removeButton) {
                mText = text;
                mColorTag = colorTag;
                mRemoveButton = removeButton;
            }
        }

    }


}
