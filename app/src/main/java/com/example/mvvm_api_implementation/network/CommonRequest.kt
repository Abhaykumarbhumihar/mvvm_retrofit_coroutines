package com.example.mvvm_api_implementation.network

import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

open class CommonRequest {

 suspend fun <T:Any>apiRequest(call:suspend ()->Response<T>):T{
     val  response=call.invoke()
     if(response.isSuccessful){
         return  response.body()!!
     }else{
         val error=response.errorBody().toString()
         val message=StringBuilder()
         error.let {
             try {
                 message.append(JSONObject(it).getString("message"))
             } catch (jsonException: JSONException) {
                 message.append("\n ${jsonException}")
             }
             message.append("\n")
         }
         throw CommonException(message.toString())
     }
 }
}