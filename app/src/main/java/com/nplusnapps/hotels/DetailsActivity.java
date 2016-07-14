package com.nplusnapps.hotels;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout toolBarLayout =
                (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        final Hotel selectedHotel = getIntent().
                getParcelableExtra(MainActivity.EXTRA_SELECTED_HOTEL);

        if (selectedHotel != null) {
            toolBarLayout.setTitle(selectedHotel.getName());

            String hotelImage = selectedHotel.getImage();
            ImageView imageView = (ImageView) findViewById(R.id.hotel_image);
            ImageFromFileHelper.setImageFromFile(this, hotelImage, imageView, true);

            ViewGroup starsGroup = (ViewGroup) findViewById(R.id.stars_layout);

            int hotelStars = (int) selectedHotel.getStars();
            for (int i = 0; i < 5; i++) {
                ((ImageView) starsGroup.getChildAt(i)).
                        getDrawable().setLevel(i < hotelStars ? 1 : 0);
            }

            ((TextView) findViewById(R.id.details_label)).setText(
                    Html.fromHtml(getString(R.string.details_hotel,
                            selectedHotel.getAddress(),
                            selectedHotel.getSuitesString(),
                            selectedHotel.getDistance())));

            findViewById(R.id.button_location).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String geoUri = String.format(Locale.ENGLISH,
                            "geo:%f,%f?q=" + selectedHotel.getAddress(),
                            selectedHotel.getLatitude(), selectedHotel.getLongitude());
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(DetailsActivity.this,
                                getString(R.string.toast_maps), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            toolBarLayout.setTitle(getTitle());
        }
    }
}
