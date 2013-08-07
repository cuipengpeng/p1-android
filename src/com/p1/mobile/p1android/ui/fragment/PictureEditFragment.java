package com.p1.mobile.p1android.ui.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage.ScaleType;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.GPUImageView.Size;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.logic.WriteShare;
import com.p1.mobile.p1android.filter.Filter;
import com.p1.mobile.p1android.filter.FilterManager;
import com.p1.mobile.p1android.ui.listeners.BitmapLoaderListener;
import com.p1.mobile.p1android.ui.listeners.ContextualBackListener;
import com.p1.mobile.p1android.ui.phone.AbstractShareActivity;
import com.p1.mobile.p1android.ui.phone.AbstractShareActivity.PictureProvider;
import com.p1.mobile.p1android.ui.widget.FilterButton;
import com.p1.mobile.p1android.ui.widget.ToggleableHorizontalScrollView;
import com.p1.mobile.p1android.ui.widget.ToggleableHorizontalScrollView.OnItemSelectedListener;
import com.p1.mobile.p1android.util.BitmapLoaderTask;
import com.p1.mobile.p1android.util.BitmapUtils;
import com.p1.mobile.p1android.util.PerformanceMeasure;
import com.p1.mobile.p1android.util.Utils;

public class PictureEditFragment extends Fragment implements
        OnItemSelectedListener, PictureProvider {
    private static final String TAG = PictureEditFragment.class.getSimpleName();

    private final int QUALITY = 100;

    private GPUImageView mImageView;
    private ToggleableHorizontalScrollView mScrollLayout;
    private String mUriString;
    private boolean mBitmapExists;
    private String mSavedPictureUriString;
    private List<Filter> mFilterList;
    private ContextualBackListener mBackListener;
    private int mImageHeight;
    private int mImageWidth;
    private RelativeLayout mImageViewBox;
    private ImageButton mDoneButton;
    private ImageButton mCloseButton;
    private float mImageRatio;

    public static PictureEditFragment newInstance(String uri,
            boolean bitmapExists) {
        PictureEditFragment fragment = new PictureEditFragment();
        Bundle args = new Bundle();
        args.putString("uri", uri);
        args.putBoolean("bitmapExists", bitmapExists);
        fragment.setArguments(args);
        return fragment;
    }

    public void setImageUri(String uri) {
        mUriString = uri;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        mUriString = args.getString("uri");
        mBitmapExists = args.getBoolean("bitmapExists");
        Log.d(TAG, "Recieved uri string " + mUriString);
        Log.d(TAG, "Recieved bitmap exists " + mBitmapExists);

        if (!mBitmapExists) {
            new BitmapLoaderTask(getActivity(), new BitmapLoaderListener() {

                @Override
                public void onBitmapLoaded(Bitmap bitmap) {
                    P1Application.tempCameraImage = bitmap;
                    mImageView.setImage(bitmap);
                    mBitmapExists = true;
                    setFilterButtonImages();
                }

            }).execute(mUriString);
        }
        int perf2 = PerformanceMeasure.startMeasure();
        mFilterList = FilterManager.getAllFilters(getActivity());
        PerformanceMeasure.endMeasure(perf2, "Load all filters");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AbstractShareActivity) activity).setPictureProvider(this);
        mBackListener = (ContextualBackListener) activity;
    }

    @Override
    public void onDetach() {
        mBackListener = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        int perf1 = PerformanceMeasure.startMeasure();
        View view = inflater.inflate(R.layout.picture_edit_fragment_layout,
                container, false);
        mImageView = new GPUImageView(getActivity());
        mImageViewBox = (RelativeLayout) view.findViewById(R.id.imageViewBox);
        mImageViewBox.addView(mImageView,
                new android.view.ViewGroup.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        setImageViewDimentions();

        mScrollLayout = (ToggleableHorizontalScrollView) view
                .findViewById(R.id.filterScrollLayout);

        mCloseButton = (ImageButton) view
                .findViewById(R.id.pictureEditCloseButton);
        mCloseButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mBackListener.onContextualBack();
            }
        });

        mDoneButton = (ImageButton) view
                .findViewById(R.id.pictureEditDoneButton);
        mDoneButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onDone();
            }
        });
        setupFilterButtons();
        mScrollLayout.setSelectedItem(0);
        mScrollLayout.setOnItemSelectedListener(this);
        PerformanceMeasure.endMeasure(perf1, "onCreateView ");
        if (mBitmapExists && mUriString == null) {
            mImageView.setImage(P1Application.tempCameraImage);
            setFilterButtonImages();
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "On destroy view");
        mImageView.getGPUImage().deleteImage();
    }

    private void setImageViewDimentions() {

        setImageDimentions();

        Point screenSize = new Point();
        Utils.getScreenSize(screenSize, getActivity());

        mImageRatio = (float) mImageHeight / (float) mImageWidth;
        float screenRatio = (float) screenSize.y / (float) screenSize.x;

        Log.d(TAG, "screen  h/ w " + screenSize.y + " " + screenSize.x);
        Log.d(TAG, "pic h/ w " + mImageHeight + " " + mImageWidth);
        Log.d(TAG, "ratio h/ w " + mImageRatio);
        Log.d(TAG, "screenRatio h/ w " + screenRatio);

        int newHeight = 0;
        int newWidth = 0;

        if (mImageRatio <= screenRatio) {
            newHeight = (int) (((float) screenSize.x / (float) mImageWidth) * (float) mImageHeight);
            newWidth = (int) (((float) screenSize.x / (float) mImageWidth) * (float) mImageWidth);
        } else {
            newHeight = (int) (((float) screenSize.y / (float) mImageHeight) * (float) mImageHeight);
            newWidth = (int) (((float) screenSize.y / (float) mImageHeight) * (float) mImageWidth);
        }
        Log.d(TAG, "New size " + newHeight + " " + newWidth);

        setLayoutParams(newHeight, newWidth);

        mImageView.mForceSize = new Size(newWidth, newHeight);

        mImageView.setScaleType(ScaleType.CENTER_INSIDE);

    }

    private void setLayoutParams(int newHeight, int newWidth) {
        LayoutParams params = (LayoutParams) mImageViewBox.getLayoutParams();
        if (mImageHeight > mImageWidth) {
            Log.d(TAG, "Modify rules height " + mImageHeight + " width "
                    + mImageWidth);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);

            ((LayoutParams) mImageView.getLayoutParams())
                    .addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mImageView.getLayoutParams().height = newHeight;
            mImageView.getLayoutParams().width = newWidth;

        } else if (mImageHeight == mImageWidth) {
            Log.d(TAG, "Modify rules height " + mImageHeight + " width "
                    + mImageWidth);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            ((LayoutParams) mImageView.getLayoutParams())
                    .addRule(RelativeLayout.CENTER_VERTICAL);
        } else if (mImageWidth > mImageHeight) {
            params.addRule(RelativeLayout.CENTER_VERTICAL);
        }

        params.height = newHeight;
        params.width = newWidth;

    }

    private void setImageDimentions() {
        if (mBitmapExists) {
            mImageWidth = P1Application.tempCameraImage.getWidth();
            mImageHeight = P1Application.tempCameraImage.getHeight();
        } else {
            int orientation = BitmapUtils.getOrientation(getActivity(),
                    Uri.parse(mUriString));
            String path = Utils.getRealPathFromURI(getActivity(), mUriString);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inPurgeable = true;
            BitmapFactory.decodeFile(path, options);

            Log.d(TAG, "Decoded bitmap size " + options.outHeight + " "
                    + options.outWidth + " orientation " + orientation);
            if (orientation == 90 || orientation == 270) {
                mImageWidth = options.outHeight;
                mImageHeight = options.outWidth;
            } else {
                mImageHeight = options.outHeight;
                mImageWidth = options.outWidth;
            }
        }
    }

    private void setupFilterButtons() {

        int perf2 = PerformanceMeasure.startMeasure();
        for (Filter filter : mFilterList) {
            if (filter == null) {
                Log.e(TAG, "Filter is null");
                continue;
            }

            FilterButton button = new FilterButton(getActivity()
                    .getApplicationContext(), filter);

            button.setTag(filter);

            mScrollLayout.addItem(button);

        }
        PerformanceMeasure.endMeasure(perf2, "setbuttons");
    }

    private void setFilterButtonImages() {
        int perf1 = PerformanceMeasure.startMeasure();
        Bitmap thumbBitmap = null;
        try {
            if (mBitmapExists) {
                int width;
                int height;
                if (mImageHeight > mImageWidth) {
                    width = 120;
                    height = (int) (120f * mImageRatio);
                } else {
                    width = (int) (120f * mImageRatio);
                    height = 120;
                }
                thumbBitmap = BitmapUtils.getResizedBitmap(
                        P1Application.tempCameraImage, width, height);
            } else {
                thumbBitmap = BitmapUtils.getCorrectlyOrientedImage(
                        getActivity(), mUriString, 120);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        PerformanceMeasure.endMeasure(perf1, "Decode bitmap");
        for (Filter filter : mFilterList) {
            if (filter == null) {
                Log.e(TAG, "Filter is null");
                continue;
            }
            FilterButton button = (FilterButton) mScrollLayout.getItem(filter);
            button.setImage(thumbBitmap);

        }
    }

    private void applyFilter(Filter filter) {
        int perf1 = PerformanceMeasure.startMeasure();
        if (filter.getGPUFilter() != null) {
            filter.applyFilter(getActivity().getApplicationContext(),
                    mImageView);
        }
        PerformanceMeasure
                .endMeasure(perf1, "Apply filter " + filter.getName());
    }

    public void sharePicture(final Share share) {
        final Point saveSize = BitmapUtils.determineSaveSize(mImageWidth,
                mImageHeight);
        new SharePictureTask(share).execute(saveSize);
    }

    @Override
    public void onItemSelected(View view) {
        Filter filter = (Filter) view.getTag();
        if (filter != null) {
            applyFilter(filter);
        }
    }

    private void onDone() {

        ((AbstractShareActivity) getActivity()).showShareDialog();
    }

    @Override
    public List<String> getSelectedPictures() {
        List<String> list = new ArrayList<String>();
        if (!TextUtils.isEmpty(mSavedPictureUriString)) {
            list.add(mSavedPictureUriString);
        }
        return list;
    }

    private class SharePictureTask extends AsyncTask<Point, Void, Bitmap> {

        private Share share;

        public SharePictureTask(Share share) {
            this.share = share;
        }

        @Override
        protected Bitmap doInBackground(Point... params) {
            Log.d(TAG, "Capturing image");
            try {
                Bitmap capturedBitmap = mImageView.capture(params[0].x,
                        params[0].y);
                saveFilteredImage(capturedBitmap);
                Log.d(TAG, "Image successfully captured");
                return capturedBitmap;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            WriteShare.addSinglePicture(share, bitmap);
            WriteShare.send(share);
        }

        @SuppressLint("SimpleDateFormat")
        private void saveFilteredImage(Bitmap bitmap) {
            File mediaStorageDir = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    getString(R.string.app_name));
            Log.i(TAG, "Save Picture to " + mediaStorageDir.toString());
            if (!mediaStorageDir.exists()) {
                Log.i(TAG, "!mediaStorageDir.exists()");
                if (!mediaStorageDir.mkdirs()) {
                    Log.i(TAG, "!mediaStorageDir.mkdirs()");
                    return;
                }
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());
            File mediaFile = new File(mediaStorageDir.getPath()
                    + File.separator + "P1_" + timeStamp + ".jpg");
            Log.i(TAG, "mediaFile uri " + mediaFile.toString());
            try {
                FileOutputStream stream = new FileOutputStream(mediaFile);
                bitmap.compress(CompressFormat.JPEG, QUALITY, stream);
            } catch (IOException exception) {

                Log.w(TAG, "IOException during saving bitmap", exception);
                return;
            }

            MediaScannerConnection.scanFile(
                    P1Application.getP1ApplicationContext(),
                    new String[] { mediaFile
                    .toString() }, new String[] { "image/jpeg" }, null);
            Log.i(TAG, "Finished saving");

        }

    }

}
