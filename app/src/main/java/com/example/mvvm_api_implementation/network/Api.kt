package com.example.mvvm_api_implementation.network

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url


interface Api {
    @GET
    suspend fun doPlaces(
        @Url url: String,
        @QueryMap(encoded = true) options: HashMap<String, Any>

    ): Response<JsonElement>
}