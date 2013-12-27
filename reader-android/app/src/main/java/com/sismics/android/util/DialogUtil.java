package com.sismics.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.sismics.reader.R;

/**
 * Utility class for dialogs.
 * 
 * @author bgamard
 */
public class DialogUtil {

    /**
     * Create a dialog with an OK button.
     * @param context
     * @param title
     * @param message
     * @return
     */
    public static void showOkDialog(Activity context, int title, int message) {
        if (context == null || context.isFinishing()) {
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        builder.setTitle(title)
        .setMessage(message)
        .setCancelable(true)
        .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        }).create().show();
    }
}
