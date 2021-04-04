package com.leaf.explorer.fragments.explorer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.leaf.explorer.activity.LeafActivity;
import com.leaf.explorer.dialog.FileCopyDialog;
import com.leaf.explorer.fragments.explorer.adapters.StorageFileAdapter;
import com.leaf.explorer.app.AppActivity;
import com.leaf.explorer.dialog.FilePasteDialog;
import com.leaf.explorer.file_share.adapter.GroupEditableListFragment;
import com.leaf.explorer.dialog.FileDeletionDialog;
import com.leaf.explorer.dialog.FileRenameDialog;
import com.leaf.explorer.file_share.service.WorkerService;
import com.leaf.explorer.file_share.util.NotReadyException;
import com.leaf.explorer.file_share.util.AppUtils;
import com.leaf.explorer.file_share.util.FileUtils;
import com.leaf.explorer.R;
import com.leaf.explorer.file_share.activity.ChangeStoragePathActivity;
import com.leaf.explorer.file_share.db.AccessDatabase;
import com.leaf.explorer.file_share.model.FileShortcutObject;
import com.leaf.explorer.file_share.model.WritablePathObject;
import com.leaf.explorer.file_share.widget.GroupEditableListAdapter;
import com.genonbeta.android.framework.io.DocumentFile;
import com.genonbeta.android.framework.io.LocalDocumentFile;
import com.genonbeta.android.framework.widget.PowerfulActionMode;
import com.google.android.material.snackbar.Snackbar;
import com.leaf.explorer.view.ActionModeCallback;
import com.leaf.explorer.view.DeleteView;
import com.leaf.explorer.view.ZipCopy;
import com.leaf.explorer.view.ZipView;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FileListFragment
        extends GroupEditableListFragment<StorageFileAdapter.GenericFileHolder, GroupEditableListAdapter.GroupViewHolder, StorageFileAdapter>
{
  public static final String TAG = FileListFragment.class.getSimpleName();

  static LeafActivity leafActivity;

  public final static int REQUEST_WRITE_ACCESS = 264;

  public final static String ACTION_FILE_LIST_CHANGED = "com.leaf.explorer.action.FILE_LIST_CHANGED";
  public final static String EXTRA_FILE_PARENT = "extraPath";
  public final static String EXTRA_FILE_NAME = "extraFile";
  public final static String EXTRA_FILE_LOCATION = "extraFileLocation";

  public final static String ARG_SELECT_BY_CLICK = "argSelectByClick";

  private boolean mSelectByClick = false;
  private DocumentFile mLastKnownPath;
  private final IntentFilter mIntentFilter = new IntentFilter();
  private MediaScannerConnection mMediaScanner;
  private OnPathChangedListener mPathChangedListener;
  private final BroadcastReceiver mReceiver = new BroadcastReceiver()
  {
    private Snackbar mUpdateSnackbar;

    @Override
    public void onReceive(Context context, Intent intent)
    {
      if ((ACTION_FILE_LIST_CHANGED.equals(intent.getAction()) && intent.hasExtra(EXTRA_FILE_PARENT))) {
        try {
          Object parentUri = intent.getParcelableExtra(EXTRA_FILE_PARENT);

          if (parentUri == null && getAdapter().getPath() == null) {
            refreshList();
          } else if (parentUri != null) {
            final DocumentFile parentFile = FileUtils.fromUri(getContext(), (Uri) parentUri);

            if (getAdapter().getPath() != null && parentFile.getUri().equals(getAdapter().getPath().getUri()))
              refreshList();
            else if (intent.hasExtra(EXTRA_FILE_NAME)) {
              if (mUpdateSnackbar == null)
                mUpdateSnackbar = createSnackbar(R.string.mesg_newFilesReceived);

              mUpdateSnackbar
                      .setText(getString(R.string.mesg_fileReceived, intent.getStringExtra(EXTRA_FILE_NAME)))
                      .setAction(R.string.butn_show, v -> goPath(parentFile))
                      .show();
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else if (getAdapter().getPath() == null
              && AccessDatabase.ACTION_DATABASE_CHANGE.equals(intent.getAction())
              && (AccessDatabase.TABLE_WRITABLEPATH.equals(intent.getStringExtra(AccessDatabase.EXTRA_TABLE_NAME))
              || AccessDatabase.TABLE_FILEBOOKMARK.equals(intent.getStringExtra(AccessDatabase.EXTRA_TABLE_NAME))))
        refreshList();
    }
  };

  public boolean handleEditingAction(int id, final FileListFragment fragment, List<StorageFileAdapter.GenericFileHolder> selectedItemList) {

    final StorageFileAdapter adapter = fragment.getAdapter();

    if (id == R.id.action_mode_file_delete) {
      new FileDeletionDialog(fragment.getContext(), selectedItemList, new FileDeletionDialog.Listener() {
        @Override
        public void onFileDeletion(WorkerService.RunningTask runningTask, Context context, DocumentFile file) {
          fragment.scanFile(file);
        }

        @Override
        public void onCompleted(WorkerService.RunningTask runningTask, Context context, int fileSize) {
          context.sendBroadcast(new Intent(ACTION_FILE_LIST_CHANGED)
                  .putExtra(EXTRA_FILE_PARENT, adapter.getPath() == null
                          ? null
                          : adapter.getPath().getUri()));
        }
      }).show();
    } else if (id == R.id.action_mode_file_rename) {
      new FileRenameDialog<>(fragment.getContext(), selectedItemList, new FileRenameDialog.OnFileRenameListener() {
        @Override
        public void onFileRename(DocumentFile file, String displayName) {
          fragment.scanFile(file);
        }

        @Override
        public void onFileRenameCompleted(Context context) {
          context.sendBroadcast(new Intent(ACTION_FILE_LIST_CHANGED)
                  .putExtra(EXTRA_FILE_PARENT, adapter.getPath() == null
                          ? null
                          : adapter.getPath().getUri()));
        }
      }).show();

    } else if (id == R.id.action_mode_file_details) {
      MaterialDialog.Builder bulder = new MaterialDialog.Builder(requireContext())
              .title("Properties")
              .customView(R.layout.file_details, true)
              .icon(getResources().getDrawable(R.mipmap.ic_launcher))
              .limitIconToDefaultSize();

      MaterialDialog materialDialog = bulder.build();

      for (StorageFileAdapter.GenericFileHolder item : selectedItemList) {
        if (item.file != null) {
          TextView fileName = (TextView) materialDialog.findViewById(R.id.fileName);
          TextView fileSize = (TextView) materialDialog.findViewById(R.id.fileSize);
          TextView filePath = (TextView) materialDialog.findViewById(R.id.filePath);
          TextView fileLastModified = (TextView) materialDialog.findViewById(R.id.fileLastModified);

          SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US);

          fileName.setText("Name :" + item.file.getName());
          fileSize.setText("Size :" + FileUtils.sizeExpression(item.file.length(), false));
          filePath.setText("Path :" + item.file.getParentFile().getUri().getPath());
          fileLastModified.setText("Last Modified :" + sdf.format(item.file.lastModified()));
        }

      }
      materialDialog.show();
    } else if (id == R.id.action_mode_file_copy) {
      Snackbar mUpdateSnackbar;
      String paste = "copied";
      String message = String.format(Locale.getDefault(), "%d items waiting to be %s", selectedItemList.size(), paste);

      final List<Uri> copiedItems = new ArrayList<>();

      for (StorageFileAdapter.GenericFileHolder item : selectedItemList)
        if (item.file != null)
          copiedItems.add(item.file.getUri());

      View.OnClickListener onClickListener = v -> {

        new FileCopyDialog(fragment.getContext(), copiedItems, getAdapter().getPath(), new FileCopyDialog.Listener() {
          @Override
          public void onFilePaste(WorkerService.RunningTask runningTask, Context context, DocumentFile file) {
            fragment.scanFile(file);
          }

          @Override
          public void onCompleted(WorkerService.RunningTask runningTask, Context context, int fileSize) {
            context.sendBroadcast(new Intent(ACTION_FILE_LIST_CHANGED)
                    .putExtra(EXTRA_FILE_PARENT, adapter.getPath() == null
                            ? null
                            : adapter.getPath().getUri()));

          }
        }).show();
      };

      mUpdateSnackbar = createSnackbar(R.string.mesg_newFilesReceived);
      mUpdateSnackbar
              .setDuration(Snackbar.LENGTH_INDEFINITE)
              .setText(message)
              .setAction("Copy", onClickListener)
              .show();

    } else if (id == R.id.action_mode_file_move) {
      Snackbar mUpdateSnackbar;
      String paste = "Moved";
      String message = String.format(Locale.getDefault(), "%d items waiting to be %s", selectedItemList.size(), paste);

      final List<Uri> copiedItems = new ArrayList<>();

      for (StorageFileAdapter.GenericFileHolder item : selectedItemList)
        if (item.file != null)
          copiedItems.add(item.file.getUri());

      View.OnClickListener onClickListener = v -> {

        new FilePasteDialog(fragment.getContext(), copiedItems, getAdapter().getPath(), new FilePasteDialog.Listener() {
          @Override
          public void onFilePaste(WorkerService.RunningTask runningTask, Context context, DocumentFile file) {
            fragment.scanFile(file);
          }

          @Override
          public void onCompleted(WorkerService.RunningTask runningTask, Context context, int fileSize) {
            context.sendBroadcast(new Intent(ACTION_FILE_LIST_CHANGED)
                    .putExtra(EXTRA_FILE_PARENT, adapter.getPath() == null
                            ? null
                            : adapter.getPath().getUri()));

            new DeleteView(context, copiedItems, null);
          }

        }).show();
      };

      mUpdateSnackbar = createSnackbar(R.string.mesg_newFilesReceived);
      mUpdateSnackbar
              .setDuration(Snackbar.LENGTH_INDEFINITE)
              .setText(message)
              .setAction("Move", onClickListener)
              .show();
    } else
      return true;

    return false;
  }

  private Context context() {
    Context context = getContext();

    if (context != null) {
      return context;
    } else {
      Context fragmentActivity = getActivity();

      if (fragmentActivity != null) {
        return fragmentActivity;
      } else {
        return leafActivity;
      }
    }
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    leafActivity = (LeafActivity) getContext();

    setFilteringSupported(true);
    setDefaultOrderingCriteria(StorageFileAdapter.MODE_SORT_ORDER_ASCENDING);
    setDefaultSortingCriteria(StorageFileAdapter.MODE_SORT_BY_NAME);
    setDefaultGroupingCriteria(StorageFileAdapter.MODE_GROUP_BY_DEFAULT);

    setDefaultSelectionCallback(new SelectionCallback(this));

    if (getArguments() != null) {
      if (getArguments().containsKey(ARG_SELECT_BY_CLICK))
        mSelectByClick = getArguments().getBoolean(ARG_SELECT_BY_CLICK, false);
    }
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    setEmptyImage(R.drawable.ic_folder_white_24dp);
    setEmptyText(getString(R.string.text_listEmptyFiles));
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);

    mMediaScanner = new MediaScannerConnection(getActivity(), null);

    mIntentFilter.addAction(ACTION_FILE_LIST_CHANGED);
    mIntentFilter.addAction(AccessDatabase.ACTION_DATABASE_CHANGE);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == AppActivity.RESULT_OK)
      if (requestCode == REQUEST_WRITE_ACCESS) {
        Uri pathUri = data.getData();

        if (Build.VERSION.SDK_INT >= 21 && pathUri != null && getContext() != null) {
          getContext().getContentResolver().takePersistableUriPermission(pathUri,
                  Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

          try {
            DocumentFile documentFile = DocumentFile.fromUri(getContext(), pathUri, true);
            AppUtils.getDatabase(getContext()).publish(
                    new WritablePathObject(documentFile.getName(), pathUri));
            goPath(null);
          } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), R.string.mesg_somethingWentWrong, Toast.LENGTH_SHORT).show();
          }
        }
      }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.actions_file_list, menu);

    MenuItem mountDirectory = menu.findItem(R.id.actions_file_list_mount_directory);

    if (Build.VERSION.SDK_INT >= 21 && mountDirectory != null)
      mountDirectory.setVisible(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    int id = item.getItemId();

    if (id == R.id.actions_file_list_mount_directory) {
      requestMountStorage();
    } else if (id == R.id.actions_file_list_toggle_shortcut
            && getAdapter().getPath() != null) {
      shortcutItem(new FileShortcutObject(getAdapter().getPath().getName(), getAdapter().getPath().getUri()));
    } else
      return super.onOptionsItemSelected(item);

    return true;
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu)
  {
    super.onPrepareOptionsMenu(menu);

    MenuItem shortcutMenuItem = menu.findItem(R.id.actions_file_list_toggle_shortcut);

    if (shortcutMenuItem != null) {
      boolean hasPath = getAdapter().getPath() != null;
      shortcutMenuItem.setEnabled(hasPath);

      if (hasPath)
        try {
          AppUtils.getDatabase(getContext()).reconstruct(new FileShortcutObject(getAdapter().getPath().getUri()));
          shortcutMenuItem.setTitle(R.string.butn_removeShortcut);
        } catch (Exception e) {
          shortcutMenuItem.setTitle(R.string.butn_addShortcut);
        }
    }
  }

  @Override
  public StorageFileAdapter onAdapter()
  {
    final AppUtils.QuickActions<GroupEditableListAdapter.GroupViewHolder> quickActions = clazz -> {
      if (!clazz.isRepresentative()) {
        registerLayoutViewClicks(clazz);

        clazz.getView().findViewById(R.id.layout_image).setOnClickListener(v -> {
          if (getSelectionConnection() != null)
            getSelectionConnection().setSelected(clazz.getAdapterPosition());
        });

        clazz.getView().findViewById(R.id.menu).setOnClickListener(v -> {
          final StorageFileAdapter.GenericFileHolder fileHolder = getAdapter().getList().get(clazz.getAdapterPosition());
          final FileShortcutObject shortcutObject;
          boolean isFile = fileHolder.file.isFile();
          // boolean isDirectory = fileHolder.file.isDirectory();
          boolean canChange = fileHolder.file.canWrite()
                  || fileHolder instanceof StorageFileAdapter.ShortcutDirectoryHolder;
          boolean canRead = fileHolder.file.canRead();
          boolean isSensitive = fileHolder instanceof StorageFileAdapter.StorageHolderImpl
                  || fileHolder instanceof StorageFileAdapter.ShortcutDirectoryHolder;
          PopupMenu popupMenu = new PopupMenu(getContext(), v);
          Menu menuItself = popupMenu.getMenu();

          if (fileHolder instanceof StorageFileAdapter.ShortcutDirectoryHolder)
            shortcutObject = ((StorageFileAdapter.ShortcutDirectoryHolder) fileHolder).getShortcutObject();
          else if (fileHolder.file.isDirectory()) {
            FileShortcutObject testedObject;

            try {
              testedObject = new FileShortcutObject(fileHolder.file.getUri());
              AppUtils.getDatabase(getContext()).reconstruct(testedObject);
            } catch (Exception e) {
              testedObject = null;
            }

            shortcutObject = testedObject;
          } else
            shortcutObject = null;

          popupMenu.getMenuInflater().inflate(R.menu.action_mode_file, menuItself);

          menuItself.findItem(R.id.action_mode_file_open).setVisible(canRead && isFile);
          menuItself.findItem(R.id.action_mode_file_rename).setEnabled(canChange);
          menuItself.findItem(R.id.action_mode_file_details).setVisible(canRead && isFile);
          menuItself.findItem(R.id.action_mode_file_delete).setEnabled(canChange && !isSensitive);
          menuItself.findItem(R.id.action_mode_file_show)
                  .setVisible(fileHolder instanceof StorageFileAdapter.RecentFileHolder);
          menuItself.findItem(R.id.action_mode_file_change_save_path)
                  .setVisible(FileUtils.getApplicationDirectory(getContext()).getUri()
                          .equals(fileHolder.file.getUri()));
          menuItself.findItem(R.id.action_mode_file_eject_directory)
                  .setVisible(fileHolder instanceof StorageFileAdapter.WritablePathHolder);
          menuItself.findItem(R.id.action_mode_file_toggle_shortcut)
                  .setVisible(!isFile)
                  .setTitle(shortcutObject == null
                          ? R.string.butn_addShortcut
                          : R.string.butn_removeShortcut);

          popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            ArrayList<StorageFileAdapter.GenericFileHolder> generateSelectionList = new ArrayList<>();
            generateSelectionList.add(fileHolder);

            if (id == R.id.action_mode_file_open) {
              performLayoutClickOpen(clazz);
            } else if (id == R.id.action_mode_file_show
                    && fileHolder.file.getParentFile() != null) {
              goPath(fileHolder.file.getParentFile());
            } else if (id == R.id.action_mode_file_eject_directory
                    && fileHolder instanceof StorageFileAdapter.WritablePathHolder) {
              AppUtils.getDatabase(getContext()).remove(((StorageFileAdapter.WritablePathHolder) fileHolder).pathObject);
            } else if (id == R.id.action_mode_file_toggle_shortcut) {
              shortcutItem(shortcutObject != null
                      ? shortcutObject
                      : new FileShortcutObject(fileHolder.friendlyName, fileHolder.file.getUri()));
            } else if (id == R.id.action_mode_file_change_save_path) {
              startActivity(new Intent(getContext(), ChangeStoragePathActivity.class));
            } else
              return handleEditingAction(id, FileListFragment.this, generateSelectionList);

            return true;
          });

          popupMenu.show();
        });
      }
    };

    return new StorageFileAdapter(getActivity())
    {
      @NonNull
      @Override
      public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
      {
        GroupViewHolder holder = super.onCreateViewHolder(parent, viewType);

        if (viewType == GroupEditableListAdapter.VIEW_TYPE_ACTION_BUTTON) {
          registerLayoutViewClicks(holder);
          return holder;
        }

        return AppUtils.quickAction(holder, quickActions);
      }
    };
  }

  @Override
  public boolean onDefaultClickAction(GroupEditableListAdapter.GroupViewHolder holder)
  {
    performLayoutClickOpen(holder);
    return true;
  }

  @Override
  public void onResume()
  {
    super.onResume();
    requireActivity().registerReceiver(mReceiver, mIntentFilter);
    mMediaScanner.connect();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    requireActivity().unregisterReceiver(mReceiver);
    mMediaScanner.disconnect();
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState)
  {
    super.onSaveInstanceState(outState);

    if (getAdapter().getPath() != null)
      outState.putString(EXTRA_FILE_LOCATION, getAdapter().getPath().getUri().toString());
  }

  @Override
  public void onViewStateRestored(@Nullable Bundle savedInstanceState)
  {
    super.onViewStateRestored(savedInstanceState);

    if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_FILE_LOCATION)) {
      try {
        goPath(FileUtils.fromUri(getContext(), Uri.parse(savedInstanceState.getString(EXTRA_FILE_LOCATION))));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void onListRefreshed()
  {
    super.onListRefreshed();

    // Try to bring scope to the top if the user is viewing another folder
    DocumentFile pathOnTrial = getAdapter().getPath();

    if (!(mLastKnownPath == null && getAdapter().getPath() == null)
            && (mLastKnownPath != null && !mLastKnownPath.equals(pathOnTrial)))
      getListView().scrollToPosition(0);

    mLastKnownPath = pathOnTrial;
  }

  protected void shortcutItem(FileShortcutObject shortcutObject)
  {
    AccessDatabase database = AppUtils.getDatabase(getContext());

    try {
      database.reconstruct(shortcutObject);
      database.remove(shortcutObject);

      createSnackbar(R.string.mesg_removed).show();
    } catch (Exception e) {
      database.insert(shortcutObject);
      createSnackbar(R.string.mesg_added).show();
    }
  }

  @Override
  public Snackbar createSnackbar(int resId, Object... objects)
  {
    return Snackbar.make(getListView(), getString(resId, objects), Snackbar.LENGTH_SHORT);
  }

  public void goPath(DocumentFile file)
  {
    if (file != null && !file.canRead()) {
      createSnackbar(R.string.mesg_errorReadFolder, file.getName())
              .show();

      return;
    }

    if (mPathChangedListener != null)
      mPathChangedListener.onPathChanged(file);

    getAdapter().goPath(file);
    refreshList();
  }

  public void requestMountStorage()
  {

    startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_WRITE_ACCESS);
    Toast.makeText(getActivity(), R.string.mesg_mountDirectoryHelp, Toast.LENGTH_LONG).show();
  }

  @Override
  public boolean performLayoutClick(GroupEditableListAdapter.GroupViewHolder holder)
  {
    try {
      StorageFileAdapter.GenericFileHolder fileInfo = getAdapter().getItem(holder);

      if (fileInfo.getViewType() == GroupEditableListAdapter.VIEW_TYPE_ACTION_BUTTON
              && fileInfo.getRequestCode() == StorageFileAdapter.REQUEST_CODE_MOUNT_FOLDER)
        requestMountStorage();
      else if (fileInfo instanceof StorageFileAdapter.FileHolder
              && mSelectByClick && getSelectionConnection() != null)
        return getSelectionConnection().setSelected(holder);
      else if (fileInfo instanceof StorageFileAdapter.DirectoryHolder
              || fileInfo instanceof StorageFileAdapter.WritablePathHolder) {
        FileListFragment.this.goPath(fileInfo.file);

        if (getSelectionCallback() != null && getSelectionCallback().isSelectionActivated() && !AppUtils.getDefaultPreferences(getContext()).getBoolean("helpFolderSelection", false))
          createSnackbar(R.string.mesg_helpFolderSelection)
                  .setAction(R.string.butn_gotIt, v -> AppUtils.getDefaultPreferences(getContext())
                          .edit()
                          .putBoolean("helpFolderSelection", true)
                          .apply())
                  .show();
      } else
        return super.performLayoutClick(holder);

      return true;
    } catch (NotReadyException e) {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public boolean performLayoutLongClick(GroupEditableListAdapter.GroupViewHolder holder)
  {
    try {
      StorageFileAdapter.GenericFileHolder fileHolder = getAdapter().getItem(holder.getAdapterPosition());

      if ((fileHolder instanceof StorageFileAdapter.DirectoryHolder
              || fileHolder instanceof StorageFileAdapter.WritablePathHolder
      )
              && getSelectionConnection() != null
              && getSelectionConnection().setSelected(holder))
        return true;
    } catch (NotReadyException e) {
      e.printStackTrace();
    }

    return super.performLayoutLongClick(holder);
  }


  @Override
  public boolean performLayoutClickOpen(GroupEditableListAdapter.GroupViewHolder holder) {
        /*
        try {
            DocumentFile fileMime = getAdapter().getItem(holder).file;
            if(fileMime.getName().contains(".zip") || fileMime.getName().contains("ZIP")) {
                if(!fileMime.getUri().getPath().contains("/tree/")) {
                    MaterialDialog.Builder bulder = new MaterialDialog.Builder(requireContext())
                            .title("Do you want to Extract :" + fileMime.getName())
                            .positiveText("Extract")
                                .negativeText("Preview")
                            .onNegative((dialog, which) -> {
                                extraZipFile(fileMime, false);
                            })
                            .onPositive((dialog, which) -> {
                                extraZipFile(fileMime, true);
                            });

                    MaterialDialog materialDialog = bulder.build();
                    materialDialog.show();
                } else {
                    return FileUtils.openUriForeground(getActivity(), getAdapter().getItem(holder).file);
                }
                return true;
            } else {
                return FileUtils.openUriForeground(getActivity(), getAdapter().getItem(holder).file);
            }
        } catch (NotReadyException e) {
            // Do nothing
        }

        return super.performLayoutClickOpen(holder);
    }
    */
    try {
      return FileUtils.openUriForeground(getActivity(), getAdapter().getItem(holder).file);
    } catch (NotReadyException e) {
      // Do nothing
    }

    return super.performLayoutClickOpen(holder);
  }

  public void extraZipFile(DocumentFile zipFile, boolean currentPath) {
    new ZipView(getContext(), zipFile, currentPath, new ZipView.Listener() {
      @Override
      public void onRunning(WorkerService.RunningTask runningTask, Context context, DocumentFile file) {
        scanFile(file);
        if(!currentPath) {
          getAdapter().goPath(context.getCacheDir());
        }
      }

      @Override
      public void onCompleted(WorkerService.RunningTask runningTask, Context context, int fileSize) {
        context.sendBroadcast(new Intent(ACTION_FILE_LIST_CHANGED)
                .putExtra(EXTRA_FILE_PARENT, getAdapter().getPath() == null
                        ? null
                        : getAdapter().getPath().getUri()));
      }
    });
  }

  public void copyZipFile(DocumentFile file){
    new ZipCopy(requireContext(), file, new ZipCopy.Listener() {
      @Override
      public void onRunning(WorkerService.RunningTask runningTask, Context context, DocumentFile file) {
        scanFile(file);
        getAdapter().goPath(DocumentFile.fromFile(context.getExternalCacheDir()));

      }

      @Override
      public void onCompleted(WorkerService.RunningTask runningTask, Context context, int fileSize) {
        context.sendBroadcast(new Intent(ACTION_FILE_LIST_CHANGED)
                .putExtra(EXTRA_FILE_PARENT, getAdapter().getPath() == null
                        ? null
                        : getAdapter().getPath().getUri()));
      }
    });
  }

  public void scanFile(DocumentFile file)
  {
    // FIXME: 9/11/18 There should be insert, remove, update
    if (!(file instanceof LocalDocumentFile) || !mMediaScanner.isConnected())
      return;

    String filePath = ((LocalDocumentFile) file).getFile().getAbsolutePath();

    mMediaScanner.scanFile(filePath, file.isDirectory() ? file.getType() : null);

  }

  public void setOnPathChangedListener(OnPathChangedListener pathChangedListener)
  {
    mPathChangedListener = pathChangedListener;
  }

  public interface OnPathChangedListener
  {
    void onPathChanged(DocumentFile file);
  }

  private class SelectionCallback extends ActionModeCallback<StorageFileAdapter.GenericFileHolder>
  {
    private final FileListFragment mFragment;

    public SelectionCallback(FileListFragment fragment)
    {
      super(fragment);
      mFragment = fragment;
    }

    @Override
    public boolean onCreateActionMenu(Context context, PowerfulActionMode actionMode, Menu menu)
    {
      super.onCreateActionMenu(context, actionMode, menu);
      actionMode.getMenuInflater().inflate(R.menu.selection_mode_file, menu);

      return true;
    }

    @Override
    public boolean onActionMenuItemSelected(Context context, PowerfulActionMode actionMode, MenuItem item)
    {
      int id = item.getItemId();

      if (getFragment().getSelectionConnection().getSelectedItemList().size() == 0)
        return super.onActionMenuItemSelected(context, actionMode, item);

      if (handleEditingAction(id, mFragment, getFragment().getSelectionConnection().getSelectedItemList()))
        return super.onActionMenuItemSelected(context, actionMode, item);

      return true;
    }
  }
}