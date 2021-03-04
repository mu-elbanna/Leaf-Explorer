package com.leaf.explorer.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.leaf.explorer.R;
import com.leaf.explorer.app.AppActivity;
import com.leaf.explorer.file_share.adapter.SmartFragmentPagerAdapter;
import com.leaf.explorer.file_share.fragment.ApplicationListFragment;
import com.leaf.explorer.file_share.fragment.ImageListFragment;
import com.leaf.explorer.file_share.fragment.MusicListFragment;
import com.leaf.explorer.file_share.fragment.VideoListFragment;
import com.leaf.explorer.fragments.ArchiveFragment;

import java.util.Objects;

public class PanelActivity extends AppActivity {

    public static final String TAG = PanelActivity.class.getSimpleName();

    private SmartFragmentPagerAdapter pagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_manager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        final TabLayout tabLayout = findViewById(R.id.activity_content_sharing_tab_layout);
        viewPager = findViewById(R.id.activity_content_sharing_view_pager);

        pagerAdapter = new SmartFragmentPagerAdapter(this, getSupportFragmentManager());

        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(0, MusicListFragment.class, null));
        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(1, VideoListFragment.class, null));
        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(2, ApplicationListFragment.class, null));
        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(3, ImageListFragment.class, null));
        pagerAdapter.add(new SmartFragmentPagerAdapter.StableItem(4, ArchiveFragment.class, null));

        pagerAdapter.createTabs(tabLayout, false, true);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(final TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });

        int scrollto = Objects.requireNonNull(getIntent().getExtras()).getInt("no.");
        viewPager.setCurrentItem(scrollto, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home)
            finish();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onBackPressed() {

        Object activeItem = pagerAdapter.getItem(viewPager.getCurrentItem());

        if ((activeItem instanceof AppActivity.OnBackPressedListener
                && ((AppActivity.OnBackPressedListener) activeItem).onBackPressed())) {
        } else {
            // Let the system handle the back button
            super.onBackPressed();
        }

        //   if (viewPager.getCurrentItem() > 0) {
        //       viewPager.setCurrentItem(0, true);
        //   } else if ((activeItem instanceof Activity.OnBackPressedListener
        //           && ((Activity.OnBackPressedListener) activeItem).onBackPressed())) {

        //   } else {
        //       // Let the system handle the back button
        //       super.onBackPressed();
        //   }
    }

}
