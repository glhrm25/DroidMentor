package com.example.droidmentor.Networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
sealed class StatusInternet{
    data object StatusOff: StatusInternet()
    data object StatusOn: StatusInternet()
    data object StatusIdle: StatusInternet()
}

class NetworkMonitor(private val context: Context) {
    /** callbackFlow é basicamente flow normal, diferença que internamente funciona à base de
     * de channel internamente, ou seja, quando alguém retira valor, valor sai do channel*/
    val isOnline: Flow<StatusInternet> = callbackFlow {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
        val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        trySend(if(hasInternet) StatusInternet.StatusOn else StatusInternet.StatusOff) // Lança o primeiro valor real


        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Rede disponível, mas pode ainda não estar validada
                val caps = connectivityManager.getNetworkCapabilities(network)
                val validated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
                trySend(StatusInternet.StatusOn)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                // Verifica se o Android testou e confirmou que a ligação chega mesmo à internet.
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(if(hasInternet && isValidated) StatusInternet.StatusOn else
                StatusInternet.StatusOff)
            }

            override fun onLost(network: Network) {
                trySend(StatusInternet.StatusOff)
            }
            //
            override fun onUnavailable() {
                trySend(StatusInternet.StatusOff)
            }
        }


      // cria uma thread que fica à espera de avisos do so caso alguem ligue internet ou desligue
        connectivityManager.registerDefaultNetworkCallback(callback)

        // Remove o registo automaticamente para evitar memory leaks
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}