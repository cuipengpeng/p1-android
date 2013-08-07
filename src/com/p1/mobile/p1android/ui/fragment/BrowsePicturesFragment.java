package com.p1.mobile.p1android.ui.fragment;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.Options;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.p1.mobile.p1android.Actions;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.BrowseFilter;
import com.p1.mobile.p1android.content.BrowseList;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.Content.ContentIOSession;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Picture;
import com.p1.mobile.p1android.content.Picture.PictureIOSession;
import com.p1.mobile.p1android.content.logic.ReadBrowse;
import com.p1.mobile.p1android.content.logic.ReadPicture;
import com.p1.mobile.p1android.ui.adapters.BrowsePictureGridAdapter;
import com.p1.mobile.p1android.ui.view.ViewUtils;
import com.p1.mobile.p1android.ui.view.ViewUtils.AutoHideListener;

public class BrowsePicturesFragment extends Fragment implements
        IContentRequester, OnItemClickListener,
        OnSharedPreferenceChangeListener, OnRefreshListener {

    private static final String TAG = BrowsePicturesFragment.class
            .getSimpleName();

    private static final boolean HAS_MENU = true;

    private GridView mPictureGridView;
    private BrowseList mPictureBrowseList;
    private BrowsePictureGridAdapter mPictureAdapter;

    private PullToRefreshAttacher mPullToRefreshAttacher;
    private boolean requestRefresh = false;
    private BrowseFilter mFilter;

    private View mProgressBar;
    private View mErrorMessage;

    private AutoHideListener mHideListener;

    public static Fragment newInstance() {
        BrowsePicturesFragment fragment = new BrowsePicturesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(HAS_MENU);

    }

    public void retryFetchingInformation() {
        if (mPictureBrowseList != null && mFilter != null) {
            ContentIOSession io = mPictureBrowseList.getIOSession();
            try{
                if (!io.isValid()) {
                    ReadBrowse.fillBrowseList(mPictureBrowseList, mFilter);
                }
            } finally {
                io.close();
            }
        } else {
            Log.d(TAG, "Either mPictureBrowseList or mFilter is null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.browse_picture_fragment_layout,
                container, false);

        mPictureGridView = (GridView) view
                .findViewById(R.id.browse_picture_container);
        mPictureGridView.setOnItemClickListener(this);

        // conflict between swipe refresh listener and auto hide listener
        mHideListener = ViewUtils.autoHide(
                getActivity().findViewById(R.id.browse_filter),
                mPictureGridView, -getActivity().getResources()
                        .getDimensionPixelSize(R.dimen.actionbar_height));
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
        mPullToRefreshAttacher.setRefreshableView(mPictureGridView,
                BrowsePicturesFragment.this);
    }

    @Override
    public void onDestroyView() {
        mPictureAdapter.destroy();
//        mPullToRefreshAttacher.setRefreshComplete();
        ContentHandler.getInstance().removeRequester(this); // No longer request

        BrowseFragment.unregisterFilterLisener(getActivity(), this);
        super.onDestroyView();
    }

    public void resetData(BrowseFilter browseFilter) {
        if (!browseFilter.equals(mFilter) || mPictureBrowseList == null) {
            mPictureBrowseList = ReadBrowse.requestBrowsePicturesList(
                    BrowsePicturesFragment.this, browseFilter);
            mPictureAdapter = new BrowsePictureGridAdapter(mPictureBrowseList,
                    browseFilter, getActivity(), this);

            mPictureGridView.setSelection(0);
        }
        mFilter = browseFilter;

        mProgressBar.setVisibility(View.VISIBLE);

        mPictureGridView.setAdapter(mPictureAdapter);
        contentChanged(mPictureBrowseList);
    }

    long rememberedLastUpdate = 0;

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

                mPictureAdapter.notifyDataSetChanged();
                mPictureGridView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        } finally {
            io.close();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        String picID = (String) mPictureAdapter.getItem(position);
        if (picID != null) {
            Picture pic = ReadPicture.requestPicture(picID, null);

            PictureIOSession io = pic.getIOSession();
            String ownerId;
            try {
                ownerId = io.getOwnerId();
            } finally {
                io.close();
            }

            startPictureView(ownerId, picID, view);

        }

    }

    private void startPictureView(String ownerId, String picID, View view) {
        Intent intent = new Intent(Actions.USER_PICTURES);
        intent.putExtra("userId", ownerId);
        intent.putExtra("pictureId", picID);
        getActivity().startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        BrowseFilter newBrowseFilter = BrowseFragment.getBrowseFilter(pref);
        ContentIOSession io = mPictureBrowseList.getIOSession();
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
            ReadBrowse.requestRefreshedPictures(this, mFilter);
            requestRefresh = true;
        }
    }
}
