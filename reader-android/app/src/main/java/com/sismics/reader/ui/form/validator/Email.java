package com.sismics.reader.ui.form.validator;

import java.util.regex.Pattern;

import android.content.Context;

import com.sismics.reader.R;

/**
 * Email validator.
 * 
 * @author bgamard
 */
public class Email implements ValidatorType {

    /**
     * Pattern de validation.
     */
    private static Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+");
    
    @Override
    public boolean validate(String text) {
        return EMAIL_PATTERN.matcher(text).matches();
    }

    @Override
    public String getErrorMessage(Context context) {
        return context.getResources().getString(R.string.validate_error_email);
    }
}
