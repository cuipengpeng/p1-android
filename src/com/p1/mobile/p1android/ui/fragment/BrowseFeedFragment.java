package com.p1.mobile.p1android.ui.fragment;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.Options;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.p1.mobile.p1android.Actions;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.Feed.FeedIOSession;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.IContentRequester.IChildContentRequester;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;
import com.p1.mobile.p1android.content.logic.ReadFeed;
import com.p1.mobile.p1android.ui.helpers.FeedItem;
import com.p1.mobile.p1android.ui.helpers.NotificationsCounterUpdater;
import com.p1.mobile.p1android.ui.listeners.NavigationListener;
import com.p1.mobile.p1android.ui.widget.CounterBubble;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1TextView;
import com.p1.mobile.p1android.ui.widget.UserPictureView;
import com.p1.mobile.p1android.util.Utils;

public class BrowseFeedFragment extends Fragment implements
        PullToRefreshAttacher.OnRefreshListener {
    public static final String TAG = BrowseFeedFragment.class.getSimpleName();

    private P1ActionBar mActionBar;
    private ListView listView;
    private FeedAdapter feedAdapter;
    private int lastFetchSize;
    private TextView statusFooter;
    private View headerFiller;
    private ViewGroup rootView;
    private View progressBar;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private boolean requestRefresh = false;
    private UserPictureView picView;

    public static BrowseFeedFragment newInstance() {
        BrowseFeedFragment browseFeedFragment = new BrowseFeedFragment();
        return browseFeedFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = (ViewGroup) View.inflate(getActivity(),
                R.layout.feed_fragment, null);
        mActionBar = (P1ActionBar) rootView.findViewById(R.id.actionBar);
        listView = (ListView) rootView.findViewById(R.id.feedListView);
        progressBar = rootView.findViewById(R.id.progressbar);
        listView.setEmptyView(progressBar);
        View footer = View.inflate(getActivity(), R.layout.status_footer, null);
        statusFooter = (TextView) footer.findViewById(R.id.status_footer_tv);
        listView.addFooterView(footer);
        listView.addFooterView(View.inflate(getActivity(),
                R.layout.ab_list_footer_filler, null));
        headerFiller = View.inflate(getActivity(),
                R.layout.ab_list_footer_filler, null);
        listView.addHeaderView(headerFiller);
        listView.setDividerHeight(0);
        initActionBar(inflater, rootView);
        attachRefresh();

        return rootView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            attachRefresh();
            retryFetchingInformation();
        }
    }

    public void retryFetchingInformation() {
        ReadFeed.requestFeed(null);
    }

    private void attachRefresh() {
        mActionBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isResumed())
                    return;
                Options options = new Options();
                options.refreshScrollDistance = 0.3f;
                mPullToRefreshAttacher = new PullToRefreshAttacher(
                        getActivity(), options);
                mPullToRefreshAttacher.setRefreshableView(listView,
                        BrowseFeedFragment.this);
            }
        }, 0);
    }

    @Override
    public void onRefreshStarted(View view) {
        requestRefresh = true;
        ReadFeed.requestRefreshedFeed(feedAdapter);
    }

    @SuppressLint("NewApi")
    private void setScrollFriction() {
        if (Utils.hasHoneycomb()) {
            listView.setFriction(ViewConfiguration.getScrollFriction() * 4);
        }
    }

    private void initActionBar(LayoutInflater inflater, View containerView) {
        mActionBar = (P1ActionBar) containerView.findViewById(R.id.actionBar);
        P1TextView titleView = new P1TextView(getActivity());
        titleView.setText(getActivity().getResources().getString(
                R.string.feed_fragment_title));
        titleView.setTextAppearance(getActivity(), R.style.P1LargerTextLight);
        titleView.setGravity(Gravity.CENTER);

        mActionBar.setCenterView(titleView);

        if (getActivity() instanceof NavigationListener) {
            picView = (UserPictureView) inflater.inflate(
                    R.layout.user_picture_view, null);
            picView.setAction(new P1ActionBar.ShowNotificationsAction(
                    R.drawable.ic_about_white,
                    ((NavigationListener) getActivity())));
            picView.setNotificationsView(new CounterBubble(getActivity(),
                    new NotificationsCounterUpdater()));
            mActionBar.setRightView(picView);
        } else {
            Log.e(TAG,
                    "Activity of BrowseFragment is not a NavigationListener. BrowseFragment is probably placed in a bad activity");
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (feedAdapter == null) {
            feedAdapter = new FeedAdapter();
            feedAdapter.contentChanged(ReadFeed.requestFeed(feedAdapter));
        }
        listView.setAdapter(feedAdapter);
        listView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                if (totalItemCount - 3 < view.getLastVisiblePosition()
                        && feedAdapter != null
                        && lastFetchSize != totalItemCount) {
                    lastFetchSize = totalItemCount;
                    listView.post(new Runnable() {
                        @Override
                        public void run() {
                            feedAdapter.contentChanged(ReadFeed
                                    .requestFeed(feedAdapter));
                        }
                    });
                }
            }

        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPullToRefreshAttacher != null) {
            mPullToRefreshAttacher.setRefreshComplete();
        }
    }

    @Override
    public void onDestroyView() {
        ContentHandler.getInstance().removeRequester(feedAdapter);
        super.onDestroyView();
    }

    public static final int[] FEED_LAYOUT_RES_IDS = { R.layout.feed_item_1,
            R.layout.feed_item_2, R.layout.feed_item_3, R.layout.feed_item_4,
            R.layout.feed_item_5, R.layout.feed_item_6, R.layout.feed_item_7,
            R.layout.feed_item_8, R.layout.feed_item_9 };

    private class FeedAdapter extends BaseAdapter implements IContentRequester,
            IChildContentRequester {
        private ArrayList<Share> shares = new ArrayList<Share>();
        private ArrayList<FeedItem> feedItems = new ArrayList<FeedItem>();

        @Override
        public int getCount() {
            return shares.size();
        }

        @Override
        public int getViewTypeCount() {
            return FEED_LAYOUT_RES_IDS.length;
        }

        @Override
        public int getItemViewType(int position) {
            ShareIOSession io = shares.get(position).getIOSession();
            try {
                return io.getPictureIds().size() - 1;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                io.close();
            }
            return -1;
        }

        @Override
        public Object getItem(int position) {
            return shares.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Share share = (Share) getItem(position);
            final View view;
            LayoutInflater il = LayoutInflater.from(parent.getContext());
            final FeedItem feedItem;
            if (convertView == null) {
                int type = getItemViewType(position);
                view = il.inflate(FEED_LAYOUT_RES_IDS[type], parent, false);
                feedItem = new FeedItem(getActivity(), false);
                view.setTag(feedItem);
                feedItems.add(feedItem);
                feedItem.setView(view);
            } else {
                view = convertView;
                feedItem = (FeedItem) view.getTag();
            }
            ContentHandler.getInstance().removeRequester(feedItem);
            feedItem.requestUpdatesForShare(share.getId());
            return view;
        }

        @Override
        public void contentChanged(Content content) {
            FeedIOSession io = ((FeedIOSession) content.getIOSession());
            try {
                if (io.isValid() && io.getShareIdList().isEmpty()) {
                    Log.w(TAG, "Found empty and valid feed");
                    View emptyFeedView = View.inflate(getActivity(),
                            R.layout.empty_feed_welcome_screen, null);
                    rootView.removeView(emptyFeedView);
                    rootView.addView(emptyFeedView);
                    listView.setEmptyView(emptyFeedView);
                    rootView.removeView(progressBar);
                    feedAdapter.notifyDataSetChanged();
                    emptyFeedView.findViewById(R.id.start_sharing_button).setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startCameraActivity();
                                }
                    });
                    return;
                }

                if (requestRefresh
                        || (io.getShareIdList() != null && io.getShareIdList()
                                .size() > shares.size())) {
                    requestRefresh = false;
                    if (mPullToRefreshAttacher != null) {
                        mPullToRefreshAttacher.setRefreshComplete();
                    } else {
                        Log.w(TAG, "mPullToRefreshAdapter is null");
                    }
                    if (io.hasFailedNetworkOperation()) {
                        Toast.makeText(
                                getActivity(),
                                getActivity()
                                        .getString(
                                                R.string.failed_to_load),
                                Toast.LENGTH_LONG).show();
                    }
                    shares.clear();
                    for (String shareId : io.getShareIdList()) {
                        Share share = ContentHandler.getInstance().getShare(
                                shareId, null);
                        // relay on share is actually valid when feed returns
                        if (share != null) {
                            shares.add(share);
                        } else {
                            throw new NullPointerException();
                        }
                    }
                    if (!io.hasMore()) {
                        LayoutParams layoutParams = statusFooter
                                .getLayoutParams();
                        layoutParams.height = 1;
                    }
                    feedAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                io.close();
            }
        }

        @Override
        public void removeChildRequestors() {
            for (FeedItem i : feedItems) {
                ContentHandler.getInstance().removeRequester(i);
            }
            feedItems.clear();
        }
    }

    private void startCameraActivity() {
        Intent intent = new Intent(Actions.CUSTOM_GALLERY);
        startActivity(intent);
    }
}
