package com.p1.mobile.p1android.ui.phone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.BrowseFilter;
import com.p1.mobile.p1android.ui.fragment.BrowseFragment;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.AbstractAction;
import com.p1.mobile.p1android.ui.widget.P1TextView;

public class BrowseFilterActivity extends FlurryActivity {
    public static final String TAG = BrowseFilterActivity.class.getSimpleName();

    public static final String KEY_PREFILTERBY = "filterBy";
    public static final String KEY_PREFGENDER = "gender";

    private static final int FILTER_TEXT_SELECTED_COLOR = Color
            .parseColor("#74a4cd");
    private static final int FILTER_BY_DEFAULT_COLOR = 0xe0e0e0;

    private String mFilterBy, mGender;

    private RelativeLayout mSortByLatestRelativeLayout;
    private RelativeLayout mSortByPopularRelativeLayout;
    private RelativeLayout mSortByRandomRelativeLayout;
    private ImageView mSortByLatestImageView;
    private ImageView mSortByPopularImageView;
    private ImageView mSortByRandomImageView;
    private TextView mSortByLatestTextView;
    private TextView mSortByPopularTextView;
    private TextView mSortByRandomTextView;

    private Button mGenderAllButton;
    private ImageButton mGenderFemaleImageButton;
    private ImageButton mGenderMaleImageButton;

