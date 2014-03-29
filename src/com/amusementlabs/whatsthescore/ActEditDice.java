
package com.amusementlabs.whatsthescore;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.amusementlabs.whatsthescore.core.Die;
import com.amusementlabs.whatsthescore.core.Game;
import com.amusementlabs.whatsthescore.util.GameDataManager;
import com.amusementlabs.whatsthescore.util.Logr;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.ArrayList;

import static com.amusementlabs.whatsthescore.util.Constants.NUM_MAX_DICE;

//import org.holoeverywhere.app.ListActivity;
//import org.holoeverywhere.widget.ArrayAdapter;
//import org.holoeverywhere.widget.TextView;
//import org.holoeverywhere.widget.Toast;

@EActivity(R.layout.act_edit_dice)
@OptionsMenu(R.menu.act_edit_dice)
public class ActEditDice extends SherlockFragmentActivity implements NumberPickerDialogFragment.NumberPickerDialogHandler {

    private static final int MIN_SIDES = 2;
    private static final int MAX_SIDES = 99;


    private Game mGame;
    private ArrayList<RelativeLayout> mDiceParentLayoutArray;
    private ArrayList<TextView> mNumSidesViewArray;
    private ArrayList<ImageButton> mRemoveButtonArray;
    private int mNumDice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //manually set title
        setTitle(R.string.act_edit_dice_title);

        //get game
        mGame = GameDataManager.getGame(this);

        mDiceParentLayoutArray = new ArrayList<RelativeLayout>();
        mNumSidesViewArray = new ArrayList<TextView>();
        mRemoveButtonArray = new ArrayList<ImageButton>();


    }

    @AfterViews
    void setUpViews() {

        //get handles to output fields
        mNumSidesViewArray.add((TextView) findViewById(R.id.act_edit_dice_numsides_1));
        mNumSidesViewArray.add((TextView) findViewById(R.id.act_edit_dice_numsides_2));
        mNumSidesViewArray.add((TextView) findViewById(R.id.act_edit_dice_numsides_3));
        mNumSidesViewArray.add((TextView) findViewById(R.id.act_edit_dice_numsides_4));
        mNumSidesViewArray.add((TextView) findViewById(R.id.act_edit_dice_numsides_5));
        mNumSidesViewArray.add((TextView) findViewById(R.id.act_edit_dice_numsides_6));

        //get handles to parent layouts for dice, so they can be set to View.GONE for empty dice
        mDiceParentLayoutArray.add((RelativeLayout) findViewById(R.id.act_edit_dice_parent_die_1));
        mDiceParentLayoutArray.add((RelativeLayout) findViewById(R.id.act_edit_dice_parent_die_2));
        mDiceParentLayoutArray.add((RelativeLayout) findViewById(R.id.act_edit_dice_parent_die_3));
        mDiceParentLayoutArray.add((RelativeLayout) findViewById(R.id.act_edit_dice_parent_die_4));
        mDiceParentLayoutArray.add((RelativeLayout) findViewById(R.id.act_edit_dice_parent_die_5));
        mDiceParentLayoutArray.add((RelativeLayout) findViewById(R.id.act_edit_dice_parent_die_6));


        //get handles to remove buttons
        mRemoveButtonArray.add((ImageButton) findViewById(R.id.act_edit_dice_imagebutton_remove_1));
        mRemoveButtonArray.add((ImageButton) findViewById(R.id.act_edit_dice_imagebutton_remove_2));
        mRemoveButtonArray.add((ImageButton) findViewById(R.id.act_edit_dice_imagebutton_remove_3));
        mRemoveButtonArray.add((ImageButton) findViewById(R.id.act_edit_dice_imagebutton_remove_4));
        mRemoveButtonArray.add((ImageButton) findViewById(R.id.act_edit_dice_imagebutton_remove_5));
        mRemoveButtonArray.add((ImageButton) findViewById(R.id.act_edit_dice_imagebutton_remove_6));


        //set listeners for remove buttons
        for (int i = 0; i < NUM_MAX_DICE; i++) {
            final int temp = i; //final value for inner class
            mRemoveButtonArray.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeDie(temp);
                }
            });
        }

        //populate views with data
        mNumDice = 0;
        for (int i = 0; i < NUM_MAX_DICE; i++) { //loop for each possible die
            Die d = mGame.getDice().get(i);

            if (d != null) { //if die exists
                mDiceParentLayoutArray.get(i).setVisibility(View.VISIBLE); //show die
                mNumSidesViewArray.get(i).setText("" + d.getNumSides());
                mNumDice++;
            } else { //blank die spot
                mDiceParentLayoutArray.get(i).setVisibility(View.INVISIBLE); //hide die
            }
        }
    }


    @OptionsItem(R.id.menu_dice_add)
    boolean launchNumberPicker() {
        if (mNumDice < NUM_MAX_DICE) { //dont add if we already have full dice grid
            NumberPickerBuilder npb = new NumberPickerBuilder()
                    .setFragmentManager(getSupportFragmentManager())
                    .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                    .setPlusMinusVisibility(View.INVISIBLE)
                    .setDecimalVisibility(View.INVISIBLE);
            npb.show();
        }
        return true;
    }

    @OptionsItem
    boolean homeSelected() {
        super.onBackPressed();
        return true;
    }


    @Override
    public void finish() {

        setResult(RESULT_OK);
        GameDataManager.storeGame(this, mGame);

        super.finish();
    }


    private void addDie(int numSides) {

        int i = 0;
        //find where we should add the die by searching for null in mGame
        try {
            while (mGame.getDice().get(i) != null) {
                i++;
            }
        } catch (IndexOutOfBoundsException e) {
            Logr.e("Dice index out of bounds- too many dice to add another?");
            return;
        }

        //add die to found empty space
        mGame.addDie(new Die(numSides), i);

        //update UI
        mDiceParentLayoutArray.get(i).setVisibility(View.VISIBLE);
        mNumSidesViewArray.get(i).setText("" + numSides);
        mNumDice++;

    }

    private void removeDie(int i) {
        mGame.removeDie(i);
        mDiceParentLayoutArray.get(i).setVisibility(View.INVISIBLE);
        mNumDice--;
    }

    @Override
    public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
        if (number < MIN_SIDES)
            number = MIN_SIDES;
        else if (number > MAX_SIDES)
            number = MAX_SIDES;


        addDie(number);
    }


}
