package com.leaf.explorer.fragments.explorer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.genonbeta.android.framework.io.DocumentFile;
import com.google.android.material.snackbar.Snackbar;
import com.leaf.explorer.R;
import com.leaf.explorer.app.AppActivity;
import com.leaf.explorer.config.AppConfig;
import com.leaf.explorer.file_share.adapter.PathResolverRecyclerAdapter;
import com.leaf.explorer.file_share.dialog.FolderCreationDialog;
import com.leaf.explorer.file_share.model.TitleSupport;
import com.leaf.explorer.file_share.util.DetachListener;
import com.leaf.explorer.file_share.util.FileUtils;
import com.leaf.explorer.file_share.util.IconSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class StorageExplorerFragment2
        extends FileListFragment
        implements AppActivity.OnBackPressedListener, DetachListener, IconSupport, TitleSupport
{
    public static final String TAG = StorageExplorerFragment2.class.getSimpleName();

    private RecyclerView mPathView;
    private FilePathResolverRecyclerAdapter mPathAdapter;
    private DocumentFile mRequestedPath = null;

    public static DocumentFile getReadableFolder(DocumentFile documentFile)
    {
        DocumentFile parent = documentFile.getParentFile();

        if (parent == null)
            return null;

        return parent.canRead()
                ? parent
                : getReadableFolder(parent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setDividerView(R.id.fragment_fileexplorer_separator);
    }

    @Override
    protected RecyclerView onListView(View mainContainer, ViewGroup listViewContainer)
    {
        View adaptedView = getLayoutInflater().inflate(R.layout.storage_fragment_file_explorer, null, false);
        listViewContainer.addView(adaptedView);

        mPathView = adaptedView.findViewById(R.id.fragment_fileexplorer_pathresolver);
        mPathAdapter = new FilePathResolverRecyclerAdapter(getContext());

        mPathAdapter.setOnClickListener(holder -> goPath(holder.index.object));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setStackFromEnd(true);

        mPathView.setLayoutManager(layoutManager);
        mPathView.setHasFixedSize(true);
        mPathView.setAdapter(mPathAdapter);

        return super.onListView(mainContainer, adaptedView.findViewById(R.id.fragment_fileexplorer_listViewContainer));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (mRequestedPath != null)
            requestPath(mRequestedPath);
    }

    @Override
    public boolean onBackPressed()
    {
        DocumentFile path = getAdapter().getPath();

        if (path == null)
            return false;

        DocumentFile parentFile = getReadableFolder(path);

        if (parentFile == null || File.separator.equals(parentFile.getName()))
            goPath(null);
        else
            goPath(parentFile);

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actions_file_explorer, menu);

        SharedPreferences prefs = requireContext().getSharedPreferences(AppConfig.SHOW_HIDDEN, 0);
        boolean uriBoolean = prefs.getBoolean("show_hidden_files", false);
        menu.findItem(R.id.show_hidden_files).setChecked(uriBoolean);
    }

    @Override
    protected void onListRefreshed()
    {
        super.onListRefreshed();

        mPathAdapter.goTo(getAdapter().getPath());
        mPathAdapter.notifyDataSetChanged();

        if (mPathAdapter.getItemCount() > 0)
            mPathView.smoothScrollToPosition(mPathAdapter.getItemCount() - 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.actions_file_explorer_create_folder) {
            if (getAdapter().getPath() != null && getAdapter().getPath().canWrite())
                new FolderCreationDialog(getContext(), getAdapter().getPath(), directoryFile -> refreshList()).show();
            else
                Snackbar.make(getListView(), R.string.mesg_currentPathUnavailable, Snackbar.LENGTH_SHORT).show();
        } else if (id == R.id.show_hidden_files) {

            SharedPreferences prefs = requireContext().getSharedPreferences(AppConfig.SHOW_HIDDEN, 0);
            boolean uriBoolean = prefs.getBoolean("show_hidden_files", false);
            if (uriBoolean) {
                SharedPreferences.Editor editor = requireContext().getSharedPreferences(AppConfig.SHOW_HIDDEN, 0).edit();
                editor.putBoolean("show_hidden_files", false);
                editor.apply();

                item.setChecked(false);
                refreshList();
            } else {
                SharedPreferences.Editor editor = requireContext().getSharedPreferences(AppConfig.SHOW_HIDDEN, 0).edit();
                editor.putBoolean("show_hidden_files", true);
                editor.apply();

                item.setChecked(true);
                refreshList();
            }

        } else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public int getIconRes()
    {
        return R.drawable.ic_folder_white_24dp;
    }

    public PathResolverRecyclerAdapter getPathAdapter()
    {
        return mPathAdapter;
    }

    public RecyclerView getPathView()
    {
        return mPathView;
    }

    @Override
    public CharSequence getTitle(Context context)
    {
        return context.getString(R.string.text_fileExplorer);
    }

    public void requestPath(DocumentFile file)
    {
        if (!isAdded()) {
            mRequestedPath = file;
            return;
        }

        mRequestedPath = null;

        goPath(file);
    }

    private class FilePathResolverRecyclerAdapter extends PathResolverRecyclerAdapter<DocumentFile>
    {
        public FilePathResolverRecyclerAdapter(Context context)
        {
            super(context);
        }

        @Override
        public Holder.Index<DocumentFile> onFirstItem()
        {
            return new Holder.Index<>(getContext().getString(R.string.text_home), R.drawable.ic_home_white_24dp, null);
        }

        public void goTo(DocumentFile file)
        {
            ArrayList<Holder.Index<DocumentFile>> pathIndex = new ArrayList<>();
            DocumentFile currentFile = file;

            while (currentFile != null) {
                Holder.Index<DocumentFile> index = new Holder.Index<>(currentFile.getName(), currentFile);

                pathIndex.add(index);

                currentFile = currentFile.getParentFile();

                if (currentFile == null && ".".equals(index.title))
                    index.title = getString(R.string.text_fileRoot);
            }

            initAdapter();

            synchronized (getList()) {
                while (pathIndex.size() != 0) {
                    int currentStage = pathIndex.size() - 1;

                    getList().add(pathIndex.get(currentStage));
                    pathIndex.remove(currentStage);
                }
            }
        }
    }
}
