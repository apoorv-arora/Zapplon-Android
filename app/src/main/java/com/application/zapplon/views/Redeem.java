package com.application.zapplon.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.Voucher;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.RequestWrapper;
import com.application.zapplon.utils.TypefaceSpan;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.UploadManagerCallback;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

public class Redeem extends AppCompatActivity implements UploadManagerCallback{

    private ZApplication zapp;
    private int width, height;
    private boolean destroyed = false;
    private boolean loading = false;
    SharedPreferences prefs;
    ProgressDialog progressDialog;

    ArrayList<Voucher> wishes;
    private MyListAdapter mAdapter;
    private ListView mListView;
    LinearLayout mListViewFooter;
    Activity mContext;
    ArrayList<GetImage> getImageArray = new ArrayList<GetImage>();
    private int mScrollState;

    private int count = 10;
    private int mWishesTotalCount;
    
    private AsyncTask mAsyncRunning, mAsyncRunning2;
    private LayoutInflater inflater;
    View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.redeem_layout);
        prefs = getApplicationContext().getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getApplication();
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();
        inflater = LayoutInflater.from(this);
        mContext = this;

        mListView = (ListView) findViewById(R.id.voucher_listView);
        mListView.setDivider(null);
        mListView.setDividerHeight(width/20);

        try {
            headerView = inflater.inflate(R.layout.redeem_list_header, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(headerView != null) {
            ImageView coins = (ImageView) headerView.findViewById(R.id.coins);
            coins.getLayoutParams().height = height/2;
            coins.getLayoutParams().width = width;
            coins.setImageBitmap(CommonLib.getBitmap(mContext, R.drawable.redeemcoins, width, height));

            mListView.addHeaderView(headerView);
        }

        setupActionBar();
        setListeners();
        refreshView();
        UploadManager.addCallback(this);

    }

    @Override
    public void onDestroy() {
        destroyed = true;
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        UploadManager.removeCallback(this);
        super.onDestroy();
    }

    private void refreshView() {
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);
        mAsyncRunning = new GetVouchers().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        if (mAsyncRunning2 != null)
            mAsyncRunning2.cancel(true);
        mAsyncRunning2 = new GetPoints().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
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

    private void setListeners() {
        findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshView();
            }
        });
    }


    private void setupActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        SpannableString s = new SpannableString("Redeem");
        s.setSpan(
                new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
                        getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
                0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        final boolean isAndroidL = Build.VERSION.SDK_INT >= 21; // Build.AndroidL
        if (!isAndroidL)
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.zapplon_dark_feedback));

        actionBar.setTitle(s);
    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if(requestType == CommonLib.AVAIL_VOUCHER) {
            if(progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if(!destroyed && status) {
                Toast.makeText(mContext, "Congratulations \\nYour voucher will be mailed to you", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }

    private class GetPoints extends AsyncTask<Object, Void, Object> {

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                String url = "";
                url = CommonLib.SERVER + "user/viewPoints?";
                Object info = RequestWrapper.RequestHttp(url, RequestWrapper.GET_POINTS, RequestWrapper.FAV);
                CommonLib.ZLog("url", url);
                return info;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (destroyed)
                return;

            if (headerView != null && result != null && result instanceof Integer && ((int)result) != -1) {
                ((TextView)headerView.findViewById(R.id.current_zapps)).setText(String.valueOf(result));
                ((TextView)headerView.findViewById(R.id.total_zapps)).setText(mContext.getResources().getString(R.string.total_zapps));
            }
        }
    }

    public class MyListAdapter extends ArrayAdapter<Voucher> {

        private List<Voucher> wishes;

        public MyListAdapter(Activity context, int resourceId, List<Voucher> wishes) {
            super(context.getApplicationContext(), resourceId, wishes);
            this.wishes = wishes;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Voucher wish = wishes.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.voucher_layout, null);
            }

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder();
                viewHolder.comapny_name = (TextView) convertView.findViewById(R.id.company_name);
                viewHolder.voucher_value = (TextView) convertView.findViewById(R.id.v_value);
                viewHolder.logo = (ImageView) convertView.findViewById(R.id.v_image);
                viewHolder.voucher_value_remaining = (TextView) convertView.findViewById(R.id.v_value_remaining);
                viewHolder.redeem = (TextView) convertView.findViewById(R.id.redeem);
                convertView.setTag(viewHolder);
            }

            setImageFromUrlOrDisk(wish.getImage_url(), viewHolder.logo, "", width, height, false);
            convertView.findViewById(R.id.redeem).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (wish.isValid()) {
                        progressDialog = ProgressDialog.show(mContext, null, "Checking details. Please wait!!!");
                        UploadManager.availVoucher(wish.getVoucherId());
                    } else {
                        Toast.makeText(mContext, "You cannot avail this voucher right now", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            viewHolder.comapny_name.setText(wish.getComapany_name());
            viewHolder.voucher_value.setText("Rs. " + wish.getValue() + "");

            if(wish.getZappsRequired() > 0) {
                if (wish.getZappsRequired() == 1)
                    viewHolder.voucher_value_remaining.setText(wish.getZappsRequired() + " more Zapp Required");
                else
                    viewHolder.voucher_value_remaining.setText(wish.getZappsRequired() + " more Zapps Required");
                viewHolder.voucher_value_remaining.setVisibility(View.VISIBLE);
            } else {
                viewHolder.voucher_value_remaining.setVisibility(View.INVISIBLE);
            }

            setImageFromUrlOrDisk(wish.getImage_url(), viewHolder.logo, "", width / 20, width / 20, false);
            if(wish.isValid()) {
                viewHolder.redeem.setText(getResources().getString(R.string.redeem));
                viewHolder.redeem.setVisibility(View.VISIBLE);
            } else {
                viewHolder.redeem.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

        protected class ViewHolder {
            TextView comapny_name;
            TextView voucher_value;
            TextView voucher_value_remaining;
            TextView redeem;
            ImageView logo;
        }

    }

    private class GetVouchers extends AsyncTask<Object, Void, Object> {

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progress_container).setVisibility(View.VISIBLE);

            findViewById(R.id.content).setAlpha(1f);

            findViewById(R.id.content).setVisibility(View.GONE);

            findViewById(R.id.empty_view).setVisibility(View.GONE);
            super.onPreExecute();
        }

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                String url = "";
                url = CommonLib.SERVER + "voucher/list?start=0&count=" + count;
                Object info = RequestWrapper.RequestHttp(url, RequestWrapper.GET_VOUCHER, RequestWrapper.FAV);
                CommonLib.ZLog("url", url);
                return info;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (destroyed)
                return;

            findViewById(R.id.progress_container).setVisibility(View.GONE);
            if (result != null) {
                findViewById(R.id.content).setVisibility(View.VISIBLE);
                if (result instanceof Object[]) {
                    Object[] arr = (Object[]) result;
                    mWishesTotalCount = (Integer) arr[0];
                    setWishes((ArrayList<Voucher>) arr[1]);
                    if( ((ArrayList<Voucher>) arr[1]).size()  == 0 ) {
                        findViewById(R.id.content).setVisibility(View.GONE);
                        findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.empty_view_text)).setText("Nothing here yet");

                    } else {
                        findViewById(R.id.content).setVisibility(View.VISIBLE);
                        findViewById(R.id.empty_view).setVisibility(View.GONE);
                    }
                }
            } else {
                if (CommonLib.isNetworkAvailable(Redeem.this)) {
                    Toast.makeText(mContext, getResources().getString(R.string.error_try_again),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
                            .show();

                    findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

                    findViewById(R.id.content).setVisibility(View.GONE);
                }
            }
        }

    }

    // set all the wishes here
    private void setWishes(ArrayList<Voucher> wishes) {
        this.wishes = wishes;
        if (wishes != null && wishes.size() > 0 && mWishesTotalCount > wishes.size()
                && mListView.getFooterViewsCount() == 0) {
            mListViewFooter = new LinearLayout(getApplicationContext().getApplicationContext());
            mListViewFooter.setBackgroundResource(R.color.white);
            mListViewFooter.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, width /5));
            mListViewFooter.setGravity(Gravity.CENTER);
            mListViewFooter.setOrientation(LinearLayout.HORIZONTAL);
            ProgressBar pbar = new ProgressBar(mContext, null,
                    android.R.attr.progressBarStyleSmallInverse);
            mListViewFooter.addView(pbar);
            pbar.setTag("progress");
            pbar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mListView.addFooterView(mListViewFooter);
        }
        mAdapter = new MyListAdapter(Redeem.this, R.layout.voucher_layout, this.wishes);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount - 1 < mWishesTotalCount
                        && !loading && mListViewFooter != null) {
                    if (mListView.getFooterViewsCount() == 1) {
                        loading = true;
                        new LoadModeWishes().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, totalItemCount - 1);
                    }
                } else if (totalItemCount - 1 == mWishesTotalCount && mListView.getFooterViewsCount() > 0) {
                    mListView.removeFooterView(mListViewFooter);
                }
            }
        });
    }

    private void setImageFromUrlOrDisk(final String url, final ImageView imageView, final String type, int width,
                                       int height, boolean useDiskCache) {

        if (cancelPotentialWork(url, imageView)) {

            GetImage task = new GetImage(url, imageView, width, height, useDiskCache, type);

            final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), zapp.cache.get(url), task);
            imageView.setImageDrawable(asyncDrawable);
            if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                    && ((ViewGroup) imageView.getParent()).getChildAt(1) != null
                    && ((ViewGroup) imageView.getParent()).getChildAt(1) instanceof ProgressBar) {
                ((ViewGroup) imageView.getParent()).getChildAt(1).setVisibility(View.GONE);
            }
            if (zapp.cache.get(url) == null) {
                try {
                    task.executeOnExecutor(CommonLib.THREAD_POOL_EXECUTOR_IMAGE);
                } catch (RejectedExecutionException e) {
                    CommonLib.sPoolWorkQueueImage.clear();
                }
                getImageArray.add(task);
            }
        } else if (imageView != null && imageView.getDrawable() != null
                && ((BitmapDrawable) imageView.getDrawable()).getBitmap() != null) {
            imageView.setBackgroundResource(0);
            if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                    && ((ViewGroup) imageView.getParent()).getChildAt(1) != null
                    && ((ViewGroup) imageView.getParent()).getChildAt(1) instanceof ProgressBar) {
                ((ViewGroup) imageView.getParent()).getChildAt(1).setVisibility(View.GONE);
            }
        }
    }

    private class GetImage extends AsyncTask<Object, Void, Bitmap> {

        String url = "";
        private WeakReference<ImageView> imageViewReference;
        private int width;
        private int height;
        boolean useDiskCache;
        String type;
        String url2 = "";

        public GetImage(String url, ImageView imageView, int width, int height, boolean useDiskCache, String type) {
            this.url = url;
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.width = width;
            this.height = height;
            this.useDiskCache = true;// useDiskCache;
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null && imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                        && ((ViewGroup) imageView.getParent()).getChildAt(1) != null
                        && ((ViewGroup) imageView.getParent()).getChildAt(1) instanceof ProgressBar)
                    ((ViewGroup) imageView.getParent()).getChildAt(1).setVisibility(View.VISIBLE);
            }
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap bitmap = null;
            try {
                if (mScrollState != AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    if (!destroyed) {
                        if (useDiskCache) {
                            bitmap = CommonLib.getBitmapFromDisk(url,getApplicationContext());
                        }

                        if (bitmap == null) {
                            try {
                                BitmapFactory.Options opts = new BitmapFactory.Options();
                                opts.inJustDecodeBounds = true;
                                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                                BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

                                opts.inSampleSize = CommonLib.calculateInSampleSize(opts, width, height);
                                opts.inJustDecodeBounds = false;
                                opts.inPreferredConfig = Bitmap.Config.RGB_565;

                                bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null,
                                        opts);

                                if (useDiskCache) {
                                    CommonLib.writeBitmapToDisk(url, bitmap, getApplicationContext(),
                                            Bitmap.CompressFormat.JPEG);
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } catch (Error e) {
                                zapp.cache.clear();
                            }
                        }

                    } else {
                        this.cancel(true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if(destroyed)
                return;

            if (isCancelled()) {
                bitmap = null;
            }

            if (bitmap != null) {
                zapp.cache.put(url, bitmap);

                if (this.type.equalsIgnoreCase("user"))
                    bitmap = CommonLib.getRoundedCornerBitmap(bitmap, width);

            } else if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                GetImage task = getBitmapWorkerTask(imageView);
                if (task != null) {
                    if (task.url2.equals("")) {
                        task.url2 = new String(task.url);
                    }
                    task.url = "";
                }
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();

                if (imageView != null && mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    GetImage currentTask = getBitmapWorkerTask(imageView);

                    if ((!url.equals("")) && currentTask != null
                            && (currentTask.url.equals(url) || currentTask.url2.equals(url))) {
                        GetImage task = new GetImage(url, imageView, width, height, true, type);
                        final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), bitmap, task);
                        imageView.setImageDrawable(asyncDrawable);
                        imageView.setBackgroundResource(0);
                        if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                                && ((ViewGroup) imageView.getParent()).getChildAt(1) != null
                                && ((ViewGroup) imageView.getParent()).getChildAt(1) instanceof ProgressBar) {
                            ((ViewGroup) imageView.getParent()).getChildAt(1).setVisibility(View.GONE);
                        }
                    } else {
                        CommonLib.ZLog("getimagearray-imageview", "wrong bitmap");
                    }
                    getImageArray.remove(this);

                } else if (imageView != null) {
                    GetImage task = getBitmapWorkerTask(imageView);
                    if (task != null) {
                        // if(task.url2.equals("")) {
                        task.url2 = new String(task.url);
                        // }
                        task.url = "";
                    }
                } else if (imageView == null) {
                    CommonLib.ZLog("getimagearray-imageview", "null");
                }
            }
            /*
             * if (imageViewReference != null && bitmap != null) { final
			 * ImageView imageView = imageViewReference.get(); if (imageView !=
			 * null) { imageView.setImageBitmap(bitmap);
			 * imageView.setBackgroundResource(0); getImageArray.remove(this); }
			 * }
			 */
        }
    }

    private GetImage getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private class AsyncDrawable extends BitmapDrawable {
        // private final SoftReference<GetImage> bitmapWorkerTaskReference;
        private final GetImage bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, GetImage bitmapWorkerTask) {
            super(res, bitmap);
            // bitmapWorkerTaskReference = new
            // SoftReference<GetImage>(bitmapWorkerTask);
            bitmapWorkerTaskReference = bitmapWorkerTask;
        }

        public GetImage getBitmapWorkerTask() {
            return bitmapWorkerTaskReference;
            // return bitmapWorkerTaskReference.get();
        }
    }

    public boolean cancelPotentialWork(String data, ImageView imageView) {
        final GetImage bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {

            final String bitmapData = bitmapWorkerTask.url;
            if (!bitmapData.equals(data)) {
                if (bitmapWorkerTask.url2.equals("")) {
                    bitmapWorkerTask.url2 = new String(bitmapWorkerTask.url);
                }
                // Cancel previous task
                bitmapWorkerTask.url = "";
                bitmapWorkerTask.cancel(true);
                // getImageArray.clear();
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was
        // cancelled
        return true;
    }

    private class LoadModeWishes extends AsyncTask<Integer, Void, Object> {

        // execute the api
        @Override
        protected Object doInBackground(Integer... params) {
            int start = params[0];
            try {
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                String url = "";
                url = CommonLib.SERVER + "voucher/list?start="+start+"&count=" + count;
                Object info = RequestWrapper.RequestHttp(url, RequestWrapper.GET_VOUCHER, RequestWrapper.FAV);
                CommonLib.ZLog("url", url);
                return info;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (destroyed)
                return;
            if (result != null && result instanceof Object[]) {
                Object[] arr = (Object[]) result;
                wishes.addAll((ArrayList<Voucher>) arr[1]);
                mAdapter.notifyDataSetChanged();
            }
            loading = false;
        }
    }
}
