package com.example_gallary;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example_gallary.album.Albums;
import com.example_gallary.album.AlbumsUtils;
import com.example_gallary.album.MediaFromAlbums;
import com.example_gallary.info.SectionInfoFactory;
import com.example_gallary.info.SectionItemInfoDialog;
import com.example_gallary.info.SectionItemInfoFactory;
import com.genonbeta.android.framework.app.Fragment;
import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.example_gallary.sectionedrecyclerviewadapter.SectionAdapter;
import com.example_gallary.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import com.leaf.explorer.R;
import com.leaf.explorer.file_share.model.TitleSupport;
import java.util.ArrayList;

public class Example6Fragment
        extends Fragment
        implements ExpandableMovieSection.ClickListener,
        TitleSupport, SnackbarSupport,
        com.genonbeta.android.framework.app.FragmentImpl {

    private static final String DIALOG_TAG = "SectionItemInfoDialogTag";

    private SectionedRecyclerViewAdapter sectionedAdapter;
    ArrayList<Albums> albumsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_ex6, container, false);

        sectionedAdapter = new SectionedRecyclerViewAdapter();

        final RecyclerView recyclerView = view.findViewById(R.id.recyclerview);


        albumsList = AlbumsUtils.getAllAlbums(requireActivity());
        String[] resultFolders = AlbumsUtils.initFolders(requireActivity(), albumsList);

        for (String path : resultFolders) {
            String[] mediaUrls = MediaFromAlbums.listMedia(path);

            if (mediaUrls.length != 0) {
                final GridLayoutManager glm = new GridLayoutManager(getContext(), 2);
                glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(final int position) {
                        if (sectionedAdapter.getSectionItemViewType(position) == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER) {
                            return 2;
                        }
                        return 1;
                    }
                });
                recyclerView.setLayoutManager(glm);

                sectionedAdapter.addSection(new ExpandableMovieSection(getActivity(), path,
                        mediaUrls, this));
            }
        }

        recyclerView.setAdapter(sectionedAdapter);


        return view;
    }


    @Override
    public void onHeaderRootViewClicked(@NonNull final ExpandableMovieSection section) {
        final SectionAdapter sectionAdapter = sectionedAdapter.getAdapterForSection(section);

        // store info of current section state before changing its state
        final boolean wasExpanded = section.isExpanded();
        final int previousItemsTotal = section.getContentItemsTotal();

        section.setExpanded(!wasExpanded);
        sectionAdapter.notifyHeaderChanged();

        if (wasExpanded) {
            sectionAdapter.notifyItemRangeRemoved(0, previousItemsTotal);
        } else {
            sectionAdapter.notifyAllItemsInserted();
        }
    }

    @Override
    public void onItemRootViewClicked(@NonNull final ExpandableMovieSection section, final int itemAdapterPosition) {
        final SectionItemInfoDialog dialog = SectionItemInfoDialog.getInstance(
                SectionItemInfoFactory.create(itemAdapterPosition, sectionedAdapter),
                SectionInfoFactory.create(section, sectionedAdapter.getAdapterForSection(section))
        );
        dialog.show(getParentFragmentManager(), DIALOG_TAG);
    }

    @Override
    public CharSequence getTitle(Context context)
    {
        return context.getString(R.string.text_photo);
    }



}
