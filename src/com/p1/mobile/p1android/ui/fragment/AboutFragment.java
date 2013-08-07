package com.p1.mobile.p1android.ui.fragment;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.io.model.SoftwareLicense;
import com.p1.mobile.p1android.ui.adapters.AboutAdapter;
import com.p1.mobile.p1android.ui.dialog.SoftwareLicenseDialogFragment;

public class AboutFragment extends ListFragment {
	
	List<SoftwareLicense> licenses = new LinkedList<SoftwareLicense>();
	private AssetManager assetManager;
	private String noticeDir = "notices/";
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		assetManager = getActivity().getAssets();
		
		initLicenseList();
		
		ListView listView = getListView();
		setListAdapter(new AboutAdapter(getActivity(), licenses));
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				SoftwareLicense license = licenses.get(position);
				try {
					if(license.getLicenseFile() != null) {
						SoftwareLicenseDialogFragment dialog = new SoftwareLicenseDialogFragment();
						dialog.setContent(assetManager.open(noticeDir + license.getLicenseFile(), AssetManager.ACCESS_BUFFER));
						dialog.show(getFragmentManager(), getString(R.string.license));
					}
				} catch (IOException e) {
					Toast.makeText(getActivity(), getString(R.string.error_opening_license), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		
	}
	
	private void initLicenseList() {
		
		licenses.add(
				new SoftwareLicense("ACRA - Application Crash Report for Android", 
									"https://github.com/ACRA/acra", 
									getString(R.string.apache_2_0_license), 
									"acra"
				)
		);
		
		licenses.add(
				new SoftwareLicense("Android ViewBadger", 
									"https://github.com/jgilfelt/android-viewbadger", 
									getString(R.string.apache_2_0_license), 
									null
				)
		);
		
		licenses.add(
				new SoftwareLicense("Apache Commons", 
									"http://commons.apache.org/", 
									getString(R.string.apache_2_0_license), 
									null
				)
		);
		
		licenses.add(
				new SoftwareLicense("GSON", 
									"https://code.google.com/p/google-gson/", 
									getString(R.string.apache_2_0_license), 
									null
				)
		);
		
		licenses.add(
				new SoftwareLicense("JSON", 
									"http://www.json.org/", 
									"JSON License", 
									"json"
				)
		);
		
		licenses.add(
				new SoftwareLicense("ORMLite", 
									"http://www.json.org/", 
									getString(R.string.isc_license), 
									"ormlite"
				)
		);
		
		licenses.add(
				new SoftwareLicense("RoboSpice", 
									"http://www.json.org/", 
									getString(R.string.apache_2_0_license), 
									null
				)
		);
		
		licenses.add(
				new SoftwareLicense("Spring Framework", 
									"http://www.springsource.org/spring-framework", 
									getString(R.string.apache_2_0_license), 
									null
				)
		);
		
		licenses.add(
				new SoftwareLicense("Universal Image Loader", 
									"http://www.springsource.org/spring-framework", 
									getString(R.string.apache_2_0_license), 
									null
				)
		);
	
	}

}
