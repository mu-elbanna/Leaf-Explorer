package com.leaf.explorer.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.genonbeta.android.framework.widget.PowerfulActionMode;
import com.leaf.explorer.R;
import com.leaf.explorer.file_share.activity.ShareActivity;
import com.leaf.explorer.file_share.adapter.ShareableListFragment;
import com.leaf.explorer.file_share.fragment.EditableListFragment;
import com.leaf.explorer.file_share.model.Shareable;
import com.leaf.explorer.file_share.view.EditableListFragmentImpl;
import com.leaf.explorer.file_share.widget.EditableListAdapterImpl;

import java.util.ArrayList;
import java.util.List;

public class ActionModeCallback<T extends Shareable> extends EditableListFragment.SelectionCallback<T>
{
    public ActionModeCallback(EditableListFragmentImpl<T> fragment)
    {
        super(fragment);
    }

    @Override
    public boolean onPrepareActionMenu(Context context, PowerfulActionMode actionMode)
    {
        super.onPrepareActionMenu(context, actionMode);
        return true;
    }

    @Override
    public boolean onCreateActionMenu(Context context, PowerfulActionMode actionMode, Menu menu)
    {
        super.onCreateActionMenu(context, actionMode, menu);
        actionMode.getMenuInflater().inflate(R.menu.action_mode_callback, menu);
        return true;
    }

    @Override
    public boolean onActionMenuItemSelected(Context context, PowerfulActionMode actionMode, MenuItem item)
    {
        int id = item.getItemId();

        List<T> selectedItemList = new ArrayList<>(getFragment().getSelectionConnection().getSelectedItemList());

        if (selectedItemList.size() > 0
                && (id == R.id.action_mode_share_LeafShare || id == R.id.action_mode_share_all_apps)) {
            Intent shareIntent = new Intent()
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .setAction((item.getItemId() == R.id.action_mode_share_all_apps)
                            ? (selectedItemList.size() > 1 ? Intent.ACTION_SEND_MULTIPLE : Intent.ACTION_SEND)
                            : (selectedItemList.size() > 1 ? ShareActivity.ACTION_SEND_MULTIPLE : ShareActivity.ACTION_SEND));

            if (selectedItemList.size() > 1) {
                ShareableListFragment.MIMEGrouper mimeGrouper = new ShareableListFragment.MIMEGrouper();
                ArrayList<Uri> uriList = new ArrayList<>();
                ArrayList<CharSequence> nameList = new ArrayList<>();

                for (T sharedItem : selectedItemList) {
                    uriList.add(sharedItem.uri);
                    nameList.add(sharedItem.fileName);

                    if (!mimeGrouper.isLocked())
                        mimeGrouper.process(sharedItem.mimeType);
                }

                shareIntent.setType(mimeGrouper.toString())
                        .putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
                        .putCharSequenceArrayListExtra(ShareActivity.EXTRA_FILENAME_LIST, nameList);
            } else if (selectedItemList.size() == 1) {
                T sharedItem = selectedItemList.get(0);

                shareIntent.setType(sharedItem.mimeType)
                        .putExtra(Intent.EXTRA_STREAM, sharedItem.uri)
                        .putExtra(ShareActivity.EXTRA_FILENAME_LIST, sharedItem.fileName);
            }

            try {
                getFragment().getContext().startActivity(item.getItemId() == R.id.action_mode_share_all_apps
                        ? Intent.createChooser(shareIntent, getFragment().getContext().getString(R.string.text_fileShareAppChoose))
                        : shareIntent);
            } catch (Throwable e) {
                e.printStackTrace();
                Toast.makeText(getFragment().getActivity(), R.string.mesg_somethingWentWrong, Toast.LENGTH_SHORT).show();

                return false;
            }
        } else
            return super.onActionMenuItemSelected(context, actionMode, item);

        return true;
    }

    public static class SelectionDuo<T extends Shareable>
    {
        private final EditableListFragmentImpl<T> mFragment;
        private final EditableListAdapterImpl<T> mAdapter;

        public SelectionDuo(EditableListFragmentImpl<T> fragment, EditableListAdapterImpl<T> adapter)
        {
            mFragment = fragment;
            mAdapter = adapter;
        }

        public EditableListAdapterImpl<T> getAdapter()
        {
            return mAdapter;
        }

        public EditableListFragmentImpl<T> getFragment()
        {
            return mFragment;
        }
    }
}
