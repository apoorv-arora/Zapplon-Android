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

/**
 * Created by apoorvarora on 06/03/16.
 */
public class MyBookings  extends ActionBarActivity implements ZTabClickCallback {

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
    private static final int VIEWPAGER_INDEX_INTRACITY_FRAGMENT = 0;
    private static final int VIEWPAGER_INDEX_INTERCITY_FRAGMENT = 1;
//    private static final int VIEWPAGER_INDEX_SELFDRIVE_FRAGMENT = 2;

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
        homePager.setCurrentItem(VIEWPAGER_INDEX_INTRACITY_FRAGMENT);
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

        SpannableString s = new SpannableString(getResources().getString(R.string.deal_history_text));
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
                .getChildAt(VIEWPAGER_INDEX_INTRACITY_FRAGMENT);
        final TextView deal2Header = (TextView) ((LinearLayout) tabs.getChildAt(0))
                .getChildAt(VIEWPAGER_INDEX_INTERCITY_FRAGMENT);

//        final TextView deal3Header = (TextView) ((LinearLayout) tabs.getChildAt(0))
//                .getChildAt(VIEWPAGER_INDEX_SELFDRIVE_FRAGMENT);

        deal1Header.setTextColor(getResources().getColor(tabsSelectedColor));
        deal2Header.setTextColor(getResources().getColor(tabsUnselectedColor));
//        deal3Header.setTextColor(getResources().getColor(tabsUnselectedColor));

