package com.example_gallary;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.leaf.explorer.R;

final class ItemViewHolder extends RecyclerView.ViewHolder {

    final View rootView;
    final TextView tvItem;
    final TextView tvSubItem;
    ImageView mImageView;

    ItemViewHolder(@NonNull final View view) {
        super(view);

        rootView = view;
        tvItem = view.findViewById(R.id.tvItem);
        tvSubItem = view.findViewById(R.id.tvSubItem);
        mImageView = view.findViewById(R.id.imgItem);
    }
}
