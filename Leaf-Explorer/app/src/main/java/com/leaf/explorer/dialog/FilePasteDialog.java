package com.leaf.explorer.dialog;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.genonbeta.android.framework.io.DocumentFile;
import com.leaf.explorer.R;
import com.leaf.explorer.file_share.base.AppConfig;
import com.leaf.explorer.file_share.service.WorkerService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import com.leaf.explorer.file_share.util.FileUtils;

public class FilePasteDialog extends AlertDialog.Builder {

    public FilePasteDialog(final Context context, final List<Uri> selectedItemList, final DocumentFile destination, final Listener listener) {
        super(context);

        setTitle("Move");
        setMessage(getContext().getResources().getQuantityString(R.plurals.ques_moveFile, selectedItemList.size(), selectedItemList.size()));

        setNegativeButton(R.string.butn_cancel, null);
        setPositiveButton("Move", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int p2) {
                        new WorkerService.RunningTask() {
                            int mTotalPaste = 0;

                            @Override
                            public void onRun() {

                                for (Uri currentUri : selectedItemList) {

                                    try {
                                        DocumentFile copy = FileUtils.fromUri(context, currentUri);

                                        copydoc(copy, destination);
                                    } catch (Exception e) {
                                        showMessage(e);
                                    }
                                }

                                if (listener != null)
                                    listener.onCompleted(this, getService(), mTotalPaste);
                            }

                            private void showMessage(Exception e) {
                                showMessage(e.getMessage());
                            }

                            private void showMessage(String message) {
                                Toast.makeText(getService(), message, Toast.LENGTH_SHORT).show();
                            }

                            private void copydoc(DocumentFile src, DocumentFile path) throws Exception {
                                try {

                                    if (src.isDirectory()) {
                                        DocumentFile createdDir = path.createDirectory(src.getName());

                                        DocumentFile[] files = src.listFiles();

                                        for (DocumentFile file : files) {
                                            try {
                                                copydoc(file, createdDir);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        copy(src, path);
                                    }

                                } catch (Exception e) {
                                    throw new Exception(String.format("Error copying %s", "Files"));
                                }
                            }

                            private void copy(DocumentFile src, DocumentFile path) throws Exception {

                                //  String mime = mime(src.getUri().getPath());
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

                                listener.onFilePaste(this, getContext(), src);
                                publishStatusText(file.getName());
                            }

                            //----------------------------------------------------------------------------------------------

                            public String mime(String URI) {
                                String type = null;
                                String extention = MimeTypeMap.getFileExtensionFromUrl(URI);
                                if (extention != null) {
                                    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extention);
                                }
                                return type;
                            }


                        }.setTitle(getContext().getString(R.string.text_pastingFilesOngoing))
                                .setIconRes(R.drawable.ic_folder_white_24dp_static)
                                .run(context);
                    }
                }
        );
    }

    public interface Listener {
        void onFilePaste(WorkerService.RunningTask runningTask, Context context, DocumentFile file);

        void onCompleted(WorkerService.RunningTask runningTask, Context context, int fileSize);
    }
}
