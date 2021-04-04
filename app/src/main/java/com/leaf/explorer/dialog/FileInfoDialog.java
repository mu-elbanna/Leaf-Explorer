package com.leaf.explorer.dialog;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import com.leaf.explorer.fragments.explorer.adapters.StorageFileAdapter;
import com.leaf.explorer.file_share.util.FileUtils;

import java.util.List;

public class FileInfoDialog extends AlertDialog.Builder {
    public FileInfoDialog(final Context context, final List<StorageFileAdapter.GenericFileHolder> items) {
        super(context);

        setTitle("Info");

        for (StorageFileAdapter.GenericFileHolder item : items) {
            if (item.file != null)
            setMessage(item.file.getName() + "/n/n" + item.file.lastModified() + "/n/n" + FileUtils.sizeExpression(item.file.length(), false));
        }

    }
}
