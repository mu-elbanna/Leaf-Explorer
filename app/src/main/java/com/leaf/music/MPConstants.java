package com.leaf.music;

import android.content.Context;
import android.content.res.Resources;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MPConstants {

    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return  dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp){
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static List<File> referencedDirectoryList(Context context) {

        return new ArrayList<>(Arrays.asList(ContextCompat.getExternalFilesDirs(context,null)));
    }

    public static String buildPath(String[] splitPath, int count)
    {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; (i < count && i < splitPath.length); i++) {
            stringBuilder.append(File.separator);
            stringBuilder.append(splitPath[i]);
        }

        return stringBuilder.toString();
    }
}