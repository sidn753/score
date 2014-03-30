package com.amusementlabs.whatsthescore;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.amusementlabs.whatsthescore.core.Game;
import com.amusementlabs.whatsthescore.core.User;
import com.amusementlabs.whatsthescore.util.Constants;
import com.amusementlabs.whatsthescore.util.StaticCounter;

import java.util.ArrayList;


public class DialogEditPlayer extends SherlockDialogFragment {

    private final User mUser;
    private final Game mGame;


    //fields to populate with desired data on dialog confirm

    int mTeamName = 0;
    int mColor = 0;
    private ArrayList<ImageButton> mColorButtonArray;

    private EditText mNameInput;
    private String mStoredName = "";

    public DialogEditPlayer(Game g, User u) {
        mGame = g;
        mUser = u;
    }


    public interface DialogCallbackListener {
        public void onDialogPositiveClick(User u, String name, int teamName, int color, boolean addAnotherFlag);
    }

    // Use this instance of the interface to deliver action events
    DialogCallbackListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DialogCallbackListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DialogCallbackListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_edit_player, null);

        mNameInput = (EditText) view.findViewById(R.id.dialog_add_player_edittext_name);
        mNameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mNameInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    confirmDialog(false);
                    getDialog().dismiss();
                    return true;
                }
                return false;
            }
        });

        mColorButtonArray = new ArrayList<ImageButton>();
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_15));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_1));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_2));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_3));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_4));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_5));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_6));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_7));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_8));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_9));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_10));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_11));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_12));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_13));
        mColorButtonArray.add((ImageButton) view.findViewById(R.id.dialog_add_player_color_14));


        //set listeners for color buttons
        for (int i = 0; i < mColorButtonArray.size(); i++) {
            final int temp = i; //final value for inner class
            mColorButtonArray.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (ImageButton i : mColorButtonArray) {
                        i.setSelected(false);
                    }

                    v.setSelected(true);
                    setChosenColor(temp);
                }
            });
        }

        String positiveButtonText;

        //if we're editing player
        if (mUser != null) {
            mNameInput.setText(mUser.getName());
            mColorButtonArray.get(mUser.getTeam()).setSelected(true);
            setChosenColor(mUser.getTeam());
            mStoredName = mUser.getName(); //store name to allow duplicate in the case of editing
            positiveButtonText = getString(R.string.save_button_text);
        } else { //new player
            int nextPlayerNumber = StaticCounter.getCount();
            mNameInput.setHint("Player " + nextPlayerNumber);
            positiveButtonText = getString(R.string.add_player_button_text);
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        confirmDialog(false);
                    }
                })
                .setNeutralButton(getString(R.string.add_another_button_text),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                confirmDialog(true);
                            }
                        })
                .setNegativeButton(R.string.cancel_button_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogEditPlayer.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }


    private final void confirmDialog(boolean addAnotherFlag) {
        String name = mNameInput.getText().toString();

        if (name.equals("") || name == null) { // if no name entered default to "Player #"
            name = mNameInput.getHint().toString();
            StaticCounter.incrementCount();
        }

        //duplicates not allowed
        if (name.equals(mStoredName) || !mGame.getUserNames().contains(name))
            mListener.onDialogPositiveClick(mUser, name, mTeamName, mColor, addAnotherFlag);
        else
            Toast.makeText(getActivity(), "Adding player failed- duplicate names not allowed", Toast.LENGTH_SHORT).show();

        return;
    }


    private void setChosenColor(int index) {

        if (index == Constants.NO_TEAM) {
            mTeamName = 0;
            mColor = 0;
            return;
        }
        mTeamName = index;
        mColor = getResources().obtainTypedArray(R.array.team_colors).getColor(index, 0);

    }


}
