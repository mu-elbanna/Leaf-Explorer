package com.leaf.explorer.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.genonbeta.android.framework.io.DocumentFile;

import org.monora.uprotocol.client.android.R;
import org.monora.uprotocol.client.android.model.FileModel;

import java.io.File;
import java.util.ArrayList;

public class Utils {

    public static String formatLeftExact(Context context, long diff) {
        String str = "";

        int diffSeconds = (int) (diff / 1000 % 60);
        int diffMinutes = (int) (diff / (60 * 1000) % 60);
        int diffHours = (int) (diff / (60 * 60 * 1000) % 24);
        int diffDays = (int) (diff / (24 * 60 * 60 * 1000));

        if (diffDays > 0)
            str += " " + context.getResources().getQuantityString(R.plurals.days, diffDays, diffDays);

        if (diffHours > 0)
            str += " " + context.getResources().getQuantityString(R.plurals.hours, diffHours, diffHours);

        if (diffMinutes > 0)
            str += " " + context.getResources().getQuantityString(R.plurals.minutes, diffMinutes, diffMinutes);

        if (diffDays == 0 && diffHours == 0 && diffMinutes == 0 && diffSeconds > 0)
            str += " " + context.getResources().getQuantityString(R.plurals.seconds, diffSeconds, diffSeconds);

        return str.trim();
    }

    public static class MIMEGrouper
    {
        public static final String TYPE_GENERIC = "*";

        private String mMajor;
        private String mMinor;
        private boolean mLocked;

        public MIMEGrouper()
        {

        }

        public boolean isLocked()
        {
            return mLocked;
        }

        public String getMajor()
        {
            return mMajor == null ? TYPE_GENERIC : mMajor;
        }

        public String getMinor()
        {
            return mMinor == null ? TYPE_GENERIC : mMinor;
        }

        public void process(String mimeType)
        {
            if (mimeType == null || mimeType.length() < 3 || !mimeType.contains(File.separator))
                return;

            String[] splitMIME = mimeType.split(File.separator);

            process(splitMIME[0], splitMIME[1]);
        }

        public void process(String major, String minor)
        {
            if (mMajor == null || mMinor == null) {
                mMajor = major;
                mMinor = minor;
            } else if (getMajor().equals(TYPE_GENERIC))
                mLocked = true;
            else if (!getMajor().equals(major)) {
                mMajor = TYPE_GENERIC;
                mMinor = TYPE_GENERIC;

                mLocked = true;
            } else if (!getMinor().equals(minor)) {
                mMinor = TYPE_GENERIC;
            }
        }

        @Override
        public String toString()
        {
            return getMajor() + File.separator + getMinor();
        }
    }

}
