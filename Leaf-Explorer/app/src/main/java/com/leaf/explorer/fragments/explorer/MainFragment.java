package com.leaf.explorer.fragments.explorer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.genonbeta.android.framework.ui.callback.SnackbarSupport;
import com.google.android.material.tabs.TabLayout;
import com.leaf.explorer.R;
import com.leaf.explorer.app.AppActivity;
import com.leaf.explorer.file_share.adapter.SmartFragmentPagerAdapter;
import com.leaf.explorer.file_share.model.TitleSupport;
import com.leaf.explorer.fragments.explorer.StorageExplorerFragment;
import com.leaf.explorer.fragments.explorer.StorageExplorerFragment2;

public class MainFragment extends com.genonbeta.android.framework.app.Fragment
        implements TitleSupport, SnackbarSupport, com.genonbeta.android.framework.app.FragmentImpl, AppActivity.OnBackPressedListener {

    private ViewPager mViewPager;
    private SmartFragmentPagerAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.fragment_main, container, false);

        final TabLayout tabLayout = view.findViewById(R.id.layout_main_tab_layout);
        mViewPager = view.findViewById(R.id.layout_main_view_pager);
        mAdapter = new SmartFragmentPagerAdapter(getContext(), getChildFragmentManager());

        mAdapter.add(new SmartFragmentPagerAdapter.StableItem(0, StorageExplorerFragment.class, null));
        mAdapter.add(new SmartFragmentPagerAdapter.StableItem(1, StorageExplorerFragment2.class, null));

        mAdapter.createTabs(tabLayout, false, true);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                mViewPager.setCurrentItem(tab.getPosition());

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

        return view;
    }

    @Override
    public CharSequence getTitle(Context context)
    {
        return context.getString(R.string.text_home);
    }

    @Override
    public boolean onBackPressed()
    {
        Object activeItem = mAdapter.getItem(mViewPager.getCurrentItem());

        return activeItem instanceof AppActivity.OnBackPressedListener
                && ((AppActivity.OnBackPressedListener) activeItem).onBackPressed();
    }
}
