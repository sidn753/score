package com.amusementlabs.whatsthescore.util;

import com.amusementlabs.whatsthescore.R;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EBean;

@EBean
public class KeyboardHelper {


    private final int NUM_KEY = 0;
    private final int INCREMENT_KEY = 1;
    private final int POS_NEG_KEY = 2;
    private final int BACKSPACE_KEY = 3;
    private final int CLEAR_KEY = 4;
    private final int SAVE_KEY = 5;

    private KeyboardCallbackListener mParent;
    private int mValue;


    public interface KeyboardCallbackListener {
        public void keyboardHelperCallback(int value, boolean saveRound);
    }

    @Click(R.id.key_0)
    void key0() { onKeyPress(NUM_KEY, 0); }

    @Click(R.id.key_1)
    void key1() { onKeyPress(NUM_KEY, 1); }

    @Click(R.id.key_2)
    void key2() { onKeyPress(NUM_KEY, 2); }

    @Click(R.id.key_3)
    void key3() { onKeyPress(NUM_KEY, 3); }

    @Click(R.id.key_4)
    void key4() { onKeyPress(NUM_KEY, 4); }

    @Click(R.id.key_5)
    void key5() { onKeyPress(NUM_KEY, 5); }

    @Click(R.id.key_6)
    void key6() { onKeyPress(NUM_KEY, 6); }

    @Click(R.id.key_7)
    void key7() { onKeyPress(NUM_KEY, 7); }

    @Click(R.id.key_8)
    void key8() { onKeyPress(NUM_KEY, 8); }

    @Click(R.id.key_9)
    void key9() { onKeyPress(NUM_KEY, 9); }

    @Click(R.id.key_plus_1)
    void keyPlus1() { onKeyPress(INCREMENT_KEY, 1); }

    @Click(R.id.key_plus_10)
    void keyPlus10() { onKeyPress(INCREMENT_KEY, 10); }

    @Click(R.id.key_minus_1)
    void keyMinus1() { onKeyPress(INCREMENT_KEY, -1); }

    @Click(R.id.key_minus_10)
    void keyMinus10() { onKeyPress(INCREMENT_KEY, -10); }

    @Click(R.id.key_posneg)
    void keyPosNeg() { onKeyPress(POS_NEG_KEY, 0); }

    @Click(R.id.key_backspace)
    void keyBackspace() { onKeyPress(BACKSPACE_KEY, 0); }

    @Click(R.id.key_clear)
    void keyClear() { onKeyPress(CLEAR_KEY, 0);}

    @Click(R.id.key_save)
    void keySave() { onKeyPress(SAVE_KEY, 0); }


    private void onKeyPress(int keyType, int value) {

        switch (keyType) {
            case NUM_KEY:
                numPress(value);
                break;
            case INCREMENT_KEY:
                incrementPress(value);
                break;
            case POS_NEG_KEY:
                posNegPress();
                break;
            case BACKSPACE_KEY:
                backspacePress();
                break;
            case CLEAR_KEY:
                clearPress();
                break;
            case SAVE_KEY:
                savePress();
                break;
        }

        if (mParent == null) {
            Logr.e("KeyboardHelper listener not found: make sure to call "
                    + "prepareHelper(KeyboardCallbackListener parent, int currentValue) before using helper.");
        } else {
            mParent.keyboardHelperCallback(mValue, (keyType == SAVE_KEY));
        }

    }

    private void savePress() {
        mValue = 0;
    }

    private void clearPress() {
        mValue = 0;
    }

    private void backspacePress() {
        mValue = mValue / 10;
    }

    private void posNegPress() {
        mValue = 0 - mValue;
    }

    private void incrementPress(int i) {
        mValue += i;
    }

    private void numPress(int i) {
        if (mValue >= 0)
            mValue = mValue * 10 + i;
        else
            mValue = mValue * 10 - i; //should subtract if value is negative

    }

    public void prepareHelper(KeyboardCallbackListener parent, int currentValue) {
        mParent = parent;
        mValue = currentValue;
    }

}
