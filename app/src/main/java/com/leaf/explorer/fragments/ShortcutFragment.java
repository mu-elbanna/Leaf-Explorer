package com.leaf.explorer.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaf.explorer.R;
import com.leaf.explorer.adapter.ShortcutAdapter;
import com.leaf.explorer.file_share.activity.ContentSharingActivity;
import com.leaf.explorer.file_share.fragment.EditableListFragment;
import com.leaf.explorer.file_share.model.TitleSupport;
import com.leaf.explorer.file_share.util.AppUtils;
import com.leaf.explorer.file_share.util.NotReadyException;
import com.leaf.explorer.file_share.widget.EditableListAdapter;

public class ShortcutFragment extends EditableListFragment<ShortcutAdapter.ShortcutHolder, EditableListAdapter.EditableViewHolder, ShortcutAdapter>
        implements TitleSupport
{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSortingSupported(false);

        setDefaultViewingGridSize(1, 2);

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

    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public ShortcutAdapter onAdapter()
    {
        final AppUtils.QuickActions<EditableListAdapter.EditableViewHolder> quickActions = clazz -> {
            registerLayoutViewClicks(clazz);

            View visitView = clazz.getView().findViewById(R.id.menu);
            visitView.setOnClickListener(
                    v -> Toast.makeText(getContext(), "working", Toast.LENGTH_SHORT).show());

        };

        return new ShortcutAdapter(getActivity())
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
        try {
            ShortcutAdapter.ShortcutHolder item = getAdapter().getItem(holder);

            if (item.getName().startsWith("Images")) {
                Intent mIntent = new Intent(getContext(), ContentSharingActivity.class);
                mIntent.putExtra("no.", 3);
                requireContext().startActivity(mIntent);

            } else if (item.getName().startsWith("Musics")) {
                Intent mIntent = new Intent(getContext(), ContentSharingActivity.class);
                mIntent.putExtra("no.", 2);
                requireContext().startActivity(mIntent);

            } else if (item.getName().startsWith("Videos")) {
                Intent mIntent = new Intent(getContext(), ContentSharingActivity.class);
                mIntent.putExtra("no.", 4);
                requireContext().startActivity(mIntent);

            } else if (item.getName().startsWith("Documents")) {
                Intent mIntent = new Intent(getContext(), ContentSharingActivity.class);
                mIntent.putExtra("no.", 1);
                requireContext().startActivity(mIntent);

            } else if (item.getName().startsWith("Archives")) {
                Intent mIntent = new Intent(getContext(), ContentSharingActivity.class);
                mIntent.putExtra("no.", 1);
                requireContext().startActivity(mIntent);

            } else if (item.getName().startsWith("App")) {
                Intent mIntent = new Intent(getContext(), ContentSharingActivity.class);
                mIntent.putExtra("no.", 0);
                requireContext().startActivity(mIntent);

            }
        } catch (NotReadyException e) {
            e.printStackTrace();
        }
        return true;
    }


    @Override
    public CharSequence getTitle(Context context)
    {
        return context.getString(R.string.text_shortcut);
    }
}
