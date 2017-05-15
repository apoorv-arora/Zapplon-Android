package com.application.zapplon.views;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.NoSwipeViewPager;
import com.application.zapplon.utils.PagerSlidingTabStrip;
import com.application.zapplon.utils.TypefaceSpan;
import com.application.zapplon.utils.ZTabClickCallback;

import java.lang.ref.SoftReference;

public class IntercityActivity extends ActionBarActivity implements ZTabClickCallback {

    private ZApplication zapp;
    private SharedPreferences prefs;
    private int width;

    private NoSwipeViewPager homePager;
    private SparseArray<SoftReference<Fragment>> fragments = new SparseArray<SoftReference<Fragment>>();

    LayoutInflater inflater;

    private boolean destroyed = false;

    int currentPageSelected = 0;
    public boolean fromExternalSource = false;

    // Viewpager Fragment Index
    private static final int VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT = 0;
    private static final int VIEWPAGER_INDEX_ONEWAY_FRAGMENT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_bar_activity);

        inflater = LayoutInflater.from(this);
        prefs = getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getApplication();
        width = getWindowManager().getDefaultDisplay().getWidth();

        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setUpActionBar();

        // Home tabs
        homePager = (NoSwipeViewPager) findViewById(R.id.pager);
        homePager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        homePager.setOffscreenPageLimit(2);
        homePager.setCurrentItem(VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT);
        homePager.setSwipeable(true);

        setUpTabs();
    }


    private void setUpActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        SpannableString s = new SpannableString(getResources().getString(R.string.intercity));
        s.setSpan(
                new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
                        getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
                0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        final boolean isAndroidL = Build.VERSION.SDK_INT >= 21; // Build.AndroidL
        if (!isAndroidL)
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.zapplon_dark_feedback));

        actionBar.setTitle(s);
    }

    private void setUpTabs() {
        // tabs
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setmZapplonHome(false);
        tabs.setAllCaps(false);
        tabs.setForegroundGravity(Gravity.LEFT);
        tabs.setShouldExpand(true);
        tabs.setViewPager(homePager);
        tabs.setDividerColor(getResources().getColor(R.color.transparent1));
        tabs.setBackgroundColor(getResources().getColor(R.color.color_blue));
        tabs.setUnderlineColor(getResources().getColor(R.color.zhl_dark));
        tabs.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold), 0);
        tabs.setIndicatorColor(getResources().getColor(R.color.white));
        tabs.setIndicatorHeight((int) getResources().getDimension(R.dimen.height3));
        tabs.setTextSize((int) getResources().getDimension(R.dimen.size15));
        tabs.setUnderlineHeight(0);
        tabs.setTabPaddingLeftRight(12);
        tabs.setInterfaceForClick(this);

        final int tabsUnselectedColor = R.color.zdhl3;
        final int tabsSelectedColor = R.color.white;

        final TextView deal1Header = (TextView) ((LinearLayout) tabs.getChildAt(0))
                .getChildAt(VIEWPAGER_INDEX_ONEWAY_FRAGMENT);
        final TextView deal2Header = (TextView) ((LinearLayout) tabs.getChildAt(0))
                .getChildAt(VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT);

        deal1Header.setTextColor(getResources().getColor(tabsUnselectedColor));
        deal2Header.setTextColor(getResources().getColor(tabsSelectedColor));

        setPageChangeListenerOnTabs(tabs, tabsUnselectedColor, tabsSelectedColor, deal1Header, deal2Header);
    }


    @Override
    public void onTabClick(int position) {
        if (currentPageSelected == position) {

            try {
                switch (position) {

                    case VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT:

                        // Home Scroll Top
                        if (fragments.get(VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT) != null) {
                            IntercitySearchFragment hf = (IntercitySearchFragment) fragments.get(VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT).get();
                            if (hf != null) {
                                hf.setType(IntercitySearchFragment.ROUND_TRIP_FRAGMENT);
                            }
                        } else {
                            MyPagerAdapter hAdapter = (MyPagerAdapter) homePager.getAdapter();
                            if (hAdapter != null) {
                                try {
                                    IntercitySearchFragment fragMent = (IntercitySearchFragment) hAdapter.instantiateItem(homePager,
                                            VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT);
                                    if (fragMent != null){
                                        fragMent.setType(IntercitySearchFragment.ROUND_TRIP_FRAGMENT);
                                    }
                                    //      fragMent.scrollToTop();
                                } catch (Exception e) {
                                }
                            }
                        }
                        break;

                    case VIEWPAGER_INDEX_ONEWAY_FRAGMENT:

                        // Search Scroll Top
                        if (fragments.get(VIEWPAGER_INDEX_ONEWAY_FRAGMENT) != null) {
                            IntercitySearchFragment srf = (IntercitySearchFragment) fragments.get(VIEWPAGER_INDEX_ONEWAY_FRAGMENT).get();
                            if (srf != null) {
                                srf.setType(IntercitySearchFragment.ONE_WAY_FRAGMENT);
                            }

                        } else {
                            MyPagerAdapter hAdapter = (MyPagerAdapter) homePager.getAdapter();
                            if (hAdapter != null) {
                                try {
                                    IntercitySearchFragment fragMent = (IntercitySearchFragment) hAdapter.instantiateItem(homePager,
                                            VIEWPAGER_INDEX_ONEWAY_FRAGMENT);
                                    if (fragMent != null) {
                                        fragMent.setType(IntercitySearchFragment.ONE_WAY_FRAGMENT);
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }

                        break;
                }
            } catch (Exception e) {

            }

        }
    }

    private void setPageChangeListenerOnTabs(PagerSlidingTabStrip tabs, final int tabsUnselectedColor,
                                             final int tabsSelectedColor, final TextView deal1Header, final TextView deal2Header) {
        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {

                currentPageSelected = arg0;

                // SearchFragment
                if (arg0 == VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT) {

                    if (fragments.get(VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT) != null) {

                        if (fragments.get(VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT).get() instanceof IntercitySearchFragment) {
                            IntercitySearchFragment intercityBookings = (IntercitySearchFragment) fragments.get(VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT).get();
                            if (intercityBookings != null) {
                                intercityBookings.setType(IntercitySearchFragment.ROUND_TRIP_FRAGMENT);
                            }
                        }

                    } else {
                        MyPagerAdapter hAdapter = (MyPagerAdapter) homePager.getAdapter();
                        if (hAdapter != null) {
                            try {
                                IntercitySearchFragment fragMent = (IntercitySearchFragment) hAdapter.instantiateItem(homePager,
                                        VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT);
                                if (fragMent != null) {
                                    fragMent.setType(IntercitySearchFragment.ONE_WAY_FRAGMENT);
                                }
                            } catch (Exception e) {
                            }
                        }
                    }

                    deal2Header.setTextColor(getResources().getColor(tabsSelectedColor));
                    deal1Header.setTextColor(getResources().getColor(tabsUnselectedColor));

                } else if (arg0 == VIEWPAGER_INDEX_ONEWAY_FRAGMENT) {

                    if (fragments.get(VIEWPAGER_INDEX_ONEWAY_FRAGMENT) != null) {

                        if (fragments.get(VIEWPAGER_INDEX_ONEWAY_FRAGMENT).get() instanceof MyIntracityBookingsFragment) {
                            IntercitySearchFragment srf = (IntercitySearchFragment) fragments.get(VIEWPAGER_INDEX_ONEWAY_FRAGMENT).get();
                            if (srf != null) {
                                srf.setType(IntercitySearchFragment.ONE_WAY_FRAGMENT);
                            }
                        }

                    } else {
                        MyPagerAdapter hAdapter = (MyPagerAdapter) homePager.getAdapter();
                        if (hAdapter != null) {
                            try {
                                IntercitySearchFragment fragMent = (IntercitySearchFragment) hAdapter.instantiateItem(homePager,
                                        VIEWPAGER_INDEX_ONEWAY_FRAGMENT);
                                if (fragMent != null) {
                                    fragMent.setType(IntercitySearchFragment.ONE_WAY_FRAGMENT);
                                }
                            } catch (Exception e) {
                            }
                        }
                    }

                    deal1Header.setTextColor(getResources().getColor(tabsSelectedColor));
                    deal2Header.setTextColor(getResources().getColor(tabsUnselectedColor));
                }

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (position == 0) {

                    int alphaValueUnderline = (int) ((((positionOffset - 0) * (255 - 0)) / (1 - 0)) + 0);
                    ((PagerSlidingTabStrip) findViewById(R.id.tabs))
                            .setUnderlineColor(Color.argb(alphaValueUnderline, 228, 228, 228));

                } else if (position > 0) {
                    ((PagerSlidingTabStrip) findViewById(R.id.tabs)).setUnderlineColor(Color.argb(255, 255, 125, 105));
                }
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            fragments.put(position, null);
            super.destroyItem(container, position, object);
        }


        @Override
        public int getCount() {
            return 2;
        }

        private String[] ids = { getResources().getString(R.string.round_trip), getResources().getString(R.string.one_way)};

        public String getPageTitle(int pos) {
            return ids[pos];
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {

                case VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT: {
                    CommonLib.ZLog("Intercity fragment", "Creating new round trip fragment");
                    IntercitySearchFragment roundTripBookings = new IntercitySearchFragment();
                    roundTripBookings.setType(IntercitySearchFragment.ROUND_TRIP_FRAGMENT);
                    fragments.put(VIEWPAGER_INDEX_ROUNDTRIP_FRAGMENT, new SoftReference<Fragment>(roundTripBookings));
                    return roundTripBookings;
                }

                case VIEWPAGER_INDEX_ONEWAY_FRAGMENT: {
                    CommonLib.ZLog("Intercity fragment", "Creating new one way fragment");
                    IntercitySearchFragment onewayBookings = new IntercitySearchFragment();
                    onewayBookings.setType(IntercitySearchFragment.ONE_WAY_FRAGMENT);
                    fragments.put(VIEWPAGER_INDEX_ONEWAY_FRAGMENT, new SoftReference<Fragment>(onewayBookings));
                    return onewayBookings;
                }


            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
