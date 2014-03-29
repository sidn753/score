package com.amusementlabs.whatsthescore.util;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;
import com.amusementlabs.whatsthescore.R;

/**
 * A custom Text View that lowers the text size when the text is to big for the TextView. Modified version of one found on stackoverflow
 *
 * @author Andreas Krings - www.ankri.de
 * @version 1.0
 */
public class AutoScaleTextView extends TextView {
    private Paint textPaint;

    private float mPreferredTextSize;
    private float mMinTextSize;
    private CharSequence mLastText;

    public AutoScaleTextView(Context context) {
        this(context, null);
    }

    public AutoScaleTextView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.autoScaleTextViewStyle);

        // Use this constructor, if you do not want use the default style
        // super(context, attrs);
    }

    public AutoScaleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.textPaint = new Paint();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoScaleTextView, defStyle, 0);
        this.mMinTextSize = a.getDimension(R.styleable.AutoScaleTextView_minTextSize, 10f);
        a.recycle();

        this.mPreferredTextSize = this.getTextSize();
    }


    public void setMinTextSize(float minTextSize) {
        this.mMinTextSize = minTextSize;
    }


    private void refitText(String text, int textWidth) {


        if (textWidth <= 0 || text == null || text.length() == 0)
            return;

        // the width
        int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();

        final float threshold = 0.5f; // How close we have to be

        this.textPaint.set(this.getPaint());

        while ((mPreferredTextSize - mMinTextSize) > threshold) {
            float size = (mPreferredTextSize + mMinTextSize) / 2;
            this.textPaint.setTextSize(size);
            if (this.textPaint.measureText(text) >= targetWidth)
                mPreferredTextSize = size; // too big
            else
                mMinTextSize = size; // too small
        }


        // Use min size so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, mMinTextSize);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        if (mLastText == null || !text.equals(mLastText)) {
            mLastText = text;
            this.refitText(text.toString(), this.getWidth());
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
        if (width != oldwidth)
            this.refitText(this.getText().toString(), width);
    }


}
