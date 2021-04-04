package com.leaf.explorer.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.genonbeta.android.framework.io.DocumentFile;
import com.leaf.explorer.R;
import com.leaf.explorer.file_share.service.WorkerService;

public class CustomDialog extends AlertDialog.Builder {

    public CustomDialog(final Context context, final DocumentFile items, final Listener listener) {
        super(context);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_rename, null);
        setView(view);

    }




    public interface Listener {

        void onRunning(WorkerService.RunningTask runningTask, Context context, DocumentFile file);

        void onCompleted(WorkerService.RunningTask runningTask, Context context, int fileSize);
    }
}
