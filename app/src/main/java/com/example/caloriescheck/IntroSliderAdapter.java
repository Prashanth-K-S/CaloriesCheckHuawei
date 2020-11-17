package com.example.caloriescheck;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;

public class IntroSliderAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<String> mIcons;

    private ArrayList<String> mTitles;

    private ArrayList<String> mDescriptions;

    private LayoutInflater mInflater;

    @Override
    public int getCount() {
        return mIcons.size();
    }

    public IntroSliderAdapter(Context mContext, ArrayList<String> mIcons, ArrayList<String> mTitles, ArrayList<String> mDescriptions) {
        this.mContext = mContext;
        this.mIcons = mIcons;

        this.mTitles = mTitles;

        this.mDescriptions = mDescriptions;

        mInflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View introView = mInflater.inflate(R.layout.intro_item, container, false);

        LottieAnimationView mBackGroundImage = introView.findViewById(R.id.lav_intro);
        mBackGroundImage.setAnimation(mIcons.get(position));

        TextView mTitle = introView.findViewById(R.id.tv_title);
        TextView mDescription = introView.findViewById(R.id.tv_description);


        mTitle.setText(mTitles.get(position));

        mDescription.setText(mDescriptions.get(position));


        container.addView(introView);
        return introView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
