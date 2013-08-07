package com.p1.mobile.p1android.ui.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.io.model.tags.SimpleTag;

/**
 * @author Anton Gronlund
 * 
 * A popup used for rating Tags.
 */
public class RatingPopup {
    public static final String TAG = RatingPopup.class.getSimpleName();
    public static final int ANIMATION_TIME = 550;  // Animation time in milliseconds
    private static float ratingButtonSize;

    //private PopupWindow mWindow;
    private View mRootView;
    private ImageView mArrow;
    private Context mContext;
    private Button mReviewButton;
    private Button mActiveRatingButton;
    private List<Button> mRatingButtons = new ArrayList<Button>();
    private List<View> mButtonSeparators = new ArrayList<View>();
    private View mReviewButtonSeparator;
    private OnClickListener mRatingClickListener, mUndoClickListener;
    
   
    // TODO prevent losing window when rotating screen. Easy fix would be to call dismiss from parent activities, but there might exist a more proper fix.
    public RatingPopup(Context context){
        mContext = context;
        ratingButtonSize = context.getResources().getDimension(R.dimen.share_rating_button_size);
        //mWindow = new PopupWindow();
        setRootView(R.layout.rating_popup_layout);
        
        
        
    }
    
    public void show(SimpleTag tag, View anchor, ViewGroup parentView){
        
        /*mWindow.setBackgroundDrawable(new BitmapDrawable(mContext.getResources()));
        mWindow.setWidth(LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(LayoutParams.WRAP_CONTENT);
        mWindow.setTouchable(true);
        mWindow.setFocusable(false);
        mWindow.setOutsideTouchable(true);*/
        
     
        if(mRootView == null)
            setRootView(R.layout.rating_popup_layout);
        
        View rootContent = mRootView.findViewById(R.id.scroller);
        //rootContent.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rootContent.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Log.d(TAG, "Popup rootContent measurements: "+rootContent.getMeasuredWidth()+'x'+rootContent.getMeasuredHeight());
        
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        
        Rect anchorRect = new Rect();
        anchor.getGlobalVisibleRect(anchorRect);
        Log.d(TAG, "Rect: "+anchorRect);
        int wantedCenter = anchorRect.centerX();   //((RelativeLayout.LayoutParams)anchor.getLayoutParams()).leftMargin;  //  anchorRect.centerX();  // (int)(tag.getLocationX());
        wantedCenter = (int)(tag.getLocationX() * screenWidth);
        int popupWidth = rootContent.getMeasuredWidth();
        int xPos;
        if(wantedCenter - popupWidth/2 <= 0){ // snap to left
            xPos = 0;
        } 
        else if (wantedCenter + popupWidth/2 >= screenWidth) { // snap to right
            xPos = screenWidth-popupWidth;
        } else {
            xPos = wantedCenter - popupWidth/2;
        }
        int arrowPos = wantedCenter-xPos-mArrow.getLayoutParams().width/2;
        
        // set arrow location to point at tag
        ViewGroup.MarginLayoutParams arrowParam = (ViewGroup.MarginLayoutParams)mArrow.getLayoutParams();
        arrowParam.leftMargin = (int) arrowPos;
        
        //int statusBarHeight = (int) Math.ceil(25 * mContext.getResources().getDisplayMetrics().density);
        int yPos = anchorRect.top-mReviewButton.getLayoutParams().height-mArrow.getLayoutParams().height*3/8;
        if(yPos <= mContext.getResources().getDimension(R.dimen.header_height)){
            yPos = (int)mContext.getResources().getDimension(R.dimen.header_height);
        }
        
        
        if(parentView != mRootView.getParent()){
            parentView.addView(mRootView);
            ((RelativeLayout.LayoutParams)mRootView.getLayoutParams()).leftMargin = xPos;
            ((RelativeLayout.LayoutParams)mRootView.getLayoutParams()).topMargin = yPos;
            //((RelativeLayout.LayoutParams)mRootView.getLayoutParams()).topMargin = ((RelativeLayout.LayoutParams)anchor.getLayoutParams()).topMargin-mReviewButton.getLayoutParams().height;
            //((RelativeLayout.LayoutParams)mRootView.getLayoutParams()).topMargin = (int) (displayMetrics.heightPixels*tag.getLocationY()-mReviewButton.getLayoutParams().height*1.3);  // *1.2 to add some density dependent extra space
            Log.d(TAG, "Showing rating");
        }else{
            Log.w(TAG, "Rating already shown");
        }
        
        Log.d(TAG, "Tag location: "+(int)(tag.getLocationX())+'x'+(int)(tag.getLocationY()));
        
        //mWindow.update();
        
        
    }
    
