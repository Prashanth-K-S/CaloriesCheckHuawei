package com.example.caloriescheck;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

public class GetStartedActivity extends AppCompatActivity {

    private ViewPager mIntroPager;
    private LinearLayout mDotsLayout;
    private ArrayList<String> mIcons = new ArrayList<>();
    private ArrayList<String> mTitles = new ArrayList<>();
    private ArrayList<String> mDescriptions = new ArrayList<>();
    private ArrayList<String> mArabicTitles = new ArrayList<>();
    private ArrayList<String> mArabicDescriptions = new ArrayList<>();
    private ImageView[] mDots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);
        initViews();
    }

    //To initialize views
    private void initViews() {
        mIntroPager = findViewById(R.id.vp_intro);
        mDotsLayout = findViewById(R.id.ll_dotsLayout);

        mIntroPager.setAdapter(new IntroSliderAdapter(GetStartedActivity.this, createIconsList(), createTitleList(), createDescriptionList()));

        addLineIndicators(0);
    }


    //To create icon list for slider
    private ArrayList<String> createIconsList() {
        mIcons.clear();
        mIcons.add("scanner.json");
        mIcons.add("scanner.json");
        mIcons.add("scanner.json");
        mIcons.add("scanner.json");
        return mIcons;
    }

    //To create title list(english) for slider
    private ArrayList<String> createTitleList() {
        mTitles.clear();
        mTitles.add("Title 1");
        mTitles.add("Title 2");
        mTitles.add("Title 3");
        mTitles.add("Title 4");
        return mTitles;
    }


    //To create description list(english) for slider
    private ArrayList<String> createDescriptionList() {
        mDescriptions.clear();
        mDescriptions.add("Description 1");
        mDescriptions.add("Description 2");
        mDescriptions.add("Description 3");
        mDescriptions.add("Description 4");
        return mDescriptions;
    }


    //To add line indicators to pager
    public void addLineIndicators(int position) {
        mDots = new ImageView[mIcons.size()];
        mDotsLayout.removeAllViews();
        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new ImageView(GetStartedActivity.this);
            mDots[i].setSelected(true);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16));
            params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            mDots[i].setLayoutParams(params);
            mDotsLayout.addView(mDots[i]);
        }
        //To change indicator color
        changeIndicatorColor(position);

        mIntroPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {

                //To change indicator color
                changeIndicatorColor(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    //To change indicator color
    private void changeIndicatorColor(int i) {
        mDots[i].setSelected(true);
        mDots[i].setImageDrawable(getResources().getDrawable(R.drawable.tab_selector));

        for (int j = 0; j < mDots.length; j++) {
            if (j != i) {
                mDots[j].setSelected(false);
                mDots[j].setImageDrawable(getResources().getDrawable(R.drawable.tab_selector));
            }
        }
    }

    //To convert dp to px
    public int dpToPx(int dp) {
        float density = getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }
}