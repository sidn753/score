package com.amusementlabs.whatsthescore;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.amusementlabs.whatsthescore.util.GameDataManager;
import com.amusementlabs.whatsthescore.util.Logr;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;


public class DialogResetGame extends SherlockDialogFragment implements NumberPickerDialogFragment.NumberPickerDialogHandler {


    private int mResetToValue = 0; //default to 0
    private TextView mResetToView;

    public DialogResetGame() { }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_reset_game, null);

        mResetToView = (TextView) view.findViewById(R.id.reset_to_value);
        mResetToView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchNumberPicker();
            }
        });


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        GameDataManager.resetGame(getActivity(), mResetToValue);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), 0, null);
                    }
                })
                .setNegativeButton(R.string.cancel_button_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DialogResetGame.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }


    void launchNumberPicker() {
        NumberPickerBuilder npb = new NumberPickerBuilder()
                .setFragmentManager(getChildFragmentManager())
                .setReference(1337)
                .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                .setPlusMinusVisibility(View.VISIBLE)
                .setDecimalVisibility(View.INVISIBLE)
                .setTargetFragment(DialogResetGame.this);
        npb.show();
    }


    @Override
    public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
        Logr.w("TEST");
        mResetToValue = number;
        mResetToView.setText("" + number);
    }


}