        setPageChangeListenerOnTabs(tabs, tabsUnselectedColor, tabsSelectedColor, deal1Header, deal2Header);
    }


    @Override
    public void onTabClick(int position) {
        if (currentPageSelected == position) {

            try {
                switch (position) {

                    case VIEWPAGER_INDEX_INTERCITY_FRAGMENT:

                        // Home Scroll Top
                        if (fragments.get(VIEWPAGER_INDEX_INTERCITY_FRAGMENT) != null) {
                            MyIntercityBookingsFragment hf = (MyIntercityBookingsFragment) fragments.get(VIEWPAGER_INDEX_INTERCITY_FRAGMENT).get();
                            if (hf != null) {
                            }
                        } else {
                            MyPagerAdapter hAdapter = (MyPagerAdapter) homePager.getAdapter();
                            if (hAdapter != null) {
                                try {
                                    MyIntercityBookingsFragment fragMent = (MyIntercityBookingsFragment) hAdapter.instantiateItem(homePager,
                                            VIEWPAGER_INDEX_INTERCITY_FRAGMENT);
                                    if (fragMent != null){
                                    }
                                    //      fragMent.scrollToTop();
                                } catch (Exception e) {
                                }
                            }
                        }
                        break;

                    case VIEWPAGER_INDEX_INTRACITY_FRAGMENT:

                        // Search Scroll Top
                        if (fragments.get(VIEWPAGER_INDEX_INTRACITY_FRAGMENT) != null) {
                            MyIntracityBookingsFragment srf = (MyIntracityBookingsFragment) fragments.get(VIEWPAGER_INDEX_INTRACITY_FRAGMENT).get();
                            if (srf != null) {
                                //srf.scrollToTop();
                            }

                        } else {
                            MyPagerAdapter hAdapter = (MyPagerAdapter) homePager.getAdapter();
                            if (hAdapter != null) {
                                try {
                                    MyIntracityBookingsFragment fragMent = (MyIntracityBookingsFragment) hAdapter.instantiateItem(homePager,
                                            VIEWPAGER_INDEX_INTRACITY_FRAGMENT);
                                    if (fragMent != null){
                                    }
                                    //fragMent.scrollToTop();
                                } catch (Exception e) {
                                }
                            }
                        }

                        break;


//                    case VIEWPAGER_INDEX_SELFDRIVE_FRAGMENT:
//
//                        // Search Scroll Top
//                        if (fragments.get(VIEWPAGER_INDEX_SELFDRIVE_FRAGMENT) != null) {
//                            SelfDriveBookings srf = (SelfDriveBookings) fragments.get(VIEWPAGER_INDEX_SELFDRIVE_FRAGMENT).get();
//                            if (srf != null) {
//                                // srf.scrollToTop();
//                            }
//
//                        } else {
//                            MyPagerAdapter hAdapter = (MyPagerAdapter) homePager.getAdapter();
//                            if (hAdapter != null) {
//                                try {
//                                    SelfDriveBookings fragMent = (SelfDriveBookings) hAdapter.instantiateItem(homePager,
//                                            VIEWPAGER_INDEX_SELFDRIVE_FRAGMENT);
//                                    if (fragMent != null){}
//                                    // fragMent.scrollToTop();
//                                } catch (Exception e) {
//                                }
//                            }
//                        }
//
//                        break;

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
                if (arg0 == VIEWPAGER_INDEX_INTERCITY_FRAGMENT) {

                    if (fragments.get(VIEWPAGER_INDEX_INTERCITY_FRAGMENT) != null) {

                        if (fragments.get(VIEWPAGER_INDEX_INTERCITY_FRAGMENT).get() instanceof MyIntracityBookingsFragment) {
                            MyIntercityBookingsFragment intercityBookings = (MyIntercityBookingsFragment) fragments.get(VIEWPAGER_INDEX_INTERCITY_FRAGMENT).get();
                            if (intercityBookings != null) {
                                // if (!srf.searchCallsInitiatedFromHome)
                                // srf.initiateSearchCallFromHome();

                            }
                        }

                    } else {
                        MyPagerAdapter hAdapter = (MyPagerAdapter) homePager.getAdapter();
                        if (hAdapter != null) {
                            try {
                                MyIntercityBookingsFragment fragMent = (MyIntercityBookingsFragment) hAdapter.instantiateItem(homePager,
                                        VIEWPAGER_INDEX_INTERCITY_FRAGMENT);
                                if (fragMent != null) {
                                    // if
                                    // (!fragMent.searchCallsInitiatedFromHome)
                                    // fragMent.initiateSearchCallFromHome();

                                }
                            } catch (Exception e) {
                                // Crashlytics.logException(e);
                            }
                        }
                    }

                    deal2Header.setTextColor(getResources().getColor(tabsSelectedColor));
                    deal1Header.setTextColor(getResources().getColor(tabsUnselectedColor));
//                    deal3Header.setTextColor(getResources().getColor(tabsUnselectedColor));

                } else if (arg0 == VIEWPAGER_INDEX_INTRACITY_FRAGMENT) {

                    if (fragments.get(VIEWPAGER_INDEX_INTRACITY_FRAGMENT) != null) {

                        if (fragments.get(VIEWPAGER_INDEX_INTRACITY_FRAGMENT).get() instanceof MyIntracityBookingsFragment) {
                            MyIntracityBookingsFragment srf = (MyIntracityBookingsFragment) fragments.get(VIEWPAGER_INDEX_INTRACITY_FRAGMENT).get();
                            if (srf != null) {

                                // if (!srf.searchCallsInitiatedFromHome)
                                // srf.initiateSearchCallFromHome();

                            }
                        }

                    } else {
                        MyPagerAdapter hAdapter = (MyPagerAdapter) homePager.getAdapter();
                        if (hAdapter != null) {
                            try {
                                MyIntracityBookingsFragment fragMent = (MyIntracityBookingsFragment) hAdapter.instantiateItem(homePager,
                                        VIEWPAGER_INDEX_INTRACITY_FRAGMENT);
                                if (fragMent != null) {

                                    // if
                                    // (!fragMent.searchCallsInitiatedFromHome)
                                    // fragMent.initiateSearchCallFromHome();

                                }
                            } catch (Exception e) {
                                // Crashlytics.logException(e);
                            }
                        }
                    }

                    deal1Header.setTextColor(getResources().getColor(tabsSelectedColor));
                    deal2Header.setTextColor(getResources().getColor(tabsUnselectedColor));
//                    deal3Header.setTextColor(getResources().getColor(tabsUnselectedColor));
                }

//                else if (arg0 == VIEWPAGER_INDEX_SELFDRIVE_FRAGMENT) {
//
//                    if (fragments.get(VIEWPAGER_INDEX_SELFDRIVE_FRAGMENT) != null) {
//
//                        if (fragments.get(VIEWPAGER_INDEX_SELFDRIVE_FRAGMENT).get() instanceof SelfDriveBookings) {
//                            SelfDriveBookings srf = (SelfDriveBookings) fragments.get(VIEWPAGER_INDEX_SELFDRIVE_FRAGMENT).get();
//                            if (srf != null) {
//
//                                // if (!srf.searchCallsInitiatedFromHome)
//                                // srf.initiateSearchCallFromHome();
//
//                            }
//                        }
//
//                    } else {
//                        MyPagerAdapter hAdapter = (MyPagerAdapter) homePager.getAdapter();
//                        if (hAdapter != null) {
//                            try {
//                                SelfDriveBookings fragMent = (SelfDriveBookings) hAdapter.instantiateItem(homePager,
//                                        VIEWPAGER_INDEX_SELFDRIVE_FRAGMENT);
//                                if (fragMent != null) {
//
//                                    // if
//                                    // (!fragMent.searchCallsInitiatedFromHome)
//                                    // fragMent.initiateSearchCallFromHome();
//
//                                }
//                            } catch (Exception e) {
//                                // Crashlytics.logException(e);
//                            }
//                        }
//                    }
//
//                    deal3Header.setTextColor(getResources().getColor(tabsSelectedColor));
//                    deal2Header.setTextColor(getResources().getColor(tabsUnselectedColor));
//                    deal1Header.setTextColor(getResources().getColor(tabsUnselectedColor));
//                }
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

        private String[] ids = { getResources().getString(R.string.intracity).toUpperCase(), getResources().getString(R.string.intercity).toUpperCase()};

        public String getPageTitle(int pos) {
            return ids[pos];
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {

                case VIEWPAGER_INDEX_INTERCITY_FRAGMENT: {
                    CommonLib.ZLog("Intercity fragment", "Creating new intercity fragment");
                    MyIntercityBookingsFragment intercityBookings = new MyIntercityBookingsFragment();
                    fragments.put(VIEWPAGER_INDEX_INTERCITY_FRAGMENT, new SoftReference<Fragment>(intercityBookings));
                    return intercityBookings;
                }

                case VIEWPAGER_INDEX_INTRACITY_FRAGMENT: {
                    MyIntracityBookingsFragment intracityBookings = new MyIntracityBookingsFragment();
                    fragments.put(VIEWPAGER_INDEX_INTRACITY_FRAGMENT, new SoftReference<Fragment>(intracityBookings));
                    return intracityBookings;
                }


//                case VIEWPAGER_INDEX_SELFDRIVE_FRAGMENT: {
//                    SelfDriveBookings selfDriveBookings = new SelfDriveBookings();
//                    fragments.put(VIEWPAGER_INDEX_INTRACITY_FRAGMENT, new SoftReference<Fragment>(selfDriveBookings));
//                    return selfDriveBookings;
//                }

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
