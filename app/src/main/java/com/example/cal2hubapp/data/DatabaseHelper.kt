
package com.example.cal2hubapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

object UserContract {
    object UserEntry : BaseColumns {
        const val TABLE_NAME = "users"
        const val COLUMN_NAME_USERNAME = "username"
        const val COLUMN_NAME_PASSWORD_HASH = "password_hash"
        const val COLUMN_NAME_SALT = "salt"
        const val COLUMN_NAME_EMAIL = "email"
        const val COLUMN_NAME_MOBILE = "mobile"
        const val COLUMN_NAME_BIRTHDAY = "birthday"
        const val COLUMN_NAME_FAVORITE_FOOD = "favorite_food"
    }
}

private const val SQL_CREATE_ENTRIES =
    "CREATE TABLE ${UserContract.UserEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${UserContract.UserEntry.COLUMN_NAME_USERNAME} TEXT UNIQUE," +
            "${UserContract.UserEntry.COLUMN_NAME_PASSWORD_HASH} TEXT," +
            "${UserContract.UserEntry.COLUMN_NAME_SALT} TEXT," +
            "${UserContract.UserEntry.COLUMN_NAME_EMAIL} TEXT," +
            "${UserContract.UserEntry.COLUMN_NAME_MOBILE} TEXT," +
            "${UserContract.UserEntry.COLUMN_NAME_BIRTHDAY} TEXT," +
            "${UserContract.UserEntry.COLUMN_NAME_FAVORITE_FOOD} TEXT)"

private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${UserContract.UserEntry.TABLE_NAME}"

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "Cal2Hub.db"
    }
}
