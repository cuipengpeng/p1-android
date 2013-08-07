package com.p1.mobile.p1android.ui.phone;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Share;
import com.p1.mobile.p1android.content.Share.ShareIOSession;
import com.p1.mobile.p1android.content.logic.ReadShare;
import com.p1.mobile.p1android.content.logic.WriteShare;
import com.p1.mobile.p1android.ui.fragment.VenueListFragment;
import com.p1.mobile.p1android.ui.fragment.VenueListFragment.VenueLocationsCallback;
import com.p1.mobile.p1android.ui.helpers.VenueSearchHelper;
import com.p1.mobile.p1android.ui.widget.P1ActionBar;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.ListenerAction;
import com.p1.mobile.p1android.ui.widget.P1ActionBar.OnActionListener;
import com.p1.mobile.p1android.ui.widget.P1TextView;
import com.p1.mobile.p1android.util.Utils;

/**
 * 
 * @author Viktor Nyblom
 * 
 */
public class VenueActivity extends FlurryFragmentActivity implements
        VenueLocationsCallback, IContentRequester, OnActionListener,
        MKGeneralListener, BDLocationListener, LocationListener {
    static final String TAG = VenueActivity.class.getSimpleName();

    private static final String BAIDU_MAPS_KEY = "236B34A91A2D1077129B9D2275E6E320BBD86AFA";
    private static final boolean ZOOM_CONTROLS_ENABLED = false;
    private static final GeoPoint DEFAULT_LOCATION_BEIJING = new GeoPoint(
            (int) (39.915 * 1E6), (int) (116.404 * 1E6));
    private static final int DEFAULT_ZOOM_LEVEL = 15;
    private static final boolean START_GPS = true;
    private static final boolean ENABLE_LOCATION_CACHE = false;

    private Share mShare;
    private BMapManager mBMapMan = null;
    private MapView mMapView = null;
    private LocationClient mLocationClient;
    private BDLocation mLastLocation;
    private VenueListFragment mFragment;
    private EditText mSearchText;
    private VenueSearchHelper mSearchHelper;
    private LocationManager mLocationManager;
    private String mProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Must be initialized before setContentView
        mBMapMan = new BMapManager(getApplication());
        mBMapMan.init(BAIDU_MAPS_KEY, this);

        setContentView(R.layout.venue_activity);

        initSearchText();
        initActionBar();

        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.setBuiltInZoomControls(ZOOM_CONTROLS_ENABLED);

        MapController mMapController = mMapView.getController();
        mMapController.setCenter(DEFAULT_LOCATION_BEIJING);
        mMapController.setZoom(DEFAULT_ZOOM_LEVEL);

        if (getIntent().getExtras() != null) {

            final String shareId = getIntent().getExtras().getString("shareId");

            mShare = ReadShare.requestShare(shareId, this);
        } else {
            mShare = WriteShare.initNewShare();
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        mFragment = VenueListFragment.newInstance();
        ft.replace(R.id.venueFragmentContainer, mFragment);
        ft.commit();

        setupLocationClient();
        setupLocationManager();
        // Tiananmen Square coords
        mSearchHelper = new VenueSearchHelper(mFragment, 39.915, 116.404);
    }

    private void setupLocationManager() {
        // Get the location manager
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        mProvider = mLocationManager.getBestProvider(criteria, false);
        Location location = mLocationManager.getLastKnownLocation(mProvider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + mProvider + " has been selected.");
            onLocationChanged(location);
        }
    }

    private void initSearchText() {
        mSearchText = (EditText) findViewById(R.id.venueSearchText);
        mSearchText.addTextChangedListener(new TextWatcher() {

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
                mSearchHelper.setNewSearchString(s.toString());
            }
        });

        mSearchText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMapView.setVisibility(View.GONE);
            }
        });
    }

    private void initActionBar() {
        P1ActionBar actionBar = (P1ActionBar) findViewById(R.id.venueActionBar);

        P1TextView textView = new P1TextView(this);
        textView.setText(getResources().getString(R.string.venue_title));
        textView.setTextAppearance(this, R.style.P1LargerTextLight);
        actionBar.setCenterView(textView);

        P1ActionBar.ListenerAction action = new ListenerAction(
                R.drawable.btn_contextual_close, this);
        actionBar.setLeftAction(action);
    }

    private void setupLocationClient() {
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(this);
        LocationClientOption options = new LocationClientOption();
        options.setOpenGps(START_GPS);
        options.setCoorType("gcj02");
        options.disableCache(ENABLE_LOCATION_CACHE);
        options.setScanSpan(20000);
        mLocationClient.setLocOption(options);
    }

    @Override
    public void onStart() {
        mLocationClient.start();

        super.onStart();
    }

    @Override
    public void onPause() {
        mLocationManager.removeUpdates(this);
        if (mBMapMan != null) {
            mBMapMan.stop();
        }
        mMapView.onPause();
        super.onPause();

    }

    @Override
    public void onResume() {
        mLocationManager.requestLocationUpdates(mProvider, 1000, 50, this);

        if (mBMapMan != null) {
            mBMapMan.start();
        }
        int locationResponse = mLocationClient.requestLocation();
        Log.d(TAG, "requestLocation " + locationResponse);
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onStop() {
        mLocationClient.stop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mMapView.destroy();

        if (mBMapMan != null) {
            mBMapMan.stop();
            mBMapMan = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mMapView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onVenueSelected(String venueId) {
        ShareIOSession io = mShare.getIOSession();
        String shareId = null;
        try {
            io.setVenueId(venueId);
            shareId = io.getId();
        } finally {
            io.close();
        }
        Intent intent = new Intent();
        intent.putExtra("shareId", shareId);
        intent.putExtra("venueId", venueId);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void contentChanged(Content content) {
        Log.d(TAG, "Content Changed ");
        if (!(content instanceof Share)) {
            return;
        }
        // TODO handle content changed
    }

    @Override
    public void onAction() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onGetNetworkState(int arg0) {
        Log.d(TAG, "Baidu IERROR " + arg0);

    }

    @Override
    public void onGetPermissionState(int arg0) {
        Log.d(TAG, "Baidu IERROR " + arg0);
    }

    public void addVenueLocations(List<BDLocation> locationList) {
        ItemizedOverlay<OverlayItem> locationOverlay = new ItemizedOverlay<OverlayItem>(
                getResources().getDrawable(R.drawable.ic_checkbox_selected),
                mMapView);
        mMapView.getOverlays().add(locationOverlay);
        GeoPoint geoPoint = null;
        for (BDLocation location : locationList) {
            geoPoint = Utils.BDToGeoPoint(location);
            locationOverlay.addItem(new OverlayItem(geoPoint, "A", "A"));
        }

    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        Log.d(TAG, "location latitude " + location.getLatitude());
        Log.d(TAG, "location latitude " + location.getLongitude());

        mLastLocation = location;

        setMyLocationOverlay();

    }

    private void setMyLocationOverlay() {
        MyLocationOverlay locationOverlay = new MyLocationOverlay(mMapView);
        LocationData locdata = new LocationData();
        locdata.latitude = mLastLocation.getLatitude();
        locdata.longitude = mLastLocation.getLongitude();
        locdata.direction = 0.0f;
        locationOverlay.setData(locdata);
        mMapView.getOverlays().add(locationOverlay);
        mMapView.refresh();
        mMapView.getController().animateTo(
                Utils.LocationDataToGeoPoint(locdata));
    }

    @Override
    public void onReceivePoi(BDLocation poiLocation) {
        Log.d(TAG, "onReceivePoi");

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mLastLocation == null) {
            mLastLocation = new BDLocation();
        }

        mFragment.setLocation(location.getLongitude(), location.getLatitude());
        mLastLocation.setLatitude(location.getLatitude());
        mLastLocation.setLongitude(location.getLongitude());
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}
