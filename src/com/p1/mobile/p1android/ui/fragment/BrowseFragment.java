package com.p1.mobile.p1android.ui.fragment;

import java.lang.reflect.Field;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.BrowseFilter;
import com.p1.mobile.p1android.ui.helpers.NotificationsCounterUpdater;
import com.p1.mobile.p1android.ui.listeners.NavigationListener;
import com.p1.mobile.p1android.ui.phone.BrowseFilterActivity;
import com.p1.mobile.p1android.ui.widget.CounterBubble;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.UserPictureView;

/**
 * Parent fragment containing the logic for switching between Browse pictures
 * and Browse members
 * 
 * @author Viktor Nyblom Modified by Cui Pengpeng
 * 
 */
public class BrowseFragment extends Fragment implements OnClickListener,
        OnSharedPreferenceChangeListener {
    public static final String TAG = BrowseFragment.class.getSimpleName();

    public static final String FILTER_PREF_FILE_NAME = "browse_filter";

    // private FrameLayout mContainer;
    private P1ActionBar mActionBar;

    private static String POPULAR;
    private static String RENCENT;
    private static String RAND;

    private static String PICTURES;

    private static String MEMBERS;

    public static boolean isPicture = true;

    private BrowseFilter mFilter;

    private TextView mFilterTextView;

    private View mBrowseFilterView;

    private RelativeLayout mTogglePictiureRelativeLayout;
    private RelativeLayout mToggleMemberRelativeLayout;
    private RelativeLayout mBrowseFilterRelativeLayout;
    private RadioButton mPictureRadioButton;
    private RadioButton mMemberRadioButton;
    
    public static BrowseFragment newInstance() {
        BrowseFragment fragment = new BrowseFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        POPULAR = getActivity().getString(R.string.browse_filter_popular);
        RENCENT = getActivity().getString(R.string.browse_filter_recent);
        RAND = getActivity().getString(R.string.browse_filter_random);
        PICTURES = getActivity().getString(R.string.browse_filter_pictures);
        MEMBERS = getActivity().getString(R.string.browse_filter_members);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView");

        mFilter = registerFilterLisener(getActivity(), this);
        View view = inflater.inflate(R.layout.browse_fragment_layout, null);

        mActionBar = (P1ActionBar) view.findViewById(R.id.actionBar);
        mBrowseFilterRelativeLayout = (RelativeLayout) view.findViewById(R.id.rl_browse_filter);
        mBrowseFilterRelativeLayout.setOnClickListener(this);
        
        RadioGroup toogle = (RadioGroup) inflater.inflate(
                R.layout.ab_toggle_layout, null);
        toogle.getChildAt(isPicture ? 0 : 1).performClick();
        
        mPictureRadioButton = (RadioButton) toogle.findViewById(R.id.browse_toggle_picture);
        mPictureRadioButton.setClickable(false);
        mMemberRadioButton = (RadioButton) toogle.findViewById(R.id.browse_toggle_user);
        mMemberRadioButton.setClickable(false);
        mTogglePictiureRelativeLayout = (RelativeLayout) toogle.findViewById(R.id.rl_browse_toggle_picture);
        mTogglePictiureRelativeLayout.setOnClickListener(this);
        mToggleMemberRelativeLayout = (RelativeLayout) toogle.findViewById(R.id.rl_browse_toggle_user);
        mToggleMemberRelativeLayout.setOnClickListener(this);
        
        mActionBar.setCenterView(toogle);

        mFilterTextView = (TextView) view.findViewById(R.id.filter_text);
        updateFilterText();
        mBrowseFilterView = view.findViewById(R.id.browse_filter);
        mBrowseFilterView.setOnClickListener(this);
        view.findViewById(R.id.filter_button).setOnClickListener(this);
        // TODO set profile picture and notifications counter
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
            Log.e(TAG,
                    "Activity of BrowseFragment is not a NavigationListener. BrowseFragment is probably placed in a bad activity");
        }

        return view;
    }

    private void updateFilterText() {
        String filterBy = mFilter.getFilterBy();
        if (filterBy.equals(BrowseFilter.BY_POPULAR)) {
            filterBy = POPULAR;
        } else if (filterBy.equals(BrowseFilter.BY_RANDOM)) {
            filterBy = RAND;
        } else if (filterBy.equals(BrowseFilter.BY_RECENT)) {
            filterBy = RENCENT;
        }
        filterBy += isPicture ? PICTURES : MEMBERS;
        mFilterTextView.setText(filterBy);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.filter_button:
        case R.id.rl_browse_filter:
            startFilterActivity();
            break;
        case R.id.rl_browse_toggle_picture:
            mPictureRadioButton.setChecked(true);
            mMemberRadioButton.setChecked(false);
            switchToPictures();
            updateFilterText();
            break;
        case R.id.rl_browse_toggle_user:
            mMemberRadioButton.setChecked(true);  
            mPictureRadioButton.setChecked(false);
            switchToMembers();
            updateFilterText();
            break;
        }
    }

    @Override
    public void onDestroyView() {
        unregisterFilterLisener(getActivity(), this);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isPicture)
            switchToPictures();
        else
            switchToMembers();
    }

    private void switchToMembers() {
        isPicture = false;
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        Fragment members = getChildFragmentManager().findFragmentByTag("members");
        Fragment pictures = getChildFragmentManager().findFragmentByTag("pictures");
        if (members == null) {
            Log.d(TAG, "MEMBERS_TAG gives null fragment");
            members = BrowseMembersFragment.newInstance();
            ft.add(R.id.browseContainer, members, "members");
        }
        if (pictures != null)
            ft.hide(pictures);
        ft.show(members);
        ft.commit();
    }

    private void switchToPictures() {
        isPicture = true;
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        Fragment members = getChildFragmentManager().findFragmentByTag("members");
        Fragment pictures = getChildFragmentManager().findFragmentByTag("pictures");
        if (pictures == null) {
            Log.d(TAG, "PICTURES_TAG gives null fragment");
            pictures = (BrowsePicturesFragment) BrowsePicturesFragment
                    .newInstance();
            ft.add(R.id.browseContainer, pictures, "pictures");
        }
        if (members != null)
            ft.hide(members);
        ft.show(pictures);
        ft.commit();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Fragment members = getChildFragmentManager().findFragmentByTag("members");
        Fragment pictures = getChildFragmentManager().findFragmentByTag("pictures");
        if (members != null) {
            members.onHiddenChanged(hidden || members.isHidden());
        }
        if (pictures != null) {
            pictures.onHiddenChanged(hidden || pictures.isHidden());
        }
    }
    
    public void startFilterActivity() {
        Intent intent = new Intent(getActivity(), BrowseFilterActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        mFilter = getBrowseFilter(pref);
        updateFilterText();

    }

    // utils for dealing with preference
    public static BrowseFilter getBrowseFilter(SharedPreferences pref) {
        BrowseFilter newBrowseFilter = new BrowseFilter();

        String filterBy = pref.getString(BrowseFilterActivity.KEY_PREFILTERBY,
                BrowseFilter.BY_POPULAR);
        newBrowseFilter.setFilterBy(filterBy.intern());
        String gender = pref.getString(BrowseFilterActivity.KEY_PREFGENDER,
                BrowseFilter.GENDER_ALL);
        newBrowseFilter.setGender(gender.intern());
        return newBrowseFilter;
    }

    public static BrowseFilter registerFilterLisener(Context context,
            OnSharedPreferenceChangeListener listener) {
        SharedPreferences pref = context.getSharedPreferences(
                FILTER_PREF_FILE_NAME, Context.MODE_PRIVATE);
        pref.registerOnSharedPreferenceChangeListener(listener);
        return getBrowseFilter(pref);
    }

    public static void unregisterFilterLisener(Context context,
            OnSharedPreferenceChangeListener listener) {
        SharedPreferences pref = context.getSharedPreferences(
                FILTER_PREF_FILE_NAME, Context.MODE_PRIVATE);
        pref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    
    /**
     * Temp fix for
     * <a>http://stackoverflow.com/questions/16728426/android-nested-fragment-approach</>
     */
    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class
                    .getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
