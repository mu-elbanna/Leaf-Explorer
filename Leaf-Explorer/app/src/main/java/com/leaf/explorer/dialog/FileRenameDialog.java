package com.leaf.explorer.dialog;

import android.content.Context;

import com.genonbeta.android.framework.io.DocumentFile;
import com.leaf.explorer.R;
import com.leaf.explorer.fragments.explorer.adapters.StorageFileAdapter;
import com.leaf.explorer.file_share.dialog.AbstractSingleTextInputDialog;
import com.leaf.explorer.file_share.model.FileShortcutObject;
import com.leaf.explorer.file_share.model.WritablePathObject;
import com.leaf.explorer.file_share.service.WorkerService;
import com.leaf.explorer.file_share.util.AppUtils;
import com.leaf.explorer.file_share.util.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class FileRenameDialog<T extends StorageFileAdapter.GenericFileHolder> extends AbstractSingleTextInputDialog
{
    public static final String TAG = FileRenameDialog.class.getSimpleName();
    public static final int JOB_RENAME_FILES = 0;

    private final List<T> mItemList = new ArrayList<>();

    public FileRenameDialog(final Context context, List<T> itemList, final OnFileRenameListener renameListener)
    {
        super(context);

        mItemList.addAll(itemList);

        setTitle(mItemList.size() > 1
                ? R.string.text_renameMultipleItems
                : R.string.text_rename);

        getEditText().setText(mItemList.size() > 1
                ? "%d"
                : mItemList.get(0).fileName);

        setOnProceedClickListener(R.string.butn_rename, dialog -> {
            final String renameTo = getEditText().getText().toString();

            if (getItemList().size() == 1
                    && renameFile(getItemList().get(0), renameTo, renameListener)) {
                if (renameListener != null)
                    renameListener.onFileRenameCompleted(getContext());
                return true;
            }

            try {
                String.format(renameTo, getItemList().size());
            } catch (Exception e) {
                return false;
            }

            new WorkerService.RunningTask()
            {
                @Override
                protected void onRun()
                {
                    int fileId = 0;

                    for (T fileHolder : getItemList()) {
                        publishStatusText(fileHolder.friendlyName);

                        String ext = FileUtils.getFileFormat(fileHolder.file.getName());
                        ext = ext != null ? String.format(".%s", ext) : "";

                        renameFile(fileHolder, String.format("%s%s", String.format(renameTo, fileId), ext), renameListener);
                        fileId++;
                    }

                    if (renameListener != null)
                        renameListener.onFileRenameCompleted(getService());
                }
            }.setTitle(context.getString(R.string.text_renameMultipleItems))
                    .setIconRes(R.drawable.ic_compare_arrows_white_24dp_static)
                    .run(context);

            return true;
        });
    }

    public List<T> getItemList()
    {
        return mItemList;
    }

    public boolean renameFile(T holder, String renameTo, OnFileRenameListener renameListener)
    {
        try {
            if (holder instanceof StorageFileAdapter.ShortcutDirectoryHolder) {
                FileShortcutObject object = ((StorageFileAdapter.ShortcutDirectoryHolder) holder).getShortcutObject();

                if (object != null) {
                    object.title = renameTo;
                    AppUtils.getDatabase(getContext()).publish(object);
                }
            } else if (holder instanceof StorageFileAdapter.WritablePathHolder) {
                WritablePathObject object = ((StorageFileAdapter.WritablePathHolder) holder).pathObject;

                if (object != null) {
                    object.title = renameTo;
                    AppUtils.getDatabase(getContext()).publish(object);
                }
            } else if (holder.file.canWrite() && holder.file.renameTo(renameTo)) {
                if (renameListener != null)
                    renameListener.onFileRename(holder.file, renameTo);

                return true;
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    public interface OnFileRenameListener
    {
        void onFileRename(DocumentFile file, String displayName);

        void onFileRenameCompleted(Context context);
    }
}
