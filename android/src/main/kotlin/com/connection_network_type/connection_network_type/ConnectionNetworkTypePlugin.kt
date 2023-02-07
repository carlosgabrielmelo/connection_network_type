package com.connection_network_type.connection_network_type

import androidx.annotation.NonNull
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** ConnectionNetworkTypePlugin */
class ConnectionNetworkTypePlugin: FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var eventChannel: EventChannel
  private lateinit var context: Context
  private lateinit var connectivityManager: ConnectivityManager
  private var broadcastReceiver: NetworkBroadcastReceiver? = null


  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "connection_network_type")
    channel.setMethodCallHandler(this)

    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger,"connection_network_type_status")
    eventChannel.setStreamHandler(this)

    // Record context
    context = flutterPluginBinding.applicationContext
    connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  }

  @RequiresApi(Build.VERSION_CODES.N)
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "networkStatus") {
      result.success(getNetworkState(connectivityManager,context))
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    // Sign up for notifications
    if (broadcastReceiver == null) {
      broadcastReceiver = NetworkBroadcastReceiver(events,connectivityManager,context)
    }
    val filter = IntentFilter()
    filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
    context.registerReceiver(broadcastReceiver,filter)


  }

  override fun onCancel(arguments: Any?) {
    if (broadcastReceiver != null) {
      context.unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
  }
}

private  class NetworkBroadcastReceiver(val events: EventChannel.EventSink?,val connectivityManager: ConnectivityManager,val context: Context): BroadcastReceiver() {
  @RequiresApi(Build.VERSION_CODES.N)
  override fun onReceive(p0: Context?, p1: Intent?) {
    events?.success(getNetworkState(connectivityManager,context));
  }
}

// Get network status
@RequiresApi(Build.VERSION_CODES.N)
private  fun getNetworkState(connectivityManager: ConnectivityManager, context: Context): String {
  if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    if (capabilities == null){
      return NetworkState.unReachable.toString()
    }
    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
      return NetworkState.wifi.toString()
    }

    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
      return getMobileNetworkType(context, connectivityManager)
    }
  }else{
    val networkInfo = connectivityManager.activeNetworkInfo
    if (networkInfo == null || !networkInfo.isConnected) {
      return NetworkState.unReachable.toString()
    }
    val type = networkInfo.type
    when(type){
      ConnectivityManager.TYPE_ETHERNET,ConnectivityManager.TYPE_WIFI,ConnectivityManager.TYPE_WIMAX -> {
        return NetworkState.wifi.toString()
      }
      ConnectivityManager.TYPE_MOBILE,ConnectivityManager.TYPE_MOBILE_DUN,ConnectivityManager.TYPE_MOBILE_HIPRI -> {
        return getMobileNetworkType(context, connectivityManager)
      }
      else -> return NetworkState.unReachable.toString()
    }
  }
  return NetworkState.unReachable.toString()
}

@RequiresApi(Build.VERSION_CODES.N)
private  fun getMobileNetworkType(context: Context, connectivityManager: ConnectivityManager): String {
  if (context == null) {
    return NetworkState.mobileOther.toString()
  }
  
  val networkInfo = connectivityManager.activeNetworkInfo
  if (networkInfo == null) {
    return NetworkState.mobileOther.toString()
  }
  val mobile2G_types = arrayOf(
          TelephonyManager.NETWORK_TYPE_1xRTT,
          TelephonyManager.NETWORK_TYPE_EDGE,
          TelephonyManager.NETWORK_TYPE_GPRS,
          TelephonyManager.NETWORK_TYPE_CDMA,
          TelephonyManager.NETWORK_TYPE_IDEN
  )
  if (networkInfo.subtype in mobile2G_types) {
    return NetworkState.mobile2G.toString()
  }
  val mobile3G_types = arrayOf(
          TelephonyManager.NETWORK_TYPE_UMTS,
          TelephonyManager.NETWORK_TYPE_EVDO_0,
          TelephonyManager.NETWORK_TYPE_EVDO_A,
          TelephonyManager.NETWORK_TYPE_HSDPA,
          TelephonyManager.NETWORK_TYPE_HSUPA,
          TelephonyManager.NETWORK_TYPE_HSPA,
          TelephonyManager.NETWORK_TYPE_EVDO_B,
          TelephonyManager.NETWORK_TYPE_EHRPD,
          TelephonyManager.NETWORK_TYPE_HSPAP
  )
  if (networkInfo.subtype in mobile3G_types) {
    return NetworkState.mobile3G.toString()
  }
  if (networkInfo.subtype == TelephonyManager.NETWORK_TYPE_LTE) {
    return NetworkState.mobile4G.toString()
  }
  if (networkInfo.subtype == TelephonyManager.NETWORK_TYPE_NR) {
    return NetworkState.mobile5G.toString()
  }
  return NetworkState.mobileOther.toString()
}

private enum class NetworkState {
  unReachable,
  mobile2G,
  mobile3G,
  wifi,
  mobile4G,
  mobile5G,
  mobileOther
}
