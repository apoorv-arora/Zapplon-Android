package com.application.zapplon.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditTextThin extends EditText {
	public EditTextThin(Context context) {
		super(context);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Thin));
	}

	public EditTextThin(Context context, AttributeSet attr) {
		super(context,attr);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Thin));
	}

	public EditTextThin(Context context, AttributeSet attr, int i) {
		super(context,attr,i);
		setTypeface(CommonLib.getTypeface(context, CommonLib.Thin));
	}
}