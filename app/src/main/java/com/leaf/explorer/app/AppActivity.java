package com.leaf.explorer.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.leaf.explorer.dialog.AppProfileEditorDialog;
import com.leaf.explorer.file_share.base.GlideApp;

import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.leaf.explorer.dialog.AppPermissionRequest;
import com.leaf.explorer.file_share.base.AppConfig;
import com.leaf.explorer.file_share.service.DeviceScannerService;
import com.leaf.explorer.file_share.service.WorkerService;
import com.leaf.explorer.file_share.util.AppUtils;
import com.leaf.explorer.R;
import com.leaf.explorer.file_share.db.AccessDatabase;
import com.leaf.explorer.file_share.service.CommunicationService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public abstract class AppActivity extends AppCompatActivity
{

    private boolean mSkipPermissionRequest = false;
    private AlertDialog mOngoingRequest;
    public static final int REQUEST_PICK_PROFILE_PHOTO = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        boolean mCustomFontsEnabled = isUsingCustomFonts();

        if (mCustomFontsEnabled) {
            Log.d(AppActivity.class.getSimpleName(), "Custom fonts have been applied");
            getTheme().applyStyle(R.style.TextAppearance_Ubuntu, true);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!AppUtils.AppcheckRunningConditions(this)) {
            if (!mSkipPermissionRequest)
                requestRequiredPermissions(true);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (AppUtils.checkRunningConditions(this))
           // AppUtils.startForegroundService(this, new Intent(this, CommunicationService.class));
        Toast.makeText(this, "Permission Granted, Now you can access this app.", Toast.LENGTH_SHORT).show();
        else
            requestRequiredPermissions(!mSkipPermissionRequest);
    }

    public void setSkipPermissionRequest(boolean skip)
    {
        mSkipPermissionRequest = skip;
    }

    public boolean requestRequiredPermissions(boolean killActivityOtherwise)
    {
        if (mOngoingRequest != null && mOngoingRequest.isShowing())
            return false;

        for (AppPermissionRequest.PermissionRequest request : AppUtils.getAppPermissions(this))
            if ((mOngoingRequest = AppPermissionRequest.requestIfNecessary(this, request, killActivityOtherwise)) != null)
                return false;

        return true;
    }

    /**
     * Exits app closing all the active services and connections.
     * This will also prevent this activity from notifying {@link CommunicationService}
     * as the user leaves to the state of {@link AppActivity#onPause()}
     */
    public void exitApp()
    {

        stopService(new Intent(this, CommunicationService.class));
        stopService(new Intent(this, DeviceScannerService.class));
        stopService(new Intent(this, WorkerService.class));

        finish();
    }

    public AccessDatabase getDatabase()
    {
        return AppUtils.getDatabase(this);
    }

    protected SharedPreferences getDefaultPreferences()
    {
        return AppUtils.getDefaultPreferences(this);
    }

    public boolean isUsingCustomFonts()
    {
        return getDefaultPreferences().getBoolean("custom_fonts", false);
    }

    public interface OnBackPressedListener
    {
        boolean onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_PROFILE_PHOTO)
            if (resultCode == RESULT_OK && data != null) {
                Uri chosenImageUri = data.getData();

                if (chosenImageUri != null) {
                    GlideApp.with(this)
                            .load(chosenImageUri)
                            .centerCrop()
                            .override(200, 200)
                            .into(new Target<Drawable>()
                            {
                                @Override
                                public void onLoadStarted(@Nullable Drawable placeholder)
                                {

                                }

                                @Override
                                public void onLoadFailed(@Nullable Drawable errorDrawable)
                                {

                                }

                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition)
                                {
                                    try {
                                        Bitmap bitmap = Bitmap.createBitmap(AppConfig.PHOTO_SCALE_FACTOR, AppConfig.PHOTO_SCALE_FACTOR, Bitmap.Config.ARGB_8888);
                                        Canvas canvas = new Canvas(bitmap);
                                        FileOutputStream outputStream = openFileOutput("profilePicture", MODE_PRIVATE);

                                        resource.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                                        resource.draw(canvas);
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                                        outputStream.close();

                                        notifyUserProfileChanged();
                                    } catch (Exception error) {
                                        error.printStackTrace();
                                    }
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder)
                                {

                                }

                                @Override
                                public void getSize(@NonNull SizeReadyCallback cb)
                                {

                                }

                                @Override
                                public void removeCallback(@NonNull SizeReadyCallback cb)
                                {

                                }

                                @Nullable
                                @Override
                                public Request getRequest()
                                {
                                    return null;
                                }

                                @Override
                                public void setRequest(@Nullable Request request)
                                {

                                }

                                @Override
                                public void onStart()
                                {

                                }

                                @Override
                                public void onStop()
                                {

                                }

                                @Override
                                public void onDestroy()
                                {

                                }
                            });
                }
            }
    }

    public void onUserProfileUpdated()
    {

    }

    public void loadProfilePictureInto(String deviceName, ImageView imageView)
    {
        try {
            FileInputStream inputStream = openFileInput("profilePicture");
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            GlideApp.with(this)
                    .load(bitmap)
                    .circleCrop()
                    .into(imageView);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            imageView.setImageDrawable(AppUtils.getDefaultIconBuilder(this).buildRound(deviceName));
        }
    }

    public void notifyUserProfileChanged()
    {
        if (!isFinishing())
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    onUserProfileUpdated();
                }
            });
    }

    public void startProfileEditor()
    {
        new AppProfileEditorDialog(this).show();
    }

    public void requestProfilePictureChange()
    {
        startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), REQUEST_PICK_PROFILE_PHOTO);
    }
}
