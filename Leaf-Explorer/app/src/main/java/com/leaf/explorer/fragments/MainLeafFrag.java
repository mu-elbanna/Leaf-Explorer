package com.leaf.explorer.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.leaf.explorer.R;
import com.leaf.explorer.file_share.activity.ContentSharingActivity;
import com.leaf.explorer.file_share.activity.WebShareActivity;
import com.leaf.explorer.file_share.base.GlideApp;
import com.leaf.explorer.file_share.activity.ConnectSetupActivity;
import com.leaf.explorer.file_share.model.TitleSupport;
import com.leaf.explorer.file_share.util.FileUtils;
import com.leaf.explorer.view.helper.ArcProgress;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MainLeafFrag extends com.genonbeta.android.framework.app.Fragment
        implements TitleSupport, SnackbarSupport, com.genonbeta.android.framework.app.FragmentImpl {

    private final ArrayList<DataModel> dataModels= new ArrayList<>();
    public boolean isExternalSD_available;
    private File externalSD_root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_leaf_frag, container, false);

        GridView listView = view.findViewById(R.id.list);
        GridView shareIt = view.findViewById(R.id.shareIt);

        File path = Environment.getDataDirectory();
        dataModels.add(new DataModel("Internal",
                "Free : " + getAvailableInternalStorageSize(path),
                "Total : " + getTotalInternalMemorySize(path),
                "Used : " + getUsedInternalMemorySize(path),
                getAvailableInternalStoragePercentage(path)));

        File file = new File("/storage");
        File[] temp = file.listFiles();
        File toBe = new File("");
        if (temp != null) {
            for (File aTemp : temp) {
                if (aTemp.isDirectory() && aTemp.canRead() && Objects.requireNonNull(aTemp.listFiles()).length > 0) {
                    isExternalSD_available = true;
                    toBe = aTemp;
                }
                if (isExternalSD_available)
                    externalSD_root = toBe;
            }
        }

        if (isExternalSD_available) {
            File mediaDir = externalSD_root;
            dataModels.add(new DataModel("SD card",
                    "Free : " + getAvailableInternalStorageSize(mediaDir),
                    "Total : " + getTotalInternalMemorySize(mediaDir),
                    "Used : " + getUsedInternalMemorySize(mediaDir),
                    getAvailableInternalStoragePercentage(mediaDir)));
        } else {
            dataModels.add(new DataModel("Not Found",
                    "Free : " + "0",
                    "Total : " + "0",
                    "Used : " + "0",
                    0));
        }

        CustomAdapter adapter = new CustomAdapter(dataModels, getActivity());
        listView.setAdapter(adapter);

        ArrayList<ShareIt> mShareIt= new ArrayList<>();
        mShareIt.add(new ShareIt("Send", R.drawable.ic_send_icon));
        mShareIt.add(new ShareIt("Receive", R.drawable.ic_recieve_icon));
        mShareIt.add(new ShareIt("PC Receive", R.drawable.ic_devices_white_24dp));

        ShareAdapter shareAdapter = new ShareAdapter(mShareIt, getActivity());
        shareIt.setAdapter(shareAdapter);

        shareIt.setOnItemClickListener((parent, view1, position, viewId) -> {
            TextView shareView = view1.findViewById(R.id.name);
            String shareUrl = shareView.getText().toString();

            if (shareUrl.startsWith("Send")) {
                Intent mIntent = new Intent(getContext(), ContentSharingActivity.class);
                mIntent.putExtra("no.", 0);
                startActivity(mIntent);
            } else if (shareUrl.startsWith("Receive")) {
                startActivity(new Intent(getContext(), ConnectSetupActivity.class)
                        .putExtra(ConnectSetupActivity.EXTRA_ACTIVITY_SUBTITLE, getString(R.string.text_receive)));

            } else if (shareUrl.startsWith("PC Receive")) {
                startActivity(new Intent(getContext(), WebShareActivity.class));
            }
        });

        return view;
    }

    @Override
    public CharSequence getTitle(Context context)
    {
        return context.getString(R.string.text_home);
    }

    private static String getUsedInternalMemorySize(File path) {
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availableBlocks = stat.getAvailableBlocksLong();

        long totalSize = totalBlocks * blockSize;
        long availableSize = availableBlocks * blockSize;

        return FileUtils.sizeExpression(totalSize - availableSize, true);
    }

    private static String getTotalInternalMemorySize(File path) {
        Log.d("getPath", path.getPath());
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return FileUtils.sizeExpression(totalBlocks * blockSize, true);
    }

    private static String getAvailableInternalStorageSize(File path) {
        Log.d("getPath", path.getPath());
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return FileUtils.sizeExpression(availableBlocks * blockSize, true);
    }

    private int getAvailableInternalStoragePercentage(File path) {
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long totalSize = totalBlocks * blockSize;
        long availableBlocks = stat.getAvailableBlocksLong();
        long availableSize = availableBlocks * blockSize;
        Log.d("here is", "" + ((availableSize * 100) / totalSize));
        int size = (int) ((availableSize * 100) / totalSize);
        return 100 - size;
    }

    public static class DataModel {

        String name;
        String free;
        String total;
        String used;
        int percentage;

        DataModel(String name, String free, String total, String used, int percentage) {
            this.name=name;
            this.free=free;
            this.total=total;
            this.used=used;
            this.percentage=percentage;
        }

        public String getName() {
            return name;
        }
        public String getFree() {
            return free;
        }
        String getTotal() {
            return total;
        }
        public String getUsed() {
            return used;
        }
        int getPercentage() {
            return percentage;
        }
    }

    public static class CustomAdapter extends ArrayAdapter<DataModel> {

        Context mContext;
        // View lookup cache
        private static class ViewHolder {
            TextView name;
            TextView free;
            TextView total;
            TextView used;
            ArcProgress progressStorage;
        }

        CustomAdapter(ArrayList<DataModel> data, Context context) {
            super(context, R.layout.home_memory_info, data);
            this.mContext=context;

        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            DataModel dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag

            if (convertView == null) {

                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.home_memory_info, parent, false);
                viewHolder.name = convertView.findViewById(R.id.name);
                viewHolder.free = convertView.findViewById(R.id.free);
                viewHolder.total = convertView.findViewById(R.id.total);
                viewHolder.used = convertView.findViewById(R.id.used);
                viewHolder.progressStorage = convertView.findViewById(R.id.progress_storage);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            assert dataModel != null;
            viewHolder.name.setText(dataModel.getName());
            viewHolder.free.setText(dataModel.getFree());
            viewHolder.total.setText(dataModel.getTotal());
            viewHolder.used.setText(dataModel.getUsed());
            viewHolder.progressStorage.setProgress(dataModel.getPercentage());
            // Return the completed view to render on screen
            return convertView;
        }
    }



    public static class ShareIt {

        String name;
        int icon;

        ShareIt(String name, int icon) {
            this.name = name;
            this.icon = icon;
        }

        public String getName() {
            return name;
        }
        public int getIcon() {
            return icon;
        }
    }

    public static class ShareAdapter extends ArrayAdapter<ShareIt> {

        Context mContext;
        // View lookup cache
        private static class ViewHolder {
            TextView name;
            ImageView icon;
        }

        ShareAdapter(ArrayList<ShareIt> data, Context context) {
            super(context, R.layout.share_it_adapter, data);
            this.mContext = context;

        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            ShareIt mShareIt = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag

            if (convertView == null) {

                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.share_it_adapter, parent, false);
                viewHolder.name = convertView.findViewById(R.id.name);
                viewHolder.icon = convertView.findViewById(R.id.icon);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.name.setText(mShareIt.getName());

            GlideApp.with(getContext())
                    .load(mShareIt.getIcon())
                    .override(300)
                    .centerCrop()
                    .into(viewHolder.icon);

            return convertView;
        }
    }

}
