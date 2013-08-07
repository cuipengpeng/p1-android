package com.p1.mobile.p1android.ui.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.FollowList;
import com.p1.mobile.p1android.content.FollowList.FollowListIOSession;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadFollow;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.ui.helpers.FriendViewHolder;
import com.p1.mobile.p1android.util.Chinese2PinyinUtil;
import com.p1.mobile.p1android.util.ChineseRegexUtil;

public class FollowListAdapter extends BaseAdapter implements IContentRequester {
    public static final String TAG = FollowListAdapter.class.getSimpleName();

    private Activity mActivity;
    private FollowList mFollowList;

    /** Needed as the adapter content must be modified only in the UI thread */
    private List<String> mUserIdList = new ArrayList<String>();
    /** User ID list for search result */
    private List<String> mUserIdListSearchResult = new ArrayList<String>();
    /** Map save userID and user name's search key */
    private Map<String, String> mUserSearchKeyMap = new HashMap<String, String>();
    /** Map save userID and user name's compare key, used to sort users */
    private Map<String, String> mUserCompareKeyMap = new HashMap<String, String>();

    private boolean mIsSearchMode = false;

    private int total = 0;
    private int highestRequest = 0;

    public static final int NO_CHANGED = -1;

    private SearchUserRequester mSearchUserRequester = new SearchUserRequester();

    /** Used to keep track of all IContentRequesters */
    private List<IContentRequester> mActiveIContentRequesters = new ArrayList<IContentRequester>();

    private boolean mIsFollower;

    private List<String> mFollowingList;

    public FollowListAdapter(Activity activity, FollowList followList,
            boolean isFollower) {
        Log.d(TAG, "Constructor");
        this.mActivity = activity;
        this.mFollowList = followList;
        mIsFollower = isFollower;
        if (isFollower) {
            contentChanged(ReadFollow.requestLoggedInFollowingList(this));
        }

    }

    @Override
    public int getCount() {
        if (mIsSearchMode) {
            return mUserIdListSearchResult.size();
        } else {
            return total;
        }
    }

    @Override
    public void notifyDataSetChanged() {
        FollowListIOSession io = mFollowList.getIOSession();
        try {
            mUserIdList.clear();
            mUserIdList.addAll(io.getUserIdList());
            total = io.getPaginationTotal();

        } finally {
            io.close();
        }
        Log.d(TAG, "Dataset changed. Available information size is "
                + mUserIdList.size() + ", requested is " + highestRequest);
        if (highestRequest >= mUserIdList.size()) { // Not enough information is
                                                    // present
            ReadFollow.fillFollowList(mFollowList);
        }

        super.notifyDataSetChanged();

        // Get user search key
        for (String userID : mUserIdList) {
            if (userID != null) {
                // mActiveIContentRequesters.add(requester);
                User targetUser = ReadUser.requestUser(userID,
                        mSearchUserRequester);

                if (targetUser != null) {
                    String userSearchKey = getUserSearchKey(targetUser);
                    mUserSearchKeyMap.put(userID, userSearchKey);

                    String userCompareKey = getUserCompareKey(targetUser);
                    mUserCompareKeyMap.put(userID, userCompareKey);
                }
            }
        }

        // Sort Chinese and English name together
        Comparator<String> comparator = new Comparator<String>() {
            public int compare(String user1, String user2) {
                String compare1 = mUserCompareKeyMap.get(user1);
                String compare2 = mUserCompareKeyMap.get(user2);

                return compare1.compareTo(compare2);
            }
        };
        Collections.sort(mUserIdList, comparator);
    }

