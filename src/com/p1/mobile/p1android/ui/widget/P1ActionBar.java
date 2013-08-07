package com.p1.mobile.p1android.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.listeners.NavigationListener;

/**
 * Custom Action Bar providing methods for adding one left and one right Action
 * as well as a center View.
 * 
 * This widget is intended to be used as the top bar. Not setting right, left or
 * center view will leave the ActionBar empty.
 * 
 * To set the center view, use the {@link #setCenterView(View view)
 * setCenterView} method and provide a arbitrary view. This view will be
 * centered in the action bar and match parent height, by default.
 * 
 * @author Viktor Nyblom
 * 
 */
public class P1ActionBar extends RelativeLayout implements OnClickListener {

    private ImageButton mLeftButton;
    private ImageButton mRightButton;
    private RelativeLayout mCenterStage;
    private RelativeLayout mActionBar;
    private LinearLayout mLayout;

    /**
     * Interface providing methods for a standard Action.
     * 
     */
    public interface Action {
        public int getDrawable();

        public void performAction();
    }

    public P1ActionBar(Context context) {
        super(context);
        init(context);
    }

    public P1ActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public P1ActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBar = (RelativeLayout) inflater.inflate(R.layout.p1actionbar,
                null);

        addView(mActionBar);
        mCenterStage = (RelativeLayout) mActionBar
                .findViewById(R.id.actionbar_center_stage);

        mLeftButton = (ImageButton) mActionBar
                .findViewById(R.id.actionbar_left_btn);

        mRightButton = (ImageButton) mActionBar
                .findViewById(R.id.actionbar_right_btn);

        mLayout = (LinearLayout) mActionBar.findViewById(R.id.actionbar_layout);

    }

    /**
     * Set the Action for the left action button.
     * 
     * @param action
     */
    public void setLeftAction(Action action) {
        mLeftButton.setOnClickListener(this);
        mLeftButton.setTag(action);
        mLeftButton.setImageResource(action.getDrawable());
        mLeftButton.setVisibility(View.VISIBLE);
    }

    /**
     * Set the Action for the right action button
     * 
     * @param action
     */
    public void setRightAction(Action action) {

        mRightButton.setOnClickListener(this);
        mRightButton.setTag(action);
        mRightButton.setImageResource(action.getDrawable());

        mRightButton.setVisibility(View.VISIBLE);
    }

    /**
     * Sets right view to arbitrary view
     * 
     * @param view
     */
    public void setRightView(View view) {
        android.widget.LinearLayout.LayoutParams pl = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (view instanceof UserPictureView)
            pl.rightMargin = 0;
        else
            pl.rightMargin = mLayout.getPaddingLeft();
        pl.weight = 1;
        pl.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        mLayout.addView(view, -1, pl);
        mLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Set the view to be displayed in the middle of the action bar. This view
     * will be centered by default.
     * 
     * @param view
     */
    public void setCenterView(View view) {
        mCenterStage.addView(view);
        mCenterStage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        final Object tag = v.getTag();
        if (tag instanceof Action) {
            final Action action = (Action) tag;
            action.performAction();
        }
    }

    public static abstract class AbstractAction implements Action {
        final private int mDrawable;

        public AbstractAction(int drawable) {
            mDrawable = drawable;
        }

        @Override
        public int getDrawable() {
            return mDrawable;
        }
    }

    public static class IntentAction extends AbstractAction {
        private static final String TAG = IntentAction.class.getSimpleName();
        private Context mContext;
        private Intent mIntent;

        public IntentAction(int drawable, Context context, Intent intent) {
            super(drawable);
            mContext = context;
            mIntent = intent;
        }

        public void setIntent(Intent intent) {
            mIntent = intent;
        }

        @Override
        public void performAction() {
            Log.d(TAG, "Perform action");
            if (mIntent == null) {
                Log.d(TAG, "Null intent");
                return;
            }
            try {
                mContext.startActivity(mIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static class ListenerAction extends AbstractAction {

        private OnActionListener listener;

        public ListenerAction(int drawable, OnActionListener listener) {
            super(drawable);
            this.listener = listener;
        }

        @Override
        public void performAction() {
            listener.onAction();
        }

    }

    public static class ShowNotificationsAction extends AbstractAction {

        private NavigationListener listener;

        public ShowNotificationsAction(int drawable, NavigationListener listener) {
            super(drawable);
            this.listener = listener;
        }

        @Override
        public void performAction() {
            listener.navigateToNotifications();
        }

    }

    public interface OnActionListener {
        public void onAction();
    }
}
