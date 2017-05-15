package com.application.zapplon.views;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.utils.CommonLib;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by apoorvarora on 15/02/16.
 */
public class AppConfigFragment extends Fragment {

    private View getView;
    private boolean destroyed = false;
    private int width, height;
    private Activity activity;
    private ZApplication zapp;

    public static AppConfigFragment newInstance(Bundle bundle) {
        AppConfigFragment fragment = new AppConfigFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.app_config_fragment, null);
        destroyed = false;
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int type = 0;
        getView = getView();
        destroyed = false;
        width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        activity = getActivity();
        zapp = (ZApplication) activity.getApplication();

        Bundle bundle = getArguments();
        if( bundle != null ) {
            String title = "";
            String imageUrl = "";
            String description = "";
            String footer = "";
            String buttonString ="";
            String buttonAction = "";
            boolean buttonVisibility = false;

            if(bundle.containsKey("title"))
                title = String.valueOf(bundle.get("title"));
            else
                getActivity().finish();

            if(bundle.containsKey("image"))
                imageUrl = String.valueOf(bundle.get("image"));
            else
                getActivity().finish();

            if(bundle.containsKey("description"))
                description = String.valueOf(bundle.get("description"));
            else
                getActivity().finish();

            if(bundle.containsKey("footer"))
                footer = String.valueOf(bundle.get("footer"));
            else
                getActivity().finish();

            if(bundle.containsKey("buttonString"))
                buttonString = String.valueOf(bundle.get("buttonString"));
            else
                getActivity().finish();

            if(bundle.containsKey("buttonAction"))
                buttonAction = String.valueOf(bundle.get("buttonAction"));
            else
                getActivity().finish();

            if(bundle.containsKey("buttonVisibility"))
                buttonVisibility = Boolean.parseBoolean(String.valueOf(bundle.get("buttonVisibility")));
            else
                getActivity().finish();

            if(!buttonVisibility)
                getView.findViewById(R.id.proceed_button_text).setVisibility(View.GONE);
            else {
                getView.findViewById(R.id.proceed_button_text).setVisibility(View.VISIBLE);
                ((TextView)getView.findViewById(R.id.proceed_button_text)).setText(buttonString);
            }

            final String action = buttonAction;
            getView.findViewById(R.id.proceed_button_text).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(action));
                    startActivity(intent);
                }
            });

            ((TextView)getView.findViewById(R.id.title)).setText(title);
            ((TextView)getView.findViewById(R.id.description)).setText(description);
            setImageFromUrlOrDisk(imageUrl, (ImageView)getView.findViewById(R.id.image), "", width, height, false);
            ((TextView)getView.findViewById(R.id.bottom_footer_text)).setText(footer);

        } else
            getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        destroyed = true;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }

    private void setImageFromUrlOrDisk(final String url, final ImageView imageView, final String type, int width,
                                       int height, boolean useDiskCache) {

        if (cancelPotentialWork(url, imageView)) {

            GetImage task = new GetImage(url, imageView, width, height, useDiskCache, type);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), zapp.cache.get(url + type), task);
            imageView.setImageDrawable(asyncDrawable);
            if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                    && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                    && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
                ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
            }
            if (zapp.cache.get(url + type) == null) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1L);
            } else if (imageView != null && imageView.getDrawable() != null
                    && ((BitmapDrawable) imageView.getDrawable()).getBitmap() != null) {
                imageView.setBackgroundResource(0);
                Bitmap blurBitmap = null;
                if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
                    ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
                }
            }
        }
    }

    private class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<GetImage> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, GetImage bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<GetImage>(bitmapWorkerTask);
        }

        public GetImage getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public boolean cancelPotentialWork(String data, ImageView imageView) {
        final GetImage bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.url;
            if (!bitmapData.equals(data)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was
        // cancelled
        return true;
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

    private class GetImage extends AsyncTask<Object, Void, Bitmap> {

        String url = "";
        private WeakReference<ImageView> imageViewReference;
        private int width;
        private int height;
        boolean useDiskCache;
        String type;
        Bitmap blurBitmap;

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
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar)
                    ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.VISIBLE);
            }
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap bitmap = null;
            try {

                String url2 = url + type;

                if (destroyed) {
                    return null;
                }

                if (useDiskCache) {
                    bitmap = CommonLib.getBitmapFromDisk(url2, activity.getApplicationContext());
                }

                if (bitmap == null) {
                    try {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

                        opts.inSampleSize = CommonLib.calculateInSampleSize(opts, width, height);
                        opts.inJustDecodeBounds = false;

                        bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

                        if (useDiskCache) {
                            CommonLib.writeBitmapToDisk(url2, bitmap, activity.getApplicationContext(),
                                    Bitmap.CompressFormat.JPEG);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (Error e) {
                        e.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    synchronized (zapp.cache) {
                        zapp.cache.put(url2, bitmap);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (!destroyed) {
                if (isCancelled()) {
                    bitmap = null;
                }
                if (imageViewReference != null && bitmap != null) {
                    final ImageView imageView = imageViewReference.get();
                    if (imageView != null) {
                        imageView.setImageBitmap(bitmap);
                        if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                                && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                                && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
                            ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }
}