    @Override
    /**
     * Returns the id of the User at that position
     */
    public String getItem(int position) {
        Log.i(TAG, "getItem()-------position = " + position);
        if (mIsSearchMode) {
            return mUserIdListSearchResult.get(position);
        } else {
            if (position > highestRequest) {
                highestRequest = position;
                Log.d(TAG, "Highest request is " + highestRequest);
            }

            if (position >= mUserIdList.size()) {
                ReadFollow.fillFollowList(mFollowList);
                return null;
            }
            return mUserIdList.get(position); // TOOD use pagination
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView");
        View row = convertView;
        FriendViewHolder holder = null;
        if (row == null) {
            row = mActivity.getLayoutInflater().inflate(
                    R.layout.follow_list_item, null);

            holder = new FriendViewHolder(
                    (TextView) row.findViewById(R.id.follow_list_text),
                    (ImageView) row.findViewById(R.id.follow_list_picture),
                    (ToggleButton) row.findViewById(R.id.follow_button));
            mActiveIContentRequesters.add(holder);
            holder.follow.setVisibility(mIsFollower ? View.VISIBLE : View.GONE);
            row.setTag(holder);
        } else {

            holder = (FriendViewHolder) row.getTag();
            ContentHandler.getInstance().removeRequester(holder);
        }
        String userId = getItem(position);
        if (userId != null) {
            User user = ReadUser.requestUser(userId, holder);
            holder.contentChanged(user);
            if (mFollowingList != null) {
                holder.follow.setOnCheckedChangeListener(null);
                holder.follow.setChecked(mFollowingList.contains(userId));
                holder.follow.setEnabled(true);
                holder.follow.setOnCheckedChangeListener(holder);
            }
            // Consistently update the UI
        } else {
            holder.contentChanged(null);
            if (mFollowingList != null) {
                holder.follow.setOnCheckedChangeListener(null);
                holder.follow.setChecked(false);
                holder.follow.setEnabled(false);
                holder.follow.setOnCheckedChangeListener(holder);
            }
        }
        return row;
    }

    public void destroy() {
        ContentHandler.getInstance().removeRequester(mSearchUserRequester);
        for (IContentRequester requester : mActiveIContentRequesters) {
            ContentHandler.getInstance().removeRequester(requester);
        }
        mActiveIContentRequesters.clear();
        ContentHandler.getInstance().removeRequester(this);
    }

    public int search(String searchArgs) {
        // If user input contains '|', fail our search, by replace it by a
        // upper-case latter, which
        // is not in our search key
        String input = StringUtils.stripAccents(searchArgs).trim()
                .toLowerCase(Locale.getDefault()).replace("|", "A");
        if (input.length() > 0) {
            boolean matchStart = input.charAt(0) >= 'a'
                    && input.charAt(0) <= 'z';
            if (matchStart)
                input = " " + input;
            mUserIdListSearchResult.clear();
            mIsSearchMode = true;
            for (String userID : mUserIdList) {
                if (userID != null) {
                    String searchKey = mUserSearchKeyMap.get(userID);
                    if (searchKey.contains(input)) {
                        mUserIdListSearchResult.add(userID);
                    }
                }
            }
        } else {
            mIsSearchMode = false;
            return NO_CHANGED;
        }

        super.notifyDataSetChanged();
        return mUserIdListSearchResult.size();
    }

    /**
     * Search key used for compare when searching Example: Search key of
     * "wangyu(Chinese)" is " wangyu| wu|wangyu(Chinese)|", key of "Adam Kull"
     * is " adam kull| ak|" The space is for matching the start of input, but
     * Chinese match can start anywhere
     */
    public String getUserSearchKey(User user) {
        UserIOSession io = user.getIOSession();
        StringBuilder strBuilder = new StringBuilder();
        try {
            String userName = StringUtils.stripAccents(
                    io.getPreferredFullName()).toLowerCase(Locale.getDefault());
            boolean containsChinese = ChineseRegexUtil
                    .isContainsChinese(userName);
            String enReg = " "
                    + (containsChinese ? Chinese2PinyinUtil
                            .converterToSpell(userName) : userName);
            strBuilder.append(enReg);
            strBuilder.append("| ");

            if (containsChinese) {
                strBuilder.append(Chinese2PinyinUtil
                        .converterToFirstSpell(userName));
            } else {
                boolean appendNext = false;
                for (int i = 0; i < enReg.length(); i++) {
                    if (enReg.charAt(i) == ' ') {
                        appendNext = true;
                    } else if (appendNext) {
                        strBuilder.append(enReg.charAt(i));
                        appendNext = false;
                    }
                }
            }
            strBuilder.append("|");
            if (containsChinese) {
                strBuilder.append(userName);
                strBuilder.append("|");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        } finally {
            io.close();
        }
        String key = strBuilder.toString();
        return key;
    }

    /**
     * Compare key used to sort Chinese name and English name
     */
    public String getUserCompareKey(User user) {
        UserIOSession io = user.getIOSession();

        String userCompareKey = "";
        try {
            String userName = io.getPreferredFullName();
            userCompareKey = userName;

            if (ChineseRegexUtil.isContainsChinese(userName)) {
                userCompareKey = Chinese2PinyinUtil.converterToSpell(userName);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        } finally {
            io.close();
        }

        return userCompareKey.toLowerCase(Locale.getDefault());
    }

    public class SearchUserRequester implements IContentRequester {

        @Override
        public void contentChanged(Content content) {
            User targetUser = (User) content;
            String userSearchKey = getUserSearchKey(targetUser);
            String userCompareKey = getUserCompareKey(targetUser);

            UserIOSession io = ((com.p1.mobile.p1android.content.User) content)
                    .getIOSession();
            try {

                String userID = io.getId();

                mUserSearchKeyMap.put(userID, userSearchKey);
                mUserCompareKeyMap.put(userID, userCompareKey);

            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                io.close();
            }
        }
    }

    @Override
    public void contentChanged(Content content) {
        FollowListIOSession io = (FollowListIOSession) content.getIOSession();
        try {
            mFollowingList = io.getUserIdList();
            notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        } finally {
            io.close();
        }
    }
}