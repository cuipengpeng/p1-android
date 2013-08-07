package com.p1.mobile.p1android.ui.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.FollowList;
import com.p1.mobile.p1android.content.FollowList.FollowListIOSession;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.IContentRequester.IChildContentRequester;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadFollow;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.content.logic.WriteFollow;
import com.p1.mobile.p1android.util.Utils;
import com.squareup.picasso.Picasso;

public class LikerListAdapter extends BaseAdapter {
    public static final String TAG = LikerListAdapter.class.getSimpleName();

    private Context mCtx;
    private List<String> mLikeUserIds;
    // private String mShareId;

    /** Used to keep track of all IContentRequesters */
    private List<IContentRequester> mActiveIContentRequesters = new ArrayList<IContentRequester>();

    public LikerListAdapter(Context context, List<String> likeIds) {
        this.mCtx = context;
        this.mLikeUserIds = likeIds;
    }

    @Override
    public int getCount() {
        return mLikeUserIds.size();
    }

    @Override
    /**
     * Returns the id of the User at that position
     */
    public String getItem(int position) {
        return mLikeUserIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mCtx).inflate(
                    R.layout.follow_list_item, null);

            holder = new ViewHolder(
                    (TextView) convertView.findViewById(R.id.follow_list_text),
                    (ImageView) convertView
                            .findViewById(R.id.follow_list_picture),
                    (ToggleButton) convertView.findViewById(R.id.follow_button));
            mActiveIContentRequesters.add(holder);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            ContentHandler.getInstance().removeRequester(holder);
        }
        final String userId = getItem(position);
        if (userId != null) {
            User user = ReadUser.requestUser(userId, holder);
            holder.contentChanged(user);
            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.openProfile(mCtx, userId);
                }
            });
        } else {
            holder.contentChanged(null);
        }
        return convertView;
    }

    public void destroy() {
        for (IContentRequester requester : mActiveIContentRequesters) {
            ContentHandler.getInstance().removeRequester(requester);
        }
        mActiveIContentRequesters.clear();
    }

    public interface IListChanged {
        void listChanged(int size);
    }

    public class ViewHolder implements IContentRequester,
            IChildContentRequester, OnCheckedChangeListener {
        public TextView textView;
        public ImageView imageView;
        public ToggleButton followButton;
        private String mUserid;

        private IContentRequester mFollowingListRequester = new IContentRequester() {

            @Override
            public void contentChanged(Content content) {
                FollowList followList = (FollowList) content;
                FollowListIOSession io = followList.getIOSession();
                try {
                    if (!io.isValid()) {
                        return;
                    }
                    followButton.setOnCheckedChangeListener(null);
                    followButton.setChecked(io.isFollowing(mUserid));
                    followButton.setOnCheckedChangeListener(ViewHolder.this);
                } finally {
                    io.close();
                }
            }

        };

        public ViewHolder(TextView textView, ImageView imageView,
                ToggleButton toogleButton) {
            this.followButton = toogleButton;
            this.imageView = imageView;
            this.textView = textView;
            // updateView(textView, imageView);
        }

        // public void updateView(TextView textView, ImageView imageView) {
        // if (textView != this.textView && imageView != this.imageView) {
        // this.textView = textView;
        // this.imageView = imageView;
        // resetViews();
        // }
        // this.textView = textView;
        // this.imageView = imageView;
        // }

        @Override
        /** Called when the User displayed by the ViewHolder views is updated */
        public void contentChanged(Content content) {
            if (content == null) {
                resetViews();
                mUserid = null;
                return;
            }
            UserIOSession io = ((com.p1.mobile.p1android.content.User) content)
                    .getIOSession();
            try {
                mUserid = io.getId();
                String userName = io.getPreferredFullName();
                P1Application.picasso
                        .load(Uri.parse(io.getProfileThumb100Url())).noFade()
                        .placeholder(null).into(imageView);
                textView.setText(userName != null ? userName : "Unknown");
                mFollowingListRequester.contentChanged(ReadFollow
                        .requestLoggedInFollowingList(mFollowingListRequester));
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                io.close();
            }
        }

        public void resetViews() {
            textView.setText("Unknown");
            imageView.setImageBitmap(null);
        }

        @Override
        public void removeChildRequestors() {
            ContentHandler.getInstance().removeRequester(
                    mFollowingListRequester);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            if (mUserid != null)
                WriteFollow.toggleFollow(mUserid);
        }

    }

    public void updateList(List<String> likeUserIds) {
        this.mLikeUserIds = likeUserIds;
        notifyDataSetChanged();
    }

}