package com.leaf.explorer.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.genonbeta.android.framework.io.DocumentFile;
import com.leaf.explorer.R;

import com.leaf.explorer.file_share.adapter.ApplicationListAdapter;
import com.leaf.explorer.file_share.adapter.ImageListAdapter;
import com.leaf.explorer.file_share.base.GlideApp;
import com.leaf.explorer.file_share.model.Shareable;
import com.leaf.explorer.file_share.util.TimeUtils;
import com.leaf.explorer.file_share.widget.EditableListAdapter;
import com.leaf.explorer.file_share.widget.GalleryGroupEditableListAdapter;
import com.leaf.explorer.file_share.widget.GroupEditableListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ArchivesListAdapter
        extends EditableListAdapter<ArchivesListAdapter.ImageHolder, EditableListAdapter.EditableViewHolder>
{
    private final ContentResolver mResolver;
    private final int mSelectedInset;

    public ArchivesListAdapter(Context context)
    {
        super(context);
        mResolver = context.getContentResolver();
        mSelectedInset = (int) context.getResources().getDimension(R.dimen.space_list_grid);
    }

    @Override
    public List<ImageHolder> onLoad()
    {
        ArrayList<ImageHolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = mResolver.query(allImagesuri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }
            do{
                ImageHolder folds = new ImageHolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                //String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder+"/"));
                folderpaths = folderpaths+folder+"/";
                if (!picPaths.contains(folderpaths)) {
                    picPaths.add(folderpaths);

                    folds.setPath(folderpaths);
                    folds.setFolderName(folder);
                    folds.setFirstPic(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    folds.addpics();
                    picFolders.add(folds);
                }else{
                    for(int i = 0;i<picFolders.size();i++){
                        if(picFolders.get(i).getPath().equals(folderpaths)){
                            picFolders.get(i).setFirstPic(datapath);
                            picFolders.get(i).addpics();
                        }
                    }
                }
            }while(cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(int i = 0;i < picFolders.size();i++){
            Log.d("picture folders",picFolders.get(i).getFolderName()+" and path = "+picFolders.get(i).getPath()+" "+picFolders.get(i).getNumberOfPics());
        }

        //reverse order ArrayList
       /* ArrayList<imageFolder> reverseFolders = new ArrayList<>();

        for(int i = picFolders.size()-1;i > reverseFolders.size()-1;i--){
            reverseFolders.add(picFolders.get(i));
        }*/

        return picFolders;
    }

    @NonNull
    @Override
    public EditableListAdapter.EditableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {

        return new EditableListAdapter.EditableViewHolder(getInflater().inflate(isGridLayoutRequested()
                ? R.layout.row_image_grid
                : R.layout.row_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EditableListAdapter.EditableViewHolder holder, int position)
    {
        try {
            final View parentView = holder.getView();
            final ImageHolder object = getItem(position);


            ViewGroup container = parentView.findViewById(R.id.container);
            ImageView image = parentView.findViewById(R.id.image);
            TextView text1 = parentView.findViewById(R.id.text);
            TextView text2 = parentView.findViewById(R.id.text2);

            text1.setText(object.getFolderName());
            String folderSizeString=""+object.getNumberOfPics()+" Media";
            text2.setText(folderSizeString);

            parentView.setSelected(object.isSelectableSelected());

            GlideApp.with(getContext())
                    .load(object.getFirstPic())
                    .override(300)
                    .centerCrop()
                    .into(image);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isGridSupported()
    {
        return true;
    }

    public static class ImageHolder extends Shareable
    {
        private  String path;
        private  String FolderName;
        private String firstPic;
        private int numberOfPics = 0;

        public ImageHolder(String FolderName)
        {
            super(0, FolderName, null, null, 0, 0, null);
        }

        public ImageHolder()
        {
            super(0, null, null, null, 0, 0, null);
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getFolderName() {
            return FolderName;
        }

        public void setFolderName(String folderName) {
            FolderName = folderName;
        }

        public int getNumberOfPics() {
            return numberOfPics;
        }

        public void setNumberOfPics(int numberOfPics) {
            this.numberOfPics = numberOfPics;
        }

        public void addpics(){
            this.numberOfPics++;
        }

        public String getFirstPic() {
            return firstPic;
        }

        public void setFirstPic(String firstPic) {
            this.firstPic = firstPic;
        }
    }


}
