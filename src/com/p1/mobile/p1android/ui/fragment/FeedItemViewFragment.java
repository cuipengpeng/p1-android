package com.p1.mobile.p1android.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;
import com.p1.mobile.p1android.content.logic.ReadComment;
import com.p1.mobile.p1android.content.logic.ReadShare;
import com.p1.mobile.p1android.content.logic.WriteComment;
import com.p1.mobile.p1android.ui.helpers.FeedItem;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.ListenerAction;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.OnActionListener;
import com.p1.mobile.p1android.ui.widget.P1EditText;

public class FeedItemViewFragment extends Fragment implements OnActionListener {
    private FeedItem feedItem;
    private String shareId;
    private String pictureId;
    private P1ActionBar mActionBar;
    private P1EditText commentEditText;
    private Type mType = Type.share;

    enum Type {
        share, picture
    };

    public static Fragment newInstance(String shareId, String pictureId) {
        FeedItemViewFragment feedItemViewFragment = new FeedItemViewFragment();
        Bundle args = new Bundle();
        args.putString("shareId", shareId);
        args.putString("pictureId", pictureId);
        feedItemViewFragment.setArguments(args);
        return feedItemViewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.shareId = getArguments() != null ? getArguments().getString(
                "shareId") : "";
        this.pictureId = getArguments() != null ? getArguments().getString(
                "pictureId") : "";
        if (pictureId != null && !pictureId.isEmpty()) {
            mType = Type.picture;
        } else if (shareId != null && !shareId.isEmpty()) {
            mType = Type.share;
        }
        if (pictureId == null)
            pictureId = "";
        if (shareId == null)
            shareId = "";

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View content = View.inflate(getActivity(),
                R.layout.single_feed_item_fragment, null);
        commentEditText = (P1EditText) content
                .findViewById(R.id.sendbar_msg_txt);
        final Button sendButton = (Button) content
                .findViewById(R.id.sendbar_button);
        sendButton.setEnabled(false);
        commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sendButton.setEnabled(TextUtils.isEmpty(s.toString()) ? false : true);
            }
        });
        commentEditText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendComment();
                    return true;
                }
                return false;
            }
        });
        commentEditText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                commentEditText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollToBottom();
                    }
                }, 300);
            }
        });
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });

        // must not be null
        Share share = ReadShare.requestShare(shareId, null);
        if (share == null) {
            throw new NullPointerException();
        }
        ShareIOSession io = share.getIOSession();
        int type = -1;
        try {
            if (mType == Type.picture) {
                type = 0;
            } else {
                type = io.getPictureIds().size() - 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            io.close();
        }

        ViewGroup feedContainer = (ViewGroup) content
                .findViewById(R.id.feed_container);
        View feedItemView = inflater.inflate(
                BrowseFeedFragment.FEED_LAYOUT_RES_IDS[type], feedContainer,
                false);
        feedContainer.addView(feedItemView);
        feedItem = new FeedItem(getActivity(), true);
        feedItem.setView(feedItemView);
        mActionBar = (P1ActionBar) content.findViewById(R.id.feed_item_ab);
        mActionBar.setLeftAction(new ListenerAction(
                R.drawable.modal_close_button, this));
        return content;
    }

    private void sendComment() {
        String comment = commentEditText.getText().toString();
        if (!TextUtils.isEmpty(comment)) {
            if (Type.share.equals(mType)) {
                WriteComment.sendShareComment(comment, shareId);
            } else if (Type.picture.equals(mType)) {
                WriteComment.sendPictureComment(comment, pictureId);
            }
        }
        commentEditText.setText("");
        scrollToBottom();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (Type.share.equals(mType)) {
            feedItem.requestUpdatesForShare(shareId);
            ReadComment.fillComments(ContentHandler.getInstance().getShare(
                    shareId, null));
        } else if (Type.picture.equals(mType)) {
            feedItem.requestUpdatesForPicture(pictureId);
            ReadComment.fillComments(ContentHandler.getInstance().getPicture(
                    pictureId, null));
        } else {
            Log.e(FeedItemViewFragment.class.getSimpleName(),
                    "Nothing to show!!");
        }
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        scrollToBottom();
    }

    private void scrollToBottom() {
        final ScrollView scrollView = ((ScrollView) getView().findViewById(
                R.id.item_scrollview));
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, 5);
    }

    @Override
    public void onDestroyView() {
        ContentHandler.getInstance().removeRequester(feedItem);
        super.onDestroyView();
    }

    @Override
    public void onAction() {
        // TODO implement using contextual back
        getActivity().finish();
    }
}
