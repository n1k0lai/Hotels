package com.nplusnapps.hotels;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.IOException;

public class ImageFromFileHelper {

    public static void setImageFromFile(Context context, String file, ImageView image, boolean tint) {
        BitmapDrawable drawable = null;

        if (file != null) {
            try {
                FileInputStream input = context.openFileInput(file);
                drawable = new BitmapDrawable(BitmapFactory.decodeStream(input));
                input.close();
            } catch (IOException e) {
                Log.e("I/O exception", e.getMessage(), e);
            }
        }

        if (drawable != null) {
            if (tint) {
                image.setColorFilter(context.getResources().getColor(R.color.color_tint));
            }
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setImageDrawable(drawable);
        } else {
            image.setScaleType(ImageView.ScaleType.CENTER);
            image.setImageResource(R.drawable.ic_action_photo);
        }
    }
}
