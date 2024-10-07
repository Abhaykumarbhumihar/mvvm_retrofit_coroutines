package com.example.mvvm_api_implementation.network

import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.HashMap


interface Api {


    @GET
    @Headers("Accept: application/json")
    suspend fun getData(
        @Url url: String,
        @Header("Authorization") Authorization: String

    ): Response<JsonElement>


    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST
    suspend fun postData(
        @Url url: String,
        @FieldMap body: HashMap<String, Any>
    ): Response<JsonElement>



    @Multipart
    @POST
    suspend fun multipartRequest(
        @Url url: String,
        @PartMap map: HashMap<String, RequestBody>,
        @Part id_doc: MultipartBody.Part?
    ): Response<JsonElement>

    @Multipart
    @Headers("Accept: application/json")
    @POST
    suspend fun postDatamulti(
        @Url url: String,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: ArrayList<MultipartBody.Part>
    ): Response<JsonElement>


    @Multipart
    @Headers("Accept: application/json")
    @POST
    suspend fun postDatamulti(
        @Url url: String,
        @Header("Authorization") Authorization: String,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: ArrayList<MultipartBody.Part>
    ): Response<JsonElement>


    @Multipart
    @Headers("Accept: application/json")
    @POST
    suspend fun multiwithtoke(
        @Url url: String,
        @Header("Authorization") Authorization: String,
        @Part file: ArrayList<MultipartBody.Part>
    ): Response<JsonElement>



    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST
    suspend fun postDatawithheard(
        @Url url: String,
        @Header("Authorization") Authorization: String,
        @FieldMap body: HashMap<String, Any>
    ): Response<JsonElement>



    @Headers("Accept: application/json")
    @POST
    suspend fun postDatawithoutbody(
        @Url url: String,
        @Header("Authorization") Authorization: String
    ): Response<JsonElement>

    @GET("place/nearbysearch/json?")
    suspend fun doPlaces(
        @Query(value = "type", encoded = true) type: String?,
        @Query(
            value = "location",
            encoded = true
        ) location: String?,
        @Query(value = "name", encoded = true) name: String?,
        @Query(
            value = "opennow",
            encoded = true
        ) sensor: Boolean,
        @Query(
            value = "rankby",
            encoded = true
        ) rankby: String,
        @Query(value = "key", encoded = true) key: String?
    ): Response<JsonElement>


    @Multipart
    @Headers("Accept: application/json")
    @POST
    suspend fun uploadFood(
        @Url url: String,
        @Header("Authorization") Authorization: String,
        @Part foodimages: ArrayList<MultipartBody.Part?>?,
        @Part(value = "meal_type") meal_type: RequestBody?,
        @Part(value = "meal_name") meal_name: RequestBody?,
        @Part(value = "time") time: RequestBody?,
        @Part(value = "description") description: RequestBody?,
        @Part(value = "try_something_desc") try_something_desc: RequestBody?,
        @Part(value = "how_feel") how_feel: RequestBody?,
        @Part(value = "location") location: RequestBody?,
        @Part(value = "supplements") supplements: RequestBody?,
        @Part(value = "other") other: RequestBody?,
        @Part(value = "meal_contain") meal_contain: RequestBody?
    ): Response<JsonElement>


}