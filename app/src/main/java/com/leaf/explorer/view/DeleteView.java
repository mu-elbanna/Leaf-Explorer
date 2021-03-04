package com.leaf.explorer.view;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.genonbeta.android.framework.io.DocumentFile;
import com.leaf.explorer.R;
import com.leaf.explorer.file_share.service.WorkerService;
import com.leaf.explorer.file_share.util.FileUtils;

import java.util.List;

public class DeleteView {

    public DeleteView(final Context context, final List<Uri> selectedItemList, final DocumentFile items){
        super();

        new WorkerService.RunningTask() {

            int mTotalDeletion = 0;

            @Override
            public void onRun()
            {
                for (Uri currentUri : selectedItemList) {

                    try {
                        DocumentFile copy = FileUtils.fromUri(context, currentUri);
                        if (selectedItemList == null && items != null) {
                            delete(items);
                        } else if (selectedItemList != null && items == null) {
                            delete(copy);
                        }
                    } catch (Exception e) {
                        showMessage(e);
                    }
                }
            }

            private void showMessage(Exception e) {
                showMessage(e.getMessage());
            }

            private void showMessage(String message) {
                Toast.makeText(getService(), message, Toast.LENGTH_SHORT).show();
            }

            private void delete(DocumentFile file)
            {
                if (getInterrupter().interrupted())
                    return;

                boolean isDirectory = file.isDirectory();
                boolean isFile = file.isFile();

                if (isDirectory)
                    deleteDirectory(file);

                if (file.delete()) {
                    if (isFile)
                        mTotalDeletion++;

                    publishStatusText(file.getName());
                }
            }

            private void deleteDirectory(DocumentFile folder)
            {
                DocumentFile[] files = folder.listFiles();

                if (files != null)
                    for (DocumentFile anotherFile : files)
                        delete(anotherFile);
            }

        }.setTitle(context.getString(R.string.text_deletingFilesOngoing))
                .setIconRes(R.drawable.ic_folder_white_24dp_static)
                .run(context);

    }
}
