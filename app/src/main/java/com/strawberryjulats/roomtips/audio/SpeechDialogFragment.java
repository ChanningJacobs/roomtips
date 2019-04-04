package com.strawberryjulats.roomtips.audio;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.strawberryjulats.roomtips.CameraActivity;
import com.strawberryjulats.roomtips.R;

import org.jetbrains.annotations.Nullable;

public class SpeechDialogFragment extends BottomSheetDialogFragment {
    private static final String TAG = "SpeechDialogFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.speech_bottom_sheet, container, false);
        CameraActivity cm = (CameraActivity)getActivity();
        Button button = view.findViewById(R.id.speech_dismiss);
        cm.recordOn();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Trying to switch");
                cm.recordOn();
                dismiss();
            }
        });
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog d = super.onCreateDialog(savedInstanceState);
        // view hierarchy is inflated after dialog is shown
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                //this disables outside touch
                d.getWindow().findViewById(R.id.touch_outside).setOnClickListener(null);
                //this prevents dragging behavior
                View content = d.getWindow().findViewById(R.id.design_bottom_sheet);
                ((CoordinatorLayout.LayoutParams) content.getLayoutParams()).setBehavior(null);
            }
        });
        return d;
    }
}