package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

class PlaceResponse(val status: String, val places: List<Place>)

class Place(var name: String, val location: Location, @SerializedName("formatted_address") val address: String)

class Location(var lng: String, var lat: String)