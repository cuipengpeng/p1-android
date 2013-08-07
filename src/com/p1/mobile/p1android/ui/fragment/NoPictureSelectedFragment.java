package com.p1.mobile.p1android.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.p1.mobile.p1android.R;

public class NoPictureSelectedFragment extends Fragment {
    public static final String TAG = NoPictureSelectedFragment.class
            .getSimpleName();

    public interface OnChoosePictureCallback {
        void onChoosePicture();
    }

    public static NoPictureSelectedFragment newInstance() {
        NoPictureSelectedFragment fragment = new NoPictureSelectedFragment();

        return fragment;
    }

    private Button mButton;
    private OnChoosePictureCallback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.no_picture_layout, container,
                false);
        mButton = (Button) view.findViewById(R.id.choosePictureButton);

        mButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCallback.onChoosePicture();
            }
        });

        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCallback = (OnChoosePictureCallback) getActivity();
    }
}
