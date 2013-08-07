/**
 * FriendsListFragment.java
 *
 * Copyright (c) 2012 Yay Media Lab Ltd. All rights reserved.
 *
 */
package com.p1.mobile.p1android.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.Conversation;
import com.p1.mobile.p1android.content.Conversation.ConversationIOSession;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.User;
import com.p1.mobile.p1android.content.User.UserIOSession;
import com.p1.mobile.p1android.content.background.BackgroundNetworkService;
import com.p1.mobile.p1android.content.logic.ReadConversation;
import com.p1.mobile.p1android.content.logic.ReadUser;
import com.p1.mobile.p1android.content.logic.WriteConversation;
import com.p1.mobile.p1android.content.logic.WriteMessage;
import com.p1.mobile.p1android.ui.adapters.BrowseMessagesListAdapter;
import com.p1.mobile.p1android.ui.adapters.BrowseMessagesListAdapter.ChatViewHolder;
import com.p1.mobile.p1android.ui.listeners.NavigationListener;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.ListenerAction;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.OnActionListener;
import com.p1.mobile.p1android.ui.widget.P1TextView;
import com.p1.mobile.p1android.ui.widget.UserPictureView;
import com.p1.mobile.p1android.util.Utils;

/**
 * @author Viktor Nyblom
 * 
 */
