package com.example.mvvm_api_implementation.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Coroutines {
    fun IO(work:suspend(()->Unit) )=CoroutineScope(Dispatchers.IO).launch{
        work()
    }


    fun main(work:suspend (()->Unit))= CoroutineScope((Dispatchers.Main)).launch {
        work()
    }
}