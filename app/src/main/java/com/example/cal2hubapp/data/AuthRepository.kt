
package com.example.cal2hubapp.data

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import com.example.cal2hubapp.data.models.User
import com.example.cal2hubapp.util.SecurityUtils

class AuthRepository(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_status_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_USER_USERNAME = "current_user_username"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun register(username: String, password: String, birthday: String, favoriteFood: String): Boolean {
        if (getUserByUsername(username) != null) {
            return false // User already exists
        }

        val db = dbHelper.writableDatabase
        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)

        val values = ContentValues().apply {
            put(UserContract.UserEntry.COLUMN_NAME_USERNAME, username)
            put(UserContract.UserEntry.COLUMN_NAME_PASSWORD_HASH, passwordHash)
            put(UserContract.UserEntry.COLUMN_NAME_SALT, salt)
            put(UserContract.UserEntry.COLUMN_NAME_BIRTHDAY, birthday)
            put(UserContract.UserEntry.COLUMN_NAME_FAVORITE_FOOD, favoriteFood)
        }

        val newRowId = db.insert(UserContract.UserEntry.TABLE_NAME, null, values)
        return newRowId != -1L
    }

    fun login(username: String, password: String): Boolean {
        val user = getUserByUsername(username)
        if (user != null) {
            val hashedPassword = SecurityUtils.hashPassword(password, user.salt)
            if (hashedPassword == user.passwordHash) {
                setLoggedIn(true)
                setCurrentUser(user)
                return true
            }
        }
        return false
    }

    fun logout() {
        setLoggedIn(false)
        prefs.edit().remove(KEY_CURRENT_USER_USERNAME).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getCurrentUser(): User? {
        val username = prefs.getString(KEY_CURRENT_USER_USERNAME, null) ?: return null
        return getUserByUsername(username)
    }

    fun getUserByUsername(username: String): User? {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            UserContract.UserEntry.COLUMN_NAME_USERNAME,
            UserContract.UserEntry.COLUMN_NAME_PASSWORD_HASH,
            UserContract.UserEntry.COLUMN_NAME_SALT,
            UserContract.UserEntry.COLUMN_NAME_EMAIL,
            UserContract.UserEntry.COLUMN_NAME_MOBILE,
            UserContract.UserEntry.COLUMN_NAME_BIRTHDAY,
            UserContract.UserEntry.COLUMN_NAME_FAVORITE_FOOD
        )
        val selection = "${UserContract.UserEntry.COLUMN_NAME_USERNAME} = ?"
        val selectionArgs = arrayOf(username)

        val cursor = db.query(
            UserContract.UserEntry.TABLE_NAME,
            projection, selection, selectionArgs, null, null, null
        )

        with(cursor) {
            if (moveToFirst()) {
                return User(
                    username = getString(getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_NAME_USERNAME)),
                    passwordHash = getString(getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_NAME_PASSWORD_HASH)),
                    salt = getString(getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_NAME_SALT)),
                    email = getString(getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_NAME_EMAIL)),
                    mobile = getString(getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_NAME_MOBILE)),
                    birthday = getString(getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_NAME_BIRTHDAY)),
                    favoriteFood = getString(getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_NAME_FAVORITE_FOOD))
                )
            }
        }
        return null
    }

    fun updatePassword(username: String, newPassword: String): Boolean {
        val db = dbHelper.writableDatabase
        val salt = SecurityUtils.generateSalt()
        val newPasswordHash = SecurityUtils.hashPassword(newPassword, salt)

        val values = ContentValues().apply {
            put(UserContract.UserEntry.COLUMN_NAME_PASSWORD_HASH, newPasswordHash)
            put(UserContract.UserEntry.COLUMN_NAME_SALT, salt)
        }

        val selection = "${UserContract.UserEntry.COLUMN_NAME_USERNAME} = ?"
        val selectionArgs = arrayOf(username)
        val count = db.update(UserContract.UserEntry.TABLE_NAME, values, selection, selectionArgs)
        return count > 0
    }

    fun updateUser(updatedUser: User): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(UserContract.UserEntry.COLUMN_NAME_EMAIL, updatedUser.email)
            put(UserContract.UserEntry.COLUMN_NAME_MOBILE, updatedUser.mobile)
        }
        val selection = "${UserContract.UserEntry.COLUMN_NAME_USERNAME} = ?"
        val selectionArgs = arrayOf(updatedUser.username)
        val count = db.update(UserContract.UserEntry.TABLE_NAME, values, selection, selectionArgs)
        return count > 0
    }

    private fun setCurrentUser(user: User) {
        prefs.edit().putString(KEY_CURRENT_USER_USERNAME, user.username).apply()
    }

    private fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
}
