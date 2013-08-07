package com.p1.mobile.p1android.ui.fragment;

import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Profile;
import com.p1.mobile.p1android.content.Profile.MaritalStatus;
import com.p1.mobile.p1android.content.logic.ReadProfile;
import com.p1.mobile.p1android.content.logic.WriteProfile;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.ListenerAction;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.OnActionListener;
import com.p1.mobile.p1android.ui.widget.P1TextView;

public class RelationshipStatusFragment extends Fragment implements
		OnActionListener, OnClickListener {
	private static final String TAG = RelationshipStatusFragment.class
			.getSimpleName();
	private static final String CONTENT_KEY = "content";

	private ImageView mSelectedImageView1;
	private ImageView mSelectedImageView2;
	private ImageView mSelectedImageView3;
	private ImageView mSelectedImageView4;
	private ImageView mSelectedImageView5;
	private ImageView mSelectedImageView6;

	private P1TextView mRelationshipTextView1;
	private P1TextView mRelationshipTextView2;
	private P1TextView mRelationshipTextView3;
	private P1TextView mRelationshipTextView4;
	private P1TextView mRelationshipTextView5;
	private P1TextView mRelationshipTextView6;
	
	private RelativeLayout mRelationshipRelativeLayout1;
	private RelativeLayout mRelationshipRelativeLayout2;
	private RelativeLayout mRelationshipRelativeLayout3;
	private RelativeLayout mRelationshipRelativeLayout4;
	private RelativeLayout mRelationshipRelativeLayout5;
	private RelativeLayout mRelationshipRelativeLayout6;

    public static String[] mMaritalStatusStrArray  = new String[6];
	
	private P1ActionBar mActionBar;
	private P1TextView mActionBarTitle;

	private String mTitleStr;
	private String mSelectedStr;
	private String mOriginalContentStr;
	
    private View mDividerView1;
    private View mDividerView2;
    public MaritalStatus mMaritalStatus;
    public Profile mProfile;
    
	public static Fragment newInstance(String previousText) {
		RelationshipStatusFragment fragment = new RelationshipStatusFragment();
		if (previousText != null) {
			Bundle bundle = new Bundle();
			bundle.putString(CONTENT_KEY, previousText);
			fragment.setArguments(bundle);
		}
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (getArguments() != null) {
            mSelectedStr = ((String) getArguments().get(CONTENT_KEY));
            mOriginalContentStr = ((String) getArguments().get(CONTENT_KEY));
		}
		
		mMaritalStatusStrArray[0] = getResources().getString(R.string.relationship_ship_single);
		mMaritalStatusStrArray[1] = getResources().getString(R.string.relationship_ship_in_relationship);
		mMaritalStatusStrArray[2] = getResources().getString(R.string.relationship_ship_engaged);
		mMaritalStatusStrArray[3] = getResources().getString(R.string.relationship_ship_married);
		mMaritalStatusStrArray[4] = getResources().getString(R.string.relationship_ship_complicated);
		mMaritalStatusStrArray[5] = getResources().getString(R.string.relationship_ship_private);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.relationship_status_fragment,
				container, false);

		initActionBar(inflater, view);
		
		mRelationshipRelativeLayout1 = (RelativeLayout) view.findViewById(R.id.rl_relationship_status1);
		mRelationshipRelativeLayout1.setOnClickListener(this);
		mRelationshipRelativeLayout2 = (RelativeLayout) view.findViewById(R.id.rl_relationship_status2);
		mRelationshipRelativeLayout2.setOnClickListener(this);
		mRelationshipRelativeLayout3 = (RelativeLayout) view.findViewById(R.id.rl_relationship_status3);
		mRelationshipRelativeLayout3.setOnClickListener(this);
		mRelationshipRelativeLayout4 = (RelativeLayout) view.findViewById(R.id.rl_relationship_status4);
		mRelationshipRelativeLayout4.setOnClickListener(this);
		mRelationshipRelativeLayout5 = (RelativeLayout) view.findViewById(R.id.rl_relationship_status5);
		mRelationshipRelativeLayout5.setOnClickListener(this);
		mRelationshipRelativeLayout6 = (RelativeLayout) view.findViewById(R.id.rl_relationship_status6);
		mRelationshipRelativeLayout6.setOnClickListener(this);
		mRelationshipRelativeLayout6.setVisibility(View.VISIBLE);
        mDividerView1 = view.findViewById(R.id.v_relationship_status_v1);
        mDividerView1.setVisibility(View.VISIBLE);
        mDividerView2 = view.findViewById(R.id.v_relationship_status_v2);
        mDividerView2.setVisibility(View.VISIBLE);
		
		mSelectedImageView1 = (ImageView) view.findViewById(R.id.iv_relationship_status_selected1);
		mSelectedImageView2 = (ImageView) view.findViewById(R.id.iv_relationship_status_selected2);
		mSelectedImageView3 = (ImageView) view.findViewById(R.id.iv_relationship_status_selected3);
		mSelectedImageView4 = (ImageView) view.findViewById(R.id.iv_relationship_status_selected4);
		mSelectedImageView5 = (ImageView) view.findViewById(R.id.iv_relationship_status_selected5);
		mSelectedImageView6 = (ImageView) view.findViewById(R.id.iv_relationship_status_selected6);

		 mRelationshipTextView1 = (P1TextView) view.findViewById(R.id.tv_relationship_status1);
		 mRelationshipTextView2 = (P1TextView) view.findViewById(R.id.tv_relationship_status2);
		 mRelationshipTextView3 = (P1TextView) view.findViewById(R.id.tv_relationship_status3);
		 mRelationshipTextView4 = (P1TextView) view.findViewById(R.id.tv_relationship_status4);
		 mRelationshipTextView5 = (P1TextView) view.findViewById(R.id.tv_relationship_status5);
		 mRelationshipTextView6 = (P1TextView) view.findViewById(R.id.tv_relationship_status6);
		
		return view;
	}

	private void initActionBar(LayoutInflater inflater, View containerView) {
		mActionBar = (P1ActionBar) containerView
				.findViewById(R.id.user_profile_action_bar);
		mActionBarTitle = new P1TextView(getActivity());
        mActionBarTitle.setText(getResources().getString(
                R.string.profile_detail_relationship));
		mActionBarTitle.setTextAppearance(getActivity(),
				R.style.P1LargerTextLight);
		mActionBarTitle.setGravity(Gravity.CENTER);
		mActionBar.setCenterView(mActionBarTitle);

        mActionBar.setLeftAction(new ListenerAction(
                R.drawable.back_arrow_button, this));
		
		mActionBar.setRightAction(new ListenerAction(R.drawable.relationship_status_save, new ActionBarSaveListener()));

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mRelationshipTextView1.setText(mMaritalStatusStrArray[0]);
		mRelationshipTextView2.setText(mMaritalStatusStrArray[1]);
		mRelationshipTextView3.setText(mMaritalStatusStrArray[2]);
		mRelationshipTextView4.setText(mMaritalStatusStrArray[3]);
		mRelationshipTextView5.setText(mMaritalStatusStrArray[4]);
		mRelationshipTextView6.setText(mMaritalStatusStrArray[5]);
		
		mProfile = ReadProfile.requestLoggedInProfile(null);
		
		setSelectedImageViewGone();
		if(mSelectedStr.equals(mMaritalStatusStrArray[0])){
			mSelectedImageView1.setVisibility(View.VISIBLE);
		}else if(mSelectedStr.equals(mMaritalStatusStrArray[1])) {
			mSelectedImageView2.setVisibility(View.VISIBLE);
		}else if(mSelectedStr.equals(mMaritalStatusStrArray[2])) {
			mSelectedImageView3.setVisibility(View.VISIBLE);
		}else if(mSelectedStr.equals(mMaritalStatusStrArray[3])) {
			mSelectedImageView4.setVisibility(View.VISIBLE);
		}else if(mSelectedStr.equals(mMaritalStatusStrArray[4])) {
			mSelectedImageView5.setVisibility(View.VISIBLE);
		}else if(mSelectedStr.equals(mMaritalStatusStrArray[5])) {
			mSelectedImageView6.setVisibility(View.VISIBLE);
		}
	}

	class ActionBarSaveListener implements OnActionListener {

		@Override
		public void onAction() {
            if (mSelectedStr.equals(getResources().getString(
                    R.string.relationship_ship_in_relationship))) {
                mMaritalStatus = MaritalStatus.IN_RELATIONSHIP;
            } else if (mSelectedStr.equals(getResources().getString(
                    R.string.relationship_ship_complicated))) {
                mMaritalStatus = MaritalStatus.COMPLICATED;
            } else if (mSelectedStr.equals(getResources().getString(
                    R.string.relationship_ship_private))) {
                mMaritalStatus = MaritalStatus.OTHER;
            } else {
                for (MaritalStatus ms : MaritalStatus.values()) {
                    if (String.valueOf(ms).equals(
                            mSelectedStr.toUpperCase(Locale.CHINA))) {
                        mMaritalStatus = ms;
                        break;
                    }
                }
            }
            WriteProfile.changeMaritalStatus(mProfile, mMaritalStatus);
            
            backToLastFragment();
		}
		
	}
	
	@Override
	public void onAction() {
		if(mSelectedStr.equals(mOriginalContentStr)) {
		    backToLastFragment();
		}else {
			showChangeDialog();
		}
	}
	
    private void backToLastFragment() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fm.popBackStack();
		ft.commit();
	}

    private void showChangeDialog() {
        // show a Dialog
        new AlertDialog.Builder(getActivity()).setTitle(getResources().getString(R.string.dialog_title))
                .setMessage(getResources().getString(R.string.dialog_message))
                .setPositiveButton(getResources().getString(R.string.dialog_discard), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        backToLastFragment();
                    }
                }).setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_relationship_status1:
			setSelectedImageViewGone();
			mSelectedImageView1.setVisibility(View.VISIBLE);
			mSelectedStr = mMaritalStatusStrArray[0];
			break;
		case R.id.rl_relationship_status2:
			setSelectedImageViewGone();
			mSelectedImageView2.setVisibility(View.VISIBLE);
			mSelectedStr = mMaritalStatusStrArray[1];
			break;
		case R.id.rl_relationship_status3:
			setSelectedImageViewGone();
			mSelectedImageView3.setVisibility(View.VISIBLE);
			mSelectedStr = mMaritalStatusStrArray[2];
			break;
		case R.id.rl_relationship_status4:
			setSelectedImageViewGone();
			mSelectedImageView4.setVisibility(View.VISIBLE);
			mSelectedStr = mMaritalStatusStrArray[3];
			break;
		case R.id.rl_relationship_status5:
			setSelectedImageViewGone();
			mSelectedImageView5.setVisibility(View.VISIBLE);
			mSelectedStr = mMaritalStatusStrArray[4];
			break;
		case R.id.rl_relationship_status6:
			setSelectedImageViewGone();
			mSelectedImageView6.setVisibility(View.VISIBLE);
			mSelectedStr = mMaritalStatusStrArray[5];
			break;
		}
	}
	
	private void setSelectedImageViewGone() {
		mSelectedImageView1.setVisibility(View.GONE);
		mSelectedImageView2.setVisibility(View.GONE);
		mSelectedImageView3.setVisibility(View.GONE);
		mSelectedImageView4.setVisibility(View.GONE);
		mSelectedImageView5.setVisibility(View.GONE);
		mSelectedImageView6.setVisibility(View.GONE);
	}
}
