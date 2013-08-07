package com.p1.mobile.p1android.ui.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.Conversation;
import com.p1.mobile.p1android.content.Conversation.ConversationIOSession;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.IContentRequester.IChildContentRequester;
import com.p1.mobile.p1android.content.Message.MessageIOSession;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadMessage;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.util.Utils;
import com.squareup.picasso.Picasso;

public class BrowseMessagesListAdapter extends BaseAdapter {
    public static final String TAG = BrowseMessagesListAdapter.class
            .getSimpleName();

    private Context mContext;
    private List<IContentRequester> mActiveIContentRequesters = new ArrayList<IContentRequester>();
    String mConversationId;
    Conversation conversation;
    IContentRequester listener;
    String loggedInUserId = null;
    ArrayList<ArrayList<String>> groups = new ArrayList<ArrayList<String>>();

    private int total = 0;

    public BrowseMessagesListAdapter(Conversation conv, Context mContext,
            IContentRequester contentChangeListener) {
        super();
        conversation = conv;
        this.mContext = mContext;
        listener = contentChangeListener;
        mActiveIContentRequesters.add(listener);

    }

    public int getItemMessageCount() {
        return total;
    }

    @Override
    public void notifyDataSetChanged() {
        ConversationIOSession io = conversation.getIOSession();
        ArrayList<String> mMessageIdList = new ArrayList<String>();
        try {
            mMessageIdList.clear();
            mMessageIdList.addAll(io.getMessageIdList());
            // mVisibleMessageCount = mMessageIdList.size();
            total = mMessageIdList.size();
        } finally {
            io.close();
        }
        groups = Utils.sortAndGroupMessages(mMessageIdList);

        if (loggedInUserId == null) {
            UserRequester requester = new UserRequester();
            mActiveIContentRequesters.add(requester);
            requester.contentChanged(ReadUser.requestLoggedInUser(requester));
        }
        super.notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private boolean isSeparator(int position) {
        int tmp = position;
        for (int i = 0; i < groups.size(); i++) {
            ArrayList<String> tList = groups.get(i);
            if (tmp == 0) {
                return true;
            } else if (tmp > tList.size()) {
                tmp -= (tList.size() + 1);
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        if (isSeparator(position))
            return 0;
        else
            return 1;
    }

    @Override
    public Object getItem(int position) {
        int tmp = position;
        for (int i = 0; i < groups.size(); i++) {
            ArrayList<String> tList = groups.get(i);
            if (tmp == 0) {
                return null; // Header for this group
            } else if (tmp > tList.size()) {
                tmp -= (tList.size() + 1);
            } else {
                return tList.get(tmp - 1);
            }
        }
        return null;
    }

    // @Override
    // public boolean hasStableIds() {
    // return true;
    // }

    @Override
    public int getCount() {
        return total + groups.size();
    }

    private HashMap<String, Integer> ItemIds = new HashMap<String, Integer>();
    int index = 0;

    @Override
    public long getItemId(int position) {
        return position;
        // String messageId = (String) getItem(position);
        // if (messageId != null) {
        // if (!ItemIds.containsKey(messageId)) {
        // ItemIds.put(messageId, index++);
        // }
        // return ItemIds.get(messageId);
        // } else
        // return total + position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (isSeparator(position)) {
            convertView = setupHeaderView(position, convertView, parent);
        } else {
            convertView = setupMessageView(position, convertView);
        }
        return convertView;
    }

    private View setupMessageView(final int position, View convertView) {
        String messageId = (String) getItem(position);
        ChatViewHolder chatHolder;
        if (convertView == null
                || !(convertView.getTag() instanceof ChatViewHolder)) {

            Utils.removeRequester(convertView);
            // Creating a new LinearLayout
            convertView = new LinearLayout(mContext);
            ((LinearLayout) convertView).setOrientation(LinearLayout.VERTICAL);
            chatHolder = new ChatViewHolder((ViewGroup) convertView);
            mActiveIContentRequesters.add(chatHolder);
            convertView.setTag(chatHolder);
        } else {
            chatHolder = (ChatViewHolder) convertView.getTag();
            ContentHandler.getInstance().removeRequester(chatHolder);
        }

        if (messageId != null) {
            chatHolder.contentChanged(ReadMessage.requestMessage(messageId,
                    chatHolder));
        }
        return convertView;
    }

    private View setupHeaderView(final int position, View convertView,
            ViewGroup parent) {
        String id = (String) getItem(position + 1);
        HeaderViewHolder hh;

        if (convertView == null
                || !(convertView.getTag() instanceof HeaderViewHolder)) {
            Utils.removeRequester(convertView);
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.browse_messages_fragment_list_item_date, parent,
                    false);
            hh = new HeaderViewHolder();
            hh.mDate = (TextView) convertView.findViewById(R.id.date_divider);
            mActiveIContentRequesters.add(hh);
            convertView.setTag(hh);
        } else {
            hh = (HeaderViewHolder) convertView.getTag();
            ContentHandler.getInstance().removeRequester(hh);
        }

        if (id != null) {
            hh.contentChanged(ReadMessage.requestMessage(id, hh));
        }
        return convertView;
    }

    String firstUserId = null;

    public class ChatViewHolder implements IContentRequester,
            IChildContentRequester {

        public TextView mTimeTextView;
        public UserViewHolder mUserViewHolder = new UserViewHolder();
        public TextView mMessageOverViewTextView;
        private ViewGroup parentLayout;
        public boolean mIsOwner = false;
        public boolean mError = false;
        public boolean mSending = false;
        public String mMessageId;

        public ChatViewHolder(ViewGroup parent) {
            parentLayout = parent;
        }

        @Override
        public void contentChanged(Content content) {
            if (content == null) {
                mUserViewHolder.contentChanged(null);
                parentLayout.removeAllViews();
                mIsOwner = false;
                mMessageId = null;
                return;
            }

            MessageIOSession io = (MessageIOSession) content.getIOSession();
            View messageRowView = null;
            LayoutInflater li = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            try {
                mMessageId = io.getId();

                if (loggedInUserId != null
                        && loggedInUserId.equals(io.getOwnerId())) {
                    messageRowView = li.inflate(
                            R.layout.browse_messages_fragment_list_item_right,
                            parentLayout, false);
                    mIsOwner = false;
                } else {
                    messageRowView = li.inflate(
                            R.layout.browse_messages_fragment_list_item_left,
                            parentLayout, false);
                    mIsOwner = true;
                }

                mUserViewHolder.mProfileImageView = (ImageView) messageRowView
                        .findViewById(R.id.iv_browse_fragment_list_profile);
                mMessageOverViewTextView = (TextView) messageRowView
                        .findViewById(R.id.chat_list_item_message);
                mTimeTextView = (TextView) messageRowView
                        .findViewById(R.id.chat_list_item_message_time);
                parentLayout.removeAllViews();
                parentLayout.addView(messageRowView);
                mError = io.hasFailedNetworkOperation();
                mSending = io.isSending();
                mMessageOverViewTextView.setText(Utils.getEmoticons(mContext,
                        io.getValue()));
                if (mError) {
                    messageRowView.findViewById(R.id.chat_error_image)
                            .setVisibility(View.VISIBLE);
                    mTimeTextView.setText(mContext
                            .getString(R.string.messages_error));
                } else if (mSending) {
                    mTimeTextView.setText(mContext
                            .getString(R.string.messages_sending));
                } else {
                    mTimeTextView.setText(Utils.getTimeofDay(io
                            .getCreatedTime()));
                }
                User user = ReadUser.requestUser(io.getOwnerId(),
                        mUserViewHolder);
                mUserViewHolder.contentChanged(user);
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                io.close();
            }
        }

        @Override
        public void removeChildRequestors() {
            ContentHandler.getInstance().removeRequester(mUserViewHolder);
        }
    }

    public class HeaderViewHolder implements IContentRequester {

        public TextView mDate;

        @Override
        public void contentChanged(Content content) {
            if (content == null) {
                mDate.setText(null);
                return;
            }
            MessageIOSession io = (MessageIOSession) content.getIOSession();
            try {
                String dateStr = Utils.getDate_Absolute(io.getCreatedTime());
                mDate.setVisibility(View.VISIBLE);
                mDate.setText(dateStr);

                if (dateStr == null || dateStr.length() == 0) {
                    mDate.setVisibility(View.INVISIBLE);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                io.close();
            }
        }
    }

    public class UserViewHolder implements IContentRequester {

        public ImageView mProfileImageView;

        @Override
        public void contentChanged(Content content) {
            if (content == null) {
                mProfileImageView.setImageDrawable(null);
                return;
            }
            UserIOSession userIO = ((com.p1.mobile.p1android.content.User) content)
                    .getIOSession();

            try {
                P1Application.picasso.load(userIO.getProfileThumb100Url())
                        .noFade().placeholder(null).into(mProfileImageView);
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                userIO.close();
            }

        }
    }

    public void destroy() {
        for (IContentRequester requester : mActiveIContentRequesters) {
            ContentHandler.getInstance().removeRequester(requester);
        }
        mActiveIContentRequesters.clear();
    }

    public class UserRequester implements IContentRequester {

        @Override
        public void contentChanged(Content content) {
            if (content instanceof User) {
                UserIOSession io = ((com.p1.mobile.p1android.content.User) content)
                        .getIOSession();
                try {
                    loggedInUserId = io.getId();
                    BrowseMessagesListAdapter.this.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(TAG, "Error", e);
                } finally {
                    io.close();
                }

            }
        }
    }

    public String getMessageId(int position) {
        if (getItemViewType(position) == 1) {
            String messageId = (String) getItem(position);
            return messageId;
        } else
            return null;

    }

    public int getMessagePosition(String messageId) {
        for (int i = 0; i < getCount(); i++) {
            if (messageId.equals(getItem(i))) {
                return i;
            }
        }
        return -1;
    }

}
