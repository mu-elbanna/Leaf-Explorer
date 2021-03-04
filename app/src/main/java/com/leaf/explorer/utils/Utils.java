package com.leaf.explorer.utils;

import android.webkit.MimeTypeMap;
import android.content.res.Resources;

public class Utils {

    public static String mime(String URI) {
        String type = null;
        String extention = MimeTypeMap.getFileExtensionFromUrl(URI);
        if (extention != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extention);
        }
        return type;
    }

    //----------------------------------------------------------------------------------------------

    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return  dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp){
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }


}
