package com.application.zapplon.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.CabDetails;
import com.application.zapplon.data.SortCase;
import com.application.zapplon.data.Speak;
import com.application.zapplon.data.Surcharge;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.RequestWrapper;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.location.ZLocationCallback;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Harsh on 5/26/2016.
 */
public class SpeechDialog extends Activity implements ZLocationCallback, TextToSpeech.OnInitListener, OnUtteranceCompletedListener {

    private TextToSpeech textToSpeech;
    private ImageView btnSpeak;
    private SharedPreferences prefs;
    private MessagesAdapter messagesAdapter;
    private List<Speak> exchange;
    private ListView speechList;
    private TextView speakNow;
    SortCase sortCase;
    public static final int NEAREST = 200;
    public static final int CHEAPEST = 201;
    SpeakStatus speakStatus;
    private Context mContext;
    SpeechRecognizer mSpeechRecognizer;
    Intent mSpeechRecognizerIntent;
    boolean mIslistening;

    private AsyncTask mAsyncRunning;

    ZApplication zapp;

    @Override
    public void onCoordinatesIdentified(Location loc) {
    }

    @Override
    public void onLocationIdentified() {

    }

    @Override
    public void onLocationNotIdentified() {

    }

    @Override
    public void onDifferentCityIdentified() {

    }

    @Override
    public void locationNotEnabled() {

    }

    @Override
    public void onLocationTimedOut() {

    }

    @Override
    public void onNetworkError() {

    }

    public void getSpeech()
    {
        if (!mIslistening)
        {
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        }
    }

    String address;
    Speak speak = new Speak();

