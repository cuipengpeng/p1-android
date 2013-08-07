package com.p1.mobile.p1android.ui.view;

import com.p1.mobile.p1android.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FeedImageView extends ImageView {

    private Point size;
    private int maxHeight;
    private int minHeight;
//    private int radis;
//    private Path clipPath;
    private Drawable topShadow, bottomShadow;
    public Drawable leftCorner, rightCorner;

    public FeedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public FeedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public FeedImageView(Context context) {
        super(context);
        init(context, null, 0);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        if (attrs != null) {
            TypedArray values = context.obtainStyledAttributes(attrs,
                    R.styleable.FeedImageView, defStyle, 0);
            maxHeight = values.getDimensionPixelSize(
                    R.styleable.FeedImageView_maxHeight, 2048);
            minHeight = values.getDimensionPixelSize(
                    R.styleable.FeedImageView_minHeight, 72);
            values.recycle();
        }
//
//        radis = (int) (context.getResources().getDisplayMetrics().density * 5);

        Resources res = context.getResources();
        topShadow = res.getDrawable(R.drawable.feed_item_top_gradient);
        bottomShadow = res.getDrawable(R.drawable.feed_item_bottom_gradient);
        leftCorner = res.getDrawable(R.drawable.img_corner_left);
        rightCorner = res.getDrawable(R.drawable.img_corner_right);
    }

    public void setImageSize(Point s) {
        if (size == null || (size.x * s.y != s.x * size.y))
            requestLayout();
        size = s;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (size == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width * size.y / size.x;
        height = Math.max(height, minHeight);
        height = Math.min(height, maxHeight);
        setMeasuredDimension(width, height);
//        clipPath = new Path();
//        clipPath.addRoundRect(new RectF(0, 0, width, height + radis), radis,
//                radis, Path.Direction.CW);
        topShadow.setBounds(0, 0, width, height * 5 / 15);
        bottomShadow.setBounds(0, height * 13 / 15, width, height);
        leftCorner.setBounds(0, 0, leftCorner.getIntrinsicWidth(),
                leftCorner.getIntrinsicHeight());
        rightCorner.setBounds(width - rightCorner.getIntrinsicWidth(), 0,
                width, leftCorner.getIntrinsicHeight());
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
//        if (clipPath != null)
//            canvas.clipPath(clipPath);
        super.onDraw(canvas);
        topShadow.draw(canvas);
        bottomShadow.draw(canvas);
        leftCorner.draw(canvas);
        rightCorner.draw(canvas);
        canvas.restore();
    }
}
