package com.toyota.launcher.feature.topstatus

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Aggregates the system signals shown in the top strip. Each signal is its
 * own small Flow; they're combined into a single [TopStatusState].
 *
 * Time uses ACTION_TIME_TICK which fires once a minute (Android's recommended
 * way to render a clock without a looping coroutine).
 */
@Singleton
class TopStatusRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val timeFormatter = SimpleDateFormat("h:mm", Locale.getDefault())

    fun observe(): Flow<TopStatusState> = combine(
        timeFlow(),
        connectivityFlow(),
        bluetoothFlow(),
        locationFlow(),
    ) { time, conn, bt, loc ->
        TopStatusState(
            timeText = time,
            signalBars = conn.first,
            carrier = conn.second,
            bluetoothOn = bt,
            microphoneActive = false, // requires AppOps observation - omitted for brevity
            locationEnabled = loc,
            unreadNotifications = 0,
        )
    }

    private fun timeFlow(): Flow<String> = callbackFlow {
        val emit = { trySend(timeFormatter.format(Date())) }
        emit()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) { emit() }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        awaitClose { runCatching { context.unregisterReceiver(receiver) } }
    }

    private fun connectivityFlow(): Flow<Pair<Int, String>> = callbackFlow {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        if (cm == null) { trySend(0 to ""); close(); return@callbackFlow }

        fun snapshot() {
            val nc = cm.getNetworkCapabilities(cm.activeNetwork)
            val bars = when {
                nc == null -> 0
                nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 4
                nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> 3
                else -> 1
            }
            val carrier = if (nc?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true)
                "DCM" else if (bars == 4) "Wi-Fi" else ""
            trySend(bars to carrier)
        }

        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) = snapshot()
            override fun onLost(network: android.net.Network) = snapshot()
            override fun onCapabilitiesChanged(
                network: android.net.Network,
                caps: NetworkCapabilities,
            ) = snapshot()
        }
        cm.registerDefaultNetworkCallback(cb)
        snapshot()
        awaitClose { runCatching { cm.unregisterNetworkCallback(cb) } }
    }

    private fun bluetoothFlow(): Flow<Boolean> = flow {
        // Lightweight poll - BluetoothAdapter.ACTION_STATE_CHANGED requires
        // BLUETOOTH_CONNECT runtime grant which we may not have on first
        // boot. Polling at 30s is acceptable for a status icon.
        val bm = context.getSystemService(BluetoothManager::class.java)
        val adapter: BluetoothAdapter? = bm?.adapter
        while (true) {
            emit(adapter?.isEnabled == true)
            delay(30_000)
        }
    }.onStart { emit(false) }

    private fun locationFlow(): Flow<Boolean> = flow {
        val lm = context.getSystemService(LocationManager::class.java)
        while (true) {
            val on = try {
                lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                    lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
            } catch (_: Throwable) { false }
            emit(on)
            delay(30_000)
        }
    }.onStart { emit(false) }
}
