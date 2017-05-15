package com.application.zapplon.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.application.zapplon.data.OfflineAddress;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.RequestWrapper;

import java.util.ArrayList;

public class LocationDBManager extends SQLiteOpenHelper {

    private static final String ID = "ID";
    private static final String MESSAGEID = "AddressID";
    private static final String TYPE = "Type";
    private static final String TIMESTAMP = "Timestamp";
    private static final String BUNDLE = "Bundle";
    SQLiteDatabase db;

    private static final int DATABASE_VERSION = 2;
    private static final String CACHE_TABLE_NAME = "LOCATIONS";
    private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE " + CACHE_TABLE_NAME + " (" + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + MESSAGEID + " INTEGER, "
            + TIMESTAMP + " INTEGER, " + TYPE + " INTEGER, " + BUNDLE + " BLOB);";


    private static final String DATABASE_NAME = CommonLib.LOCATION_DB;
    Context ctx;

    public LocationDBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DICTIONARY_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public int addAddresses(ArrayList<OfflineAddress> offlineAddresses, int userId, long timestamp) {

        int result = -1;
        try {
            this.getReadableDatabase();

            SQLiteDatabase db = ctx.openOrCreateDatabase("/data/data/com.application.zapplon/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READWRITE, null);
            ContentValues values = new ContentValues();
            values.put(TIMESTAMP, timestamp);

            for (OfflineAddress address: offlineAddresses) {
                byte[] bundle = RequestWrapper.Serialize_Object(address);

                values.put(MESSAGEID, userId);
                values.put(TYPE, address.getId());
                values.put(BUNDLE, bundle);

                // Inserting Row
                result = (int) db.insert(CACHE_TABLE_NAME, null, values);
                CommonLib.ZLog("zloc addlocations else ", userId + " . " + address.getId());
            }

            db.close();
            this.close();
        } catch (Exception E) {
            try {
                this.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            result = -1;
        }
        return result;
        // Closing database connection
    }

    public ArrayList<OfflineAddress> getAddresses(int userId) {
        OfflineAddress location;
        this.getReadableDatabase();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<OfflineAddress> queries = new ArrayList<OfflineAddress>();

        try {
            db = ctx.openOrCreateDatabase("/data/data/com.application.zapplon/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READONLY, null);

            cursor = db.query(CACHE_TABLE_NAME, new String[] { ID, MESSAGEID, TIMESTAMP, TYPE, BUNDLE },
                    MESSAGEID + "=?", new String[] {Integer.toString(userId)}, null, null, null, null);
            if (cursor != null)
                cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                location = (OfflineAddress) RequestWrapper.Deserialize_Object(cursor.getBlob(4), "");
                queries.add(location);
            }

            cursor.close();
            db.close();
            this.close();
            return queries;
        } catch (SQLiteException e) {

            this.close();
        } catch (Exception E) {
            try {
                cursor.close();
                db.close();
                this.close();
            } catch (Exception ec) {
                try {
                    db.close();
                } catch (Exception e) {
                    this.close();
                }
                this.close();
            }
        }
        return queries;
    }


    public void removeAddresses() {
        try {

            this.getReadableDatabase();

            SQLiteDatabase db = ctx.openOrCreateDatabase("/data/data/com.application.zapplon/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READWRITE, null);

            // Delete all rows
            db.execSQL("delete from "+ CACHE_TABLE_NAME);
            db.close();
            this.close();
        } catch (Exception E) {
            try {
                this.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}