package com.leaf.explorer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.leaf.explorer.R;
import com.leaf.explorer.file_share.base.GlideApp;
import com.leaf.explorer.file_share.model.Shareable;
import com.leaf.explorer.file_share.widget.EditableListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ShortcutAdapter
        extends EditableListAdapter<ShortcutAdapter.ShortcutHolder, EditableListAdapter.EditableViewHolder>
{

    public ShortcutAdapter(Context context)
    {
        super(context);
    }

    @Override
    public List<ShortcutHolder> onLoad()
    {
        ArrayList<ShortcutHolder> shortcutList = new ArrayList<>();

        shortcutList.add(new ShortcutHolder("Images", "get Images", R.drawable.ic_photo_white_24dp));
        shortcutList.add(new ShortcutHolder("Videos", "get Videos", R.drawable.ic_video_white_24dp));
        shortcutList.add(new ShortcutHolder("Musics", "get Musics", R.drawable.ic_library_music_white_24dp));
        shortcutList.add(new ShortcutHolder("App", "get App", R.drawable.ic_android_white_24dp));

        //  shortcutList.add(new ShortcutHolder("Documents", "get Documents", R.drawable.ic_file_document_box_white_24dp));
        //  shortcutList.add(new ShortcutHolder("Archives", "get Archives", R.drawable.ic_arrow_down_white_24dp));

        return shortcutList;
    }

    @NonNull
    @Override
    public EditableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {

        return new EditableListAdapter.EditableViewHolder(getInflater().inflate(R.layout.leaf_shortcut_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EditableViewHolder holder, int position)
    {
        try {
            final View parentView = holder.getView();
            final ShortcutHolder object = getItem(position);

            TextView name = parentView.findViewById(R.id.nameTxt);
            ImageView faviconTab = parentView.findViewById(R.id.Image);
            TextView date = parentView.findViewById(R.id.date);
            RelativeLayout shortcut = parentView.findViewById(R.id.shortcut);

            name.setText(object.getName());
            date.setText(object.getDesc());

            GlideApp.with(getContext())
                    .load(object.getResources())
                    .override(300)
                    .centerCrop()
                    .into(faviconTab);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ShortcutHolder extends Shareable
    {
        String name, desc;
        int res;

        public ShortcutHolder(String name, String desc, int res) {
            super(0, name, null, null, 0, 0, null);
            this.name = name;
            this.desc = desc;
            this.res = res;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public int getResources() {
            return res;
        }

    }



}
