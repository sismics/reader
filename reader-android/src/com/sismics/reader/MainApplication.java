package com.sismics.reader;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import com.androidquery.callback.BitmapAjaxCallback;

import android.app.Application;

/**
 * Main application.
 * 
 * @author bgamard
 */
@ReportsCrashes(formKey = "", // TODO Create a new Drive report
        mode = ReportingInteractionMode.TOAST,
        forceCloseDialogAfterToast = true,
        resToastText = R.string.crash_toast_text)
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        ACRA.init(this);
        
        super.onCreate();
    }
    
    @Override
    public void onLowMemory() {
        BitmapAjaxCallback.clearCache();
    }
}
