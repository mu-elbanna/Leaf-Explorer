package com.leaf.explorer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.leaf.explorer.activity.LeafActivity;
import com.leaf.explorer.app.AppActivity;
import com.leaf.explorer.file_share.util.AppUtils;

public class SplashActivity extends AppActivity
{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MaterialButton mStart = findViewById(R.id.mStart);
        mStart.setOnClickListener(v -> gotoMainActivity());

        if (!AppUtils.AppcheckRunningConditions(this)) {
            mStart.setVisibility(View.VISIBLE);
        } else {
            new Handler().postDelayed(this::gotoMainActivity, 2000);
        }

    }

    private void gotoMainActivity() {
        startActivity(new Intent(this, LeafActivity.class));
        finish();
    }


}
