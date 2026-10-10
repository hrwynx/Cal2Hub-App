package com.example.cal2hubapp.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

/**
 * Reads a JSON file from the assets folder and deserializes it into a list of objects.
 *
 * @param fileName The name of the JSON file in the assets folder.
 * @param gson An instance of Gson for deserialization.
 * @return A list of deserialized objects, or an empty list if an error occurs.
 */
inline fun <reified T> Context.readAssetJsonAsList(fileName: String, gson: Gson): List<T> {
    return try {
        val jsonString = assets.open(fileName).bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<T>>() {}.type
        gson.fromJson(jsonString, listType)
    } catch (e: IOException) {
        // Handle the error, e.g., by logging it and returning an empty list.
        e.printStackTrace()
        emptyList()
    }
}
