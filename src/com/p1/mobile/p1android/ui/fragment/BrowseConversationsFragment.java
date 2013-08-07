package com.p1.mobile.p1android.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.Content.ContentIOSession;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.ConversationList;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.background.BackgroundNetworkService;
import com.p1.mobile.p1android.content.logic.ReadConversation;
import com.p1.mobile.p1android.ui.adapters.BrowseConversationListAdapter;
import com.p1.mobile.p1android.ui.adapters.BrowseConversationListAdapter.ConversationViewHolder;
import com.p1.mobile.p1android.ui.helpers.NotificationsCounterUpdater;
import com.p1.mobile.p1android.ui.listeners.NavigationListener;
import com.p1.mobile.p1android.ui.phone.MainActivity;
import com.p1.mobile.p1android.ui.phone.NewConversationActivity;
import com.p1.mobile.p1android.ui.view.ViewUtils;
import com.p1.mobile.p1android.ui.widget.CounterBubble;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1TextView;
import com.p1.mobile.p1android.ui.widget.UserPictureView;
import com.p1.mobile.p1android.util.Utils;

public class BrowseConversationsFragment extends ListFragment implements
        IContentRequester {
    private static final String TAG = BrowseConversationsFragment.class
            .getSimpleName();
    private static final String LIST_STATE_KEY = "list_state";
    private static final int CONTEXT_MENU_DELETE = 0;
    private Parcelable mListState = null;
    private BrowseConversationListAdapter mAdapter;
    ConversationList mConversationList;
    private ListView mListView;
    private View mProgressBar;
    private View mErrorMessage;
    private P1ActionBar mActionBar;
    private OnClickListener mStartConversationListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(),
                    NewConversationActivity.class);
            getActivity().startActivity(intent);
        }
    };

    private List<IContentRequester> mActiveIContentRequesters = new ArrayList<IContentRequester>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mListState = getListView().onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, mListState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.conversation_list_layout,
                container, false);

        mListView = (ListView) view.findViewById(android.R.id.list);
        LayoutInflater li = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mListView.addHeaderView(li.inflate(
                R.layout.empty_list_header_with_auto_hide, null));
        mListView.addFooterView(li.inflate(R.layout.empty_list_header, null));

        initActionBar(inflater, view);

        mProgressBar = view.findViewById(R.id.progressbar);
        mErrorMessage = view.findViewById(R.id.error_message);
        mErrorMessage.setVisibility(View.GONE);
        View createView = view.findViewById(R.id.browse_conversation_create);
        createView.setOnClickListener(mStartConversationListener);
        ViewUtils.autoHide((View) createView.getParent(), (ListView) view
                .findViewById(android.R.id.list), -getActivity().getResources()
                .getDimensionPixelSize(R.dimen.actionbar_height));
        mActiveIContentRequesters.add(this);

        return view;
    }

    public void initActionBar(LayoutInflater inflater, View containerView) {
        mActionBar = (P1ActionBar) containerView.findViewById(R.id.actionBar);

        P1TextView titleView = new P1TextView(getActivity());
        titleView.setText(getActivity().getResources().getString(
                R.string.conversations_navigation_title));
        titleView.setTextAppearance(getActivity(), R.style.P1LargerTextLight);
        titleView.setGravity(Gravity.CENTER);

        mActionBar.setCenterView(titleView);

        if (getActivity() instanceof NavigationListener) {
            UserPictureView picView = (UserPictureView) inflater.inflate(
                    R.layout.user_picture_view, null);
            picView.setAction(new P1ActionBar.ShowNotificationsAction(
                    R.drawable.ic_about_white,
                    ((NavigationListener) getActivity())));
            picView.setNotificationsView(new CounterBubble(getActivity(),
                    new NotificationsCounterUpdater()));
            mActionBar.setRightView(picView);
        } else {
            Log.e(TAG, "Activity " + getActivity().getClass().getName()
                    + " doesnot support Navigationbar");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO , not in this release
        // registerForContextMenu(mListView);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if (getActivity() instanceof MainActivity) {
                    Utils.startConversationActivity(getActivity(),
                            (String) mAdapter.getItem((int) id), false);
                }
            }
        });

        mConversationList = ReadConversation.requestConversationList(this); // Content
                                                                                // request
        mActiveIContentRequesters.add(this);
        mAdapter = new BrowseConversationListAdapter(mConversationList,
                getActivity(), this);
        setListAdapter(mAdapter);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }

        mListView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                if ((totalItemCount - (firstVisibleItem + visibleItemCount)) <= 1) {
                    mListState = getListView().onSaveInstanceState();
                }
            }
        });
        contentChanged(mConversationList);
    }

    @Override
    public void onDestroyView() {
        mAdapter.destroy();

        for (IContentRequester requester : mActiveIContentRequesters) {
            ContentHandler.getInstance().removeRequester(requester);
        }
        mActiveIContentRequesters.clear();
        super.onDestroyView();
    }

    @Override
    public void contentChanged(Content content) {
        Log.d(TAG, "contentChanged");
        ContentIOSession io = content.getIOSession();
        try {
            if (io.isValid()) {
                mAdapter.notifyDataSetChanged();
                mProgressBar.setVisibility(View.GONE);
            }
        } finally {
            io.close();
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        /**
         * TODO , not in this release
         */

        // AdapterView.AdapterContextMenuInfo info =
        // (AdapterView.AdapterContextMenuInfo) menuInfo;
        // if (v == mListView
        // && info.targetView.getTag() instanceof ConversationViewHolder) {
        // menu.setHeaderTitle("Delete Conversation");
        // menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, "Delete");
        // }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        ConversationViewHolder viewHolder = (ConversationViewHolder) info.targetView
                .getTag();
        switch (item.getItemId()) {
        case CONTEXT_MENU_DELETE:
            String convId = viewHolder.mConversationId;
            if (convId != null) {
                // Toast.makeText(getActivity(), "Delete ConvId:" + convId,
                // Toast.LENGTH_SHORT).show();

            }
            break;
        default:
            break;

        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        Intent intent = new Intent(P1Application.getP1ApplicationContext(),
                BackgroundNetworkService.class);
        intent.putExtra(BackgroundNetworkService.START_CODE,
                BackgroundNetworkService.CODE_LEAVE_CONVERSATIONS);
        P1Application.getP1ApplicationContext().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(P1Application.getP1ApplicationContext(),
                BackgroundNetworkService.class);
        intent.putExtra(BackgroundNetworkService.START_CODE,
                BackgroundNetworkService.CODE_ENTER_CONVERSATIONS);
        P1Application.getP1ApplicationContext().startService(intent);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            retryFetchingInformation();
        }
    }

    public void retryFetchingInformation() {
        if (mConversationList != null) {
            ContentIOSession io = mConversationList.getIOSession();
            try {
                if (!io.isValid()) {
                    ReadConversation.fillConversationList();
                }
            } finally {
                io.close();
            }
        }
    }
}
