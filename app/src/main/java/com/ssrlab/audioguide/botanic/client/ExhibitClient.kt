package com.ssrlab.audioguide.botanic.client

import android.util.Log
import com.ssrlab.audioguide.botanic.SplashActivity
import com.ssrlab.audioguide.botanic.db.ExhibitDao
import com.ssrlab.audioguide.botanic.db.ExhibitObject
import com.ssrlab.audioguide.botanic.utils.REQUEST_TIME_OUT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

object ExhibitClient {

    private var client: OkHttpClient? = null

    fun getExhibits(scope: CoroutineScope, dbDao: ExhibitDao, activity: SplashActivity, onSuccess: () -> Unit, onFailure: () -> Unit) {

        if (client == null) client = OkHttpClient.Builder()
            .connectTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("https://cbg.krokam.by/api/rest/placelocale/")
            .build()

        client!!.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val jArray = responseBody?.let { JSONArray(it) }

                val arrayOfExhibits = arrayListOf<ExhibitObject>()

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

                        arrayOfExhibits.add(exhibitObject)
                    }
                }

                scope.launch {
                    val dbArray = dbDao.getAllExhibits() as ArrayList

                    if (dbArray != arrayOfExhibits) {
                        dbDao.deleteExhibits()
                        activity.getExternalFilesDir(null)?.deleteRecursively()
                        for (i in arrayOfExhibits) dbDao.insert(i)
                    }
                }

                onSuccess()
            }
        })
    }

    fun getAudio(link: String, file: File, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val request = Request.Builder()
            .url(link)
            .build()

        client?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e.message!!)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("File response", "Response error")
                    response.body?.string()?.let { onFailure(it) }
                } else {
                    val fos = FileOutputStream(file)
                    fos.write(response.body?.bytes())
                    fos.close()

                    onSuccess()
                }

                response.close()
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