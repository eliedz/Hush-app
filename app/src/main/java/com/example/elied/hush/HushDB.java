package com.example.elied.hush;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class HushDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "hushDB";
    private static final int DB_VERSION = 1;


    HushDB(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE PLAYERINFO ("
        + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lastPlayer INTEGER");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
