package com.p1.mobile.p1android.ui.helpers;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Picture.ImageFormat;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;

public class NotificationImageHelper implements IContentRequester {
    private static final String TAG = NotificationImageHelper.class
            .getSimpleName();
    private ImageView mImgView;

    public NotificationImageHelper(ImageView mImgView) {
        this.setmImgView(mImgView);
    }

    @Override
    public void contentChanged(Content content) {
        String imageUrl = null;
        if (content instanceof Picture) {
            PictureIOSession ioSession = (PictureIOSession) content
                    .getIOSession();
            try {
                imageUrl = ioSession.getImageUrl(ImageFormat.IMAGE_SQUARE_180);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ioSession.close();
            }
        } else {
            Log.w(TAG, "Missing content type: " + content.getClass());
        }

        if (imageUrl != null) {
            ContentHandler.getInstance().removeRequester(this);
            P1Application.picasso.load(Uri.parse(imageUrl))
                    .placeholder(null).noFade().into(getmImgView());
            mImgView.setVisibility(View.VISIBLE);
        }
    }

    public ImageView getmImgView() {
        return mImgView;
    }

    public void setmImgView(ImageView mImgView) {
        this.mImgView = mImgView;
    }
}
