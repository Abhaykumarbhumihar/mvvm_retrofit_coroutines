package com.example.mvvm_api_implementation.nearbypalace.repository

import com.example.mvvm_api_implementation.network.CommonRequest
import com.example.mvvm_api_implementation.network.WebServices
import com.google.gson.JsonElement
import java.util.HashMap


class SearchNearByRepository(private var webServices: WebServices) : CommonRequest() {
    suspend fun searchNearByLocation(map: HashMap<String, Any>,url:String): JsonElement {
        return apiRequest {
            webServices.api.doPlaces(url,map)
        }
    }

}