# Android Nearby Places Finder

This Android application demonstrates how to use the Fused Location Provider to get the user's current location and display nearby places, such as restaurants, using the Google Places API. The app utilizes Retrofit for network requests and follows the MVVM architecture pattern.

## Features

- Continuously updates the user's location using the Fused Location Provider.
- Fetches and displays nearby places based on user input.
- Uses Retrofit for API calls and handles responses effectively.
- Implements ViewModel for managing UI-related data.

## Technologies Used

- Android SDK
- Kotlin
- Retrofit
- Gson
- OkHttp
- MVVM Architecture
- Coroutines

## Setup Instructions

### Prerequisites

- Android Studio
- Android device/emulator with Google Play Services

### Getting Started

1. **Clone the Repository**

   ```bash
   
   git clone https://github.com/Abhaykumarbhumihar/mvvm_retrofit_coroutines.git


Open in Android Studio
Add Your Google API Key

## AppConstants
```kotlin
object AppConstants {
    const val MAP_KEY = "YOUR_API_KEY" // Replace with your actual API key
    const val NEARBYURL = "nearbysearch/json" // Google Places API endpoint
}
```

## Permissions
```manifest
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```
### Application Structure
Fused Location Provider
The application uses the Fused Location Provider to fetch the user's current location continuously. This is set up in the MainActivity:

Location Updates: Implemented in startLocationUpdates() method using FusedLocationProviderClient.
Permission Handling: The app checks for location permissions and requests them if not granted.
Retrofit API Interface
The Api interface defines the method for fetching nearby places:

## Retrofit API Interface

```kotlin
interface Api {
    @GET
    suspend fun doPlaces(
        @QueryMap(encoded = true) options: HashMap<String, Any>,
        @Url url: String
    ): Response<JsonElement>
}

```

## Web Services

The WebServices class is responsible for setting up Retrofit and creating the API instance:


```kotlin
class WebServices {
    var api: Api

    init {
        val okHttpClient = OkHttpClient().newBuilder()
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .connectTimeout(180, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
        api = retrofit.create(Api::class.java)
    }
}

```

## Common Request Handling

The CommonRequest class provides a generic method for handling API responses and exceptions:


```kotlin
open class CommonRequest {
    suspend fun <T: Any> apiRequest(call: suspend () -> Response<T>): T {
        val response = call.invoke()
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            val error = response.errorBody().toString()
            // Handle error
            throw CommonException("Error: ${error}")
        }
    }
}
```

## Repository

The SearchNearByRepository is responsible for encapsulating the data operations related to fetching nearby places:

```kotlin
class SearchNearByRepository(private var webServices: WebServices) : CommonRequest() {
    suspend fun searchNearByLocation(map: HashMap<String, Any>, url: String): JsonElement {
        return apiRequest {
            webServices.api.doPlaces(map, url)
        }
    }
}

```

## ViewModel

The SearchNearByRepository is responsible for encapsulating the data operations related to fetching nearby places:

```kotlin
class SearchNearByViewModel(private var repository: SearchNearByRepository) : ViewModel() {
    var responseListener: ResponseListener? = null

    fun searchNearyByLocation(map: HashMap<String, Any>, url: String) {
        Coroutines.IO {
            try {
                val response = repository.searchNearByLocation(map, url)
                responseListener?.onSuccess(response.toString(), url)
            } catch (e: CommonException) {
                responseListener?.onFailure(e.message!!, url)
            }
        }
    }
}
```


## ViewModelProvider

The application uses the ViewModelProvider to manage the SearchNearByViewModel. Below is an example of how to implement the ViewModelProvider in the MainActivity.

```kotlin
class SearchNearByViewModelProviders (private val repository: SearchNearByRepository):ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SearchNearByViewModel::class.java)){
            return  SearchNearByViewModel(repository)as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")

    }
}
```

## Actiivty

Use ViewModelProvider in  MainActivity

```kotlin
val webServices = WebServices()
    repository = SearchNearByRepository(webServices)
        val viewModelProviders = SearchNearByViewModelProviders(repository)
        viewModel = ViewModelProvider(this, viewModelProviders)[SearchNearByViewModel::class.java]
```
