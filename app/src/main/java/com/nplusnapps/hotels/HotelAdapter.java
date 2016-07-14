package com.nplusnapps.hotels;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class HotelAdapter extends ArrayAdapter<Hotel> {

    private Context mContext;
    private LayoutInflater mInflater;
    private int mLayout, mSortOrder, mHighlightColor;

    public HotelAdapter(Context context, int resource, int id, ArrayList<Hotel> objects) {
        super(context, resource, id, objects);

        mContext = context;
        mLayout = resource;
        mSortOrder = MainActivity.ORDER_DEFAULT;
        mHighlightColor = context.getResources().
                getColor(R.color.color_primary_dark);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayout, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (!isEmpty()) {
            Hotel hotel = getItem(position);

            holder.mNameLabel.setText(hotel.getName());
            holder.mAddressLabel.setText(hotel.getAddress());
            holder.mStarsLabel.setText(String.valueOf(hotel.getStars()));

            holder.mStarsLabel.setText(mSortOrder == MainActivity.ORDER_STARS ?
                    getSpanString(String.valueOf(hotel.getStars())) :
                    String.valueOf(hotel.getStars()));

            holder.mSuitesLabel.setText(mSortOrder == MainActivity.ORDER_SUITES ?
                    getSpanString(String.valueOf(hotel.getSuitesCount())) :
                    String.valueOf(hotel.getSuitesCount()));

            holder.mDistanceLabel.setText(mSortOrder == MainActivity.ORDER_DISTANCE ?
                    getSpanString(String.valueOf(hotel.getDistance())) :
                    String.valueOf(hotel.getDistance()));

            ImageFromFileHelper.setImageFromFile(mContext, hotel.getImage(), holder.mHotelImage, false);
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    public void addItems(ArrayList<Hotel> list, boolean clear) {
        setNotifyOnChange(false);

        if (clear) {
            clear();
        }

        if (list != null) {
            for (Hotel item : list) {
                add(item);
            }
        }

        notifyDataSetChanged();
    }

    public void setOrder(int order) {
        mSortOrder = order;
    }

    private SpannableString getSpanString(String str) {
        SpannableString spanStr = new SpannableString(str);
        spanStr.setSpan(new ForegroundColorSpan(mHighlightColor), 0, spanStr.length(), 0);
        return spanStr;
    }

    public static class ViewHolder {
        public final ImageView mHotelImage;
        public final TextView mNameLabel, mAddressLabel,
                mStarsLabel, mSuitesLabel, mDistanceLabel;

        public ViewHolder(View view) {
            mHotelImage = (ImageView) view.findViewById(R.id.hotel_image);
            mNameLabel = (TextView) view.findViewById(R.id.name_label);
            mAddressLabel = (TextView) view.findViewById(R.id.address_label);
            mStarsLabel = (TextView) view.findViewById(R.id.stars_label);
            mSuitesLabel = (TextView) view.findViewById(R.id.suites_label);
            mDistanceLabel = (TextView) view.findViewById(R.id.distance_label);
        }
    }
}
