package com.zeezo.dosticard;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;

/**
 * Created by masho on 23-Jan-17.
 */
public class JoinStore extends FragmentActivity {

    TabLayout tabLayout;
    ViewPager viewPager;

    LinearLayout joinStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_store);

        joinStore = (LinearLayout) findViewById(R.id.idJoinStoreLayout);

        tabLayout = (TabLayout) findViewById(R.id.idTabLayout);
        viewPager = (ViewPager) findViewById(R.id.idViewPager);

        viewPager.setAdapter(new CustomAdapter(getSupportFragmentManager(), getApplicationContext()));
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });
    }

    private class CustomAdapter extends FragmentPagerAdapter {

        private String fragments[] = {"Add New Store", "Join Existing Store", "Add Store Branch"};

        public CustomAdapter(FragmentManager supportFragmentManager, Context applicationContext) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new NewStore();
                case 1:
                    return new ExistingStore();
                case 2:
                    return new NewStoreBranch();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments[position];
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            //Toast.makeText(getApplicationContext(), "Landscape", Toast.LENGTH_LONG).show();
            joinStore.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_background_landscape));
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(getApplicationContext(), "Portrait", Toast.LENGTH_LONG).show();
            joinStore.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_background));
        }
    }
}