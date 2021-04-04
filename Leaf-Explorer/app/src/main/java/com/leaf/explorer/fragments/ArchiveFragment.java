package com.leaf.explorer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.leaf.explorer.R;
import com.leaf.explorer.file_share.base.GlideApp;
import com.leaf.explorer.file_share.model.TitleSupport;
import com.leaf.explorer.file_share.util.IconSupport;
import com.leaf.explorer.view.ShortcutItem;

import java.util.ArrayList;

public class ArchiveFragment extends ArchiveListFragment implements IconSupport, TitleSupport {

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
        View adaptedView = getLayoutInflater().inflate(R.layout.storage_archive_file_explorer, null, false);
        listViewContainer.addView(adaptedView);

        RecyclerView mPathView = adaptedView.findViewById(R.id.fragment_fileexplorer_pathresolver);
        ArchiveAdapter mArchiveAdapter = new ArchiveAdapter(getArchive(), getContext());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setStackFromEnd(true);

        mPathView.setLayoutManager(layoutManager);
        mPathView.setHasFixedSize(true);
        mPathView.setAdapter(mArchiveAdapter);

        return super.onListView(mainContainer, adaptedView.findViewById(R.id.fragment_fileexplorer_listViewContainer));
    }

    private ArrayList<ShortcutItem> getArchive() {

        ArrayList<ShortcutItem> ShortcutDocs = new ArrayList<>();

        ShortcutDocs.add(new ShortcutItem("Images", "get Images", R.drawable.ic_photo_white_24dp));
        ShortcutDocs.add(new ShortcutItem("Videos", "get Videos", R.drawable.ic_video_white_24dp));
        ShortcutDocs.add(new ShortcutItem("Musics", "get Musics", R.drawable.ic_library_music_white_24dp));
        ShortcutDocs.add(new ShortcutItem("App", "get App", R.drawable.ic_android_white_24dp));

        return ShortcutDocs;
    }

    @Override
    public int getIconRes()
    {
        return R.drawable.ic_folder_white_24dp;
    }

    @Override
    public CharSequence getTitle(Context context)
    {
        return context.getString(R.string.text_archive);
    }

    public static class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.MyViewHolder> {

        private final ArrayList<ShortcutItem> videoList;
        Context context;

        public static class MyViewHolder extends RecyclerView.ViewHolder {

            private final TextView name;
            private final ImageView faviconTab;
            private final RelativeLayout shortcut;

            private MyViewHolder(View view) {
                super(view);

                name = view.findViewById(R.id.nameTxt);
                faviconTab = view.findViewById(R.id.Image);
                shortcut = view.findViewById(R.id.shortcut);

            }
        }
        public ArchiveAdapter(ArrayList<ShortcutItem> videoList, Context context) {
            this.context  = context;
            this.videoList = videoList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.leaf_archive_file, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final ShortcutItem item = videoList.get(position);

            holder.name.setText(item.getName());

            GlideApp.with(context)
                    .load(item.getResources())
                    .override(300)
                    .centerCrop()
                    .into(holder.faviconTab);
        }

        @Override
        public int getItemCount() {
            return videoList.size();
        }

    }
}
