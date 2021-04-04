package com.example_gallary;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example_gallary.album.AlbumsUtils;
import com.example_gallary.sectionedrecyclerviewadapter.Section;
import com.example_gallary.sectionedrecyclerviewadapter.SectionParameters;
import com.leaf.explorer.R;
import com.leaf.explorer.file_share.base.GlideApp;

final class ExpandableMovieSection extends Section {

    private final String title;
    private final String[] list;
    private final ClickListener clickListener;
    private final Activity activity;

    private boolean expanded = true;

    ExpandableMovieSection(Activity activity, @NonNull final String title, @NonNull final String[] list,
                           @NonNull final ClickListener clickListener) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.section_ex6_item)
                .headerResourceId(R.layout.section_ex6_header)
                .build());

        this.activity = activity;
        this.title = title;
        this.list = list;
        this.clickListener = clickListener;
    }

    @Override
    public int getContentItemsTotal() {
        return expanded ? list.length : 0;
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(final View view) {
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ItemViewHolder itemHolder = (ItemViewHolder) holder;

        //Movie movie = list.get(position);
        String url = list[position];

        itemHolder.tvItem.setText("movie.name");
        itemHolder.tvSubItem.setText("movie.parentName");

        GlideApp.with(activity)
                .load(url)
                .override(300)
                .centerCrop()
                .into(itemHolder.mImageView);

        itemHolder.rootView.setOnClickListener(v ->
                clickListener.onItemRootViewClicked(this, itemHolder.getAdapterPosition())
        );
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(final View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(final RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        String currentFolder = AlbumsUtils.getFolderName(title);

        headerHolder.tvTitle.setText(currentFolder);
        headerHolder.imgArrow.setImageResource(
                expanded ? R.drawable.ic_keyboard_arrow_up_black_18dp : R.drawable.ic_keyboard_arrow_down_black_18dp
        );

        headerHolder.rootView.setOnClickListener(v ->
                clickListener.onHeaderRootViewClicked(this)
        );
    }

    boolean isExpanded() {
        return expanded;
    }

    void setExpanded(final boolean expanded) {
        this.expanded = expanded;
    }

    interface ClickListener {

        void onHeaderRootViewClicked(@NonNull final ExpandableMovieSection section);

        void onItemRootViewClicked(@NonNull final ExpandableMovieSection section, final int itemAdapterPosition);
    }
}
