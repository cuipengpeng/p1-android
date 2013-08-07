package com.p1.mobile.p1android.ui.fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import com.p1.mobile.p1android.Actions;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.logic.WritePicture;
import com.p1.mobile.p1android.io.model.MediaStoreBucket;
import com.p1.mobile.p1android.ui.adapters.CustomGalleryAdapter;
import com.p1.mobile.p1android.ui.adapters.CustomGalleryAdapter.OnPictureSelectedListener;
import com.p1.mobile.p1android.ui.listeners.ContextualBackListener;
import com.p1.mobile.p1android.ui.phone.AbstractShareActivity;
import com.p1.mobile.p1android.ui.phone.AbstractShareActivity.PictureProvider;
import com.p1.mobile.p1android.ui.phone.CameraActivity;
import com.p1.mobile.p1android.ui.phone.CustomGalleryActivity;
import com.p1.mobile.p1android.ui.phone.PictureEditActivity;
import com.p1.mobile.p1android.ui.widget.CustomGalleryFooter;
import com.p1.mobile.p1android.ui.widget.CustomGalleryFooter.OnDoneListener;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.AbstractAction;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.IntentAction;

public class CustomGalleryFragment extends Fragment implements
        LoaderCallbacks<Cursor>, OnItemClickListener, OnItemSelectedListener,
        OnPictureSelectedListener, OnDoneListener, PictureProvider {
    public static final String TAG = CustomGalleryFragment.class
            .getSimpleName();

    private static final String CHANGE_COVER_KEY = "cover_picture";
    private static final String CHANGE_PROFILE_KEY = "profile_picture";

    private static final int PHOTOS_LOADER = 0;
    private static final int BUCKETS_LOADER = 1;

    private static final String[] IMAGES_PROJECTION = { Images.Media._ID,
            Images.Media.MINI_THUMB_MAGIC, Images.Media.BUCKET_DISPLAY_NAME,
            Images.Media.BUCKET_ID };
    private static final String IMAGES_ORDER_BY = Images.Media.DATE_ADDED
            + " desc";
    private static final String BUCKET_ID_PARAM = "bucket_id";
    private static final String BUCKET_SELECTION_PARAM = Images.Media.BUCKET_ID
            + " =?";

    private static final Uri MEDIA_STORE_CONTENT_URI = Images.Media.EXTERNAL_CONTENT_URI;
    public static final String PREF_SELECTED_MEDIA_BUCKET_ID = "selected_media_store_bucket";

    private GridView mGridView;
    private CustomGalleryAdapter mAdapter;
    private P1ActionBar mActionBar;
    private Spinner mBucketSpinner;
    private ArrayAdapter<MediaStoreBucket> mBucketsAdapter;
    private final List<MediaStoreBucket> mBucketsList = new ArrayList<MediaStoreBucket>();
    private ContextualBackListener mBackListener;
    private CustomGalleryFooter mFooter;
    private SharedPreferences mPreferences;

    private boolean mIsChangeProfilePicture = false;
    private boolean mIsChangeCoverPicture = false;

    public static CustomGalleryFragment newInstance(boolean changeCoverPicture,
            boolean changeProfilePicture) {
        Bundle args = new Bundle();
        args.putBoolean(CHANGE_COVER_KEY, changeCoverPicture);
        args.putBoolean(CHANGE_PROFILE_KEY, changeProfilePicture);
        CustomGalleryFragment fragment = new CustomGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        mIsChangeCoverPicture = getArguments().getBoolean(CHANGE_COVER_KEY,
                false);
        mIsChangeProfilePicture = getArguments().getBoolean(CHANGE_PROFILE_KEY,
                false);

        if (mIsChangeProfilePicture || mIsChangeCoverPicture) {
            mAdapter = new CustomGalleryAdapter(getActivity(), null, this, true);
        } else {
            mAdapter = new CustomGalleryAdapter(getActivity(), null, this);
        }

        mBucketsAdapter = new ArrayAdapter<MediaStoreBucket>(getActivity(),
                R.layout.custom_gallery_spinner_item, mBucketsList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.custom_gallery_layout, null);

        mGridView = (GridView) view.findViewById(R.id.galleryPicturesGrid);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mActionBar = (P1ActionBar) view.findViewById(R.id.actionBar);
        mFooter = (CustomGalleryFooter) view
                .findViewById(R.id.customGalleryFooter1);
        mFooter.setOnDoneListener(this);
        mFooter.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initActionBar();
        getLoaderManager().initLoader(BUCKETS_LOADER, null, this);
    }

    private void initActionBar() {
        mBucketSpinner = new Spinner(getActivity());
        mBucketSpinner.setAdapter(mBucketsAdapter);
        mBucketSpinner.setOnItemSelectedListener(this);
        mActionBar.setCenterView(mBucketSpinner);

        if (!mIsChangeCoverPicture && !mIsChangeProfilePicture) {
            final Intent intent = new Intent(Actions.CAMERA_START);
            intent.putExtra(CameraActivity.CHANGE_PROFILE_PICTURE_KEY,
                    mIsChangeProfilePicture);
            intent.putExtra(CameraActivity.CHANGE_COVER_PICTURE_KEY,
                    mIsChangeCoverPicture);
            IntentAction action = new IntentAction(
                    R.drawable.ic_navbar_camera_unselected, getActivity(),
                    intent);
            mActionBar.setRightAction(action);
        }

        final AbstractAction closeAction = new AbstractAction(
                R.drawable.btn_contextual_close) {

            @Override
            public void performAction() {
                mBackListener.onContextualBack();
            }

        };

        mActionBar.setLeftAction(closeAction);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ContextualBackListener == false) {
            throw new ClassCastException("Activities using " + TAG
                    + " must implement the interface ContextualBackListener");
        }
        mBackListener = (ContextualBackListener) activity;
        if (activity instanceof AbstractShareActivity == false) {
            throw new ClassCastException("Activities using " + TAG
                    + " must extend AbstractShareActivity");
        }
        ((AbstractShareActivity) activity).setPictureProvider(this);

    }

    @Override
    public void onPause() {
        saveSelectedBucketToPrefs();
        super.onPause();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        String[] selectionArgs = null;
        String selection = null;
        Log.d(TAG, "Create loader");
        switch (loaderID) {
        case BUCKETS_LOADER:
            Log.d(TAG, "Create loader " + loaderID);
            return new CursorLoader(getActivity(), MEDIA_STORE_CONTENT_URI,
                    IMAGES_PROJECTION, null, null, IMAGES_ORDER_BY);
        case PHOTOS_LOADER:
            if (bundle != null && bundle.containsKey(BUCKET_ID_PARAM)) {
                selection = BUCKET_SELECTION_PARAM;
                selectionArgs = new String[] { bundle
                        .getString(BUCKET_ID_PARAM) };
            }

            Log.d(TAG, "oncreateLoader PHOTOS LOADER");
            return new CursorLoader(getActivity(), MEDIA_STORE_CONTENT_URI,
                    IMAGES_PROJECTION, selection, selectionArgs,
                    IMAGES_ORDER_BY);

        default:
            // An invalid id was passed in
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        Log.d(TAG, "onloadfinished");
        switch (loader.getId()) {
        case BUCKETS_LOADER:
            Log.d(TAG, "LoadFinished BUCKET " + loader.getId());
            mBucketsList.clear();
            mBucketsList
                    .add(MediaStoreBucket.getAllPhotosBucket(getActivity()));

            if (cursor == null) {
                return;
            }
            final HashSet<String> bucketIds = new HashSet<String>();

            final int idColumn = cursor.getColumnIndex(ImageColumns.BUCKET_ID);
            final int nameColumn = cursor
                    .getColumnIndex(ImageColumns.BUCKET_DISPLAY_NAME);

            if (cursor.moveToFirst()) {
                do {
                    try {
                        final String bucketId = cursor.getString(idColumn);
                        if (bucketIds.add(bucketId)) {
                            mBucketsList.add(new MediaStoreBucket(bucketId,
                                    cursor.getString(nameColumn)));
                        }
                        mBucketsAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }

            setSelectedBucketFromPreferences();
        case PHOTOS_LOADER:
            Log.d(TAG, "LoadFinished PHOTOS " + loader.getId());
            mAdapter.swapCursor(cursor);
            break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        Log.d(TAG, "onItemClick " + mAdapter.isInSelectionMode());
        if (mIsChangeProfilePicture || mIsChangeCoverPicture) {

            View view2 = v.findViewById(R.id.gallery_thumb_icon);
            String imageUri = (String) view2.getTag();
            if (mIsChangeProfilePicture) {
                WritePicture.setProfilePicture(getActivity()
                        .getApplicationContext(), imageUri);
                mBackListener.onContextualBack();
            } else {
                WritePicture.setCoverPicture(getActivity()
                        .getApplicationContext(), imageUri);
                mBackListener.onContextualBack();
            }
            return;
        }
        if (mAdapter.isInSelectionMode()) {
            View selectionView = v
                    .findViewById(R.id.gallery_thumb_icon_selected);
            mAdapter.setPictureSelected(selectionView.isSelected(),
                    (Integer) selectionView.getTag(), selectionView);
        } else {
            View view2 = v.findViewById(R.id.gallery_thumb_icon);
            String imageUri = (String) view2.getTag();
            startPictureEditActivity(imageUri);
        }
    }

    private void startPictureEditActivity(String imageUri) {
        Log.d(TAG, "Thumb Selected Uri " + imageUri);
        if (imageUri != null) {
            Intent intent = new Intent(getActivity(), PictureEditActivity.class);
            intent.putExtra("uri", imageUri.toString());
            getActivity().startActivity(intent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "Loader reset " + loader.getId());

        switch (loader.getId()) {
        case BUCKETS_LOADER:
            mBucketsList.clear();
            mBucketsAdapter.notifyDataSetChanged();
            break;
        case PHOTOS_LOADER:
            mAdapter.swapCursor(null);
            break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View parent,
            int position, long id) {
        Log.d(TAG, "onItemSelected " + position + " " + id);
        MediaStoreBucket item = (MediaStoreBucket) adapterView
                .getItemAtPosition(position);
        if (null != item) {
            loadBucketId(item.getId());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // noop
    }

    @Override
    public void onPictureSelectedChanged(int numberOfSelected) {
        mFooter.setNumberSelectedPictures(numberOfSelected);
        if (numberOfSelected > 0 && !mFooter.isShown()) {
            showFooter(true);
        } else if (numberOfSelected <= 0 && mFooter.isShown()) {
            showFooter(false);
        }
    }

    private void showFooter(final boolean show) {
        mFooter.showFooter(show);

    }

    @Override
    public void onDone() {

        if (mAdapter.getSelectedCount() > 1) {
            ((CustomGalleryActivity) getActivity()).showShareDialog();
            try {

            } catch (ClassCastException e) {
                throw new ClassCastException(getActivity().toString()
                        + " must extend AbstractShareActivity");
            }
        } else {
            List<Integer> selectedList = mAdapter.getSelectedIds();
            Integer selectedId = selectedList.get(0);
            if (selectedId != null) {
                Uri imageUri = ContentUris.withAppendedId(
                        Images.Media.EXTERNAL_CONTENT_URI, selectedId);
                startPictureEditActivity(imageUri.toString());
            }
        }

    }

    private void setSelectedBucketFromPreferences() {
        Log.d(TAG, "setSelectedBucketFromPreferences");
        if (null != mBucketSpinner) {
            int selection = 0;

            if (mPreferences != null) {
                final String savedBucketId = mPreferences.getString(
                        PREF_SELECTED_MEDIA_BUCKET_ID, null);
                Log.d(TAG, "id from pref " + savedBucketId);
                if (savedBucketId != null) {
                    for (int i = 0, z = mBucketsList.size(); i < z; i++) {
                        if (savedBucketId.equals(mBucketsList.get(i).getId())) {
                            selection = i;
                            break;
                        }
                    }
                }
            }
            Log.d(TAG, "setSelected selection " + selection);
            Log.d(TAG,
                    "setSelected selected "
                            + mBucketSpinner.getSelectedItemPosition());
            if (selection != mBucketSpinner.getSelectedItemPosition()) {
                mBucketSpinner.setSelection(selection);
            } else {
                onItemSelected(mBucketSpinner, null, selection, 0);
            }
        }
    }

    private void saveSelectedBucketToPrefs() {

        MediaStoreBucket bucket = getSelectedBucket();
        if (null != bucket && null != mPreferences) {
            mPreferences.edit()
                    .putString(PREF_SELECTED_MEDIA_BUCKET_ID, bucket.getId())
                    .commit();
            Log.d(TAG, "SaveSelectedBUcketTo Pr " + bucket.getId());
        }
    }

    private MediaStoreBucket getSelectedBucket() {
        if (null != mBucketSpinner) {
            return (MediaStoreBucket) mBucketSpinner.getSelectedItem();
        }
        return null;
    }

    private void loadBucketId(String id) {
        if (isAdded()) {
            Bundle bundle = new Bundle();
            if (null != id) {
                Log.d(TAG, "loadBucket id not null ");
                bundle.putString(BUCKET_ID_PARAM, id);
            }
            try {
                Log.d(TAG, "loadBucket id null");
                getLoaderManager().restartLoader(PHOTOS_LOADER, bundle, this);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<String> getSelectedPictures() {
        List<String> list = new ArrayList<String>();
        for (Integer id : mAdapter.getSelectedIds()) {

            list.add(ContentUris.withAppendedId(
                    Images.Media.EXTERNAL_CONTENT_URI, id).toString());
        }
        return list;
    }

}
