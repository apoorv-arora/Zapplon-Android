package com.application.zapplon.utils;

/**
 * Created by dell on 06-Sep-16.
 */
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.application.zapplon.R;

public class CustomDatePicker extends DialogFragment {

    private int year;
    private int month;
    private int day;
    private int viewType;

    private long maxDate = 0;
    private long minDate = 0;

    private String dateFormat;
    private ZDatePicker dialog;

    private DatePickerDialog.OnDateSetListener onDateSetListener = new OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {}
    };

    public CustomDatePicker() {
    }

    public void setDate (int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public void setMaxDate(long maxDate) {
        this.maxDate = maxDate;
    }

    public void setMinDate(long minDate) {
        this.minDate = minDate;
    }

    public void setDateFormat (String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener onDateSetListener) {
        this.onDateSetListener = onDateSetListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null && bundle.get("year") != null){
            year = bundle.getInt("year");
            month = bundle.getInt("month");
            day = bundle.getInt("date");
            viewType = bundle.getInt("view_type");

        } else {
            Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }

        // Create a new instance of TimePickerDialog and return it
        dialog = new ZDatePicker(getActivity(), TimePickerDialog.THEME_HOLO_LIGHT, onDateSetListener, year, month, day);
        CommonLib.ZLog("debg", "onDateSet: " + year + ", " + month + ", " + day);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.setTitle(getActivity().getResources().getString(R.string.pick_a_date));
        dialog.setButton(TimePickerDialog.BUTTON_NEGATIVE, getActivity().getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

    private class ZDatePicker extends DatePickerDialog {

        OnDateSetListener callBack;
        DatePicker datePicker;

        public ZDatePicker(Context context, int theme, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
            super(context, theme, callBack, year, monthOfYear, dayOfMonth);
            this.callBack = callBack;
        }

        public ZDatePicker(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
            super(context, callBack, year, monthOfYear, dayOfMonth);

        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (this.callBack != null && datePicker != null) {
                datePicker.clearFocus();
                callBack.onDateSet(datePicker, datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
            }
        }

        @Override
        protected void onStop() {
        }

        @Override
        public void onDateChanged(DatePicker view, int year, int month, int day) {
            dialog.setTitle(getActivity().getResources().getString(R.string.pick_a_date));
        }

        @Override
        public void onAttachedToWindow() {
            try {
                Class<?> classForid = Class.forName("com.android.internal.R$id");
                java.lang.reflect.Field datePickerField = classForid.getField("datePicker");
                this.datePicker = (DatePicker) findViewById(datePickerField.getInt(null));
                if (maxDate > 0)
                    this.datePicker.setMaxDate(maxDate);
                if (minDate > 0)
                    this.datePicker.setMinDate(minDate);
            } catch (Exception e) {

            }

            super.onAttachedToWindow();
        }
    }
}