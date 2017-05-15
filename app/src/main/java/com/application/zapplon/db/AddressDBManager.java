package com.application.zapplon.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.application.zapplon.data.Address;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.RequestWrapper;

import java.util.ArrayList;
import java.util.List;

public class AddressDBManager extends SQLiteOpenHelper {

    private static final String ID = "ID";
    private static final String MESSAGEID = "AddressID";
    private static final String TYPE = "Type";
    private static final String TIMESTAMP = "Timestamp";
    private static final String BUNDLE = "Bundle";
    SQLiteDatabase db;

    private static final int DATABASE_VERSION = 2;
    private static final String CACHE_TABLE_NAME = "ADDRESSES";
    private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE " + CACHE_TABLE_NAME + " (" + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + MESSAGEID + " INTEGER, "
            + TIMESTAMP + " INTEGER, " + TYPE + " INTEGER, " + BUNDLE + " BLOB);";


    private static final String DATABASE_NAME = CommonLib.ADDRESS_DB;
    Context ctx;

    public AddressDBManager(Context context) {
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

    public int addAddress(Address location, int userId, long timestamp) {

        ArrayList<Address> locations = getAllAddresses(userId);
        int result = -1;
        Cursor cursor = null;

        try {

            this.getReadableDatabase();

            SQLiteDatabase db = ctx.openOrCreateDatabase("/data/data/com.application.zapplon/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READWRITE, null);
            ContentValues values = new ContentValues();
            values.put(TIMESTAMP, timestamp);

            boolean contains = false;

            for(Address address : locations){
                if(location.getAddress().equalsIgnoreCase(address.getAddress())){
                    contains = true;
                    break;
                }
            }

           // cursor = db.query(CACHE_TABLE_NAME, new String[] { ID },
             //       MESSAGEID + "=?", new String[] {Integer.toString(userId)}, null, null, TIMESTAMP + " DESC", "5");



            if (contains) {   //previous was not a correct way to check entry exists. Both object are different since id is different,each query adds new row.
                result = (int) db.update(CACHE_TABLE_NAME, values, TYPE + "=?",
                        new String[] { location.getAddressId() + "" });

                CommonLib.ZLog("zloc addlocations if ", userId + " : " + location.getAddressId() + "");

            } else {

                byte[] bundle = RequestWrapper.Serialize_Object(location);

                values.put(MESSAGEID, userId);
                values.put(TYPE, location.getAddressId());
                values.put(BUNDLE, bundle);

                // Inserting Row
                result = (int) db.insert(CACHE_TABLE_NAME, null, values);
                CommonLib.ZLog("zloc addlocations else ", userId + " . " + location.getAddressId());
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

    public ArrayList<Address> getAddresses(int userId) {
        Address location;
        this.getReadableDatabase();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<Address> queries = new ArrayList<Address>();

        try {
            db = ctx.openOrCreateDatabase("/data/data/com.application.zapplon/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READONLY, null);

            cursor = db.query(CACHE_TABLE_NAME, new String[] { ID, MESSAGEID, TIMESTAMP, TYPE, BUNDLE },
                    MESSAGEID + "=?", new String[] {Integer.toString(userId)}, null, null, TIMESTAMP + " DESC", "5");
            if (cursor != null)
                cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                location = (Address) RequestWrapper.Deserialize_Object(cursor.getBlob(4), "");
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

    public ArrayList<Address> getAllAddresses(int userId) {
        Address location;
        this.getReadableDatabase();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<Address> queries = new ArrayList<Address>();

        try {
            db = ctx.openOrCreateDatabase("/data/data/com.application.zapplon/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READONLY, null);

            cursor = db.query(CACHE_TABLE_NAME, new String[] { ID, MESSAGEID, TIMESTAMP, TYPE, BUNDLE },
                    MESSAGEID + "=?", new String[] {Integer.toString(userId)}, null, null, TIMESTAMP + " DESC", null);
            if (cursor != null)
                cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                location = (Address) RequestWrapper.Deserialize_Object(cursor.getBlob(4), "");
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

    public int deleteAddress(int userId,int type){

        ArrayList<Address> locations = getAllAddresses(userId);
        List<String> addressIdList = new ArrayList<String>();
        int result = -1;
        try{
            this.getReadableDatabase();
            SQLiteDatabase db = ctx.openOrCreateDatabase("/data/data/com.application.zapplon/databases/" + DATABASE_NAME,
                    SQLiteDatabase.OPEN_READWRITE, null);

            for(Address location : locations){
                if(location.getAddressType() == type){
                    addressIdList.add(location.getAddressId()+"");
                }
            }

            StringBuilder builder = new StringBuilder();

            // TODO : improve this step
            builder.append("(");
            for (int i=0 ;i<addressIdList.size();i++) {
                String s = addressIdList.get(i);
                builder.append(s);
                if( i != addressIdList.size()-1)
                    builder.append(",");
            }
            builder.append(")");
            result = (int) db.delete(CACHE_TABLE_NAME,TYPE+" IN "+builder.toString(),null);
            db.close();
            this.close();
        }catch (Exception E) {
            try {
                this.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            result = -1;
        }
        return result;

    }


}