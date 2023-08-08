package com.ssrlab.audioguide.botanic.client

import com.ssrlab.audioguide.botanic.db.ExhibitDao
import com.ssrlab.audioguide.botanic.db.ExhibitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object ExhibitClient {

    private var client: OkHttpClient? = null

    fun getExhibits(scope: CoroutineScope, dbDao: ExhibitDao, onSuccess: () -> Unit, onFailure: () -> Unit) {

        if (client == null) client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url("https://itamiuchiha.pythonanywhere.com/api/rest/placelocale/")
            .build()

        client!!.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val jArray = responseBody?.let { JSONArray(it) }

                for (i in 0 until jArray?.length()!!) {
                    (jArray[i] as JSONObject).apply {
                        val exhibitObject = ExhibitObject(
                            primaryId = this.getInt("id"),
                            placeId = this.getInt("place_id"),
                            imagePreview = this.getString("place_logo"),
                            qr = this.getString("place_qrcode"),
                            placeName = this.getString("place_name_locale"),
                            audioText = this.getString("text_speaker"),
                            audio = this.getString("place_sound"),
                            language = this.getInt("lang"),
                            lat = this.getDouble("lat"),
                            lng = this.getDouble("lng"),
                            images = parseJsonToMap(this.getString("place_images"))
                        )

                        scope.launch {
                            dbDao.insert(exhibitObject)
                        }
                    }
                }

                onSuccess()
            }
        })
    }

    private fun parseJsonToMap(jsonString: String): Map<String, String> {
        val jsonObject = JSONObject(jsonString)
        val map = HashMap<String, String>()

        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.getString(key)
            map[key] = value
        }

        return map
    }
}