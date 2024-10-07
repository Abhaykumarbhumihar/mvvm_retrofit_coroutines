package com.example.mvvm_api_implementation.network

import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

class CommonRequest {

 suspend fun <T:Any>apiRequest(call:suspend ()->Response<T>):T{
     val  response=call.invoke()
     if(response.isSuccessful){
         return  response.body()!!
     }else{
         val error=response.errorBody().toString()
         var message=StringBuilder()
         error.let {
             try {
                 message.append(JSONObject(it).getString("message"))
             } catch (error: JSONException) { }
             message.append("\n ${error}")
         }
         throw CommonException(message.toString())
     }
 }
}