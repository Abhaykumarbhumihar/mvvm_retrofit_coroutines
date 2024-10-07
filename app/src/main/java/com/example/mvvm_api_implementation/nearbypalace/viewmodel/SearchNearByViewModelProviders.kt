package com.example.mvvm_api_implementation.nearbypalace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mvvm_api_implementation.nearbypalace.repository.SearchNearByRepository
import java.lang.IllegalArgumentException

class SearchNearByViewModelProviders (private val repository: SearchNearByRepository):ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SearchNearByViewModel::class.java)){
            return  SearchNearByViewModel(repository)as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")

    }
}