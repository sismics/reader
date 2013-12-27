package com.sismics.reader.ui.form.validator;

import java.util.regex.Pattern;

import android.content.Context;

import com.sismics.reader.R;

/**
 * Alphanumeric validator.
 * 
 * @author bgamard
 */
public class Alphanumeric implements ValidatorType {

    private static Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");
    
    @Override
    public boolean validate(String text) {
        return ALPHANUMERIC_PATTERN.matcher(text).matches();
    }

    @Override
    public String getErrorMessage(Context context) {
        return context.getString(R.string.validate_error_alphanumeric);
    }

}
