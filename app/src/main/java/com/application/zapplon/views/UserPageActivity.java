package com.application.zapplon.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.User;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.RequestWrapper;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Displays user name, bio and profile pic
 */
public class UserPageActivity extends Activity {

	private SharedPreferences prefs;
	private int width;
	private boolean destroyed = false;
	private ZApplication zapp;
	private int userId;
	boolean isSecondProfile = false;
	ImageView imageView;
	AsyncTask mAsyncRunning;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.user_page_activity);
		zapp = (ZApplication) getApplication();

		prefs = getSharedPreferences("application_settings", 0);
		width = getWindowManager().getDefaultDisplay().getWidth();
		if (getIntent() != null && getIntent().getExtras() != null) {
			Bundle extras = getIntent().getExtras();
			if (extras.containsKey("uid")) {
				userId = extras.getInt("uid");
				isSecondProfile = true;
				// start the loader
				findViewById(R.id.userpage_progress_container).setVisibility(View.VISIBLE);
				findViewById(R.id.content_container).setVisibility(View.GONE);
				findViewById(R.id.empty_view).setVisibility(View.GONE);
				refreshView();
			}
		}
		if (!isSecondProfile)
			userId = prefs.getInt("uid", 0);
		fixSizes();
		setListeners();
	}

	private void refreshView() {
		if (mAsyncRunning != null)
			mAsyncRunning.cancel(true);
		mAsyncRunning = new GetUserDetails().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private class GetUserDetails extends AsyncTask<Object, Void, Object> {

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "user/details?user_id=" + userId;
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.USER_INFO, RequestWrapper.FAV);
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
			findViewById(R.id.userpage_progress_container).setVisibility(View.GONE);

			if (result != null) {
				if (result instanceof User) {
					User user = (User) result;
					findViewById(R.id.content_container).setVisibility(View.VISIBLE);
					setImageFromUrlOrDisk(user.getImageUrl(), imageView, "", width, width, false);
					((TextView) findViewById(R.id.name)).setText(user.getUserName());
					if(user.getBio() == null || user.getBio().length() < 1) {
						findViewById(R.id.description).setVisibility(View.GONE);
						findViewById(R.id.showall).setVisibility(View.GONE);
					} else {
						findViewById(R.id.description).setVisibility(View.VISIBLE);
						findViewById(R.id.showall).setVisibility(View.VISIBLE);
						((TextView) findViewById(R.id.description)).setText(user.getBio());
					}
				}
			} else {
				findViewById(R.id.empty_view).setVisibility(View.GONE);
				findViewById(R.id.content_container).setVisibility(View.GONE);
				if (CommonLib.isNetworkAvailable(UserPageActivity.this)) {
					Toast.makeText(UserPageActivity.this, getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(UserPageActivity.this, getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();
				}
			}

		}
	}

	private void setListeners() {
		findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshView();
			}
		});

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (prefs.getInt("uid", 0) == 0) {
			Intent intent = new Intent(UserPageActivity.this, SplashScreen.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case android.R.id.home:
			onBackPressed();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	ImageView imageViewBlur;

	private void fixSizes() {
		findViewById(R.id.name).setPadding(width / 20, width / 20, width / 20, width / 20);
		findViewById(R.id.description).setPadding(width / 20, width / 20, width / 20, width / 20);
		imageView = ((ImageView) findViewById(R.id.user_image));
		imageViewBlur = (ImageView) findViewById(R.id.drawer_user_info_background_image);

		((RelativeLayout.LayoutParams) findViewById(R.id.back_icon).getLayoutParams()).setMargins(width / 20,
				width / 20, width / 20, width / 20);

		findViewById(R.id.back_icon).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		if (!isSecondProfile) {
			setImageFromUrlOrDisk(prefs.getString("profile_pic", ""), imageView, "", width, width, false);
			((TextView) findViewById(R.id.name)).setText(prefs.getString("username", ""));
			if(prefs.getString("description", "") == null || prefs.getString("description", "").length() < 1) {
				findViewById(R.id.description).setVisibility(View.GONE);
				findViewById(R.id.showall).setVisibility(View.GONE);
			} else {
				findViewById(R.id.description).setVisibility(View.VISIBLE);
				findViewById(R.id.showall).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.description)).setText(prefs.getString("description", ""));
			}
		}
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
				if (imageViewBlur != null) {
					blurBitmap = CommonLib.fastBlur(((BitmapDrawable) imageView.getDrawable()).getBitmap(), 4);
				}
				if (imageViewBlur != null && blurBitmap != null) {
					imageViewBlur.setImageBitmap(blurBitmap);
				}
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

				if (destroyed && (imageViewReference.get() != findViewById(R.id.user_image))) {
					return null;
				}

				if (useDiskCache) {
					bitmap = CommonLib.getBitmapFromDisk(url2, getApplicationContext());
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
							CommonLib.writeBitmapToDisk(url2, bitmap, getApplicationContext(),
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

					bitmap = CommonLib.getRoundedCornerBitmap(bitmap, width);
					synchronized (zapp.cache) {
						zapp.cache.put(url2, bitmap);
					}
					if (imageViewBlur != null) {
						blurBitmap = CommonLib.fastBlur(bitmap, 4);
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
				if (imageViewBlur != null && blurBitmap != null) {
					imageViewBlur.setImageBitmap(blurBitmap);
				}
			}
		}
	}

	public void expandDescription(View v) {

		final TextView descriptiontext = (TextView) findViewById(R.id.description);
		final TextView showAll = (TextView) findViewById(R.id.showall);
		if (showAll.getText().toString().equals(getResources().getString(R.string.show_more))) {
			showAll.setText(getResources().getString(R.string.show_less));
			descriptiontext.setMaxLines(Integer.MAX_VALUE);
		} else if (showAll.getText().toString().equals(getResources().getString(R.string.show_less))) {
			showAll.setText(getResources().getString(R.string.show_more));
			descriptiontext.setMaxLines(5);
		}

	}

}
