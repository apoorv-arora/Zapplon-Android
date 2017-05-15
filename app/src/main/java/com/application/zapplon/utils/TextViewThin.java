package com.application.zapplon.utils;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewThin extends TextView {

	public TextViewThin(Context context) {
		super(context);
		//setTypeface(CommonLib.getTypeface(context, CommonLib.Bold));
		setTypeface(CommonLib.getTypeface(context, CommonLib.Thin));
		//setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold));
		setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.HINTING_ON);
	}
	
	public TextViewThin(Context context, AttributeSet attr) {
		super(context,attr);
		//setTypeface(CommonLib.getTypeface(context, CommonLib.Bold));
		setTypeface(CommonLib.getTypeface(context, CommonLib.Thin));
		//setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold));
		setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.HINTING_ON);
	}
	
	public TextViewThin(Context context, AttributeSet attr, int i) {
		super(context,attr,i);
		//setTypeface(CommonLib.getTypeface(context, CommonLib.Bold));
		setTypeface(CommonLib.getTypeface(context, CommonLib.Thin));
		//setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold));
		setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.HINTING_ON);
	}
}