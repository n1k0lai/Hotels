package com.nplusnapps.hotels;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView aboutLabel = ((TextView) findViewById(R.id.about_label));
        aboutLabel.setText(Html.fromHtml(getString(R.string.app_about,
                getString(R.string.app_name), getString(R.string.app_version),
                getString(R.string.dev_email, getString(R.string.dev_name)))));
        aboutLabel.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
