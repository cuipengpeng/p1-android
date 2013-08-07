package com.p1.mobile.p1android.ui.fragment;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.Options;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.BrowseFilter;
import com.p1.mobile.p1android.content.BrowseList;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.Content.ContentIOSession;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.logic.ReadBrowse;
import com.p1.mobile.p1android.ui.adapters.BrowseMemberListAdapter;
import com.p1.mobile.p1android.ui.view.ViewUtils;
import com.p1.mobile.p1android.ui.view.ViewUtils.AutoHideListener;

public class BrowseMembersFragment extends Fragment implements
        IContentRequester, OnSharedPreferenceChangeListener, OnRefreshListener {
    private static final String TAG = BrowseMembersFragment.class
            .getSimpleName();

    private static final boolean HAS_MENU = true;

    private ListView mMemberListView;
    private BrowseMemberListAdapter mMemberListAdapter;
    private BrowseList mMemberlist;

    private View mProgressBar;
    private View mErrorMessage;

    private boolean mIsLoadingData = false;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private boolean requestRefresh = false;
    private BrowseFilter mFilter;

    private AutoHideListener mHideListener;

    public BrowseMembersFragment() {
        super();
    }

    public static Fragment newInstance() {
        BrowseMembersFragment fragment = new BrowseMembersFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(HAS_MENU);
    }

    public void retryFetchingInformation() {
        if (mMemberlist != null && mFilter != null) {
            ContentIOSession io = mMemberlist.getIOSession();
            try {
                if (!io.isValid()) {
                    ReadBrowse.fillBrowseList(mMemberlist, mFilter);
                }
            } finally {
                io.close();
            }
        } else {
            Log.d(TAG, "Either mMemberlist or mFilter is null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.browse_member_fragment_layout,
                container, false);
        mMemberListView = (ListView) view
                .findViewById(R.id.browse_member_container);

        mHideListener = ViewUtils.autoHide(
                getActivity().findViewById(R.id.browse_filter),
                mMemberListView, -getActivity().getResources()
                        .getDimensionPixelSize(R.dimen.actionbar_height));
        mMemberListView.addHeaderView(inflater.inflate(
                R.layout.empty_list_header_with_auto_hide_padding, null));
        mMemberListView.setHeaderDividersEnabled(false);

        mMemberListView.addFooterView(View.inflate(getActivity(),
                R.layout.empty_list_header, null));
        mMemberListView.setFooterDividersEnabled(false);

        mProgressBar = view.findViewById(R.id.progressbar);

        mErrorMessage = view.findViewById(R.id.error_message);
        mErrorMessage.setVisibility(View.GONE);

        resetData(BrowseFragment.registerFilterLisener(getActivity(), this));

        attachRefresh();

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            attachRefresh();
            mHideListener.syncAndShow(true);
            retryFetchingInformation();
        }
    }
    
    private void attachRefresh() {
        Options options = new Options();
        options.refreshScrollDistance = 0.3f;
        mPullToRefreshAttacher = new PullToRefreshAttacher(
                getActivity(), options);
        mPullToRefreshAttacher.setRefreshableView(mMemberListView,
                BrowseMembersFragment.this);
    }

    @Override
    public void onDestroyView() {
        mMemberListAdapter.destroy();

        mPullToRefreshAttacher.setRefreshComplete();
        BrowseFragment.unregisterFilterLisener(getActivity(), this);
        ContentHandler.getInstance().removeRequester(this);

        super.onDestroyView();
    }

    public void resetData(BrowseFilter browseFilter) {
        if (!browseFilter.equals(mFilter) ||  mMemberlist == null) {
            mMemberlist = ReadBrowse.requestBrowseMembersList(
                    BrowseMembersFragment.this, browseFilter);
            mMemberListAdapter = new BrowseMemberListAdapter(mMemberlist,
                    browseFilter, getActivity(), this);
            mMemberListView.setSelection(0);
        }
        mFilter = browseFilter;
        mProgressBar.setVisibility(View.VISIBLE);
        mMemberListView.setAdapter(mMemberListAdapter);
        contentChanged(mMemberlist);
    }

    @Override
    public void contentChanged(Content content) {
        Log.d(TAG, "contentChanged");
        ContentIOSession io = content.getIOSession();
        requestRefresh = false;
        if (mPullToRefreshAttacher != null)
            mPullToRefreshAttacher.setRefreshComplete();
        try {
            if (io.hasFailedNetworkOperation()) {
                Toast.makeText(
                        getActivity(),
                        getActivity()
                                .getString(
                                        R.string.failed_to_load),
                        Toast.LENGTH_LONG).show();
            }
            if (io.isValid()) {
                mProgressBar.setVisibility(View.GONE);
                mMemberListView.setVisibility(View.VISIBLE);
                mMemberListAdapter.notifyDataSetChanged();
            }
        } finally {
            io.close();
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        BrowseFilter newBrowseFilter = BrowseFragment.getBrowseFilter(pref);
        ContentIOSession io = mMemberlist.getIOSession();
        try {
            if (io.isValid()) {
                io.setValid(false);
            }
        } finally {
            io.close();
        }
        resetData(newBrowseFilter);
    }

    @Override
    public void onRefreshStarted(View view) {
        if (!requestRefresh) {
            ReadBrowse.requestRefreshedMembers(this, mFilter);
            requestRefresh = true;
        }
    }
}
