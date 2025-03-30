package com.example.mobileappcar.model

import com.google.gson.annotations.SerializedName

data class Service(
    val id: Int,
    val name: String,
    val description: String? = null,
    val price: Float,
    val status: String? = null,
    val image: String? = null,
    @SerializedName("time_0800") val time0800: Boolean? = null,
    @SerializedName("time_0900") val time0900: Boolean? = null,
    @SerializedName("time_1000") val time1000: Boolean? = null,
    @SerializedName("time_1300") val time1300: Boolean? = null,
    @SerializedName("time_1400") val time1400: Boolean? = null,
    @SerializedName("time_1500") val time1500: Boolean? = null,
    @SerializedName("created_at") val createdAt: String
) {
    // Helper function to get available times
    fun getAvailableTimes(): List<String> {
        val times = mutableListOf<String>()
        if (time0800 == true) times.add("08:00")
        if (time0900 == true) times.add("09:00")
        if (time1000 == true) times.add("10:00")
        if (time1300 == true) times.add("13:00")
        if (time1400 == true) times.add("14:00")
        if (time1500 == true) times.add("15:00")
        return times
    }
}