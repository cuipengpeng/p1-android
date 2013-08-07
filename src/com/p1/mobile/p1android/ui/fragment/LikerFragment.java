package com.p1.mobile.p1android.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.ListView;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.Content.ContentIOSession;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;
import com.p1.mobile.p1android.content.Share.ShareIOSession;
import com.p1.mobile.p1android.content.logic.ReadPicture;
import com.p1.mobile.p1android.content.logic.ReadShare;
import com.p1.mobile.p1android.ui.adapters.LikerListAdapter;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.ListenerAction;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.OnActionListener;
import com.p1.mobile.p1android.ui.widget.P1TextView;

public class LikerFragment extends ListFragment implements IContentRequester,
        OnActionListener {

    private static final String TAG = LikerFragment.class.getSimpleName();
    private static final String LIST_STATE_KEY = "list_state";
    public static final String SHARE_ID = "share_id";
    public static final String CONTENT_TYPE = "type";
    private List<String> likeUserIds = new ArrayList<String>();

    // private Parcelable mListState = null;

    public P1ActionBar mActionBar;

    private ListView mListView;

    private View mProgressBar;
    private View mErrorMessage;

    private LikerListAdapter mAdapter;
    private View mListHeaderView;
    private P1TextView titleView;
    private String mContentId;

    public enum LikeContentType {
        PICTURE, SHARE
    };

    private List<IContentRequester> mActiveIContentRequesters = new ArrayList<IContentRequester>();
    private LikeContentType mContentType;

    public static LikerFragment newInstance(String shareId, String type) {
        LikerFragment fragment = new LikerFragment();
        Bundle args = new Bundle();
        args.putString(SHARE_ID, shareId);
        args.putString(CONTENT_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContentId = (getArguments() != null ? getArguments().getString(
                SHARE_ID) : "");
        mContentType = LikeContentType
                .valueOf((getArguments() != null ? getArguments().getString(
                        CONTENT_TYPE) : LikeContentType.SHARE.name()));

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // mListState = getListView().onSaveInstanceState();
        // outState.putParcelable(LIST_STATE_KEY, mListState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.likers_fragment_layout,
                container, false);
        initActionBar(inflater, view);
        mListHeaderView = new View(getActivity());
        mListHeaderView
                .setLayoutParams(new LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT,
                        (int) getActivity().getResources().getDimension(
                                R.dimen.actionbar_height)));

        mProgressBar = view.findViewById(R.id.progressbar);
        mErrorMessage = view.findViewById(R.id.error_message);
        mErrorMessage.setVisibility(View.GONE);
        return view;
    }

    public void initActionBar(LayoutInflater inflater, View containerView) {
        mActionBar = (P1ActionBar) containerView
                .findViewById(R.id.likelistActionBar);

        titleView = new P1TextView(getActivity());

        titleView.setTextAppearance(getActivity(), R.style.P1LargerTextLight);
        titleView.setGravity(Gravity.CENTER);
        mActionBar.setCenterView(titleView);
        mActionBar.setLeftAction(new ListenerAction(
                R.drawable.back_arrow_button, this));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView = getListView();

        mListView.addHeaderView(mListHeaderView);
        mListView.setHeaderDividersEnabled(false);

        mListView.addFooterView(View.inflate(getActivity(),
                R.layout.empty_list_header, null));
        mListView.setFooterDividersEnabled(false);

        mAdapter = new LikerListAdapter(getActivity(), likeUserIds);
        mListView.setAdapter(mAdapter);

        switch (mContentType) {
        case PICTURE:
            contentChanged(ReadPicture.requestPicture(mContentId, this));
            break;
        default:
            contentChanged(ReadShare.requestShare(mContentId, this));
            break;
        }

        mActiveIContentRequesters.add(this);
        // if (savedInstanceState != null) {
        // mListState = savedInstanceState.getBundle(LIST_STATE_KEY);
        // }
    }

    @Override
    public void onDestroyView() {

        for (IContentRequester requester : mActiveIContentRequesters) {
            ContentHandler.getInstance().removeRequester(requester);
        }
        mActiveIContentRequesters.clear();

        mAdapter.destroy();
        super.onDestroyView();
    }

    @Override
    public void onAction() {
        getActivity().finish();
    }

    @Override
    public void contentChanged(Content content) {
        if (content == null) {
            return;
        }

        ContentIOSession io = ((ContentIOSession) content.getIOSession());
        switch (mContentType) {
        case PICTURE:
            likeUserIds = ((PictureIOSession) io).getLikeUserIds();
            break;
        default:
            likeUserIds = ((ShareIOSession) io).getLikeUserIds();
            break;
        }

        try {
            if (!io.isValid()) {
                return;
            }

            titleView.setText(getString(R.string.liker_fragment_title,
                    likeUserIds.size()));
            mAdapter.updateList(likeUserIds);
            mProgressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            io.close();
        }
    }

}
