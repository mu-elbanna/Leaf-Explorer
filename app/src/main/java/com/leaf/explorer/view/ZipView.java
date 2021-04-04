package com.leaf.explorer.view;

import android.content.Context;
import android.os.Environment;

import com.genonbeta.android.framework.io.DocumentFile;
import com.leaf.explorer.R;
import com.leaf.explorer.file_share.service.WorkerService;
import com.leaf.explorer.file_share.util.FileUtils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;

public class ZipView {

    public ZipView(final Context context, final DocumentFile items, boolean currentPath, final Listener listener){
        super();

        new WorkerService.RunningTask() {
            @Override
            public void onRun() {
                int mTotalZip = 0;

                if(currentPath) {
                    extraZipFile(context, items);
                } else {
                    extraZipFilePath(context, items);
                }

                if (listener != null)
                    listener.onCompleted(this, getService(), mTotalZip);
            }

            public void extraZipFile(Context context, DocumentFile file) {
                try {
                    new ZipFile(file.getUri().getPath()).extractAll(file.getParentFile().getUri().getPath());

                    listener.onRunning(this, context, file);
                    publishStatusText(file.getName());
                } catch (ZipException e) {
                    // Do nothing
                }
            }

            public void extraZipFilePath(Context context, DocumentFile file) {
                try {
                    new ZipFile(file.getUri().getPath()).extractAll(context.getCacheDir().getAbsolutePath());

                    listener.onRunning(this, context, file);
                    publishStatusText(file.getName());
                } catch (ZipException e) {
                    // Do nothing
                }
            }

        }.setTitle("Extracting")
                .setIconRes(R.drawable.ic_folder_white_24dp_static)
                .run(context);

    }


    public interface Listener {
        void onRunning(WorkerService.RunningTask runningTask, Context context, DocumentFile file);

        void onCompleted(WorkerService.RunningTask runningTask, Context context, int fileSize);
    }
}
