package com.application.zapplon.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditTextLight extends EditText {
	public EditTextLight(Context context) {
		super(context);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Light));
	}
	
	public EditTextLight(Context context, AttributeSet attr) {
		super(context,attr);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Light));
	}
	
	public EditTextLight(Context context, AttributeSet attr, int i) {
		super(context,attr,i);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Light));
	}
}