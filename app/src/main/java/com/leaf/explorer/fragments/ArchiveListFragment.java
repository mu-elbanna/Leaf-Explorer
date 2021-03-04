package com.leaf.explorer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaf.explorer.R;
import com.leaf.explorer.adapter.ArchivesListAdapter;
import com.leaf.explorer.file_share.fragment.EditableListFragment;
import com.leaf.explorer.file_share.model.TitleSupport;
import com.leaf.explorer.file_share.util.AppUtils;
import com.leaf.explorer.file_share.view.GalleryGroupEditableListFragment;
import com.leaf.explorer.file_share.widget.EditableListAdapter;
import com.leaf.explorer.file_share.widget.GroupEditableListAdapter;

public class ArchiveListFragment
        extends EditableListFragment<ArchivesListAdapter.ImageHolder, EditableListAdapter.EditableViewHolder, ArchivesListAdapter>
        implements TitleSupport
{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setFilteringSupported(true);
        setDefaultOrderingCriteria(ArchivesListAdapter.MODE_SORT_ORDER_DESCENDING);
        setDefaultSortingCriteria(ArchivesListAdapter.MODE_SORT_BY_DATE);
        setDefaultViewingGridSize(2, 4);
        setUseDefaultPaddingDecoration(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        setEmptyImage(R.drawable.ic_photo_white_24dp);
        setEmptyText(getString(R.string.text_listEmptyImage));
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getContext().getContentResolver()
                .registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, getDefaultContentObserver());
    }

    @Override
    public void onPause()
    {
        super.onPause();

        getContext().getContentResolver()
                .unregisterContentObserver(getDefaultContentObserver());
    }

    @Override
    public ArchivesListAdapter onAdapter()
    {
        final AppUtils.QuickActions<EditableListAdapter.EditableViewHolder> quickActions = clazz -> {
            registerLayoutViewClicks(clazz);

            View visitView = clazz.getView().findViewById(R.id.visitView);
            visitView.setOnClickListener(
                    v -> performLayoutClickOpen(clazz));
            visitView.setOnLongClickListener(v -> performLayoutLongClick(clazz));

            clazz.getView().findViewById(getAdapter().isGridLayoutRequested()
                    ? R.id.selectorContainer : R.id.selector)
                    .setOnClickListener(v -> {
                        if (getSelectionConnection() != null)
                            getSelectionConnection().setSelected(clazz.getAdapterPosition());
                    });

        };

        return new ArchivesListAdapter(getActivity())
        {
            @NonNull
            @Override
            public EditableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                return AppUtils.quickAction(super.onCreateViewHolder(parent, viewType), quickActions);
            }
        };
    }

    @Override
    public boolean onDefaultClickAction(EditableListAdapter.EditableViewHolder holder)
    {
        return getSelectionConnection() != null
                ? getSelectionConnection().setSelected(holder)
                : performLayoutClickOpen(holder);
    }

    @Override
    public CharSequence getTitle(Context context)
    {
        return context.getString(R.string.text_archive);
    }
}
