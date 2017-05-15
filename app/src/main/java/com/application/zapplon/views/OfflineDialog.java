package com.application.zapplon.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.Address;
import com.application.zapplon.data.OfflineAddress;
import com.application.zapplon.db.AddressDBWrapper;
import com.application.zapplon.db.LocationDBWrapper;
import com.application.zapplon.services.AddressService;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.CryptoHelper;
import com.application.zapplon.utils.TypefaceSpan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class OfflineDialog extends ActionBarActivity {

    private ZApplication zapp;
    private SharedPreferences prefs;
    private Activity mContext;
    private int width, height;
    private int MAKE_CALL_INTENT = 20;
    private boolean destroyed = false;

    private ListView mListView;
    AddressAdapter addressAdapter;
    private ArrayList<Address> addresses;
    private LayoutInflater inflater;

    private Address selectedAddress;
    private RadioButton mSelectedRB;
    private int mSelectedPosition = -1;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayList<OfflineAddress> locationList ;
    private ArrayAdapter<OfflineAddress> locationAdapter;
    private boolean isLocationSelected = false;
    private double selectLat,selectLon;
    private HashMap<Integer,Integer> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offline_dialog_layout);

        prefs = getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getApplication();
        mContext = this;
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();
        inflater = LayoutInflater.from(mContext);
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.query_string);
        selectLat = zapp.lat;
        selectLon = zapp.lon;
        map = new HashMap<Integer,Integer>();

        if( getIntent() != null && getIntent().hasExtra("addresses") ) {
            addresses = (ArrayList<Address>) getIntent().getSerializableExtra("addresses");
        } else
            addresses = AddressDBWrapper.getAddresses(prefs.getInt("uid", 0));

        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setupActionBar();

        if(!prefs.getBoolean("LOCAL_ADDRESS_UPDATE",false)){
            findViewById(R.id.search_sec).setVisibility(View.VISIBLE);
        }

        findViewById(R.id.book_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callStore(prefs.getString("services_call_contact", CommonLib.OFFLINE_CALL));
            }
        });

        findViewById(R.id.search_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoCompleteTextView.setText("");
                if(isLocationSelected){
                    isLocationSelected = false;
                    findViewById(R.id.offline_footer).setVisibility(View.GONE);
                }
            }
        });

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                //... your stuff
                isLocationSelected = true;
                int pos = map.get(position);
                OfflineAddress offlineAddress = locationList.get(pos);
                selectLat = offlineAddress.getLatitude();
                selectLon = offlineAddress.getLongitude();
                autoCompleteTextView.setText(offlineAddress.getAddress());
                findViewById(R.id.offline_footer).setVisibility(View.VISIBLE);
                if(mSelectedRB != null){
                    mSelectedRB.setChecked(false);
                }
            }
        });

        findViewById(R.id.book_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View customView = inflater.inflate(R.layout.booking_dialog, null);
                final AlertDialog dialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)
                        .setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                            }
                        })
                        .setView(customView)
                        .create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                customView.findViewById(R.id.confirm_cab).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // last made booking should be less than 5 minutes
                        if (prefs.getInt("uid", 0)!=0) {

//                            if(true || (System.currentTimeMillis() - prefs.getLong("booking_sms", 300001)) > 300000) {
                                Toast.makeText(mContext, "Give us a minute, we would do your booking ASAP", Toast.LENGTH_SHORT).show();

                                // Create the message
                                String msg = prefs.getString("access_token", "") + "," + selectLat + "," + selectLon + "," + CommonLib.getIMEI(mContext) + "," + ((RadioButton) findViewById(R.id.type_cab)).isChecked();

                                // Some encryption fundae
                                CryptoHelper cryptoHelper = new CryptoHelper();
                                try {
                                    msg = cryptoHelper.encrypt(msg, null, null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                SmsManager.getDefault().sendTextMessage(prefs.getString("services_message_contact", CommonLib.OFFLINE_MESSAGE), null, msg, null, null);

                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putLong("booking_sms", System.currentTimeMillis());
                                editor.commit();

                                finish();
//                            } else {
//                                Toast.makeText(mContext, "You need to wait for 5 minutes before booking another", Toast.LENGTH_SHORT).show();
//                                finish();
//                            }
                        }
                        else {
                            Intent intent = new Intent(OfflineDialog.this, SplashScreen.class);
                            Toast.makeText(mContext, "You need to be logged in before continuing", Toast.LENGTH_SHORT).show();
                            intent.putExtra("insideApp", true);
                            startActivity(intent);
                        }
                    }
                });

                customView.findViewById(R.id.cancel_action).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

            }
        });
        ((RadioButton) findViewById(R.id.type_cab)).setChecked(true);

        mListView = (ListView) findViewById(R.id.address_list);
        mListView.setItemsCanFocus(false);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                autoCompleteTextView.setText("");
                if(position != mSelectedPosition && mSelectedRB != null){
                    mSelectedRB.setChecked(false);
                }

                mSelectedPosition = position;
                mSelectedRB = (RadioButton)view.findViewById(R.id.check);
                selectedAddress = addresses.get(position);
                selectLat = selectedAddress.getAddressLatitude();
                selectLon = selectedAddress.getAddressLongitude();
                findViewById(R.id.offline_footer).setVisibility(View.VISIBLE);
            }
        });
        refreshList();
        refreshAutoCompleteView();

        LocalBroadcastManager.getInstance(OfflineDialog.this).registerReceiver(mNotificationReceived, new IntentFilter(CommonLib.LOCAL_INTERNET_CONNECTIVITY_BROADCAST));
    }

    private class getOfflineAddressAsync extends AsyncTask<Object, Void, Object> {

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {

            String locationAddress = zapp.getAddressString();
            if(locationAddress != null) {
                boolean shouldAddressDBUpdate = prefs.getBoolean("LOCAL_ADDRESS_UPDATE", false);
                boolean fetch = true;
                if(!shouldAddressDBUpdate) {
                    ArrayList<OfflineAddress> offlineList = LocationDBWrapper.getAddresses(prefs.getInt("uid", 0));
                    if (offlineList != null && offlineList.size() > 0) {
                        OfflineAddress offlineAddress = offlineList.get(0);
                        String city = offlineAddress.getCity();
                        if (locationAddress != null && city != null && !locationAddress.contains(city)) {
                            // gotta fetch em all ~ Pokemon
                        } else
                            fetch = false;
                    }
                }
                return fetch;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result != null && result instanceof Boolean && (boolean)result) {
                String locationAddress = zapp.getAddressString();
                LocationDBWrapper.removeAddresses();
                Intent intent = new Intent(OfflineDialog.this, AddressService.class);
                intent.putExtra("address",locationAddress);
                startService(intent);
            }
        }
    }

    private BroadcastReceiver mNotificationReceived = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(OfflineDialog.this, "Internet connected!", Toast.LENGTH_SHORT).show();
            finish();
        }
    };

    private void refreshAutoCompleteView() {
        locationList = LocationDBWrapper.getAddresses(prefs.getInt("uid", 0));
        locationAdapter = new locationAdapter(OfflineDialog.this,R.layout.list_item,locationList);
        autoCompleteTextView.setAdapter(locationAdapter);

        if(locationList == null || locationList.size() < 1)
            new getOfflineAddressAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarCustomView = inflator.inflate(R.layout.white_action_bar, null);
        actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
        actionBar.setCustomView(actionBarCustomView);

        ((TextView)actionBarCustomView.findViewById(R.id.back_icon)).setText(getResources().getString(R.string.z_cross));

        ((RelativeLayout.LayoutParams)(actionBarCustomView.findViewById(R.id.back_icon)).getLayoutParams()).setMargins(width / 20, 0, 0, 0);

        SpannableString s = new SpannableString(getString(R.string.offline_title));
        s.setSpan(
                new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
                        getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
                0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);

        actionBarCustomView.findViewById(R.id.title).setPadding(width / 40, 0, width / 40, 0);
        title.setText(s);
    }

    private void refreshList() {
        //get the history of addresses used so far
        if(addresses != null && addresses.size() > 0) {
            selectedAddress = addresses.get(0);
            addressAdapter = new AddressAdapter(mContext, R.layout.recent_search_snippet, addresses);
            mListView.setAdapter(addressAdapter);
            CommonLib.setListViewHeightBasedOnChildren(mListView);

            if(!prefs.getBoolean("LOCAL_ADDRESS_UPDATE",false)){
                findViewById(R.id.or_sec).setVisibility(View.VISIBLE);
            }

        }else{
            findViewById(R.id.address_list).setVisibility(View.GONE);
            findViewById(R.id.or_sec).setVisibility(View.GONE);
        }


    }

    public void actionBarSelected(View v) {

        switch (v.getId()) {
            case R.id.home_icon_container:
                onBackPressed();

            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        LocalBroadcastManager.getInstance(OfflineDialog.this).unregisterReceiver(mNotificationReceived);
        super.onDestroy();
    }

    public void callStore(String contact) {

        StringTokenizer st = new StringTokenizer(contact, ":");
        String phoneNumberToDisplay = "";
        if (st.hasMoreTokens()) {
            phoneNumberToDisplay = st.nextToken();
        }

        if (contact.length() > 3) {
            final Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumberToDisplay));
            AlertDialog.Builder builder_loc = new AlertDialog.Builder(OfflineDialog.this, AlertDialog.THEME_HOLO_DARK);

            Dialog dialog = null;
            builder_loc.setMessage(phoneNumberToDisplay).setCancelable(true).setPositiveButton(
                    getResources().getString(R.string.dialog_call), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                            try {
                                startActivityForResult(intent, MAKE_CALL_INTENT);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }).setNegativeButton(getResources().getString(R.string.dialog_cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            dialog = builder_loc.create();
            dialog.show();

        }
    }

    private class AddressAdapter extends ArrayAdapter<Address> {

        private ArrayList<Address> wishes;
        private Activity context;

        public AddressAdapter(Activity context, int resourceId, ArrayList<Address> wishes) {
            super(context.getApplicationContext(), resourceId, wishes);
            this.context = context;
            this.wishes = wishes;
        }

        @Override
        public int getCount() {
            if (wishes == null) {
                return 0;
            } else {
                return wishes.size();
            }
        }

        protected class ViewHolder {
            TextView text;
            RadioButton checkBox;
            LinearLayout ll;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final Address address = wishes.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.offline_address_snippet, null);
            }

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder();
                viewHolder.text = (TextView) convertView.findViewById(R.id.currentLocation);
                viewHolder.checkBox = (RadioButton) convertView.findViewById(R.id.check);
                viewHolder.ll = (LinearLayout) convertView.findViewById(R.id.address_snippet_container);
                convertView.setTag(viewHolder);
            }

            viewHolder.text.setText(address.getAddress());

            viewHolder.ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(position != mSelectedPosition && mSelectedRB != null){
                        mSelectedRB.setChecked(false);
                    }

                    mSelectedPosition = position;
                    mSelectedRB = (RadioButton) view.findViewById(R.id.check);
                    selectedAddress = wishes.get(position);
                    mSelectedRB.setChecked(true);
                    selectedAddress = addresses.get(position);
                    selectLat = selectedAddress.getAddressLatitude();
                    selectLon = selectedAddress.getAddressLongitude();
                    autoCompleteTextView.setText("");
                    findViewById(R.id.offline_footer).setVisibility(View.VISIBLE);

                }
            });

            if(mSelectedPosition != position){
                viewHolder.checkBox.setChecked(false);
            } else {
                viewHolder.checkBox.setChecked(true);
                if(mSelectedRB != null && viewHolder.checkBox != mSelectedRB){
                    mSelectedRB = viewHolder.checkBox;
                }
            }


            return convertView;
        }
    }


    protected static class ViewHolder {
        TextView address;
    }

    public class locationAdapter extends ArrayAdapter<OfflineAddress> implements Filterable {

        private ArrayList<OfflineAddress> items;
        private ArrayList<OfflineAddress> itemsAll;
        private ItemFilter mFilter = new ItemFilter();

        public locationAdapter(Context context, int viewResourceId, ArrayList<OfflineAddress> items) {
            super(context, viewResourceId, items);
            this.items = items;
            this.itemsAll = items;
        }


        @Override
        public int getCount() {
            if (items == null) {
                return 0;
            } else {
                return items.size();
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if(convertView == null){
                convertView = LayoutInflater.from(OfflineDialog.this).inflate(R.layout.list_item,null);
                viewHolder = new ViewHolder();
                viewHolder.address = (TextView)convertView.findViewById(R.id.address_tab);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            OfflineAddress wish = items.get(position);
            viewHolder.address.setText(wish.getAddress());
            return convertView;
        }

        public Filter getFilter() {
            return mFilter;
        }

        private class ItemFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                map.clear();

                String filterString = "";
                try {
                    filterString = constraint.toString().toLowerCase();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                FilterResults results = new FilterResults();

                final List<OfflineAddress> list = itemsAll;

                int count = list.size();
                final ArrayList<OfflineAddress> nlist = new ArrayList<OfflineAddress>(count);

                OfflineAddress filterableAddress ;

                int j=0;

                for (int i = 0; i < count; i++) {
                    filterableAddress = list.get(i);
                    String address = filterableAddress.getAddress();
                    if((address.toLowerCase()).contains(filterString.toLowerCase())){
                        map.put(j,i);
                        nlist.add(filterableAddress);
                        j++;
                    }
                    }


                results.values = nlist;
                results.count = nlist.size();

                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                items = (ArrayList<OfflineAddress>) results.values;
                notifyDataSetChanged();
            }
        }

    }

}