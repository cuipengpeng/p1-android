package com.p1.mobile.p1android.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.p1.mobile.p1android.R;

public class CustomGalleryFooter extends RelativeLayout {
    static final String TAG = CustomGalleryFooter.class.getSimpleName();

    private static final int DEFAULT_MAX_NUM_SELECTED = 9;
    private static final int DEFAULT_NUM_SELECTED = 0;
    private static final String SEPARATOR = "/";

    public interface OnDoneListener {
        public void onDone();
    }

    private TextView mCounterText;
    private Button mDoneButton;
    private int mNumSelected;
    private int mMaxNumSelected;
    private String mPicturesSelectedString;
    private OnDoneListener mDoneListener;

    public CustomGalleryFooter(Context context, int maxNumSelected,
            int numSelected) {
        super(context);
        mMaxNumSelected = maxNumSelected;
        mNumSelected = numSelected;
        init(context);
    }

    public CustomGalleryFooter(Context context, int maxNumSelected) {
        super(context);
        mMaxNumSelected = maxNumSelected;
        mNumSelected = DEFAULT_NUM_SELECTED;
        init(context);
    }

    public CustomGalleryFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMaxNumSelected = DEFAULT_MAX_NUM_SELECTED;
        mNumSelected = DEFAULT_NUM_SELECTED;
        init(context);
    }

    public CustomGalleryFooter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMaxNumSelected = DEFAULT_MAX_NUM_SELECTED;
        mNumSelected = DEFAULT_NUM_SELECTED;
        init(context);
    }

    public void setOnDoneListener(OnDoneListener listener) {
        mDoneListener = listener;
    }

    private void init(Context context) {
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View footerView = inflater.inflate(
                R.layout.custom_gallery_footer, null);

        mDoneButton = (Button) footerView
                .findViewById(R.id.customGalleryDoneButton);
        mDoneButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mDoneListener != null) {
                    mDoneListener.onDone();
                }
            }

        });

        mCounterText = (TextView) footerView
                .findViewById(R.id.customGalleryTextView);

        mPicturesSelectedString = context.getResources().getString(
                R.string.custom_footer_pictures_selected);

        this.addView(footerView);
        resetSelectedText();
    }

    public void incrementSelectedPicturesCount() {
        if (mNumSelected < mMaxNumSelected) {
            ++mNumSelected;
            resetSelectedText();
        }
    }

    public void decrementSelectedPicturesCount() {
        if (mNumSelected > 0) {
            --mNumSelected;
            resetSelectedText();
        }
    }

    private void resetSelectedText() {
        mCounterText.setText(mNumSelected + SEPARATOR + mMaxNumSelected + " "
                + mPicturesSelectedString);
    }

    public void setNumberSelectedPictures(int numberOfSelected) {
        if (numberOfSelected <= mMaxNumSelected) {
            mNumSelected = numberOfSelected;
        } else {
            mNumSelected = mMaxNumSelected;
        }
        resetSelectedText();
    }

    public void showFooter(final boolean show) {

        Animation slideInOut = AnimationUtils
                .loadAnimation(getContext(), show ? R.anim.slide_in_from_bottom
                        : R.anim.slide_out_to_bottom);
        CustomGalleryFooter.this.setVisibility(View.VISIBLE);
        slideInOut.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                CustomGalleryFooter.this.setVisibility(show ? View.VISIBLE
                        : View.GONE);
            }
        });
        this.startAnimation(slideInOut);

    }

}