    private void setRootView(int rootId){
        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = (ViewGroup) mInflater.inflate(rootId, null);
        //mWindow.setContentView(mRootView);
        
        mArrow = (ImageView) mRootView.findViewById(R.id.arrow_down);

        mReviewButton = (Button) mRootView.findViewById(R.id.reviewButton);
        
        mRatingButtons.add((Button) mRootView.findViewById(R.id.awfulButton));
        mRatingButtons.add((Button) mRootView.findViewById(R.id.mediocreButton));
        mRatingButtons.add((Button) mRootView.findViewById(R.id.goodButton));
        mRatingButtons.add((Button) mRootView.findViewById(R.id.amazingButton));
        mButtonSeparators.add(mRootView.findViewById(R.id.separator1));
        mButtonSeparators.add(mRootView.findViewById(R.id.separator2));
        mButtonSeparators.add(mRootView.findViewById(R.id.separator3));
        mReviewButtonSeparator = mRootView.findViewById(R.id.separator4);
        
        mRatingClickListener = new OnClickListener(){
            @Override
            public void onClick(View clickedView) {
                rate((Button)clickedView);
            }
        };
        mUndoClickListener = new OnClickListener(){
            @Override
            public void onClick(View clickedView) {
                undoRate();
            }
        };
        
        for(Button button : mRatingButtons){
            button.setOnClickListener(mRatingClickListener);
        }
        
        
        
    }
    
    private void rate(Button ratingButton){
        Drawable buttonIcon = ratingButton.getCompoundDrawables()[1]; // topmost compound drawable
        if(buttonIcon instanceof TransitionDrawable){
            ((TransitionDrawable) buttonIcon).startTransition(ANIMATION_TIME/2);
        }
        else{
            Log.w(TAG, "ratingButton.getCompoundDrawables()[1] (layout.xml: Button.android:drawableTop) should be of type TransitionDrawable. It is currently a "+buttonIcon.getClass().getSimpleName());
        }
        
        mReviewButton.setVisibility(View.VISIBLE);
        mReviewButton.setClickable(true);
        int positionCount = 0;
        for(Button button : mRatingButtons){
            if(button != ratingButton){
                button.setVisibility(View.GONE);
                button.startAnimation(getVisibilityAnimation(positionCount, false, false));
                positionCount++;
            }
        }
        positionCount = 0;
        for(View v : mButtonSeparators){
            if(mButtonSeparators.indexOf(v) != mRatingButtons.indexOf(ratingButton)){  // Don't increase position count for divider right of clicked button
                positionCount++;
            }
            v.setVisibility(View.GONE);
            v.startAnimation(getMovementAnimation(positionCount, false, true));
        }
        mReviewButtonSeparator.setVisibility(View.VISIBLE);
        
        ratingButton.startAnimation(getMovementAnimation(-mRatingButtons.indexOf(ratingButton), true, true));
        mReviewButton.startAnimation(getMovementAnimation(-3, true, true));
        mReviewButtonSeparator.startAnimation(getMovementAnimation(-3, true, true));
        
        ratingButton.setOnClickListener(mUndoClickListener);
        mActiveRatingButton = ratingButton;
    }
    private void undoRate(){
        Button ratingButton = mActiveRatingButton;
        if(ratingButton != null){
            Drawable buttonIcon = ratingButton.getCompoundDrawables()[1]; // topmost compound drawable
            if(buttonIcon instanceof TransitionDrawable){
                ((TransitionDrawable) buttonIcon).reverseTransition(ANIMATION_TIME/2);
            }
            else{
                Log.w(TAG, "ratingButton.getCompoundDrawables()[1] (layout.xml: Button.android:drawableTop) should be of type TransitionDrawable. It is currently a "+buttonIcon.getClass().getSimpleName());
            }
            
            mReviewButton.setVisibility(View.GONE);
            mReviewButton.setClickable(false);
            int positionCount = 0;
            for(Button button : mRatingButtons){
                if(button != ratingButton){
                    button.setVisibility(View.VISIBLE);
                    button.startAnimation(getVisibilityAnimation(positionCount, true, true));
                    positionCount++;
                }
            }
            
            positionCount = 0;
            for(View v : mButtonSeparators){
                if(mButtonSeparators.indexOf(v) != mRatingButtons.indexOf(ratingButton)){  // Don't increase position count for divider right of clicked button
                    positionCount++;
                }
                v.setVisibility(View.VISIBLE);
                v.startAnimation(getMovementAnimation(positionCount, true, false));
                
            }
            mReviewButtonSeparator.setVisibility(View.GONE);
            
            ratingButton.startAnimation(getMovementAnimation(mRatingButtons.indexOf(ratingButton), true, false));
            mReviewButton.startAnimation(getMovementAnimation(-3, false, false));
            mReviewButtonSeparator.startAnimation(getMovementAnimation(-3, false, false));
            
            ratingButton.setOnClickListener(mRatingClickListener);
            mActiveRatingButton = null;
            
        }
        
    }
    
   
    /**
     *    
     * @return A rating of -1 to 3, where 0 is Avoid and 3 is Amazing. Returns -1 if no rating is given.
     */
    public int getCurrentRating(){
        if(mActiveRatingButton != null){
            return mRatingButtons.indexOf(mActiveRatingButton);
        }else{
            return -1;
        }
        
    }
    
