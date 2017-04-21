package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

public class FileCreateFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                // set dialog icon
                .setIcon(android.R.drawable.stat_notify_error)
                // set Dialog Title
                // Set Dialog Message
                .setMessage("No Contacts File-\nCreating a New Contacts File")

                // positive button
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(getActivity(), "Pressed OK", Toast.LENGTH_SHORT).show();
                    }
                })
                // negative button
                .setNegativeButton("", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                    }
                }).create();
    }
}