public class BrowseMessagesFragment extends ListFragment implements
        OnActionListener {
    private static final String TAG = BrowseMessagesFragment.class
            .getSimpleName();
    private static final String LIST_STATE_KEY = "list_state";
    private static final int CONTEXT_MENU_COPY = 0;
    private static final int CONTEXT_MENU_RESEND = 1;
    private static final int CONTEXT_MENU_DELETE = 2;

    private Parcelable mListState = null;
    private BrowseMessagesListAdapter mAdapter;
    private ListView mListView;
    private View mProgressBar;
    private View mErrorMessage;

    private P1ActionBar mActionBar;
    public static String CONVERSATION_ID = "conversationid";
    public static String ISFROMPROFILE = "fromporile";
    private String mConversationId;
    int currentFirstVisibleItem;
    private boolean mIsFromUserProfile = false;

    /** Top search for friends, find following/followers action bar */

    private ConversationRequester mConversationRequester = new ConversationRequester();
    private UserRequester mUserRequester = new UserRequester();
    private List<IContentRequester> mActiveIContentRequesters = new ArrayList<IContentRequester>();
    private View statusHeaderLayout;
    protected int position = -1;
    protected int top;
    protected int index;
    protected String messageId_LastVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConversationId = (getArguments() != null ? getArguments().getString(
                CONVERSATION_ID) : "");
        mIsFromUserProfile = (getArguments() != null ? getArguments()
                .getBoolean(ISFROMPROFILE) : false);
    }

    public static Fragment newInstance(String conversationId,
            boolean isFromProfile) {
        BrowseMessagesFragment fragment = new BrowseMessagesFragment();
        Bundle args = new Bundle();
        args.putString(CONVERSATION_ID, conversationId);
        args.putBoolean(ISFROMPROFILE, isFromProfile);
        fragment.setArguments(args);
        return fragment;
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
        View view = inflater.inflate(R.layout.message_list_layout, container,
                false);
        initActionBar(inflater, view);
        initSendbar(view);
        mProgressBar = view.findViewById(R.id.progressbar);
        mErrorMessage = view.findViewById(R.id.error_message);
        mErrorMessage.setVisibility(View.GONE);
        mActiveIContentRequesters.add(mConversationRequester);
        mActiveIContentRequesters.add(mUserRequester);
        return view;
    }

    private void initSendbar(View view) {
        final TextView sendMessage = (TextView) view
                .findViewById(R.id.sendbar_msg_txt);
        final Button sendButton = (Button) view
                .findViewById(R.id.sendbar_button);
        sendMessage.addTextChangedListener(new TextWatcher() {
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
                sendButton.setEnabled(s.length() == 0 ? false : true);
            }
        });
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(sendMessage.getText().toString());
                sendMessage.setText("");
            }
        });

        sendMessage.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage(v.getText().toString());
                    sendMessage.setText("");
                    return true;
                }
                return false;
            }
        });
        sendMessage.setText("");
    }

    private void sendMessage(String text) {
        String targetUserId = mConversationRequester.targetUserId;
        if (targetUserId != null)
            WriteMessage.sendMessage(text, targetUserId);
    }

    public void initActionBar(LayoutInflater inflater, View containerView) {
        mActionBar = (P1ActionBar) containerView.findViewById(R.id.actionBar);
        if (mIsFromUserProfile) {
            mActionBar.setLeftAction(new ListenerAction(
                    R.drawable.browse_filter_close_button, this));
        } else {
            mActionBar.setLeftAction(new ListenerAction(
                    R.drawable.back_arrow_button, this));
        }

        // TODO set profile picture and notifications counter
        if (getActivity() instanceof NavigationListener) {
            UserPictureView picView = (UserPictureView) inflater.inflate(
                    R.layout.user_picture_view, null);
            picView.setAction(new P1ActionBar.ShowNotificationsAction(
                    R.drawable.ic_about_white,
                    ((NavigationListener) getActivity())));
            mActionBar.setRightView(picView);
        } else {
            Log.e(TAG,
                    "Activity of BrowseFragment is not a NavigationListener. BrowseFragment is probably placed in a bad activity");
        }
    }

    /** Set current user profile image */
    public void setActionBarTile(User user) {

        P1TextView titleView = new P1TextView(getActivity());
        UserIOSession io = user.getIOSession();
        try {
            titleView.setText(io.getEnUsFullname());
        } finally {
            io.close();
        }

        titleView.setTextAppearance(getActivity(), R.style.P1LargerTextLight);
        titleView.setGravity(Gravity.CENTER);
        mActionBar.setCenterView(titleView);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView = getListView();
        mListView.setHeaderDividersEnabled(false);
        mListView.setDividerHeight(0);
        mListView.setSelectionAfterHeaderView();
        LayoutInflater li = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mListView.addHeaderView(li.inflate(R.layout.empty_list_header, null));

        statusHeaderLayout = View.inflate(getActivity(),
                R.layout.status_footer, null);
        mListView.addHeaderView(statusHeaderLayout);
        statusHeaderLayout.setVisibility(View.INVISIBLE);

        mListView.addFooterView(li.inflate(R.layout.empty_list_header, null));

        registerForContextMenu(mListView);

        if (mConversationId == null) {
            throw new NullPointerException(
                    "mConversationId is null. This results in that no Conversation can be fetched using it");
        }
        final Conversation conv = ReadConversation.requestConversation(
                mConversationId, mConversationRequester); // Content

        ReadConversation.fillConversation(conv);

        mAdapter = new BrowseMessagesListAdapter(conv, getActivity(),
                mConversationRequester);
        setListAdapter(mAdapter);
        mConversationRequester.contentChanged(conv);

        if (savedInstanceState != null) {
            mListState = savedInstanceState.getBundle(LIST_STATE_KEY);
        }

        mListView.setOnScrollListener(new OnScrollListener() {

            private int currentScrollState;

            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                currentFirstVisibleItem = firstVisibleItem;
            }

            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.currentScrollState = scrollState;
                this.isScrollCompleted();
            }

            private void isScrollCompleted() {
                if (this.currentScrollState == SCROLL_STATE_IDLE
                        && currentFirstVisibleItem == 0) {

                    try {

                        // save index and top position
                        int count = mListView.getChildCount()
                                - mListView.getHeaderViewsCount()
                                - mListView.getFooterViewsCount();
                        index = mListView.getFirstVisiblePosition() + count;
                        messageId_LastVisible = mAdapter.getMessageId(index);
                        // DONT DELETE
                        // for (int id = mListView.getFirstVisiblePosition(); id
                        // <
                        // mListView
                        // .getLastVisiblePosition(); id++) {
                        //
                        // messageVisible = mAdapter.getMessageId(id);
                        // if (messageVisible != null) {
                        // for (int i = 0; i < mListView.getChildCount();
                        // i++) {
                        // if (mListView.getChildAt(i).getTag() instanceof
                        // ChatViewHolder) {
                        // if (messageVisible.equals(((ChatViewHolder)
                        // mListView.getChildAt(i).getTag()).mMessageId)) {
                        // top = mListView.getChildAt(i).getTop();
                        // break;
                        // }
                        // }
                        // }
                        // break;
                        // }
                        // }
                        View v = mListView.getChildAt(index);
                        top = (v == null) ? 0 : v.getTop();
                    } catch (Exception e) {
                    }
                    ReadConversation.fillConversation(conv);
                }
            }

        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Intent intent = new Intent(P1Application.getP1ApplicationContext(),
                BackgroundNetworkService.class);
        intent.putExtra(BackgroundNetworkService.START_CODE,
                BackgroundNetworkService.CODE_LEAVE_MESSAGES);
        P1Application.getP1ApplicationContext().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(P1Application.getP1ApplicationContext(),
                BackgroundNetworkService.class);
        intent.putExtra(BackgroundNetworkService.START_CODE,
                BackgroundNetworkService.CODE_ENTER_MESSAGES);
        P1Application.getP1ApplicationContext().startService(intent);
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

    public class ConversationRequester implements IContentRequester {

        public String targetUserId;

        @Override
        public void contentChanged(Content content) {
            if (content != null) {
                mProgressBar.setVisibility(View.GONE);
                Conversation conv = (Conversation) content;
                ConversationIOSession io = conv.getIOSession();
                try {
                    if (!io.isValid())
                        return; // Wait for a return with valid information

                    WriteConversation.markAsRead(conv);
                    targetUserId = io.getOtherUserId();
                    if (targetUserId == null) {
                        targetUserId = io.getId();
                    }
                    mUserRequester.contentChanged(ReadUser.requestUser(
                            targetUserId, mUserRequester));

                    statusHeaderLayout
                            .setVisibility(!io.hasMore() ? View.INVISIBLE
                                    : View.VISIBLE);

                } finally {
                    io.close();
                }
            }
            if (mAdapter != null) {
                if (messageId_LastVisible != null) {
                    mAdapter.notifyDataSetChanged();
                    int newpos = mAdapter
                            .getMessagePosition(messageId_LastVisible);
                    mListView.setSelectionFromTop(newpos, top);
                } else
                    mAdapter.notifyDataSetChanged();
            }
        }
    }

    public class UserRequester implements IContentRequester {

        @Override
        public void contentChanged(Content content) {
            setActionBarTile((User) content);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (v == mListView
                && info.targetView.getTag() instanceof ChatViewHolder) {
            ChatViewHolder viewHolder = (ChatViewHolder) info.targetView
                    .getTag();
            // menu.setHeaderTitle("title");
            menu.add(Menu.NONE, CONTEXT_MENU_COPY, Menu.NONE, "Copy");

            /**
             * TODO , not in this release
             */
            // if (viewHolder.mIsOwner) {
            // if (viewHolder.mError == true) {
            // menu.add(Menu.NONE, CONTEXT_MENU_RESEND, Menu.NONE,
            // "Send Again");
            // }
            // menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, "Delete");
            // }
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        ChatViewHolder viewHolder = (ChatViewHolder) info.targetView.getTag();
        switch (item.getItemId()) {
        case CONTEXT_MENU_COPY:
            Utils.copyToClipBoard(getActivity(),
                    viewHolder.mMessageOverViewTextView.getText().toString());
            break;
        case CONTEXT_MENU_RESEND:
            /**
             * TODO , not in this release
             */
            // Toast.makeText(getActivity(),
            // "Resend messageID:" + viewHolder.mMessageId,
            // Toast.LENGTH_SHORT).show();
            break;
        case CONTEXT_MENU_DELETE:
            /**
             * TODO , not in this release
             */
            // Toast.makeText(getActivity(),
            // "Delete messageID:" + viewHolder.mMessageId,
            // Toast.LENGTH_SHORT).show();
            break;
        default:
            break;

        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onAction() {
        getActivity().finish();
    }

}
