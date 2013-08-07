package com.p1.mobile.p1android.ui.adapters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.Conversation.ConversationIOSession;
import com.p1.mobile.p1android.content.ConversationList;
import com.p1.mobile.p1android.content.ConversationList.ConversationListIOSession;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.IContentRequester.IChildContentRequester;
import com.p1.mobile.p1android.content.IContentRequester.IhasTimers;
import com.p1.mobile.p1android.content.Message;
import com.p1.mobile.p1android.content.Message.MessageIOSession;
import com.p1.mobile.p1android.content.Picture.ImageFormat;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadConversation;
import com.p1.mobile.p1android.content.logic.ReadMessage;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.util.Utils;
import com.squareup.picasso.Picasso;

public class BrowseConversationListAdapter extends BaseAdapter {
    public static final String TAG = BrowseConversationListAdapter.class
            .getSimpleName();

    private ConversationList mConversationsList;
    private Context mContext;

    private List<String> mMemberIdList = new ArrayList<String>();
    private int mHighestRequest = 0;
    private List<IContentRequester> mActiveIContentRequesters = new ArrayList<IContentRequester>();
    private IContentRequester mListener;
    String loggedInUserId = null;

    public BrowseConversationListAdapter(ConversationList memberList, Context mContext,
            IContentRequester listner) {
        super();
        this.mConversationsList = memberList;
        this.mContext = mContext;
        mListener = listner;
    }

    @Override
    public void notifyDataSetChanged() {
        ConversationListIOSession io = this.mConversationsList.getIOSession();
        try {
            mMemberIdList.clear();
            mMemberIdList.addAll(io.getConversationIdList());
        } finally {
            io.close();
        }

        Log.d(TAG, "Dataset changed. Available information size is "
                + mMemberIdList.size() + ", requested is " + mHighestRequest);
        if (mHighestRequest >= mMemberIdList.size()) { // Not enough information
                                                       // is present
            ReadConversation.requestConversationList(mListener);
            mActiveIContentRequesters.add(mListener);
        }
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMemberIdList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMemberIdList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        String convId = (String) getItem(position);

        final ConversationViewHolder memberHolder;
        LinearLayout userInfoLinearLayout;
        if (convertView == null) {
            convertView = View.inflate(mContext,
                    R.layout.browse_conversation_fragment_list_item, null);

            memberHolder = new ConversationViewHolder();
            mActiveIContentRequesters.add(memberHolder);
            userInfoLinearLayout = (LinearLayout) convertView
                    .findViewById(R.id.ll_conversation_fragment_list_header);
            memberHolder.mUserViewHolder.mProfileImageView = (ImageView) convertView
                    .findViewById(R.id.iv_conversation_fragment_list_profile);
            memberHolder.mUserViewHolder.mUserNameTextView = (TextView) convertView
                    .findViewById(R.id.tv_conversation_fragment_list_username);
            memberHolder.mMessageViewHolder.mMessageOverViewTextView = (TextView) convertView
                    .findViewById(R.id.tv_conversation_list_message);
            memberHolder.mTimeTextView = (TextView) convertView
                    .findViewById(R.id.tv_conversation_fragment_list_time);
            memberHolder.mUnreadIcon = convertView
                    .findViewById(R.id.conversation_unread_image);
            convertView.setTag(memberHolder);
        } else {
            userInfoLinearLayout = (LinearLayout) convertView
                    .findViewById(R.id.ll_conversation_fragment_list_header);
            memberHolder = (ConversationViewHolder) convertView.getTag();
            ContentHandler.getInstance().removeRequester(memberHolder);
        }

        if (convId != null) {
            memberHolder.contentChanged(ReadConversation.requestConversation(
                    convId, memberHolder));
        }
        return convertView;
    }