    @Override
    public void onInit(int i) {
        if(i != TextToSpeech.ERROR) {
            textToSpeech.setLanguage(Locale.getDefault());
            textToSpeech.setOnUtteranceCompletedListener(this);
        }

        Speak speak = new Speak();
        speak.setSpeaker(false);
        speak.setReturnText("How may I help you?");

        //speak(speak.getReturnText());
        exchange.add(speak);
        messagesAdapter = new MessagesAdapter(getApplicationContext(),R.layout.chat_item_snippet,exchange);
        speechList.setAdapter(messagesAdapter);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getSpeech();
                btnSpeak.setColorFilter(Color.GREEN);
                speakNow.setVisibility(View.VISIBLE);
            }
        }, 300);
    }

    @Override
    public void onUtteranceCompleted(String s) {
        /*if (speakStatus.getSpeakStatus())
        {
            getSpeech();
        }*/
    }

    class addressTask implements Runnable {
        @Override
        public void run() {
            String getAddress;
            try {
                if (zapp.getLocationString()==null)
                {
                    Geocoder geocoder;
                    List<Address> addresses;
                    Thread.sleep(1500);
                    geocoder = new Geocoder(getApplicationContext(),Locale.getDefault());
                    addresses = geocoder.getFromLocation(zapp.lat, zapp.lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    address = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    getAddress = address + ", " + city +", "+ state;
                }
                else
                {
                    getAddress = zapp.getAddressString();
                }
                Speak speak = new Speak();
                speak.setReturnText("Your current Address is " + getAddress);
                speak.setSpeaker(false);
                speakStatus.setSpeakStatus(false);
                exchange.add(speak);

                if(mAsyncRunning != null)
                    mAsyncRunning.cancel(true);
                mAsyncRunning = new GetUberCabs().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            } catch (InterruptedException | IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void speakDetails(String displayName, Double baseFare, Long time, Double costPerKm/*, Double surge*/)
    {
        String speakText;

        speakText =  sortCase.getIdentifier() + " cab is "+ displayName + " with Base Fare " + String.valueOf(baseFare)+" Rupees, reaching in "+
                String.valueOf(time) + " minutes with per kilometre cost "+costPerKm +". Do you want to book it ??";
        speak.setSpeaker(true);
        speakStatus.setSpeakStatus(true);
        speak(speakText);

        Speak speak = new Speak();
        speak.setReturnText(speakText);
        speak.setSpeaker(false);
        speakStatus.setSpeakStatus(false);
        exchange.add(speak);
        messagesAdapter.notifyDataSetChanged();
        speechList.setSelection(messagesAdapter.getCount()-1);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btnSpeak.setColorFilter(Color.GREEN);
                speakNow.setVisibility(View.VISIBLE);

                getSpeech();
            }
        }, 10000);
    }

    public void bookCab()
    {
        final CabDetails cab = getProducts().get(0);
        String ola_access_token = prefs.getString("ola_access_token","");
        String productId = cab.getProductId();
        if (prefs.getInt("uid", 0) != 0) {
            if (cab.getType()== CommonLib.TYPE_OLA)
            {
                if (prefs.getString("ola_access_token","")!=null&&!prefs.getString("ola_access_token","").isEmpty())
                {
                    UploadManager.cabBookingRequest(cab.getType(),ola_access_token,productId,zapp.lat,zapp.lon,address,"",0,0,"","",cab.getDisplayName());
                }
                else
                {
                    btnSpeak.setColorFilter(Color.RED);
                    speakNow.setVisibility(View.INVISIBLE);
                    Speak speak = new Speak();
                    speak.setSpeaker(false);
                    speakStatus.setSpeakStatus(false);
                    speak.setReturnText("You are not logged into Ola Cabs via Zapplon");
                    exchange.add(speak);
                    speak(speak.getReturnText());
                }
            }
            else
            {
                UploadManager.cabBookingRequest(cab.getType(),"",productId,zapp.lat,zapp.lon,address,"",0,0,"","",cab.getDisplayName());
            }
        } else {
            Intent intent = new Intent(SpeechDialog.this, SplashScreen.class);
            Toast.makeText(mContext, "Please login to continue", Toast.LENGTH_SHORT).show();
            intent.putExtra("insideApp", true);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_dialog_layout);

        zapp = (ZApplication) getApplication();
        mContext = this;
        btnSpeak = (ImageView) findViewById(R.id.btnSpeak);
        speechList = (ListView) findViewById(R.id.speechList);
        speakNow = (TextView) findViewById(R.id.speakNow);
        int width = getWindowManager().getDefaultDisplay().getWidth();

        speechList.setDivider(null);
        speechList.setDividerHeight(width / 20);
        speechList.setClipToPadding(false);

        prefs = getApplicationContext().getSharedPreferences("application_settings", 0);

        exchange = new ArrayList<Speak>();
        sortCase = new SortCase();
        speakStatus = new SpeakStatus();

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());

        SpeechRecognitionListener listener = new SpeechRecognitionListener();
        mSpeechRecognizer.setRecognitionListener(listener);

        findViewById(R.id.widget_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        textToSpeech=new TextToSpeech(getApplicationContext(), this);

        zapp.zll.forced = true;
        zapp.zll.addCallback(this);
        zapp.startLocationCheck();

        btnSpeak.setColorFilter(Color.RED);
        speakNow.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (mSpeechRecognizer != null)
        {
            mSpeechRecognizer.destroy();
        }
        super.onDestroy();
    }

    private List<CabDetails> filteredList = null;
    public static final int SORT_BY_PRICE = 101;
    public static final int SORT_BY_ARRIVAL_TIME = 102;

    private class GetUberCabs extends AsyncTask<Object, Void, Object> {

        Surcharge surcharge;

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {

                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                StringBuilder url = new StringBuilder();
                url.append(CommonLib.SERVER + "booking/product" + "?latitude=" + zapp.lat + "&longitude=" + zapp.lon);
                Object info = RequestWrapper.RequestHttp(url.toString(), RequestWrapper.UBER_LIST, RequestWrapper.FAV);
                CommonLib.ZLog("url", url.toString());

                if(info != null && info instanceof ArrayList<?>) {
                    ArrayList<CabDetails> cabs = new ArrayList<>();
                    for(CabDetails cab:(ArrayList<CabDetails>)info) {
                        if(cab.getType() == CommonLib.TYPE_OLA)
                            cabs.add(cab);
                    }
                    return cabs;
                }
                else
                    return info;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (false)
                return;

            if (result != null) {
                if (result instanceof ArrayList<?>) {
                    ArrayList<CabDetails> cabs = (ArrayList<CabDetails>) result;
                    if(cabs != null && !cabs.isEmpty()) {
                        setProducts((ArrayList<CabDetails>) result,sortCase.getSortType());
                        final CabDetails cab = filteredList.get(0);
                        String displayName = cab.getDisplayName();
                        Double baseFare = cab.getBase();
                        Long time = cab.getEstimatedTimeOfArrival();
                        surcharge = new Surcharge();
                        Double costPerKm = cab.getCostPerDistance();
                        speakDetails(displayName, baseFare, time, costPerKm);
                    } else {
                        btnSpeak.setColorFilter(Color.RED);
                        speakNow.setVisibility(View.INVISIBLE);
                        Speak speak = new Speak();
                        speak.setSpeaker(false);
                        speakStatus.setSpeakStatus(false);
                        speak.setReturnText("Sorry no cabs are available at the moment.");
                        exchange.add(speak);
                        messagesAdapter.notifyDataSetChanged();
                        speechList.setSelection(messagesAdapter.getCount()-1);
                        speak(speak.getReturnText());
                    }
                }
            } else {
                btnSpeak.setColorFilter(Color.RED);
                speakNow.setVisibility(View.INVISIBLE);
                Speak speak = new Speak();
                speak.setSpeaker(false);
                speakStatus.setSpeakStatus(false);
                speak.setReturnText("Sorry no cabs are available at the moment.");
                exchange.add(speak);
                messagesAdapter.notifyDataSetChanged();
                speechList.setSelection(messagesAdapter.getCount()-1);
                speak(speak.getReturnText());
            }
        }
    }

    private void setProducts(ArrayList<CabDetails> wishes,int sortCase) {
        this.filteredList = wishes;
        switch(sortCase)
        {
            case NEAREST:
                sort(SORT_BY_ARRIVAL_TIME);
                break;
            case CHEAPEST:
                sort(SORT_BY_PRICE);
                break;
        }

    }

    public ArrayList<CabDetails> getProducts()
    {
        return (ArrayList<CabDetails>) filteredList;
    }

    public void sort(int type) {
        switch (type) {

            case SORT_BY_ARRIVAL_TIME:
                if (getProducts()!= null) {
                    Collections.sort(getProducts(), new Comparator<CabDetails>() {
                        @Override
                        public int compare(CabDetails lhs, CabDetails rhs) {
                            return (int) (lhs.getEstimatedTimeOfArrival() - rhs.getEstimatedTimeOfArrival());
                        }
                    });
                }
                break;
            case SORT_BY_PRICE:
                if (getProducts()!= null) {
                    Collections.sort(getProducts(), new Comparator<CabDetails>() {
                        @Override
                        public int compare(CabDetails lhs, CabDetails rhs) {

                            if (lhs.getPriceEstimate() != null && rhs.getPriceEstimate() != null)
                                return (lhs.getPriceEstimate().compareTo(rhs.getPriceEstimate()));
                            else if (lhs.getCostPerDistance() != 0 && rhs.getCostPerDistance() != 0) {
                                return (int) (lhs.getCostPerDistance() - rhs.getCostPerDistance());
                            } else {
                                return (int) (lhs.getBase() - rhs.getBase());
                            }
                        }
                    });
                }
                break;
        }
    }

    String speechResult;
    protected class SpeechRecognitionListener implements RecognitionListener
    {

        @Override
        public void onBeginningOfSpeech()
        {
            //Log.d(TAG, "onBeginingOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {

        }

        @Override
        public void onEndOfSpeech()
        {
            //Log.d(TAG, "onEndOfSpeech");
        }

        @Override
        public void onError(int error)
        {
            //mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

            //Log.d(TAG, "error = " + error);
        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {

        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {

        }

        @Override
        public void onReadyForSpeech(Bundle params)
        {
            //Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle results)
        {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            assert matches != null;
            speechResult = matches.get(0);

            Speak speak1 = new Speak();
            speak1.setSpeaker(true);
            speak1.setReturnText(matches.get(0));
            speakNow.setVisibility(View.VISIBLE);

            exchange.add(speak1);
            messagesAdapter.notifyDataSetChanged();
            speechList.setSelection(messagesAdapter.getCount()-1);

            Speak speak = new Speak();

            if (speechResult.contains("cabs") || speechResult.contains("taxis")||speechResult.contains("Cabs")
                    ||speechResult.contains("Taxis")||speechResult.contains("cab")||speechResult.contains("taxi")
                    ||speechResult.contains("Cab")||speechResult.contains("Taxi")) {
                StringBuilder cabString = new StringBuilder();
                if(speechResult.contains("cabs") || speechResult.contains("Cabs") || speechResult.contains("cab") || speechResult.contains("Cab"))
                    cabString.append("cabs");
                else if(speechResult.contains("taxis") || speechResult.contains("Taxis") || speechResult.contains("Taxi") || speechResult.contains("taxi"))
                    cabString.append("taxis");

                Speak speak2 = new Speak();
                speak2.setSpeaker(false);
                speakStatus.setSpeakStatus(false);
                speak2.setReturnText("Identifying " +cabString.toString()+" near you");
                exchange.add(speak2);
                messagesAdapter.notifyDataSetChanged();
                speechList.setSelection(messagesAdapter.getCount()-1);

                if (speechResult.contains("nearest")||speechResult.contains("closest")||speechResult.contains("Nearest")||speechResult.contains("Closest"))
                {
                    sortCase.setSortType(NEAREST);
                    sortCase.setIdentifier("Nearest");
                }
                else if (speechResult.contains("cheapest"))
                {
                    sortCase.setSortType(CHEAPEST);
                    sortCase.setIdentifier("Cheapest");
                }
                else {
                    sortCase.setSortType(NEAREST);
                    sortCase.setIdentifier("Nearest");
                }
                speak(speak2.getReturnText());

                btnSpeak.setColorFilter(Color.RED);
                speakNow.setVisibility(View.INVISIBLE);

                Thread thread = new Thread(new addressTask());
                thread.start();

            } else if (speechResult.contains("yes")||speechResult.contains("Yes")||speechResult.contains("Yeah")||speechResult.contains("yeah")) {

                Speak speak2 = new Speak();
                speak2.setSpeaker(false);
                speakStatus.setSpeakStatus(false);
                speak2.setReturnText("Booking this Cab");
                speak(speak2.getReturnText());
                btnSpeak.setColorFilter(Color.RED);
                speakNow.setVisibility(View.INVISIBLE);

                exchange.add(speak2);
                messagesAdapter.notifyDataSetChanged();
                speechList.setSelection(messagesAdapter.getCount()-1);
                bookCab();
            }
            else if(speechResult.equalsIgnoreCase("no")|| speechResult.equalsIgnoreCase("No"))
            {
                Speak speak2 = new Speak();
                speak2.setSpeaker(false);
                speakStatus.setSpeakStatus(false);
                speak2.setReturnText("Thank you for Using Zapplon");

                speak(speak2.getReturnText());
                exchange.add(speak2);
                messagesAdapter.notifyDataSetChanged();
                speechList.setSelection(messagesAdapter.getCount()-1);

                btnSpeak.setColorFilter(Color.RED);
                speakNow.setVisibility(View.INVISIBLE);

            }
            else if (speechResult.equalsIgnoreCase("exit") || speechResult.equalsIgnoreCase("cancel")) {
                speak.setSpeaker(false);
                speakStatus.setSpeakStatus(false);
                speak("Exiting");
                finish();
            } else {
                messagesAdapter.notifyDataSetChanged();
                speechList.setSelection(messagesAdapter.getCount()-1);
                final Handler handler1 = new Handler();
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        speak("Please Repeat");
                    }
                },500);

                btnSpeak.setColorFilter(Color.GREEN);
                speakNow.setVisibility(View.VISIBLE);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                getSpeech();
                    }
                }, 1600);
            }
        }

        @Override
        public void onRmsChanged(float rmsdB)
        {
        }
    }

    public class MessagesAdapter extends ArrayAdapter<String> {

        private List<Speak> messagesList;
        private Context mContext;

        public MessagesAdapter(Context context, int resourceId, List<Speak> wishes) {
            super(context, resourceId ,R.id.txt);
            mContext = context;
            this.messagesList = wishes;
        }

        @Override
        public int getCount() {
            if (messagesList == null) {
                return 0;
            } else {
                return messagesList.size();
            }
        }

        protected class ViewHolder {
            TextView messageFrom;
            TextView messageTo;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Speak message = messagesList.get(position);

            if (convertView == null || convertView.findViewById(R.id.chat_snippet) == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_item_snippet, null);
            }

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder();
                viewHolder.messageFrom = (TextView) convertView.findViewById(R.id.message_preview_left);
                viewHolder.messageTo = (TextView) convertView.findViewById(R.id.message_preview_right);
                convertView.setTag(viewHolder);
            }

            if (!message.getSpeaker()) {
                if (message.getReturnText().equalsIgnoreCase(""))
                {
                    viewHolder.messageFrom.setVisibility(View.GONE);
                    viewHolder.messageTo.setVisibility(View.GONE);
                }
                else
                {
                    viewHolder.messageFrom.setVisibility(View.GONE);
                    viewHolder.messageTo.setVisibility(View.VISIBLE);
                    viewHolder.messageTo.setText(message.getReturnText());
                }

            } else {
                if (message.getReturnText().equalsIgnoreCase("")) {
                    viewHolder.messageFrom.setVisibility(View.GONE);
                    viewHolder.messageTo.setVisibility(View.GONE);
                }
                else
                {
                    viewHolder.messageFrom.setVisibility(View.VISIBLE);
                    viewHolder.messageTo.setVisibility(View.GONE);
                    viewHolder.messageFrom.setText(message.getReturnText());
                }

            }
            return convertView;
        }
    }

    private void speak(String text) {
        if(text != null) {
            if (Build.VERSION.SDK_INT >= 21) {
                Bundle bundle = new Bundle();
                bundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text);
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, bundle, text);
            } else {
                HashMap<String, String> myHashAlarm = new HashMap<String, String>();
                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text);
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
            }
        }
    }

    public class SpeakStatus implements Serializable
    {
        private boolean speakStatus;
        public SpeakStatus(){

        }

        public boolean getSpeakStatus() {
            return speakStatus;
        }

        public void setSpeakStatus(boolean speakStatus) {
            this.speakStatus = speakStatus;
        }
    }
}