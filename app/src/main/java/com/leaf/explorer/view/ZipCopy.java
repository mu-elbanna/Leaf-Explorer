package com.leaf.explorer.view;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Environment;

import com.genonbeta.android.framework.io.DocumentFile;
import com.leaf.explorer.R;
import com.leaf.explorer.file_share.base.AppConfig;
import com.leaf.explorer.file_share.service.WorkerService;
import com.leaf.explorer.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ZipCopy {

    public ZipCopy(final Context context, final DocumentFile items, final Listener listener) {
        super();

        new WorkerService.RunningTask() {
            int mTotalPaste = 0;

            @Override
            public void onRun() {
                try {
                    DocumentFile path = DocumentFile.fromFile(context.getExternalCacheDir());
                    copy(items, path);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (listener != null)
                    listener.onCompleted(this, getService(), mTotalPaste);
            }

            private void copy(DocumentFile src, DocumentFile path) throws Exception {

               // String mime = Utils.mime(src.getUri().getPath());
                DocumentFile file = path.createFile(src.getUri().getPath(), src.getName());

                ContentResolver resolver = getService().getContentResolver();
                InputStream inputStream = resolver.openInputStream(src.getUri());
                OutputStream outputStream = resolver.openOutputStream(file.getUri());

                if (inputStream == null || outputStream == null)
                    throw new IOException("Failed to open streams to start copying");

                byte[] buffer = new byte[AppConfig.BUFFER_LENGTH_DEFAULT];
                int len = 0;
                long lastRead = System.currentTimeMillis();

                while (len != -1) {
                    if ((len = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, len);
                        outputStream.flush();

                        lastRead = System.currentTimeMillis();
                    }

                    if ((System.currentTimeMillis() - lastRead) > AppConfig.DEFAULT_SOCKET_TIMEOUT || getInterrupter().interrupted())
                        throw new Exception("Timed out or interrupted. Exiting!");
                }

                outputStream.close();
                inputStream.close();

                if (src.isFile())
                    mTotalPaste++;

                listener.onRunning(this, context, src);
                publishStatusText(file.getName());
            }

        }.setTitle(context.getString(R.string.text_pastingFilesOngoing))
                .setIconRes(R.drawable.ic_folder_white_24dp_static)
                .run(context);
    }

    public interface Listener {
        void onRunning(WorkerService.RunningTask runningTask, Context context, DocumentFile file);

        void onCompleted(WorkerService.RunningTask runningTask, Context context, int fileSize);
    }
}
