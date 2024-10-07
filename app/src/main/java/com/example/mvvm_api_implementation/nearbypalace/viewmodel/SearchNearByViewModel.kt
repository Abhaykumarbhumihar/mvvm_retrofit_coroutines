package com.example.mvvm_api_implementation.nearbypalace.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mvvm_api_implementation.nearbypalace.repository.SearchNearByRepository
import com.example.mvvm_api_implementation.network.CommonException
import com.example.mvvm_api_implementation.network.Coroutines
import com.example.mvvm_api_implementation.network.ResponseListener

class SearchNearByViewModel(private var repository: SearchNearByRepository) : ViewModel() {

    var responseListener: ResponseListener? = null

    fun searchNearyByLocation(map: HashMap<String, Any>, url: String) {
        Coroutines.IO {
            try {
                val response = repository.searchNearByLocation(
                    map,url
                )
                response.let {
                    responseListener?.onSuccess(response.toString(), url)
                }
            } catch (e: CommonException) {
                responseListener?.onFailure(e.message!!, url)
            }
        }
    }
}