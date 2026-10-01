package com.example.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

data class RealTimeWeatherResult(
    val location: String,
    val weather: WeatherItem,
    val temperature: String,
    val latitude: Double,
    val longitude: Double,
    val humidity: Int? = null,
    val windSpeed: Double? = null,
    val source: String = "实时定位",
    val description: String = ""
)

data class CitySearchResult(
    val id: Long,
    val name: String,
    val country: String,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double
) {
    val displayLabel: String
        get() = buildString {
            append(name)
            if (!admin1.isNullOrBlank() && admin1 != name) {
                append(" · ").append(admin1)
            }
            if (country.isNotBlank()) {
                append(" (").append(country).append(")")
            }
        }
}

class WeatherLocationService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Primary entry point: automatically fetch current location & weather in real-time.
     * Tries:
     * 1. Device Location (GPS / Network) if permission is granted.
     * 2. IP Geolocation as robust fallback (works in emulators, offline GPS, or without permission).
     * Then queries Open-Meteo free weather API.
     */
    suspend fun fetchCurrentLocationAndWeather(): Result<RealTimeWeatherResult> = withContext(Dispatchers.IO) {
        try {
            var lat: Double? = null
            var lon: Double? = null
            var locationName: String? = null
            var sourceName = "IP网络定位"

            // 1. Try Device Location if permission is granted
            val hasFineLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarseLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFineLocation || hasCoarseLocation) {
                val loc = getDeviceLocation()
                if (loc != null) {
                    lat = loc.latitude
                    lon = loc.longitude
                    sourceName = "GPS/基站定位"
                    locationName = reverseGeocode(loc.latitude, loc.longitude)
                }
            }

            // 2. Fallback to IP Geolocation if device location not available
            if (lat == null || lon == null) {
                val ipLoc = fetchIpLocation()
                if (ipLoc != null) {
                    lat = ipLoc.first
                    lon = ipLoc.second
                    locationName = ipLoc.third
                    sourceName = "网络IP定位"
                }
            }

            // Default fallback coordinates (Beijing) if both fail
            val finalLat = lat ?: 39.9042
            val finalLon = lon ?: 116.4074
            val finalLocation = locationName?.ifBlank { "当地" } ?: "当地"

            // 3. Fetch Weather from Open-Meteo
            val weatherResult = fetchWeatherForCoordinates(finalLat, finalLon, finalLocation, sourceName)
            Result.success(weatherResult)
        } catch (e: Exception) {
            Log.e("WeatherLocationService", "Failed to fetch location/weather", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch weather for a specific custom location (e.g. searched city or coordinates).
     */
    suspend fun fetchWeatherForCity(
        cityName: String,
        latitude: Double,
        longitude: Double
    ): Result<RealTimeWeatherResult> = withContext(Dispatchers.IO) {
        try {
            val result = fetchWeatherForCoordinates(latitude, longitude, cityName, "指定城市")
            Result.success(result)
        } catch (e: Exception) {
            Log.e("WeatherLocationService", "Failed to fetch city weather", e)
            Result.failure(e)
        }
    }

    /**
     * Search cities by keyword using Open-Meteo Geocoding API (Free, no key required).
     */
    suspend fun searchCities(keyword: String): Result<List<CitySearchResult>> = withContext(Dispatchers.IO) {
        if (keyword.isBlank()) return@withContext Result.success(emptyList())
        try {
            val encodedQuery = java.net.URLEncoder.encode(keyword.trim(), "UTF-8")
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$encodedQuery&count=8&language=zh&format=json"
            val request = Request.Builder().url(url).build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("搜索城市失败: HTTP ${response.code}"))
            }

            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val resultsArray = json.optJSONArray("results") ?: return@withContext Result.success(emptyList())

            val list = mutableListOf<CitySearchResult>()
            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.getJSONObject(i)
                list.add(
                    CitySearchResult(
                        id = item.optLong("id", i.toLong()),
                        name = item.optString("name", ""),
                        country = item.optString("country", ""),
                        admin1 = item.optString("admin1", null),
                        latitude = item.optDouble("latitude", 0.0),
                        longitude = item.optDouble("longitude", 0.0)
                    )
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e("WeatherLocationService", "City search error", e)
            Result.failure(e)
        }
    }

    /**
     * Internal Open-Meteo weather API call.
     */
    private fun fetchWeatherForCoordinates(
        latitude: Double,
        longitude: Double,
        locationName: String,
        source: String
    ): RealTimeWeatherResult {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto"

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IOException("天气接口请求失败: HTTP ${response.code}")
        }

        val body = response.body?.string().orEmpty()
        val json = JSONObject(body)
        val current = json.getJSONObject("current")

        val tempDouble = current.getDouble("temperature_2m")
        val weatherCode = current.getInt("weather_code")
        val humidity = current.optInt("relative_humidity_2m")
        val windSpeed = current.optDouble("wind_speed_10m")

        val tempStr = "${tempDouble.roundToInt()}°C"
        val weatherItem = mapWmoCodeToWeatherItem(weatherCode, tempStr)

        return RealTimeWeatherResult(
            location = locationName,
            weather = weatherItem,
            temperature = tempStr,
            latitude = latitude,
            longitude = longitude,
            humidity = humidity,
            windSpeed = windSpeed,
            source = source,
            description = "${weatherItem.label} · 气温 $tempStr · 湿度 $humidity% · 风速 ${windSpeed}km/h"
        )
    }

    /**
     * Map WMO weather codes to WeatherItem
     */
    private fun mapWmoCodeToWeatherItem(code: Int, defaultTemp: String): WeatherItem {
        return when (code) {
            0 -> WeatherItem("☀️", "晴朗", defaultTemp)
            1 -> WeatherItem("🌤️", "晴间多云", defaultTemp)
            2 -> WeatherItem("⛅", "多云", defaultTemp)
            3 -> WeatherItem("☁️", "阴天", defaultTemp)
            45, 48 -> WeatherItem("🌫️", "晨雾", defaultTemp)
            51, 53, 55 -> WeatherItem("🌧️", "毛毛雨", defaultTemp)
            56, 57 -> WeatherItem("🌧️", "冻雨", defaultTemp)
            61, 63 -> WeatherItem("🌧️", "细雨", defaultTemp)
            65 -> WeatherItem("🌧️", "大雨", defaultTemp)
            66, 67 -> WeatherItem("🌨️", "雨夹雪", defaultTemp)
            71, 73 -> WeatherItem("❄️", "飞雪", defaultTemp)
            75 -> WeatherItem("❄️", "大雪", defaultTemp)
            77 -> WeatherItem("🌨️", "雪粒", defaultTemp)
            80, 81 -> WeatherItem("🌦️", "阵雨", defaultTemp)
            82 -> WeatherItem("⛈️", "强暴雨", defaultTemp)
            85, 86 -> WeatherItem("🌨️", "阵雪", defaultTemp)
            95 -> WeatherItem("⛈️", "雷阵雨", defaultTemp)
            96, 99 -> WeatherItem("⛈️", "雷阵雨", defaultTemp)
            else -> WeatherItem("⛅", "多云", defaultTemp)
        }
    }

    /**
     * Retrieve device location using Android's LocationManager
     */
    private fun getDeviceLocation(): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        var bestLocation: Location? = null
        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                        bestLocation = loc
                    }
                }
            } catch (e: SecurityException) {
                Log.w("WeatherLocationService", "SecurityException for provider: $provider", e)
            } catch (e: Exception) {
                Log.w("WeatherLocationService", "Error reading location provider: $provider", e)
            }
        }
        return bestLocation
    }

    /**
     * Reverse geocode coordinates to friendly Chinese city/district name
     */
    private fun reverseGeocode(lat: Double, lon: Double): String {
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.CHINESE)
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    val subLocality = addr.subLocality ?: addr.featureName
                    return buildString {
                        if (!city.isNullOrBlank()) append(city)
                        if (!subLocality.isNullOrBlank() && subLocality != city) {
                            if (isNotEmpty()) append(" · ")
                            append(subLocality)
                        }
                    }.ifBlank { addr.adminArea ?: "当地" }
                }
            }
        } catch (e: Exception) {
            Log.w("WeatherLocationService", "Android Geocoder failed, trying online reverse geocode", e)
        }

        // Online reverse geocode via BigDataCloud (Free, no API key needed)
        try {
            val url = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$lat&longitude=$lon&localityLanguage=zh"
            val req = Request.Builder().url(url).build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string().orEmpty()
                val json = JSONObject(body)
                val city = json.optString("city").ifBlank { json.optString("locality") }
                val region = json.optString("principalSubdivision")
                return buildString {
                    if (region.isNotBlank()) append(region)
                    if (city.isNotBlank() && city != region) {
                        if (isNotEmpty()) append(" · ")
                        append(city)
                    }
                }.ifBlank { json.optString("countryName", "当地") }
            }
        } catch (e: Exception) {
            Log.w("WeatherLocationService", "Online reverse geocode failed", e)
        }

        return "当地"
    }

    /**
     * Fetch IP geolocation (Latitude, Longitude, CityName)
     */
    private fun fetchIpLocation(): Triple<Double, Double, String>? {
        // Try BigDataCloud reverse geocode client without coords (automatically uses client IP)
        try {
            val url = "https://api.bigdatacloud.net/data/reverse-geocode-client?localityLanguage=zh"
            val req = Request.Builder().url(url).build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string().orEmpty()
                val json = JSONObject(body)
                val lat = json.optDouble("latitude", Double.NaN)
                val lon = json.optDouble("longitude", Double.NaN)
                val city = json.optString("city").ifBlank { json.optString("locality") }
                val region = json.optString("principalSubdivision")
                val locationName = buildString {
                    if (region.isNotBlank()) append(region)
                    if (city.isNotBlank() && city != region) {
                        if (isNotEmpty()) append(" · ")
                        append(city)
                    }
                }.ifBlank { json.optString("countryName", "当地") }

                if (!lat.isNaN() && !lon.isNaN() && (lat != 0.0 || lon != 0.0)) {
                    return Triple(lat, lon, locationName)
                }
            }
        } catch (e: Exception) {
            Log.w("WeatherLocationService", "BigDataCloud IP lookup failed", e)
        }

        // Try IP-API as secondary fallback
        try {
            val url = "http://ip-api.com/json/?lang=zh-CN"
            val req = Request.Builder().url(url).build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string().orEmpty()
                val json = JSONObject(body)
                if (json.optString("status") == "success") {
                    val lat = json.getDouble("lat")
                    val lon = json.getDouble("lon")
                    val city = json.optString("city")
                    val region = json.optString("regionName")
                    val locName = if (region.isNotBlank() && city.isNotBlank() && region != city) {
                        "$region · $city"
                    } else city.ifBlank { region }.ifBlank { "当地" }
                    return Triple(lat, lon, locName)
                }
            }
        } catch (e: Exception) {
            Log.w("WeatherLocationService", "ip-api.com lookup failed", e)
        }

        return null
    }
}
