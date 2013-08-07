package com.p1.mobile.p1android.ui.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.FollowList;
import com.p1.mobile.p1android.content.FollowList.FollowListIOSession;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.logic.ReadFollow;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.net.NetworkUtilities;
import com.p1.mobile.p1android.ui.listeners.AfterTextChangeWatcher;
import com.p1.mobile.p1android.ui.listeners.NavigationListener;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.ListenerAction;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.OnActionListener;
import com.p1.mobile.p1android.util.Chinese2PinyinUtil;
import com.p1.mobile.p1android.util.ChineseRegexUtil;
import com.p1.mobile.p1android.util.Utils;

public class NewConversationFragment extends Fragment implements
        OnActionListener {
    private static final String TAG = NewConversationFragment.class
            .getSimpleName();
    private ArrayList<ConversationableUser> mUsers = new ArrayList<NewConversationFragment.ConversationableUser>();
    private ArrayList<String> mPeopleList = new ArrayList<String>();
    private ListView mListView;
    private PeopleAdapter mAdapter;
    private boolean mFetchedAllFollowing = false;
    private boolean mFetchedAllFollowers = false;
    private EditText searchBar;
    private TextView statusField;
    private P1ActionBar mActionBar;

    private IContentRequester requesterFollowers = new IContentRequester() {
        @Override
        public void contentChanged(Content content) {
            addToList(content, true);
        }
    };
    private IContentRequester requesterFollowing = new IContentRequester() {
        @Override
        public void contentChanged(Content content) {
            addToList(content, false);
        }
    };

    public static NewConversationFragment newInstance() {
        return new NewConversationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // @Override
    // public Animation onCreateAnimation(int transit, boolean enter, int
    // nextAnim) {
    // final int animatorId = (enter) ? R.anim.drawer_open
    // : R.anim.drawer_close;
    // Animation anim = AnimationUtils
    // .loadAnimation(getActivity(), animatorId);
    // return anim;
    // }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = View.inflate(getActivity(),
                R.layout.new_conversation_fragment, null);

        searchBar = (EditText) view.findViewById(R.id.search_people_edittext);
        searchBar.addTextChangedListener(new AfterTextChangeWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mAdapter.getFilter().filter(s.toString());
            }
        });
        statusField = (TextView) view.findViewById(R.id.new_conv_status_search);
        mActionBar = (P1ActionBar) view.findViewById(R.id.new_conversation_ab);
        mActionBar.setLeftAction(new ListenerAction(
                R.drawable.modal_close_button, this));

        mListView = (ListView) view.findViewById(R.id.found_people_list);
        mListView.addFooterView(View.inflate(getActivity(),
                R.layout.ab_list_footer_filler, null));
        mListView.setEmptyView(view.findViewById(R.id.progressbar));
        if (mAdapter == null)
            mAdapter = new PeopleAdapter();
        mListView.setAdapter(mAdapter);

        if (getActivity() instanceof NavigationListener) {
            ((NavigationListener) getActivity()).showNavigationBar(false);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addToList(ReadFollow.requestFollowersList(
                NetworkUtilities.getLoggedInUserId(), requesterFollowers), true);
        addToList(ReadFollow.requestFollowingList(
                NetworkUtilities.getLoggedInUserId(), requesterFollowing),
                false);
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT);

    }

    @Override
    public void onDestroyView() {
        ContentHandler.getInstance().removeRequester(requesterFollowers);
        ContentHandler.getInstance().removeRequester(requesterFollowing);
        for (ConversationableUser user : mUsers) {
            ContentHandler.getInstance().removeRequester(user);
        }
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        if (getActivity() instanceof NavigationListener) {
            ((NavigationListener) getActivity()).showNavigationBar(true);
        }
        super.onDestroyView();
    }

    private void addToList(Content content, boolean isFollowers) {
        boolean hasMore = false;
        FollowListIOSession ioSession = ((FollowList) content).getIOSession();
        try {
            if (ioSession.getUserIdList() != null) {
                for (String userId : ioSession.getUserIdList()) {
                    if (!this.mPeopleList.contains(userId)) {
                        this.mPeopleList.add(userId);
                    }
                }
            }
            hasMore = ioSession.hasMore();
            if (isFollowers) {
                mFetchedAllFollowers = !hasMore;
            } else {
                mFetchedAllFollowing = !hasMore;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ioSession.close();
        }
        if (mFetchedAllFollowers && mFetchedAllFollowing) {
            ContentHandler.getInstance().removeRequester(requesterFollowers);
            ContentHandler.getInstance().removeRequester(requesterFollowing);
            fetchUserInformation();
        }
    }

    private void fetchUserInformation() {
        if (mPeopleList.isEmpty()) {
            statusField
                    .setText("You don't have any followers or follow anyone.");
            statusField.setVisibility(View.VISIBLE);
        }

        for (String userId : mPeopleList) {
            mUsers.add(new ConversationableUser(userId));
        }
    }

    private void checkIfListIsFull() {
        boolean allHaveNames = true;
        for (ConversationableUser user : mUsers) {
            if (user.mCompareKey == null || user.mCompareKey.isEmpty()) {
                allHaveNames = false;
                break;
            }
        }
        if (allHaveNames) {
            Collections.sort(mUsers);
            mAdapter.getFilter().filter(searchBar.getText().toString());
        }
    }

    private class ConversationableUser implements IContentRequester,
            Comparable<ConversationableUser> {
        private String mUserId;
        private String mName;
        private String mUserSearchKey;
        private String mCompareKey;
        private String mImgUrl;
        private ImageView mImgView;

        public ConversationableUser(String userId) {
            this.mUserId = userId;
            contentChanged(ReadUser.requestUser(userId, this));
        }

        @SuppressLint("DefaultLocale")
        @Override
        public int compareTo(ConversationableUser rhs) {
            return mCompareKey.toLowerCase().compareTo(
                    rhs.mCompareKey.toLowerCase());
        }

        @Override
        public void contentChanged(Content content) {
            UserIOSession io = ((com.p1.mobile.p1android.content.User) content)
                    .getIOSession();
            try {
                mName = io.getPreferredFullName();
                if (mName != null && !mName.isEmpty()) {
                    mUserSearchKey = mName;
                    mCompareKey = mName;
                    if (ChineseRegexUtil.isContainsChinese(mName)) {
                        String pinyin = Chinese2PinyinUtil
                                .converterToSpell(mName);
                        mCompareKey = pinyin;
                        mUserSearchKey = mUserSearchKey + "|" + pinyin;
                    }
                }
                checkIfListIsFull();
                mImgUrl = io.getProfileThumb100Url();
                if (mImgView != null && mImgUrl != null)
                    P1Application.picasso.load(Uri.parse(mImgUrl))
                            .noFade().placeholder(null).into(mImgView);
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            } finally {
                io.close();
            }
        }

        public void setImageView(ImageView imgView) {
            this.mImgView = imgView;
            if (mImgUrl != null)
                P1Application.picasso.load(Uri.parse(mImgUrl))
                        .placeholder(null).noFade().into(imgView);
        }

        @Override
        public String toString() {
            return mUserId + ", " + mCompareKey;
        }
    }

    private class PeopleAdapter extends BaseAdapter implements Filterable {
        private ArrayList<ConversationableUser> mPeople = new ArrayList<NewConversationFragment.ConversationableUser>();

        @Override
        public int getCount() {
            return mPeople.size();
        }

        @Override
        public Object getItem(int position) {
            return mPeople.get(position);
        }

        @Override
        public long getItemId(int position) {
            return Long.parseLong(mPeople.get(position).mUserId);
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater li = LayoutInflater.from(parent.getContext());
            View view;
            final ConversationableUser user = mPeople.get(position);
            if (convertView == null) {
                view = li.inflate(R.layout.follow_list_item, parent, false);
                view.findViewById(R.id.follow_button).setVisibility(
                        View.INVISIBLE);
            } else {
                view = convertView;
            }
            ((TextView) view.findViewById(R.id.follow_list_text))
                    .setText(user.mName);
            user.setImageView((ImageView) view
                    .findViewById(R.id.follow_list_picture));
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.startConversationActivity(getActivity(),
                            user.mUserId, false);
                    onAction();
                }
            });
            return view;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public Filter getFilter() {
            return new Filter() {
                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint,
                        FilterResults results) {
                    if (results.count == 0) {
                        if (!mUsers.isEmpty()) {
                            statusField.setText(getResources().getString(
                                    R.string.following_no_search_results));
                            statusField.setVisibility(View.VISIBLE);
                        }
                        notifyDataSetInvalidated();
                    } else {
                        statusField.setVisibility(View.GONE);
                        mPeople = (ArrayList<ConversationableUser>) results.values;
                        Collections.sort(mPeople);
                        notifyDataSetChanged();
                    }
                }

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (constraint == null || constraint.length() == 0) {
                        results.values = mUsers;
                        results.count = mUsers.size();
                    } else {
                        List<ConversationableUser> filteredUsers = new ArrayList<ConversationableUser>();
                        String key = constraint.toString().toLowerCase(
                                Locale.getDefault());
                        for (ConversationableUser u : mUsers) {
                            if (u.mUserSearchKey.toLowerCase(
                                    Locale.getDefault()).contains(key))
                                filteredUsers.add(u);
                        }
                        results.values = filteredUsers;
                        results.count = filteredUsers.size();
                    }
                    return results;
                }
            };
        }
    }

    @Override
    public void onAction() {
        getActivity().onBackPressed();
    }
}
