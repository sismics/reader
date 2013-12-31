package com.sismics.reader.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;

import com.androidquery.AQuery;
import com.sismics.reader.R;

/**
 * About activity.
 * TODO To transform in help activity, showing version number and license
 * 
 * @author bgamard
 */
public class AboutActivity extends FragmentActivity {

    /**
     * AQuery.
     */
    final AQuery aq = new AQuery(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        aq.id(R.id.txtAbout)
            .text(Html.fromHtml(getString(R.string.about_content)))
            .getTextView()
            .setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
