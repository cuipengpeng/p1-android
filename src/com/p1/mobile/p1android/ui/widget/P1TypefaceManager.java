package com.p1.mobile.p1android.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseArray;

/**
 * Manager for the Helvetica Neue typefaces used in the P1 app
 * 
 * @author Viktor Nyblom
 * 
 */
public class P1TypefaceManager {

    /*
     * Available typeface attribute values
     * 
     * When updating with new values, remember to also update mTypefaceList's
     * init size and the typeface attribute in attrs.xml
     */
    private static final int HELVETICA_NEUE_LIGHT = 0;
    private static final int HELVETICA_NEUE_ULTRA_LIGHT = 1;
    private static final int HELVETICA_NEUE_MEDIUM = 2;
    private static final int HELVETICA_NEUE = 3;
    private static final int HELVETICA_NEUE_BOLD = 4;

    // Array cache of already created typefaces
    private static final SparseArray<Typeface> mTypefaceList = new SparseArray<Typeface>(
            5);

    /**
     * Get the requested typeface
     * 
     * @param context
     * @param typefaceValue
     * @return
     * @throws IllegalArgumentException
     */
    public static Typeface getTypeface(Context context, int typefaceValue)
            throws IllegalArgumentException {
        Typeface typeface = mTypefaceList.get(typefaceValue);
        if (typeface == null) {
            typeface = createTypeface(context, typefaceValue);
            mTypefaceList.put(typefaceValue, typeface);
        }

        return typeface;
    }

    private static Typeface createTypeface(Context context, int typefaceValue)
            throws IllegalArgumentException {
        Typeface typeface;
        switch (typefaceValue) {
        case HELVETICA_NEUE_LIGHT:
            typeface = Typeface.createFromAsset(context.getAssets(),
                    "fonts/HelveticaNeueLight.ttf");
            break;
        case HELVETICA_NEUE_ULTRA_LIGHT:
            typeface = Typeface.createFromAsset(context.getAssets(),
                    "fonts/HelveticaNeueUltraLight.ttf");
            break;

        case HELVETICA_NEUE_MEDIUM:
            typeface = Typeface.createFromAsset(context.getAssets(),
                    "fonts/HelveticaNeueMedium.ttf");
            break;
        case HELVETICA_NEUE:
            typeface = Typeface.createFromAsset(context.getAssets(),
                    "fonts/HelveticaNeue.ttf");
            break;
        case HELVETICA_NEUE_BOLD:
            typeface = Typeface.createFromAsset(context.getAssets(),
                    "fonts/HelveticaNeueBold.ttf");
            break;
        default:
            throw new IllegalArgumentException(
                    "Unknown typeface attribute value " + typefaceValue);
        }

        return typeface;
    }
}
