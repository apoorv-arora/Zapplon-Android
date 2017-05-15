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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
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
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.UploadManagerCallback;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by Lenovo on 03/28/2016.
 */
public class EarnFragment extends Fragment implements UploadManagerCallback{

    private ZApplication zapp;
    private Activity activity;
    private View getView;
    private SharedPreferences prefs;
    private int width, height;
    private LayoutInflater vi;
    private boolean destroyed = false;

    private AsyncTask mAsyncRunning;
    private MyListAdapter mAdapter;
    private ListView mListView;
    ArrayList<Voucher> wishes;
    LinearLayout mListViewFooter;
    private int mWishesTotalCount;
    private boolean cancelled = false;
    private boolean loading = false;
    private int count = 10;
    public static final int WISH_OFFERED = 1;
    ArrayList<GetImage> getImageArray = new ArrayList<GetImage>();
    private int mScrollState;

    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.earntab_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        getView = getView();

        prefs = activity.getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) activity.getApplication();
        width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        vi = LayoutInflater.from(activity.getApplicationContext());

        mListView = (ListView) getView.findViewById(R.id.v_listView);
        mListView.setDivider(null);
        mListView.setDividerHeight(width/20);

        View randomView = new View(activity);
        randomView.setMinimumHeight(height/200);
        mListView.addHeaderView(randomView);

        setListeners();
        refreshView();
        UploadManager.addCallback(this);
    }

    private void setListeners() {
        getView.findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshView();
            }
        });
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        UploadManager.removeCallback(this);
        super.onDestroy();
    }
    @Override
    public void onDestroyView() {
        destroyed = true;
        super.onDestroyView();
    }

    private void refreshView() {
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);
        mAsyncRunning = new GetVouchers().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if(requestType == CommonLib.AVAIL_VOUCHER) {
            if(progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if(!destroyed && status) {
                Toast.makeText(activity, "Congratulations \\nYour voucher will be mailed to you", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }

    public class MyListAdapter extends ArrayAdapter<Voucher> {

        private List<Voucher> wishes;
        private Activity mContext;
        private int width;

        public MyListAdapter(Activity context, int resourceId, List<Voucher> wishes) {
            super(context.getApplicationContext(), resourceId, wishes);
            mContext = context;
            this.wishes = wishes;
            width = mContext.getWindowManager().getDefaultDisplay().getWidth();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
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
                        progressDialog = ProgressDialog.show(activity, null, "Checking details. Please wait!!!");
                        UploadManager.availVoucher(wish.getVoucherId());
                    } else {
                        Toast.makeText(activity, "You cannot avail this voucher right now", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            viewHolder.comapny_name.setText(wish.getComapany_name());
            viewHolder.voucher_value.setText("Rs. " + wish.getValue() + "");

            if(wish.getZappsRequired() >= 0) {
                if (wish.getZappsRequired() == 1)
                    viewHolder.voucher_value_remaining.setText(wish.getZappsRequired() + " Zapp Required");
                else
                    viewHolder.voucher_value_remaining.setText(wish.getZappsRequired() + " Zapps Required");
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
            getView.findViewById(R.id.progress_container).setVisibility(View.VISIBLE);

            getView.findViewById(R.id.content).setAlpha(1f);

            getView.findViewById(R.id.content).setVisibility(View.GONE);

            getView.findViewById(R.id.empty_view).setVisibility(View.GONE);
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

            getView.findViewById(R.id.progress_container).setVisibility(View.GONE);
            if (result != null) {
                getView.findViewById(R.id.content).setVisibility(View.VISIBLE);
                if (result instanceof Object[]) {
                    Object[] arr = (Object[]) result;
                    mWishesTotalCount = (Integer) arr[0];
                    setWishes((ArrayList<Voucher>) arr[1]);
                    if( ((ArrayList<Voucher>) arr[1]).size()  == 0 ) {
                        getView.findViewById(R.id.content).setVisibility(View.GONE);
                        getView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                        ((TextView)getView.findViewById(R.id.empty_view_text)).setText("Nothing here yet");

                    } else {
                        getView.findViewById(R.id.content).setVisibility(View.VISIBLE);
                        getView.findViewById(R.id.empty_view).setVisibility(View.GONE);
                    }
                }
            } else {
                if (CommonLib.isNetworkAvailable(activity)) {
                    Toast.makeText(activity, activity.getResources().getString(R.string.error_try_again),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
                            .show();

                    getView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

                    getView.findViewById(R.id.content).setVisibility(View.GONE);
                }
            }
        }

    }

    // set all the wishes here
    private void setWishes(ArrayList<Voucher> wishes) {
        this.wishes = wishes;
        if (wishes != null && wishes.size() > 0 && mWishesTotalCount > wishes.size()
                && mListView.getFooterViewsCount() == 0) {
            mListViewFooter = new LinearLayout(activity.getApplicationContext());
            mListViewFooter.setBackgroundResource(R.color.white);
            mListViewFooter.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT, width /5));
            mListViewFooter.setGravity(Gravity.CENTER);
            mListViewFooter.setOrientation(LinearLayout.HORIZONTAL);
            ProgressBar pbar = new ProgressBar(activity.getApplicationContext(), null,
                    android.R.attr.progressBarStyleSmallInverse);
            mListViewFooter.addView(pbar);
            pbar.setTag("progress");
            pbar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            mListView.addFooterView(mListViewFooter);
        }
        mAdapter = new MyListAdapter(activity, R.layout.voucher_layout, this.wishes);
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

            final AsyncDrawable asyncDrawable = new AsyncDrawable(activity.getResources(), zapp.cache.get(url), task);
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
                            bitmap = CommonLib.getBitmapFromDisk(url,activity);
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
                                    CommonLib.writeBitmapToDisk(url, bitmap, activity.getApplicationContext(),
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
