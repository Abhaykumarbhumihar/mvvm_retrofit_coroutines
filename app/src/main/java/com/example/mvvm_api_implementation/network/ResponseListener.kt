package com.example.mvvm_api_implementation.network

interface ResponseListener {

    fun onSuccess(message: String, url: String)

    fun onFailure(message: String, url: String)

}