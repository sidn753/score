package com.amusementlabs.whatsthescore;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.amusementlabs.whatsthescore.core.Game;
import com.amusementlabs.whatsthescore.core.User;
import com.amusementlabs.whatsthescore.util.*;
import org.androidannotations.annotations.*;

import java.util.ArrayList;
import java.util.Random;

import static com.amusementlabs.whatsthescore.util.Constants.*;

//import org.holoeverywhere.LayoutInflater;
//import org.holoeverywhere.app.Fragment;
//import org.holoeverywhere.widget.ArrayAdapter;
//import org.holoeverywhere.widget.ListView;
//import org.holoeverywhere.widget.TextView;

@EFragment(R.layout.frag_scores)
@OptionsMenu(R.menu.frag_scores)
public class FragScores extends SherlockFragment implements KeyboardHelper.KeyboardCallbackListener {
    private static final int RESET_DIALOG_REQUEST_CODE = 1;

    @Bean
    ScoresListAdapter mAdapter;

    @Bean
    KeyboardHelper mKeyboardHelper;

    private Context mContext;
    private Game mGame;

    @ViewById(R.id.frag_scores_round_num_view_parent)
    RelativeLayout vRoundNumBarParent;

    @ViewById(R.id.frag_scores_round_num_view_round_label)
    TextView vRoundNumLabel;

    @ViewById(R.id.frag_scores_round_num_view_total_label)
    TextView vTotalLabel;

    @ViewById(R.id.frag_scores_round_num_view_round)
    TextView vRoundNumView;

    @ViewById(R.id.frag_scores_round_num_view_total)
    TextView vTotalView;

    @ViewById(R.id.scores_list_view)
    ListView vListView;

    @ViewById(R.id.keyboard)
    TableLayout vKeyboard;

    @ViewById(R.id.frag_scores_button_container)
    LinearLayout vButtonBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //create keyboard helper
        View view = inflater.inflate(R.layout.frag_scores, container, false);


        return null; //return null to let AA handle content view
    }

    @AfterViews
    void setUpViews() {
        mContext = getActivity();
        //setHasOptionsMenu(true); //enable AB menu items

        vListView.setSelector(R.drawable.list_selector);


        vListView.setAdapter(mAdapter);
        mAdapter.updateListContents();

        //hide keyboard
        setKeyboardVisibility(false);

        //update item click listeners for preference values
        updateListItemClickListener();

    }


    @Override
    public void onPause() {
        super.onPause();
        GameDataManager.storeGame(mContext, mGame);
    }

    @Override
    public void onResume() {
        super.onResume();
        mGame = GameDataManager.getGame(mContext); //update game

        sortAndUpdateList();
        setKeyboardVisibility(false);
        updateRoundNumBar();
        updateListItemClickListener();
    }


    @OptionsItem(R.id.menu_scores_edit_players)
    boolean launchEditPlayers() {
        Intent i = new Intent(mContext, ActEditPlayers_.class);
        startActivityForResult(i, EDIT_PLAYERS_REQUEST_CODE);
        return true;
    }

    @OptionsItem(R.id.menu_reset_game)
    boolean resetGame() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new DialogResetGame();
        dialog.show(getActivity().getSupportFragmentManager(), "DialogResetgame");
        dialog.setTargetFragment(this, RESET_DIALOG_REQUEST_CODE);
        return true;
    }

    @OptionsItem(R.id.menu_random_player)
    boolean selectRandomPlayer() {
        Random rnd = new Random();
        int playerCount;

        if ((playerCount = mAdapter.getCount()) > 0)
            selectPosition(rnd.nextInt(playerCount));

        return true;
    }


    @Click(R.id.button_save_round)
    void nextRound() {

        if (PrefsHelper.shouldConfirmRound()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.save_round_button_text)
                    .setMessage(R.string.confirmation_question);

            // Add the buttons
            builder.setPositiveButton(R.string.save_button_text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mGame.advanceRound();
                    mAdapter.updateListContents();
                }
            });
            builder.setNegativeButton(R.string.cancel_button_text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //do nothing
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            mGame.advanceRound();
            mAdapter.updateListContents();
            updateRoundNumBar();

            //if we are sorting by score, order may have changed, so update adapter
            if (PrefsHelper.getSortingMode() == Game.SORT_BY_SCORE_HTL || PrefsHelper.getSortingMode() == Game.SORT_BY_SCORE_LTH) {
                sortAndUpdateList();
            }
        }

    }

    private void sortAndUpdateList() {

        //edit players may have resorted the game, resort to chosen mode
        mGame.sortUserNamesList(PrefsHelper.getSortingMode());

        //refresh list contents
        mAdapter.updateListContents();
    }

    //sets list item click listener for current preference settings
    private void updateListItemClickListener() {


        if (PrefsHelper.getInputMode() == INPUT_MODE_PLUS_ONE) {
            //hide keyboard in case it is showing
            setKeyboardVisibility(false);

            vListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mAdapter.setSelectedPosition(-1); //if anything is selected, deselect it

                    String userName = (mAdapter.getItem(position)).getName();
                    mGame.getUser(userName).incrementPendingScore();
                    mAdapter.updateListContents();
                }
            });
        } else if (PrefsHelper.getInputMode() == INPUT_MODE_KEYBOARD) {

            vListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (mAdapter.getSelectedPosition() == position) { //touch is a deselection

                        //hide keyboard
                        setKeyboardVisibility(false);
                        mAdapter.setSelectedPosition(-1); //set none selected
                        mAdapter.notifyDataSetChanged();

                    } else { //touch is a selection
                        selectPosition(position);
                    }

                }
            });
        } else {
            Logr.e("FragScores- found unknown input mode");
        }


    }

    private void selectPosition(int position) {
        mAdapter.setSelectedPosition(position);

        if (PrefsHelper.getInputMode() == INPUT_MODE_KEYBOARD) {
            setKeyboardVisibility(true);

            //send current score to keyboard helper, so it can be appended to
            mKeyboardHelper.prepareHelper(this, (mAdapter.getItem(position)).getPendingScore());
        }

        mAdapter.notifyDataSetChanged();
    }

    private void setKeyboardVisibility(boolean visible) {

        if (visible) { //show keyboard

            vKeyboard.setVisibility(View.VISIBLE);
            vButtonBar.setVisibility(View.GONE);
            vRoundNumBarParent.setVisibility(View.GONE);

        } else { //hide keyboard
            vKeyboard.setVisibility(View.GONE);
            vButtonBar.setVisibility(View.VISIBLE);

            //only show round num bar if prefs say to
            if (PrefsHelper.showRoundNum() || PrefsHelper.showTotalScore())
                vRoundNumBarParent.setVisibility(View.VISIBLE);
            else
                vRoundNumBarParent.setVisibility(View.GONE);
        }

    }

    //update text in round num bar, if it is being shown
    private void updateRoundNumBar() {
        if (PrefsHelper.showRoundNum()) {
            vRoundNumLabel.setVisibility(View.VISIBLE);
            vRoundNumView.setVisibility(View.VISIBLE);
            vRoundNumView.setText("" + mGame.getRoundNum());
        } else {
            vRoundNumLabel.setVisibility(View.GONE);
            vRoundNumView.setVisibility(View.GONE);
        }

        if (PrefsHelper.showTotalScore()) {
            vTotalLabel.setVisibility(View.VISIBLE);
            vTotalView.setVisibility(View.VISIBLE);
            vTotalView.setText("" + mGame.getTotalScore());
        } else {
            vTotalLabel.setVisibility(View.GONE);
            vTotalView.setVisibility(View.GONE);
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESET_DIALOG_REQUEST_CODE) { //game has been reset
            mGame = GameDataManager.getGame(mContext);
            sortAndUpdateList();

        } else if (requestCode == EDIT_PLAYERS_REQUEST_CODE) { //returning from edit players
            mGame = GameDataManager.getGame(mContext);
            //restore chosen sort order, ActEditPlayers will have changed this
            sortAndUpdateList();
        }

    }


    private int getTotalScore(Game game) {
        int retval = 0;
        for (String name : game.getUserNames()) {
            retval += mGame.getUser(name).getTotalScore();
        }
        return retval;
    }

    public void keyboardHelperCallback(int value, boolean saveRound) {
        if (saveRound) { //if save key pressed advance the round
            nextRound();
        } else {
            String userName = (mAdapter.getSelectedItem()).getName();
            mGame.getUser(userName).setPendingScore(value);
        }
        mAdapter.updateListContents();
    }
}


