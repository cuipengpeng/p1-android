package com.p1.mobile.p1android.ui.adapters;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.io.model.SoftwareLicense;

public class AboutAdapter extends BaseAdapter {

	public AboutAdapter(Activity activity, List<SoftwareLicense> licenses){
		this.activity = activity;
		//this.softwares = softwares;
		this.licenses = licenses;
	}
	
	static class ViewHolder {
		private TextView name;
		private TextView license;
	}
	
	private Activity activity;
	private List<SoftwareLicense> licenses = new LinkedList<SoftwareLicense>();
	
	@Override
	public int getCount() {
		return licenses.size();
	}
	
	@Override
    public Object getItem(int position) {
    	return position;
    }

    @Override
    public long getItemId(int position) {
    	return position;
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		LayoutInflater inflator = activity.getLayoutInflater();
		final SoftwareLicense license = licenses.get(position);

        if (convertView == null) {
        	holder = new ViewHolder();
        	
        	convertView = inflator.inflate(R.layout.about_item, null); 
        	
        	holder.name = (TextView) convertView.findViewById(R.id.software_name);
        	holder.license = (TextView) convertView.findViewById(R.id.license_name);
        	
        	convertView.setTag(holder);
        } 
        else {
        	holder = (ViewHolder) convertView.getTag();
        }
        
        holder.name.setText(license.getSoftwareName());
        holder.license.setText(license.getLicenseName());
        
        return convertView;
	}
	
}
