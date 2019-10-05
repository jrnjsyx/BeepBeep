package com.example.jrnjsyx.beepbeep.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class MButton extends android.support.v7.widget.AppCompatButton{

    public boolean pressed;

    public MButton(Context context) {
        super(context);
        pressed = false;
    }

    public MButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        pressed = false;
    }

    public MButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        pressed = false;
    }


}
