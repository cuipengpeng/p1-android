package com.p1.mobile.p1android.ui.phone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.p1.mobile.p1android.R;
import com.viewpagerindicator.CirclePageIndicator;

/**
 * 
 * @author pirriperdos
 * 
 */
public class WelcomeActivity extends FlurryActivity implements
        OnPageChangeListener {

    private LayoutInflater mInflater;
    private ViewPager mPager;
    private ViewGroup mImages;
    private CirclePageIndicator mIndicator;
    private View mLogin;
    private View mApply;

    private static final int[] IMAGE_RES_IDS = {
            R.drawable.landing_page_slide_1, R.drawable.landing_page_slide_3,
            R.drawable.landing_page_slide_2, R.drawable.landing_page_slide_4 };

    private String[] TITLES;
    private String[] SUBTITLES;
    private PagerAdapter mPagerAdapter = new PagerAdapter() {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View texts = mInflater.inflate(R.layout.login_welcome_text_item,
                    container, false);
            TextView title = (TextView) texts
                    .findViewById(R.id.login_welcome_title);
            TextView subtitle = (TextView) texts
                    .findViewById(R.id.login_welcome_subtitle);
            title.setText(TITLES[position]);
            if (position == 0)
                title.setGravity(Gravity.CENTER);
            subtitle.setText(SUBTITLES[position]);
            container.addView(texts);

            // lazily set image drawable to speed up inflate time
            ((ImageView) mImages.getChildAt(position))
                    .setImageResource(IMAGE_RES_IDS[position]);
            return texts;
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public boolean isViewFromObject(View v1, Object v2) {
            return v1 == v2;
        }
    };

    // using onResult to clear back stack, because CLEAR_TASK is API level 11
    private static final int RESULT_LOGIN = 1;
    private static final int RESULT_APPLY = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_welcome_activity);
        mInflater = LayoutInflater.from(this);
        TITLES = getResources().getStringArray(R.array.landing_slide_titles);
        SUBTITLES = getResources().getStringArray(
                R.array.landing_slide_subtitles);
        mPager = (ViewPager) findViewById(R.id.login_welcome_pager);
        mImages = (ViewGroup) findViewById(R.id.login_welcome_flipper);
        for (int i = 1; i < mImages.getChildCount(); i++) {
            ((ImageView) mImages.getChildAt(i)).setAlpha(0);
        }
        mIndicator = (CirclePageIndicator) findViewById(R.id.login_welcome_page_indicator);
        mIndicator.setOnPageChangeListener(this);
        mLogin = findViewById(R.id.login_welcome_longin);
        mApply = findViewById(R.id.login_welcome_apply);
        mPager.setAdapter(mPagerAdapter);
        mIndicator.setViewPager(mPager);
        mLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(WelcomeActivity.this,
                        LoginActivity.class), RESULT_LOGIN);
            }
        });
        mApply.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this,
                        ApplyActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOGIN && resultCode == RESULT_OK)
            finish();
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int position, float offset, int pixels) {
        for (int i = 0; i < mImages.getChildCount(); i++) {
            float alpha = 0;
            if (position == i) {
                alpha = 1 - offset;
            } else if (i == position + 1) {
                alpha = offset;
            }
            int alphaInt = (int) (alpha * 0xFF);
            ((ImageView) mImages.getChildAt(i)).setAlpha(alphaInt);
            ((ImageView) mImages.getChildAt(i))
                    .setVisibility(alpha == 0 ? View.INVISIBLE : View.VISIBLE);
            // setAlpha for textView is not supported for API level 9
        }
    }

    @Override
    public void onPageSelected(int arg0) {

    }
}
