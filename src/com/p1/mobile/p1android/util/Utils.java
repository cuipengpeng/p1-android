package com.p1.mobile.p1android.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.CursorLoader;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.LocationData;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.p1.mobile.p1android.P1Application;
import com.p1.mobile.p1android.R;
import com.p1.mobile.p1android.content.ContentHandler;
import com.p1.mobile.p1android.content.IContentRequester;
import com.p1.mobile.p1android.content.Message;
import com.p1.mobile.p1android.content.Message.MessageIOSession;
import com.p1.mobile.p1android.content.logic.ReadMessage;
import com.p1.mobile.p1android.ui.fragment.BrowseMessagesFragment;
import com.p1.mobile.p1android.ui.fragment.LikerFragment;
import com.p1.mobile.p1android.ui.fragment.LikerFragment.LikeContentType;
import com.p1.mobile.p1android.ui.fragment.UserProfileFragment;
import com.p1.mobile.p1android.ui.phone.LikersWrapperActivity;
import com.p1.mobile.p1android.ui.phone.MessagesWrapperActivity;
import com.p1.mobile.p1android.ui.phone.UserProfileWrapperActivity;

public class Utils {
    public static final String TAG = Utils.class.getSimpleName();

    /**
     * @param context
     * @return
     */
    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null) {
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        }

        return false;
    }

    /**
     * Starts the new activity with an animation that scales up the selected
     * source view.
     * 
     * @param activity
     * @param intent
     * @param source
     */
    public static void startActivityWithThumbnailAnimation(Activity activity,
            Intent intent, View source) {

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeThumbnailScaleUpAnimation(source,
                        BitmapUtils.drawViewOntoBitmap(source), 0, 0);

        ActivityCompat.startActivity(activity, intent, options.toBundle());

    }

    /**
     * Return the current {@link P1Application}
     * 
     * @param context
     * @return The {@link P1Application} the given context is linked to
     */
    public static P1Application getP1Application(Context context) {
        // TODO fix fc nullpointer here
        return (P1Application) context.getApplicationContext();
    }

    public static void removeRequester(View v) {
        if (v != null && v.getTag() instanceof IContentRequester)
            ContentHandler.getInstance().removeRequester(
                    (IContentRequester) v.getTag());
    }

    public static void startConversationActivity(Context ctx, String id,
            boolean isFromUserProfile) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(BrowseMessagesFragment.CONVERSATION_ID, id);
        bundle.putBoolean(BrowseMessagesFragment.ISFROMPROFILE,
                isFromUserProfile);
        intent.putExtras(bundle);
        intent.setClass(ctx, MessagesWrapperActivity.class);
        ctx.startActivity(intent);
    }

    public static void startLikerActivity(Context ctx, String shareId,
            LikeContentType type) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(LikerFragment.SHARE_ID, shareId);
        bundle.putString(LikerFragment.CONTENT_TYPE, type.name());
        intent.putExtras(bundle);
        intent.setClass(ctx, LikersWrapperActivity.class);
        ctx.startActivity(intent);
    }

    public static void openProfile(Context ctx, String userId) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(UserProfileFragment.USER_ID_KEY, userId);
        intent.putExtras(bundle);
        intent.setClass(ctx, UserProfileWrapperActivity.class);
        ctx.startActivity(intent);
    }

    @TargetApi(11)
    public static void enableStrictMode() {
        StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
                .detectAll().penaltyLog();
        StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
                .detectAll().penaltyLog();

        if (Utils.hasHoneycomb()) {
            threadPolicyBuilder.penaltyFlashScreen();
        }
        StrictMode.setThreadPolicy(threadPolicyBuilder.build());
        StrictMode.setVmPolicy(vmPolicyBuilder.build());

    }

    /**
     * Check if the device runs Honeycomb (API level 11) or higher
     * 
     * @return
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Check if the device runs Honeycomb MR1 (API level 12) or higher
     * 
     * @return
     */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * Check if the device runs Honeycomb MR1 (API level 13) or higher
     * 
     * @return
     */
    public static boolean hasHoneycombMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
    }

    /**
     * Check if the device runs Jelly Bean or higher
     * 
     * @return
     */
    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * Check if the device has a camera
     * 
     * @return
     */
    public static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    /**
     * Check if the device has a camera
     * 
     * @return
     */
    public static boolean hasFlash(Context context) {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FLASH);
    }

    // ex: returns "1 minute ago"
    public static String getRelativeTime(Date date, Context ctx) {
        if (date == null) {
            return "UnKnown";
        }

        long timeDifference = (System.currentTimeMillis() - date.getTime()) / 1000;
        int PeriodStringID = 0;
        long diff = 0;

        if (timeDifference > 60 * 60 * 24 * 365) {
            // years diff
            diff = (int) (timeDifference / (60 * 60 * 24 * 365));
            PeriodStringID = R.string.date_year;

        } else if (timeDifference > 60 * 60 * 24 * 30) {
            // month diff
            diff = (int) (timeDifference / (60 * 60 * 24 * 30));
            PeriodStringID = R.string.date_month;
        } else if (timeDifference > 60 * 60 * 24 * 7) {
            // week diff
            diff = (int) (timeDifference / (60 * 60 * 24 * 7));
            PeriodStringID = R.string.date_week;
        } else if (timeDifference > 60 * 60 * 24) {
            // day diff
            diff = (int) (timeDifference / (60 * 60 * 24));
            PeriodStringID = R.string.date_day;
        } else if (timeDifference > 60 * 60) {
            // hours diff
            diff = (int) (timeDifference / (60 * 60));
            PeriodStringID = R.string.time_hour;

        } else if (timeDifference > 60) {
            // minutes diff
            diff = (int) (timeDifference / 60);
            PeriodStringID = R.string.time_minute;
        } else {
            return ctx.getString(R.string.time_just_now);
        }
        if (Locale.getDefault().equals(Locale.CHINA)) {
            return ctx.getString(R.string.timeline_string, diff,
                    ctx.getString(PeriodStringID), (diff > 1) ? "" : "");
        }
        return ctx.getString(R.string.timeline_string, diff,
                ctx.getString(PeriodStringID), (diff > 1) ? "s" : "");
    }

    // ex: returs "30s"
    // when browse members
    public static String getTimeDifference(Date date, Context ctx) {
        long timeDifference = (System.currentTimeMillis() - date.getTime()) / 1000;
        int diff = 0;
        int DifferenceStringID = 0;
        if (timeDifference > 60 * 60 * 24 * 365) {
            // years diff
            diff = (int) (timeDifference / (60 * 60 * 24 * 365));
            DifferenceStringID = R.string.time_year;
        } else if (timeDifference > 60 * 60 * 24 * 30) {
            // month diff
            diff = (int) (timeDifference / (60 * 60 * 24 * 30));
            DifferenceStringID = R.string.time_month;
        } else if (timeDifference > 60 * 60 * 24 * 7) {
            // week diff
            diff = (int) (timeDifference / (60 * 60 * 24 * 7));
            DifferenceStringID = R.string.date_week_short;
        } else if (timeDifference > 60 * 60 * 24) {
            // day diff
            DifferenceStringID = R.string.date_day_short;
            diff = (int) (timeDifference / (60 * 60 * 24));
        } else if (timeDifference > 60 * 60) {
            // hours diff
            diff = (int) (timeDifference / (60 * 60));
            DifferenceStringID = R.string.time_hour_short;
        } else if (timeDifference > 60) {
            // minutes diff
            diff = (int) (timeDifference / 60);
            DifferenceStringID = R.string.time_minute_short;
        } else {
            DifferenceStringID = R.string.time_second_short;
        }
        return diff + ctx.getString(DifferenceStringID);
    }

    private static final String SHORT_TIME_OF_DAY = "kk:mm";
    private static final String DATE_ABSOLUTE = "EEEE dd/MM";
    private static final String DATE_ABSOLUTE_WITH_YEAR = "EEEE dd/MM/yy";
    public static final SimpleDateFormat SHORT_TIME_OF_DAY_FORMAT = new SimpleDateFormat(
            SHORT_TIME_OF_DAY);
    public static final SimpleDateFormat ABSOLUTE_DATEFORMAT = new SimpleDateFormat(
            DATE_ABSOLUTE);
    public static final SimpleDateFormat ABSOLUTE_DATEFORMAT_WITH_YEAR = new SimpleDateFormat(
            DATE_ABSOLUTE_WITH_YEAR);

    public static String getTimeofDay(Date date) {
        if (date == null) {
            return "";
        }
        return SHORT_TIME_OF_DAY_FORMAT.format(date);
    }

    public static String getDate_Absolute(Date date) {
        if (date == null) {
            return "";
        }
        Calendar previousYear = Calendar.getInstance();
        previousYear.set(Calendar.YEAR, -1);
        if (date.before(previousYear.getTime())) {
            return ABSOLUTE_DATEFORMAT_WITH_YEAR.format(date);
        } else {
            return ABSOLUTE_DATEFORMAT.format(date);
        }
    }

    public static Comparator<String> comparator = new Comparator<String>() {
        @Override
        public int compare(String rhsMessageId, String lhsMessageId) {
            Date rhsDate = getCreationDate(rhsMessageId);
            Date lhsDate = getCreationDate(lhsMessageId);
            if (rhsDate == null && lhsDate == null)
                return 0;
            else if (rhsDate == null)
                return -1;
            else if (lhsDate == null)
                return 1;
            else
                return rhsDate.compareTo(lhsDate);
        }
    };

    public static ArrayList<ArrayList<String>> sortAndGroupMessages(
            ArrayList<String> list) {
        Collections.sort(list, comparator);
        ArrayList<ArrayList<String>> groupedList = new ArrayList<ArrayList<String>>();
        String previousT = null;
        int currentIndex = 0;
        for (String t : list) {
            if (groupedList.isEmpty()) {
                groupedList.add(new ArrayList<String>());
                groupedList.get(0).add(t);
            } else if (previousT != null
                    && getDate_Absolute(getCreationDate(t)).equals(previousT)) {
                groupedList.get(currentIndex).add(t);
            } else {
                groupedList.add(new ArrayList<String>());
                currentIndex++;
                groupedList.get(currentIndex).add(t);
            }
            Date creationDate = getCreationDate(t);
            if (creationDate != null) {
                previousT = getDate_Absolute(creationDate);
            } else
                previousT = null;
        }

        return groupedList;
    }

    private static Date getCreationDate(String mMessID) {
        Message mMess = ReadMessage.requestMessage(mMessID, null);
        if (mMess == null)
            return null;
        MessageIOSession io = mMess.getIOSession();
        Date date = null;
        try {
            date = io.getCreatedTime();
        } finally {
            io.close();
        }
        return date;
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static void copyToClipBoard(Context ctx, String string) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) ctx
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(string);
        } else {
            ClipboardManager clipboard = (ClipboardManager) ctx
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText("Återställningskod", string);
            clipboard.setPrimaryClip(clip);
        }

    }

    /**
     * Versioned method for getting screen dimentions. Uses Display.getSize() on
     * API 13 and higher and the now depricated Display.getWidth() and
     * Display.getHeight() on lower API versions.
     * 
     * @param outSize
     *            Point the Point object that should be updated with the screen
     *            dimentions
     * @param context
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static void getScreenSize(Point outSize, Context context) {

        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        Display display = wm.getDefaultDisplay();

        if (Utils.hasHoneycombMR2()) {
            display.getSize(outSize);
        } else {
            int width = display.getWidth();
            int height = display.getHeight();
            outSize.x = width;
            outSize.y = height;
        }
    }

    public static int dpToPx(Context ctx, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                ctx.getResources().getDisplayMetrics());

    }

    public static GeoPoint BDToGeoPoint(BDLocation bd) {
        return new GeoPoint((int) (bd.getLatitude() * 1E6),
                (int) (bd.getLongitude() * 1E6));
    }

    public static GeoPoint LocationDataToGeoPoint(LocationData ld) {
        return new GeoPoint((int) (ld.latitude * 1E6),
                (int) (ld.longitude * 1E6));
    }

    public static void removePreviousRequesters(View view) {
        if (view.getTag() instanceof IContentRequester) {
            ContentHandler.getInstance().removeRequester(
                    (IContentRequester) view.getTag());
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                removePreviousRequesters(group.getChildAt(i));
            }
        }
    }

    @SuppressWarnings("unused")
    public static CharSequence getEmoticons(Context ctx, CharSequence msg) {

        // FIXME add more smileys
        final HashMap<String, Integer> regexMap = new HashMap<String, Integer>();
        // regexMap.put(Pattern.quote(":)"), R.drawable.emot_1);

        SpannableStringBuilder builder = new SpannableStringBuilder(msg);
        @SuppressWarnings("rawtypes")
        Iterator it = regexMap.entrySet().iterator();
        while (it.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry pairs = (Map.Entry) it.next();
            Pattern mPattern = Pattern.compile((String) pairs.getKey(),
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = mPattern.matcher(msg);
            while (matcher.find()) {
                Drawable d = ctx.getResources().getDrawable(
                        ((Integer) pairs.getValue()));
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                Object[] spans = builder.getSpans(matcher.start(),
                        matcher.end(), ImageSpan.class);
                if (spans == null || spans.length == 0) {
                    builder.setSpan(new ImageSpan(d, ImageSpan.ALIGN_BASELINE),
                            matcher.start(), matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return builder;
    }

    /**
     * If affectedList contains oldId, it is replaced with the newId while
     * maintaining the same index.
     * 
     * @param affectedList
     * @param oldId
     * @param newId
     * @return true if an id was replaced
     */
    public static boolean checkAndReplaceId(List<String> affectedList,
            String oldId, String newId) {
        if (affectedList.contains(oldId)) {
            int index = affectedList.indexOf(oldId);
            affectedList.remove(index);
            affectedList.add(index, newId);
            Log.d(TAG, "contained id replaced from " + oldId + " to " + newId);
            return true;
        }
        return false;
    }

    public static String getRealPathFromURI(Context context,
            String contentUriString) {
        Uri uri = Uri.parse(contentUriString);
        if (uri.getScheme().toString().compareTo("content") == 0) {
            String[] proj = { MediaStore.Images.Media.DATA };
            CursorLoader loader = new CursorLoader(context, uri, proj, null,
                    null, null);
            Cursor cursor = loader.loadInBackground();
            int columnIndex = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            Log.d(TAG, "Return " + path);
            return path;
        } else if (uri.getScheme().compareTo("file") == 0) {
            Log.d(TAG, "Return " + uri.getPath());
            return uri.getPath();
        }

        return null;
    }

}