@EBean
class ScoresListAdapter extends BaseAdapter {

    ArrayList<User> users;

    @RootContext
    Context mContext;
    private int mSelectedPosition = -1;

    public ScoresListAdapter() {
        super();
    }

    @Override
    public int getCount() {return users.size();}

    @Override
    public User getItem(int position) {return users.get(position);}

    @Override
    public long getItemId(int position) {return position;}


    @AfterInject
    void initAdapter() {
        updateListContents();
    }

    public void updateListContents() {
        users = GameDataManager.getGame(mContext).getUsers();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ScoresItemView personItemView;
        if (convertView == null) {
            personItemView = ScoresItemView_.build(mContext);
        } else {
            personItemView = (ScoresItemView_) convertView;
        }


        personItemView.bind(getItem(position));

        personItemView.setActivated(position == mSelectedPosition);

        return personItemView;
    }

    public void setSelectedPosition(int position) {mSelectedPosition = position;}

    public int getSelectedPosition() { return mSelectedPosition; }

    public User getSelectedItem() { return getItem(mSelectedPosition); }


}


@EViewGroup(R.layout.frag_scores_list_item)
class ScoresItemView extends LinearLayout {

    @ViewById(R.id.list_item_view_name)
    TextView vName;

    @ViewById(R.id.list_item_view_pending_score)
    TextView vScore;

    @ViewById(R.id.list_item_view_total)
    TextView vTotal;

    @ViewById(R.id.frag_scores_list_item_colortag)
    LinearLayout vColorTag;

    public ScoresItemView(Context context) {
        super(context);
    }

    public void bind(User u) {
        vName.setText(u.getName());
        vScore.setText("" + u.getPendingScore());
        vTotal.setText("" + u.getTotalScore());


        if (u.getTeam() != Constants.NO_TEAM)
            vColorTag.setBackgroundColor(u.getColor());
        else
            vColorTag.setBackgroundColor(getResources().getColor(R.color.transparent));


    }
}
