package com.leaf.explorer.app;

import android.os.StrictMode;

import com.leaf.explorer.BuildConfig;

import java.lang.Thread.UncaughtExceptionHandler;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

public class FileExplorer extends MultiDexApplication
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());

        if (BuildConfig.DEBUG)
        {
            StrictMode.ThreadPolicy.Builder threadBuilder = new StrictMode.ThreadPolicy.Builder();
            threadBuilder.detectAll();
            threadBuilder.penaltyLog();
            StrictMode.setThreadPolicy(threadBuilder.build());

            StrictMode.VmPolicy.Builder vmBuilder = new StrictMode.VmPolicy.Builder();
            vmBuilder.detectAll();
            vmBuilder.penaltyLog();
            StrictMode.setVmPolicy(vmBuilder.build());
        }
    }

    public static class CustomExceptionHandler implements UncaughtExceptionHandler
    {
        private final UncaughtExceptionHandler defaultHandler;

        public CustomExceptionHandler()
        {
            this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        }

        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable)
        {

            defaultHandler.uncaughtException(thread, throwable);
        }
    }
}