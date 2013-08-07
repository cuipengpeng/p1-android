package com.p1.mobile.p1android.ui.view;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.ui.helpers.FeedItem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class FeedLinearLayout extends LinearLayout implements
        OnGestureListener {

    private int state = FeedItem.NORMAL;
    private Drawable foreground;
    private TextPaint paint;
    private String UPLOADING;
    private String AGAIN;
    private float textWidth;
    private GestureDetector dectator;

    public FeedLinearLayout(Context context) {
        super(context);
    }

    public FeedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FeedLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
    }

    public void setState(int state) {
        if (this.state == state)
            return;
        this.invalidate();
        this.state = state;
        if (state != FeedItem.NORMAL && foreground == null) {
            foreground = getContext().getResources().getDrawable(
                    R.drawable.feed_item_foreground);
            paint = new TextPaint();
            paint.setTextSize(getContext().getResources().getDisplayMetrics().density * 14);
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            UPLOADING = getContext().getString(R.string.uploading);
            AGAIN = getContext().getString(R.string.failed);
        }
        if (state != FeedItem.NORMAL)
            textWidth = paint
                    .measureText(state == FeedItem.UPLOADING ? UPLOADING
                            : AGAIN);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result = super.drawChild(canvas, child, drawingTime);
        if (state != FeedItem.NORMAL) {
            foreground.setBounds(0, 0, getWidth(), getHeight());
            foreground.draw(canvas);
        }
        if (state == FeedItem.UPLOADING) {
            canvas.drawText(UPLOADING, (getWidth() - textWidth) / 2,
                    getHeight() / 2, paint);
        } else if (state == FeedItem.FAILED) {
            canvas.drawText(AGAIN, (getWidth() - textWidth) / 2,
                    getHeight() / 2, paint);
        }
        return result;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (state != FeedItem.NORMAL) {
            return true;
        } else
            return super.onInterceptTouchEvent(ev);
    }
   

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (state != FeedItem.NORMAL) {
            if (dectator == null)
                dectator = new GestureDetector(this);
            dectator.onTouchEvent(event);
            return true;
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (state == FeedItem.FAILED) {
            Object a = this.getTag();
            if (a instanceof FeedItem) {
                ((FeedItem) a).reload();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        return false;
    }

}
