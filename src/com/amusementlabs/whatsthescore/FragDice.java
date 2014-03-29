package com.amusementlabs.whatsthescore;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.amusementlabs.whatsthescore.core.Die;
import com.amusementlabs.whatsthescore.core.Game;
import com.amusementlabs.whatsthescore.util.GameDataManager;
import com.squareup.seismic.ShakeDetector;
import org.androidannotations.annotations.*;

import java.util.ArrayList;
import java.util.Random;

import static com.amusementlabs.whatsthescore.util.Constants.NUM_MAX_DICE;

//import org.holoeverywhere.LayoutInflater;
//import org.holoeverywhere.app.Fragment;
//import org.holoeverywhere.widget.ArrayAdapter;
//import org.holoeverywhere.widget.GridView;
//import org.holoeverywhere.widget.ListView;
//import org.holoeverywhere.widget.TextView;


@EFragment  //layout loaded in onCreateView, easier because of the arrays of views
@OptionsMenu(R.menu.frag_dice)
public class FragDice extends SherlockFragment implements ShakeDetector.Listener {


    private Context mContext;
    private Game mGame;


    private ArrayList<TextView> mDiceOutputViewArray;
    private ArrayList<TextView> mDiceNumSidesViewArray;
    private ArrayList<LinearLayout> mDiceParentLayoutArray;

    @SystemService
    SensorManager mSensorManager;

    @ViewById(R.id.button_roll_dice)
    Button mRollButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_dice, container, false);


        ShakeDetector sd = new ShakeDetector(this);
        sd.start(mSensorManager);

        mDiceNumSidesViewArray = new ArrayList<TextView>();
        mDiceOutputViewArray = new ArrayList<TextView>();
        mDiceParentLayoutArray = new ArrayList<LinearLayout>();

        //get handles to output fields
        mDiceOutputViewArray.add((TextView) view.findViewById(R.id.dice_output_1));
        mDiceOutputViewArray.add((TextView) view.findViewById(R.id.dice_output_2));
        mDiceOutputViewArray.add((TextView) view.findViewById(R.id.dice_output_3));
        mDiceOutputViewArray.add((TextView) view.findViewById(R.id.dice_output_4));
        mDiceOutputViewArray.add((TextView) view.findViewById(R.id.dice_output_5));
        mDiceOutputViewArray.add((TextView) view.findViewById(R.id.dice_output_6));

        //get handles to num sides textviews
        mDiceNumSidesViewArray.add((TextView) view.findViewById(R.id.frag_dice_textview_numsides_1));
        mDiceNumSidesViewArray.add((TextView) view.findViewById(R.id.frag_dice_textview_numsides_2));
        mDiceNumSidesViewArray.add((TextView) view.findViewById(R.id.frag_dice_textview_numsides_3));
        mDiceNumSidesViewArray.add((TextView) view.findViewById(R.id.frag_dice_textview_numsides_4));
        mDiceNumSidesViewArray.add((TextView) view.findViewById(R.id.frag_dice_textview_numsides_5));
        mDiceNumSidesViewArray.add((TextView) view.findViewById(R.id.frag_dice_textview_numsides_6));

        //get handles to parent layouts for dice, so they can be set to View.INVISIBLE for empty dice
        mDiceParentLayoutArray.add((LinearLayout) view.findViewById(R.id.frag_dice_parent_die_1));
        mDiceParentLayoutArray.add((LinearLayout) view.findViewById(R.id.frag_dice_parent_die_2));
        mDiceParentLayoutArray.add((LinearLayout) view.findViewById(R.id.frag_dice_parent_die_3));
        mDiceParentLayoutArray.add((LinearLayout) view.findViewById(R.id.frag_dice_parent_die_4));
        mDiceParentLayoutArray.add((LinearLayout) view.findViewById(R.id.frag_dice_parent_die_5));
        mDiceParentLayoutArray.add((LinearLayout) view.findViewById(R.id.frag_dice_parent_die_6));


        return view; //return null to delegate layout to AA
    }


    @Override //when parent activity is created, saved bundle and context available here
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true); //enable AB menu items

        mContext = getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        GameDataManager.storeGame(mContext, mGame);
    }


    @Override
    public void onResume() {
        super.onResume();
        mGame = GameDataManager.getGame(mContext);
        updateUI();
    }

    private void updateUI() {//grid is fixed size, so loop for each possible die
        for (int i = 0; i < NUM_MAX_DICE; i++) {
            Die d = mGame.getDice().get(i);


            if (d != null) { //if die exists
                mDiceParentLayoutArray.get(i).setVisibility(View.VISIBLE); //show die
                if (d.getLastVal() == 0) { //if die not rolled yet
                    mDiceOutputViewArray.get(i).setText("-");
                } else { //die has been rolled, set last value
                    mDiceOutputViewArray.get(i).setText("" + d.getLastVal());
                }
                mDiceNumSidesViewArray.get(i).setText("" + d.getNumSides() + " " + mContext.getString(R.string.dice_sides_label));
            } else { //blank die spot
                mDiceParentLayoutArray.get(i).setVisibility(View.INVISIBLE); //hide die
            }

        }
    }


    @OptionsItem(R.id.menu_dice_edit)
    boolean launchEditDice() {
        Intent i = new Intent(mContext, ActEditDice_.class);
        startActivityForResult(i, 1);
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mGame = GameDataManager.getGame(mContext);
        updateUI();
    }

    @Click(R.id.button_roll_dice)
    void rollDice() {
        Random r = new Random();

        Die d;
        for (int i = 0; i < NUM_MAX_DICE; i++) {
            if ((d = mGame.getDice().get(i)) != null) {
                d.setLastVal(r.nextInt(d.getNumSides()) + 1);
            }
        }
        updateUI();
    }


    @Override
    public void hearShake() {
        rollDice();
    }
}
