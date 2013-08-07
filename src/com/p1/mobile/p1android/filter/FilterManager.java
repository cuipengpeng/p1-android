package com.p1.mobile.p1android.filter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.p1.mobile.p1android.filter.impl.BWFilter;
import com.p1.mobile.p1android.filter.impl.ClairityFilter;
import com.p1.mobile.p1android.filter.impl.DuskFilter;
import com.p1.mobile.p1android.filter.impl.FocusFilter;
import com.p1.mobile.p1android.filter.impl.LightenFilter;
import com.p1.mobile.p1android.filter.impl.LomoFilter;
import com.p1.mobile.p1android.filter.impl.LuminanceFilter;
import com.p1.mobile.p1android.filter.impl.OriginalFilter;
import com.p1.mobile.p1android.filter.impl.PerceptionFilter;
import com.p1.mobile.p1android.filter.impl.RetroFilter;
import com.p1.mobile.p1android.filter.impl.SagaFilter;
import com.p1.mobile.p1android.filter.impl.SerenityFilter;
import com.p1.mobile.p1android.filter.impl.SummerFilter;
import com.p1.mobile.p1android.filter.impl.SunsightFilter;
import com.p1.mobile.p1android.filter.impl.TwilightFilter;

public class FilterManager {
    static final String TAG = FilterManager.class.getSimpleName();

    public static List<Filter> getAllFilters(Context context) {
        List<Filter> list = new ArrayList<Filter>();

        list.add(getOriginal(context));
        list.add(getClairity(context));
        list.add(getFocus(context));
        list.add(getSunsight(context));
        list.add(getPerception(context));
        list.add(getLomo(context));
        list.add(getSaga(context));
        list.add(getBW(context));
        list.add(getLuminance(context));
        list.add(getDusk(context));
        list.add(getSerenity(context));
        list.add(getSummer(context));
        list.add(getLighten(context));
        list.add(getRetro(context));
        list.add(getTwilight(context));

        return list;
    }

    public static Filter getFilter(FilterType filterType, Context context) {
        switch (filterType) {
        case CLAIRITY:
            return getClairity(context);
        case BW:
            return getBW(context);
        case DUSK:
            return getDusk(context);
        case FOCUS:
            return getFocus(context);
        case LOMO:
            return getLomo(context);
        case PERCEPTION:
            return getPerception(context);
        case SAGA:
            return getSaga(context);
        case SUNSIGHT:
            return getSunsight(context);
        case SERENITY:
            return getSerenity(context);
        case LIGHTEN:
            return getLighten(context);
        case LUMINANCE:
            return getLuminance(context);
        case SUMMER:
            return getSummer(context);
        case RETRO:
            return getRetro(context);
        case ORIGINAL:
            return getOriginal(context);
        case TWILIGHT:
            return getTwilight(context);
        default:
            return getOriginal(context);
        }
    }

    private static Filter getOriginal(Context context) {

        return new OriginalFilter(context);
    }

    private static Filter getClairity(Context context) {

        return new ClairityFilter(context);
    }

    private static Filter getBW(Context context) {
        return new BWFilter(context);
    }

    private static Filter getDusk(Context context) {
        return new DuskFilter(context);
    }

    private static Filter getFocus(Context context) {
        return new FocusFilter(context);
    }

    private static Filter getLomo(Context context) {
        return new LomoFilter(context);
    }

    private static Filter getPerception(Context context) {
        return new PerceptionFilter(context);
    }

    private static Filter getSaga(Context context) {
        return new SagaFilter(context);
    }

    private static Filter getSunsight(Context context) {
        return new SunsightFilter(context);
    }

    private static Filter getLighten(Context context) {
        return new LightenFilter(context);
    }

    private static Filter getSerenity(Context context) {
        return new SerenityFilter(context);
    }

    private static Filter getLuminance(Context context) {
        return new LuminanceFilter(context);
    }

    private static Filter getRetro(Context context) {
        return new RetroFilter(context);
    }

    private static Filter getSummer(Context context) {
        return new SummerFilter(context);
    }

    private static Filter getTwilight(Context context) {
        return new TwilightFilter(context);
    }
}