    public class ConversationViewHolder implements IContentRequester,
            IChildContentRequester, IhasTimers {

        public TextView mTimeTextView;
        public View mUnreadIcon;
        public UserViewHolder mUserViewHolder = new UserViewHolder();
        public MessageViewHolder mMessageViewHolder = new MessageViewHolder();
        public String mConversationId;
        private Timer timer = new Timer();

        @Override
        public void contentChanged(Content content) {
            if (content == null) {
                mUserViewHolder.contentChanged(null);
                mTimeTextView.setText("");
                mConversationId = null;
                return;
            }

            ConversationIOSession io = ((com.p1.mobile.p1android.content.Conversation) content)
                    .getIOSession();

            try {
                // set user variables
                mUnreadIcon.setVisibility(io.isRead() ? View.INVISIBLE
                        : View.VISIBLE);
                mConversationId = io.getId();
                String userID = io.getOtherUserId();
                User user = ReadUser.requestUser(userID, mUserViewHolder);
                mUserViewHolder.contentChanged(user);
                // Utils.attachRefreshTimer(timer, io.getLatestTime(), 30,
                // mTimeTextView);
                setTimeStampTask(io.getLatestTime());
                // mTimeTextView.setText(Utils.getRelativeTime(io.getLatestTime(),
                // mContext));
                mMessageViewHolder.mIsUnread = !io.isRead();
                Message msg = ReadMessage.requestMessage(
                        io.getNewestMessageId(), mMessageViewHolder);
                mMessageViewHolder.contentChanged(msg);
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                io.close();
            }
        }

        @Override
        public void removeChildRequestors() {
            ContentHandler.getInstance().removeRequester(mMessageViewHolder);
            ContentHandler.getInstance().removeRequester(mUserViewHolder);
        }

        @Override
        public void removetimer() {
            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
        }

        private void setTimeStampTask(final Date createdTime) {
            if (createdTime != null) {
                timer.cancel();
                timer = new Timer();
                mTimeTextView.setText(Utils.getRelativeTime(createdTime,
                        mTimeTextView.getContext()));
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (mTimeTextView != null) {
                            mTimeTextView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mTimeTextView.setText(Utils
                                            .getRelativeTime(createdTime,
                                                    mTimeTextView.getContext()));
                                }
                            }, 0);
                        }
                    }
                }, 0, 1000 * 30); // Every 30sec
            }
        }
    }

    public class UserViewHolder implements IContentRequester {

        public ImageView mProfileImageView;
        public TextView mUserNameTextView;
        private String userId;

        @Override
        public void contentChanged(Content content) {
            if (content == null) {
                mProfileImageView.setImageDrawable(null);
                mUserNameTextView.setText("Unknown");
                return;
            }
            UserIOSession io = ((com.p1.mobile.p1android.content.User) content)
                    .getIOSession();

            try {

                P1Application.picasso
                        .load(Uri.parse(io.getProfileThumb100Url())).noFade()
                        .placeholder(null).into(mProfileImageView);
                mUserNameTextView.setText(io.getEnUsFullname());
                userId = io.getId();
                mProfileImageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Utils.openProfile(mContext, userId);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                io.close();
            }
        }
    }

    public class MessageViewHolder implements IContentRequester {

        public TextView mMessageOverViewTextView;
        public boolean mIsUnread = false;

        @Override
        public void contentChanged(Content content) {
            if (content == null) {
                mMessageOverViewTextView.setText("");
                return;
            }
            MessageIOSession messageIO = ((com.p1.mobile.p1android.content.Message) content)
                    .getIOSession();

            try {
                mMessageOverViewTextView.setText(messageIO.getValue());
                // make bold if unread
                // mMessageOverViewTextView.setTypeface(null, mIsUnread ?
                // Typeface.BOLD : Typeface.NORMAL);
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                messageIO.close();
            }

        }
    }

    public void destroy() {
        for (IContentRequester requester : mActiveIContentRequesters) {
            ContentHandler.getInstance().removeRequester(requester);
        }
        mActiveIContentRequesters.clear();
    }
}