    private TextView mFinishTextView;

    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.browse_filter_activity);

        mFinishTextView = (TextView) findViewById(R.id.tv_browse_filter_finish);

        mSortByLatestRelativeLayout = (RelativeLayout) findViewById(R.id.rl_browse_filter_sort_by_latest);
        mSortByPopularRelativeLayout = (RelativeLayout) findViewById(R.id.rl_browse_filter_sort_by_popular);
        mSortByRandomRelativeLayout = (RelativeLayout) findViewById(R.id.rl_browse_filter_sort_by_random);

        mSortByLatestImageView = (ImageView) mSortByLatestRelativeLayout
                .findViewById(R.id.iv_browse_filter_sort_by_latest);
        mSortByPopularImageView = (ImageView) mSortByPopularRelativeLayout
                .findViewById(R.id.iv_browse_filter_sort_by_popular);
        mSortByRandomImageView = (ImageView) mSortByRandomRelativeLayout
                .findViewById(R.id.iv_browse_filter_sort_by_random);

        mSortByLatestTextView = (TextView) mSortByLatestRelativeLayout
                .findViewById(R.id.tv_browse_filter_sort_by_latest);
        mSortByPopularTextView = (TextView) mSortByPopularRelativeLayout
                .findViewById(R.id.tv_browse_filter_sort_by_popular);
        mSortByRandomTextView = (TextView) mSortByRandomRelativeLayout
                .findViewById(R.id.tv_browse_filter_sort_by_random);

        mGenderAllButton = (Button) findViewById(R.id.btn_browse_filter_gender_all);
        mGenderFemaleImageButton = (ImageButton) findViewById(R.id.ib_browse_filter_gender_female);
        mGenderMaleImageButton = (ImageButton) findViewById(R.id.ib_browse_filter_gender_male);

        P1ActionBar actionBar = (P1ActionBar) findViewById(R.id.actionBar);
        TextView title = new P1TextView(this);
        title.setTextAppearance(this, R.style.P1LargerTextLight);
        title.setGravity(Gravity.CENTER);
        title.setText(R.string.browse_filter_title);
        actionBar.setCenterView(title);
        final AbstractAction closeAction = new AbstractAction(
                R.drawable.btn_contextual_close) {
            @Override
            public void performAction() {
                finish();
            }
        };
        actionBar.setLeftAction(closeAction);
        mFinishTextView.setOnClickListener(new HandleActionListener());

        HandleFilterByListener filterByListener = new HandleFilterByListener();
        mSortByLatestRelativeLayout.setOnClickListener(filterByListener);
        mSortByPopularRelativeLayout.setOnClickListener(filterByListener);
        mSortByRandomRelativeLayout.setOnClickListener(filterByListener);

        HandleGenderListener genderListener = new HandleGenderListener();
        mGenderAllButton.setOnClickListener(genderListener);
        mGenderFemaleImageButton.setOnClickListener(genderListener);
        mGenderMaleImageButton.setOnClickListener(genderListener);

        init();
    }

    private void initFilterByViewsToDefault() {
        mSortByLatestImageView.setVisibility(View.INVISIBLE);
        mSortByPopularImageView.setVisibility(View.INVISIBLE);
        mSortByRandomImageView.setVisibility(View.INVISIBLE);

        mSortByLatestTextView.setTextColor(Color.BLACK);
        mSortByPopularTextView.setTextColor(Color.BLACK);
        mSortByRandomTextView.setTextColor(Color.BLACK);

        mSortByLatestRelativeLayout.setBackgroundColor(FILTER_BY_DEFAULT_COLOR);
        mSortByPopularRelativeLayout
                .setBackgroundColor(FILTER_BY_DEFAULT_COLOR);
        mSortByRandomRelativeLayout.setBackgroundColor(FILTER_BY_DEFAULT_COLOR);
    }

    @SuppressLint("ResourceAsColor")
    private void init() {

        initFilterByViewsToDefault();

        mPref = getSharedPreferences(BrowseFragment.FILTER_PREF_FILE_NAME,
                Context.MODE_PRIVATE);
        mFilterBy = mPref.getString(BrowseFilterActivity.KEY_PREFILTERBY,
                BrowseFilter.BY_POPULAR);
        mGender = mPref.getString(BrowseFilterActivity.KEY_PREFGENDER,
                BrowseFilter.GENDER_ALL);

        if (mFilterBy.equals(BrowseFilter.BY_RANDOM)) {
            mSortByRandomImageView.setVisibility(View.VISIBLE);
            mSortByRandomTextView.setTextColor(FILTER_TEXT_SELECTED_COLOR);
            mSortByRandomRelativeLayout
                    .setBackgroundResource(R.drawable.round_corner_bottom);
        } else if (mFilterBy.equals(BrowseFilter.BY_POPULAR)) {
            mSortByPopularImageView.setVisibility(View.VISIBLE);
            mSortByPopularTextView.setTextColor(FILTER_TEXT_SELECTED_COLOR);
            mSortByPopularRelativeLayout.setBackgroundColor(Color.WHITE);
        } else if (mFilterBy.equals(BrowseFilter.BY_RECENT)) {
            mSortByLatestImageView.setVisibility(View.VISIBLE);
            mSortByLatestTextView.setTextColor(FILTER_TEXT_SELECTED_COLOR);
            mSortByLatestRelativeLayout
                    .setBackgroundResource(R.drawable.round_corner_top);
        }

        if (mGender.equals(BrowseFilter.GENDER_ALL)) {
            mGenderAllButton.setSelected(true);
        } else if (mGender.equals(BrowseFilter.GENDER_FEMALE)) {
            mGenderFemaleImageButton.setSelected(true);
        } else if (mGender.equals(BrowseFilter.GENDER_MALE)) {
            mGenderMaleImageButton.setSelected(true);
        }

    }

    private void initGenderViewsToDefault() {
        mGenderAllButton.setSelected(false);
        mGenderFemaleImageButton.setSelected(false);
        mGenderMaleImageButton.setSelected(false);
    }

    class HandleFilterByListener implements OnClickListener {
        @SuppressLint("ResourceAsColor")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.rl_browse_filter_sort_by_latest:
                initFilterByViewsToDefault();
                mSortByLatestImageView.setVisibility(View.VISIBLE);
                mSortByLatestTextView.setTextColor(FILTER_TEXT_SELECTED_COLOR);
                mSortByLatestRelativeLayout
                        .setBackgroundResource(R.drawable.round_corner_top);
                mFilterBy = BrowseFilter.BY_RECENT;
                break;
            case R.id.rl_browse_filter_sort_by_popular:
                initFilterByViewsToDefault();
                mSortByPopularImageView.setVisibility(View.VISIBLE);
                mSortByPopularTextView.setTextColor(FILTER_TEXT_SELECTED_COLOR);
                mSortByPopularRelativeLayout.setBackgroundColor(Color.WHITE);
                mFilterBy = BrowseFilter.BY_POPULAR;
                break;
            case R.id.rl_browse_filter_sort_by_random:
                initFilterByViewsToDefault();
                mSortByRandomImageView.setVisibility(View.VISIBLE);
                mSortByRandomTextView.setTextColor(FILTER_TEXT_SELECTED_COLOR);
                mSortByRandomRelativeLayout
                        .setBackgroundResource(R.drawable.round_corner_bottom);
                mFilterBy = BrowseFilter.BY_RANDOM;
                break;
            }
        }
    }

    class HandleGenderListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_browse_filter_gender_all:
                mGender = BrowseFilter.GENDER_ALL;
                initGenderViewsToDefault();
                mGenderAllButton.setSelected(true);
                break;
            case R.id.ib_browse_filter_gender_female:
                mGender = BrowseFilter.GENDER_FEMALE;
                initGenderViewsToDefault();
                mGenderFemaleImageButton.setSelected(true);
                break;
            case R.id.ib_browse_filter_gender_male:
                mGender = BrowseFilter.GENDER_MALE;
                initGenderViewsToDefault();
                mGenderMaleImageButton.setSelected(true);
                break;
            }
        }
    }

    class HandleActionListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.tv_browse_filter_finish:
                mPref.edit()
                        .putString(BrowseFilterActivity.KEY_PREFGENDER, mGender)
                        .commit();
                mPref.edit()
                        .putString(BrowseFilterActivity.KEY_PREFILTERBY,
                                mFilterBy).commit();
                setResult(Activity.RESULT_OK);
                finish();
                break;
            }
        }
    }
}
