package com.ssrlab.audioguide.botanic.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "exhibit_table")
data class ExhibitObject(

    @PrimaryKey
    @SerializedName("id")
    val primaryId: Int = 0,

    @SerializedName("place_id")
    val placeId: Int = 0,

    @SerializedName("place_logo")
    val imagePreview: String = "",

    @SerializedName("place_qrcode")
    val qr: String = "",

    @SerializedName("place_name_locale")
    val placeName: String = "",

    @SerializedName("text_speaker")
    val audioText: String = "",

    @SerializedName("place_sound")
    val audio: String = "",

    @SerializedName("lang")
    val language: Int = 0,

    @SerializedName("lat")
    val lat: Double = 0.0,

    @SerializedName("lng")
    val lng: Double = 0.0,

    @SerializedName("place_images")
    val images: Map<String, String> = mapOf()
)