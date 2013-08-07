/**
 * ShareCaptionReviewEtcDialog.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.dialog;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Venue;
import com.p1.mobile.p1android.content.Venue.VenueIOSession;
import com.p1.mobile.p1android.content.logic.ReadVenue;

/**
 * @author Viktor Nyblom
 * 
 */
public class ShareDialog extends DialogFragment implements IContentRequester {
    static final String TAG = ShareDialog.class.getSimpleName();

    public static final String SHARE_DIALOG_VENUE_ID_KEY = "venueId";
    private Button mLocationButton;
    private Button mPostButton;
    private EditText mEditText;
    private WeakReference<ShareDialogListener> mCallback;
    private Venue mVenue;

    public interface ShareDialogListener {
        void onLocationRequested();

        void onPostRequested(String caption, String venueId);
    }

    public static ShareDialog newInstance(String venueId) {
        ShareDialog fragment = new ShareDialog();
        Bundle args = new Bundle();
        args.putString(SHARE_DIALOG_VENUE_ID_KEY, venueId);
        fragment.setArguments(args);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String venueId = getArguments().getString(
                SHARE_DIALOG_VENUE_ID_KEY);
        if (venueId != null) {
            mVenue = ReadVenue.requestVenue(venueId, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root,
            Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.share_dialog_layout, root, false);

        mLocationButton = (Button) view
                .findViewById(R.id.shareAddLocationButton);
        if (mLocationButton != null) {
            mLocationButton.setOnClickListener(new LocationButtonListener());
        }
        mLocationButton.setSelected(false);

        mPostButton = (Button) view.findViewById(R.id.sharePostButton);
        if (mPostButton != null) {
            mPostButton.setOnClickListener(new PostButtonListener());
        }

        mEditText = (EditText) view.findViewById(R.id.shareEditText);
        showKeyboard();
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard();
                } else {
                    hideKeyboard();
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mEditText.requestFocus();

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ContentHandler.getInstance().removeRequester(this);
        mCallback = null;
        mEditText.setOnFocusChangeListener(null);
        super.onDismiss(dialog);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = new WeakReference<ShareDialogListener>(
                    (ShareDialogListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnShareDialogFinished");
        }
    }

    private void showKeyboard() {
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void hideKeyboard() {
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private class LocationButtonListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            mCallback.get().onLocationRequested();
        }

    }

    private class PostButtonListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            String caption = mEditText.getText().toString();
            mCallback.get().onPostRequested(caption, getVenueId());

        }
    }

    private String getVenueId() {
        if (mVenue == null) {
            return null;
        }
        String venueId = null;
        VenueIOSession io = mVenue.getIOSession();
        try {
            venueId = io.getId();
        } finally {
            io.close();
        }
        return venueId;
    }

    @Override
    public void contentChanged(Content content) {
        if (!(content instanceof Venue)) {
            return;
        }

        VenueIOSession io = (VenueIOSession) content.getIOSession();
        try {
            String name = io.getName();
            mLocationButton.setText(name);
            mLocationButton.setSelected(true);
        } finally {
            io.close();
        }
    }

    public void setVenueId(String venueId) {
        mVenue = ReadVenue.requestVenue(venueId, this);
        contentChanged(mVenue);
    }

}
