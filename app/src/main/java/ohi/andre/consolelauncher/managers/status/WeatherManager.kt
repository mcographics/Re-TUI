@file:Suppress("DEPRECATION")

package ohi.andre.consolelauncher.managers.status

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.Locale
import ohi.andre.consolelauncher.R
import ohi.andre.consolelauncher.managers.HTMLExtractManager
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager
import ohi.andre.consolelauncher.managers.xml.options.Behavior
import ohi.andre.consolelauncher.UIManager

class WeatherManager(
    context: Context,
    delay: Long,
    private val size: Int,
    private val listener: StatusUpdateListener?
) : StatusManager(context, delay) {
    private var url: String? = null
    private var configurationError: String? = null
    private var locationQuery: String? = null
    private var resolvingLocation = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        val where = XMLPrefsManager.get(Behavior.weather_location)?.trim()
        val coordinates = WeatherResponseParser.parseCoordinates(where)
        if (where.isNullOrEmpty() || where == "null") {
            configurationError = context.getString(R.string.weather_location_required)
        } else if (coordinates != null) {
            setUrl(coordinates.first, coordinates.second)
        } else {
            locationQuery = where
        }
    }

    override fun update() {
        updateWeather()
    }

    override fun start() {
        if (!running) {
            publishCachedOrLoading()
            registerReconnectRefresh()
        }
        super.start()
    }

    override fun stop() {
        super.stop()
        unregisterReconnectRefresh()
    }

    fun updateWeather() {
        configurationError?.let {
            sendOutput(it)
            return
        }
        if (url == null && !resolvingLocation) {
            val query = locationQuery ?: return
            val cached = cachedCoordinates(query)
            if (cached != null) {
                setUrl(cached.first, cached.second)
            } else {
                resolveLocation(query)
                return
            }
        }

        val currentUrl = url
        if (currentUrl != null) {
            val intent = Intent(HTMLExtractManager.ACTION_WEATHER)
            intent.putExtra(XMLPrefsManager.VALUE_ATTRIBUTE, currentUrl)
            intent.putExtra(HTMLExtractManager.BROADCAST_COUNT, HTMLExtractManager.broadcastCount)
            LocalBroadcastManager.getInstance(context.applicationContext).sendBroadcast(intent)
        }
    }

    private fun resolveLocation(query: String) {
        resolvingLocation = true
        Thread {
            try {
                if (!Geocoder.isPresent()) throw IllegalStateException()
                val address = Geocoder(context, Locale.getDefault())
                    .getFromLocationName(query, 1)
                    ?.firstOrNull()
                    ?: throw IllegalArgumentException()
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                    .putString(CACHE_QUERY, query)
                    .putLong(CACHE_LATITUDE, java.lang.Double.doubleToRawLongBits(address.latitude))
                    .putLong(CACHE_LONGITUDE, java.lang.Double.doubleToRawLongBits(address.longitude))
                    .apply()
                setUrl(address.latitude, address.longitude)
                if (running) updateWeather()
            } catch (_: Exception) {
                sendOutput(context.getString(R.string.weather_location_not_found))
            } finally {
                resolvingLocation = false
            }
        }.start()
    }

    private fun cachedCoordinates(query: String): Pair<Double, Double>? {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (preferences.getString(CACHE_QUERY, null) != query ||
            !preferences.contains(CACHE_LATITUDE) || !preferences.contains(CACHE_LONGITUDE)
        ) return null
        return java.lang.Double.longBitsToDouble(preferences.getLong(CACHE_LATITUDE, 0)) to
            java.lang.Double.longBitsToDouble(preferences.getLong(CACHE_LONGITUDE, 0))
    }

    fun setLocation(latitude: Double, longitude: Double) {
        locationQuery = null
        configurationError = null
        setUrl(latitude, longitude)
        updateWeather()
    }

    private fun sendOutput(message: String) {
        val intent = Intent(UIManager.ACTION_WEATHER)
        intent.putExtra(XMLPrefsManager.VALUE_ATTRIBUTE, message)
        val cached = WeatherSnapshotStore.load(context)
        if (cached != null) {
            WeatherIntentContract.putSnapshot(
                intent,
                cached.data,
                cached = true,
                savedAtMillis = cached.savedAtMillis
            )
            intent.putExtra(UIManager.WEATHER_SYMBOL, cached.data.symbolCode)
        } else {
            WeatherIntentContract.putState(intent, WeatherLineState.UNAVAILABLE)
        }
        LocalBroadcastManager.getInstance(context.applicationContext).sendBroadcast(intent)
    }

    private fun publishCachedOrLoading() {
        val intent = Intent(UIManager.ACTION_WEATHER)
        val cached = WeatherSnapshotStore.load(context)
        if (cached != null) {
            WeatherIntentContract.putSnapshot(
                intent,
                cached.data,
                cached = true,
                savedAtMillis = cached.savedAtMillis
            )
            intent.putExtra(
                XMLPrefsManager.VALUE_ATTRIBUTE,
                cached.data.conditionLabel + ", " + cached.data.temperature + "°" + cached.data.temperatureUnit
            )
            intent.putExtra(UIManager.WEATHER_SYMBOL, cached.data.symbolCode)
        } else {
            WeatherIntentContract.putState(intent, WeatherLineState.LOADING)
            intent.putExtra(XMLPrefsManager.VALUE_ATTRIBUTE, "WX // SYNC_")
        }
        LocalBroadcastManager.getInstance(context.applicationContext).sendBroadcast(intent)
    }

    private fun registerReconnectRefresh() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || networkCallback != null) return
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (running) handler.post { updateWeather() }
            }
        }
        try {
            connectivity.registerDefaultNetworkCallback(callback)
            networkCallback = callback
        } catch (_: Exception) {
            networkCallback = null
        }
    }

    private fun unregisterReconnectRefresh() {
        val callback = networkCallback ?: return
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        try {
            connectivity?.unregisterNetworkCallback(callback)
        } catch (_: Exception) {
        }
        networkCallback = null
    }

    private fun setUrl(latitude: Double, longitude: Double) {
        url = String.format(
            Locale.US,
            "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=%.4f&lon=%.4f",
            latitude,
            longitude
        )
    }

    fun setDelay(delay: Int) {
        this.delay = delay.toLong()
    }

    companion object {
        private const val PREFS_NAME = "retui_weather"
        private const val CACHE_QUERY = "location_query"
        private const val CACHE_LATITUDE = "latitude"
        private const val CACHE_LONGITUDE = "longitude"
    }
}
