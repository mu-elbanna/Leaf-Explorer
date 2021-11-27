package com.leaf.explorer.activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.genonbeta.android.framework.io.DocumentFile;
import com.genonbeta.android.framework.widget.PowerfulActionMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.leaf.explorer.BuildConfig;
import com.leaf.explorer.R;
import com.leaf.explorer.app.AppActivity;
import com.leaf.explorer.file_share.activity.ConnectionManagerActivity;
import com.leaf.explorer.file_share.activity.ContentSharingActivity;
import com.leaf.explorer.file_share.activity.HistoryActivity;
import com.leaf.explorer.file_share.activity.WebShareActivity;
import com.leaf.explorer.file_share.model.NetworkDevice;
import com.leaf.explorer.file_share.model.WritablePathObject;
import com.leaf.explorer.file_share.util.AppUtils;
import com.leaf.explorer.file_share.util.PowerfulActionModeSupport;
import com.leaf.explorer.fragments.LeafFragment;
import com.leaf.explorer.view.DeleteView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;

public class LeafActivity extends AppActivity
        implements NavigationView.OnNavigationItemSelectedListener, PowerfulActionModeSupport {

    LeafFragment mMainFragment;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;

    public static final String TAG = LeafActivity.class.getSimpleName();
    private PowerfulActionMode mActionMode;

    private long mExitPressTime;
    private int mChosenMenuItemId;

    private boolean isExternalSD_available;
    private Uri uri;
    private final String prefsName = "mysharedpref";

    private void createShareHeaderView() {
        View headerView = mNavigationView.getHeaderView(0);
        if (headerView != null) {
            MaterialButton sendLayoutButton = headerView.findViewById(R.id.sendButton);
            MaterialButton receiveLayoutButton = headerView.findViewById(R.id.receiveButton);

            sendLayoutButton.setOnClickListener(v -> {
                Intent mIntent = new Intent(getApplicationContext(), ContentSharingActivity.class);
                mIntent.putExtra("no.", 0);
                startActivity(mIntent);
            });

            receiveLayoutButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ConnectionManagerActivity.class)
                    .putExtra(ConnectionManagerActivity.EXTRA_ACTIVITY_SUBTITLE, getString(R.string.text_receive))
                    .putExtra(ConnectionManagerActivity.EXTRA_REQUEST_TYPE, ConnectionManagerActivity.RequestType.MAKE_ACQUAINTANCE.toString())));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_imp);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMainFragment = (LeafFragment) getSupportFragmentManager().findFragmentById(R.id.activitiy_leaf_fragment);
        mNavigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.text_navigationDrawerOpen, R.string.text_navigationDrawerClose);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener()
        {
            @Override
            public void onDrawerClosed(View drawerView)
            {
                applyAwaitingDrawerAction();
            }
        });

        mNavigationView.setNavigationItemSelectedListener(this);

        createShareHeaderView();
        setExternalSD_root();
        if (isExternalSD_available) {
            SharedPreferences prefs = getSharedPreferences(prefsName, 0);
            String uriString = prefs.getString("treeuri", "0");
            if (uriString.equals("0")) {
                MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                        .title("Attention!!!")
                        .customView(R.layout.tree_uri_popup, true)
                        .cancelable(false)
                        .positiveText("Ok")
                        .onPositive((dialog, which) -> {
                            dialog.cancel();
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            startActivityForResult(intent, 42);
                        });
                MaterialDialog alertDialog = builder.build();
                alertDialog.show();
            } else {
                uri = Uri.parse(uriString);
            }
        }

        mActionMode = findViewById(R.id.content_powerful_action_mode);
        mActionMode.setOnSelectionTaskListener((started, actionMode) -> toolbar.setVisibility(!started ? View.VISIBLE : View.GONE));

      //  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      //      int result = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
      //      int result2 = ContextCompat.checkSelfPermission(this, READ_PHONE_STATE);

      //      if (result != PackageManager.PERMISSION_GRANTED && result2 != PackageManager.PERMISSION_GRANTED)
      //      {
      //          ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
      //      }
      //  }

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        createHeaderView();
    }

    @Override
    public void onUserProfileUpdated()
    {
        createHeaderView();
    }

    private void createHeaderView()
    {
        View headerView = mNavigationView.getHeaderView(0);
        Configuration configuration = getApplication().getResources().getConfiguration();

        if (Build.VERSION.SDK_INT >= 24) {
            LocaleList list = configuration.getLocales();

            if (list.size() > 0)
                for (int pos = 0; pos < list.size(); pos++)
                    if (list.get(pos).toLanguageTag().startsWith("en")) {
                        break;
                    }
        }
        if (headerView != null) {
            NetworkDevice localDevice = AppUtils.getLocalDevice(getApplicationContext());

            ImageView imageView = headerView.findViewById(R.id.layout_profile_picture_image_default);
            ImageView editImageView = headerView.findViewById(R.id.layout_profile_picture_image_preferred);
            TextView deviceNameText = headerView.findViewById(R.id.header_default_device_name_text);
            TextView versionText = headerView.findViewById(R.id.header_default_device_version_text);

            deviceNameText.setText(localDevice.nickname);
            versionText.setText(localDevice.versionName);
            loadProfilePictureInto(localDevice.nickname, imageView);

            editImageView.setOnClickListener(v -> startProfileEditor());
        }
    }

    void setExternalSD_root() {
        File file = new File("/storage");
        File[] temp = file.listFiles();
        if (temp != null) {
            for (File aTemp : temp) {
                if (aTemp.isDirectory() && aTemp.canRead() && Objects.requireNonNull(aTemp.listFiles()).length > 0) {
                    isExternalSD_available = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 42) {
            assert data != null;
            uri = data.getData();

            SharedPreferences.Editor editor = getSharedPreferences(prefsName, 0).edit();
            editor.putString("treeuri", uri.toString());
            editor.apply();

            if (Build.VERSION.SDK_INT >= 21 && uri != null && getApplicationContext() != null) {
                grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                try {
                    DocumentFile documentFile = DocumentFile.fromUri(getApplicationContext(), uri, true);
                    AppUtils.getDatabase(getApplicationContext()).publish(
                            new WritablePathObject(documentFile.getName(), uri));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), R.string.mesg_somethingWentWrong, Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

  //  @Override
 //   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
 //   {
  //      if (requestCode == PERMISSION_REQUEST_CODE) {
  //          if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
  //              recreate();
   //         } else {
   //             finish();
   //         }
   //     }
   // }

    @Override
    public PowerfulActionMode getPowerfulActionMode()
    {
        return mActionMode;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        mChosenMenuItemId = item.getItemId();

        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void applyAwaitingDrawerAction()
    {
        if (mChosenMenuItemId == 0) {
            // Do nothing
        } else if (R.id.menu_activity_main_web_share == mChosenMenuItemId) {
            startActivity(new Intent(this, WebShareActivity.class));
        } else if (R.id.menu_activity_history == mChosenMenuItemId) {
            startActivity(new Intent(this, HistoryActivity.class));
        } else if (R.id.menu_activity_clear_cache_file == mChosenMenuItemId) {
            new DeleteView(this,null, DocumentFile.fromFile(getApplicationContext().getExternalCacheDir()));
        } else if (R.id.menu_activity_main_exit == mChosenMenuItemId) {
            exitApp();
        } else if (R.id.nav_leaf_share == mChosenMenuItemId) {

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "*Best File Explorer & File Sharing app* download now. https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName();
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share App");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));

        } else if (R.id.about_leaf_me == mChosenMenuItemId) {
            aboutMyApp();
        } else if (R.id.leaf_privacypolicy == mChosenMenuItemId) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Privacy Policy");

            WebView wv = new WebView(this);
            wv.getSettings().setJavaScriptEnabled(true);
            wv.loadUrl("file:///android_asset/PrivacyPolicy.txt"); //Your Privacy Policy Url Here
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.getSettings().setJavaScriptEnabled(true);
                    view.loadUrl(url);

                    return true;
                }
            });

            alert.setView(wv);
            alert.setNegativeButton("Close", (dialog, id) -> dialog.dismiss());
            alert.show();
        } else if (R.id.leaf_rate_us == mChosenMenuItemId) {

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));

        } else if (R.id.leaf_moreapp == mChosenMenuItemId) {

            Uri uri = Uri.parse("market://search?q=pub:" + "The Mahe"); //Developer AC Name
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/search?q=pub:" + "The Mahe"))); //Developer AC Name
            }
        }

        mChosenMenuItemId = 0;
    }

    private void aboutMyApp() {

        MaterialDialog.Builder bulder = new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .customView(R.layout.about, true)
                .titleColorRes(android.R.color.black)
                .positiveText("MORE APPS")
                .positiveColor(getResources().getColor(android.R.color.black))
                .icon(getResources().getDrawable(R.mipmap.ic_launcher))
                .limitIconToDefaultSize()
                .onPositive((dialog, which) -> {

                    Uri uri = Uri.parse("market://search?q=pub:" + "The Mahe"); //Developer AC Name
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/search?q=pub:" + "The Mahe"))); //Developer AC Name
                    }
                });

        MaterialDialog materialDialog = bulder.build();

        TextView versionCode = (TextView) materialDialog.findViewById(R.id.version_code);
        TextView versionName = (TextView) materialDialog.findViewById(R.id.version_name);
        versionCode.setText("Version Code : " + BuildConfig.VERSION_CODE);
        versionName.setText("Version Name : " + BuildConfig.VERSION_NAME);

        materialDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (mMainFragment.onBackPressed())
            return;

        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else if ((System.currentTimeMillis() - mExitPressTime) < 2000) {
            exitApp();
            super.onBackPressed();
        } else {
            mExitPressTime = System.currentTimeMillis();
            Toast.makeText(this, R.string.mesg_secureExit, Toast.LENGTH_SHORT).show();
        }
    }

    public static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available Devices";
            case WifiP2pDevice.INVITED:
                return "INVITED";
            case WifiP2pDevice.CONNECTED:
                return "CONNECTED";
            case WifiP2pDevice.FAILED:
                return "FAILED";
            case WifiP2pDevice.UNAVAILABLE:
                return "UNAVAILABLE";
            default:
                return "default";
        }
    }

}
