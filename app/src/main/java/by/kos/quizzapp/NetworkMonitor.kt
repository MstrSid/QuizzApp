package by.kos.quizzapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import android.util.Log

object NetworkMonitor {

    private const val TAG = "NETWORK_MONITOR"

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    @Synchronized
    fun start(context: Context) {
        if (networkCallback != null) {
            return
        }

        val appContext = context.applicationContext

        val manager = appContext.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                Log.d(TAG, "Сетевое подключение появилось")
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                if (
                    capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    )
                ) {
                    Log.d(TAG, "Интернет доступен")

                    mainHandler.post {
                        PendingResultSync.sync(appContext)
                    }
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Сетевое подключение потеряно")
            }
        }

        try {
            manager.registerNetworkCallback(request, callback)

            connectivityManager = manager
            networkCallback = callback

            Log.d(TAG, "Мониторинг сети запущен")

        } catch (exception: Exception) {
            Log.e(
                TAG,
                "Не удалось запустить мониторинг сети",
                exception
            )
        }
    }
}
