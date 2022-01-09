package com.example.trackingapp.sensor.implementation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo.DetailedState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.util.Log
import android.view.View
import com.example.trackingapp.DatabaseManager.saveToDataBase
import com.example.trackingapp.models.ConnectionType
import com.example.trackingapp.models.Event
import com.example.trackingapp.models.EventName
import com.example.trackingapp.models.WifiConnectionState
import com.example.trackingapp.sensor.AbstractSensor
import com.example.trackingapp.util.CONST
import com.example.trackingapp.util.LoggingManager

class WifiSensor : AbstractSensor(
    "WIFISENSOR",
    "wifi"
) {

    private var mReceiver: BroadcastReceiver? = null
    private var m_context: Context? = null

    override fun getSettingsView(context: Context?): View? {
        return null
    }

    override fun isAvailable(context: Context?): Boolean {
        return true
    }

    override fun start(context: Context) {
        super.start(context)
        if (!m_isSensorAvailable) return
        m_context = context
        val timestamp = System.currentTimeMillis()

        val filter = IntentFilter()
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        mReceiver = WifiReceiver()
        try {
            context.unregisterReceiver(mReceiver)
        } catch (e: Exception) {
            //Not Registered
        }
        context.registerReceiver(mReceiver, filter)

        val netWorkType = getCurrentNetworkType(context)
        if(isWifiEnabled(context)){
            saveEntry(WifiConnectionState.ENABLED, netWorkType, timestamp)
        } else{
            saveEntry(WifiConnectionState.DISABLED, netWorkType, timestamp)
        }

        isRunning = true
    }

    override fun stop() {
        if (isRunning)
            isRunning = false
            m_context!!.unregisterReceiver(mReceiver)
    }
    private fun getCurrentNetworkType(context: Context?): ConnectionType {
        val cm: ConnectivityManager? =
            context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        val network = cm?.activeNetwork
        val capabilities = cm?.getNetworkCapabilities(network)

        return if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.CONNECTED_WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CONNECTED_MOBILE
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.CONNECTED_ETHERNET
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> ConnectionType.CONNECTED_VPN
                else -> ConnectionType.UNKOWN
            }
        } else ConnectionType.UNKOWN

    }

    private fun isWifiEnabled(context: Context): Boolean {
        val wifiManager =  context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

    fun getWifiName(context: Context): String? {
        //TODO
        val manager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (manager.isWifiEnabled) {
            val wifiInfo = manager.connectionInfo
            if (wifiInfo != null) {
                val state = WifiInfo.getDetailedStateOf(wifiInfo.supplicantState)
                if (state == DetailedState.CONNECTED || state == DetailedState.OBTAINING_IPADDR) {
                    return wifiInfo.ssid
                }
            }
        }
        return null
    }


    private fun saveEntry(connection: WifiConnectionState, wifiState: ConnectionType, timestamp: Long) {
        Event(EventName.WIFI, CONST.dateTimeFormat.format(timestamp), connection.name, wifiState.name).saveToDataBase()
    }

    private fun saveEntry(connection: WifiConnectionState, wifiState: ConnectionType, ssid: String, timestamp: Long) {
        Event(EventName.WIFI, CONST.dateTimeFormat.format(timestamp), connection.name,"${wifiState.name}: $ssid").saveToDataBase()
    }


    inner class WifiReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {
            if (!LoggingManager.isDataRecordingActive) {
                return
            }

            val timestamp = System.currentTimeMillis()

            if (intent.action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                //find out if Wifi was enabled or disabled

                when (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
                    WifiManager.WIFI_STATE_ENABLED ->{
                        val netWorkType = getCurrentNetworkType(context)
                        saveEntry(WifiConnectionState.ENABLED,  netWorkType, timestamp)
                    }

                    WifiManager.WIFI_STATE_DISABLED -> {
                        val netWorkType = getCurrentNetworkType(context)
                        saveEntry(WifiConnectionState.DISABLED,  netWorkType, timestamp)
                    }

                    else ->                     //ignore cases ENABLING, DISABLING, UNKNOWN
                        Log.i(TAG, "Wifi is enabling, disabling or unknown - ignoring state change")
                }
            }
        }
    }
}