    public void setOnReviewPressListener(OnClickListener listener){
        mReviewButton.setOnClickListener(listener);
    }
    
    public void dismiss(){
        ((ViewGroup)mRootView.getParent()).removeView(mRootView);
        undoRate();
        //mWindow.dismiss();
    }
    
    
    private static Animation getVisibilityAnimation(int distanceMultiplier, boolean entering, boolean delayed){
        AnimationSet animation = new AnimationSet(false);
        TranslateAnimation translate;
        //ScaleAnimation scale;
        AlphaAnimation alpha;
        
        if(entering){
            translate = new TranslateAnimation(-ratingButtonSize*distanceMultiplier, 0f, 0f, 0f);
            //scale = new ScaleAnimation(0f, 1f, 1f, 1f);
            alpha = new AlphaAnimation(0f, 1f);
        }else{
            translate = new TranslateAnimation(0f, -ratingButtonSize*distanceMultiplier, 0f, 0f);
            //scale = new ScaleAnimation(1f, 0f, 1f, 1f);
            alpha = new AlphaAnimation(1f, 0f);
        }
        //animation.addAnimation(scale);
        //animation.addAnimation(translate);
        translate.setDuration(ANIMATION_TIME/2);
        animation.addAnimation(alpha);
        alpha.setDuration(ANIMATION_TIME/2);
        //animation.setDuration(ANIMATION_TIME);
        if(delayed){
            animation.setStartOffset(ANIMATION_TIME/2);
        }
        animation.setInterpolator(new DecelerateInterpolator(1.2f));
        return animation;
    }
    private static Animation getMovementAnimation(int distanceMultiplier, boolean entering, boolean delayed){
        TranslateAnimation translate;
        if(entering){
            translate = new TranslateAnimation(-ratingButtonSize*(distanceMultiplier), 0f, 0f, 0f);
        }else{
            translate = new TranslateAnimation(0f, -ratingButtonSize*(distanceMultiplier), 0f, 0f);
        }
        translate.setDuration(ANIMATION_TIME/2);
        translate.setInterpolator(new DecelerateInterpolator(1.2f));
        if(delayed){
            translate.setStartOffset(ANIMATION_TIME/2);
        }
        return translate;
    }
    
    
    
    
    
}
