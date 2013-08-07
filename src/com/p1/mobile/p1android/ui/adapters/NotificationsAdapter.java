package com.p1.mobile.p1android.ui.adapters;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.Content;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.NotificationList;
import com.p1.mobile.p1android.content.NotificationList.NotificationListIOSession;
import com.p1.mobile.p1android.content.logic.ReadNotification;
import com.p1.mobile.p1android.ui.helpers.NotificationInformation;

public class NotificationsAdapter extends BaseAdapter implements
        IContentRequester {
    private ArrayList<NotificationInformation> dataRequesters = new ArrayList<NotificationInformation>();
    private boolean runningRequest = false;
    private List<String> notificationIdList = new ArrayList<String>();

    public NotificationsAdapter() {
        requestUpdates(true);
    }

    public void onDestroy() {
        for (NotificationInformation data : dataRequesters)
            data.removeRequesters();
    }

    @Override
    public int getCount() {
        return notificationIdList.size();
    }

    @Override
    public Object getItem(int position) {
        return notificationIdList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        final NotificationInformation tag;
        if (convertView == null) {
            view = inflater.inflate(R.layout.notification_item, parent, false);
            tag = new NotificationInformation(view);
            dataRequesters.add(tag);
            view.setTag(tag);
        } else {
            view = convertView;
            tag = (NotificationInformation) view.getTag();
        }
        tag.setView(view);
        tag.requestUpdateForId(notificationIdList.get(position));
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tag.openContent();
            }
        });
        view.findViewById(R.id.noti_user_img).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tag.openProfile();
                    }
                });
        return view;
    }

    @Override
    public void contentChanged(Content content) {
        NotificationListIOSession ioSession = ((NotificationList) content)
                .getIOSession();
        try {
            if (ioSession.isValid()) {
                notificationIdList = ioSession.getNotificationIdList();
                notifyDataSetChanged();
            }
        } finally {
            ioSession.close();
        }
    }

    public void requestUpdates(boolean shouldRequestUpdates) {
        if (shouldRequestUpdates) {
            if (!runningRequest) {
                contentChanged(ReadNotification.requestNotificationList(this));
            }
        } else {
            ContentHandler.getInstance().removeRequester(this);
            onDestroy();
        }
        runningRequest = shouldRequestUpdates;
    }

}
