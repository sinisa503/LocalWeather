package com.example.android.localweather.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

import com.example.android.localweather.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by SINISA on 21.3.2016..
 */
public class MyEditTextPreference extends EditTextPreference {
    private int googleResultCode;
    public MyEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        GoogleApiAvailability googlePlayServicesUtil = GoogleApiAvailability.getInstance();
        googleResultCode = googlePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (googleResultCode == ConnectionResult.SUCCESS){
            setWidgetLayoutResource(R.layout.pref_current_location);
        }
    }

//    @Override
//    protected View onCreateView(ViewGroup parent) {
//        View view = super.onCreateView(parent);
//        View currentLocation = view.findViewById(R.id.current_location);
//        currentLocation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Context c = getContext();
//                PlacePicker.IntentBuilder placePicker = new PlacePicker.IntentBuilder();
//                Activity settingsActivity = (SettingsActivity)getContext();
//                try{
//                    settingsActivity.startActivityForResult(placePicker.build(settingsActivity),
//                            SettingsActivity.PLACE_PICKER_REQUEST);
//
//                }catch (GooglePlayServicesNotAvailableException |GooglePlayServicesRepairableException e){
//
//                }
//                Toast.makeText(getContext(), "Place Picker", Toast.LENGTH_LONG).show();
//            }
//        });
//        return view;
//    }
    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        EditText et = getEditText();
        et.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int mMinLength = 3;
                Dialog d = getDialog();
                if (d instanceof AlertDialog) {
                    AlertDialog dialog = (AlertDialog) d;
                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    // Check if the EditText is empty
                    if (s.length() < mMinLength) {
                        // Disable OK button
                        positiveButton.setEnabled(false);
                    } else {
                        // Re-enable the button.
                        positiveButton.setEnabled(true);
                    }
                }
            }
        });
    }
}
