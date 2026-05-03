package com.connection_network_type.connection_network_type

import androidx.annotation.NonNull
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
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
  /**
   * The MethodChannel that will the communication between Flutter and native Android
   *
   * This local reference serves to register the plugin with the Flutter Engine and unregister it
   * when the Flutter Engine is detached from the Activity
   */
  private lateinit var channel : MethodChannel
  private lateinit var eventChannel: EventChannel
  private lateinit var context: Context
  private lateinit var connectivityManager: ConnectivityManager
  private var broadcastReceiver: NetworkBroadcastReceiver? = null

  private var telephonyManager: TelephonyManager? = null
  // Listener used on API 30 only (PhoneStateListener was deprecated in API 31).
  private var displayInfoListener: PhoneStateListener? = null
  // Callback used on API 31+. Held as Any? so the TelephonyCallback class is not
  // referenced from a field on devices below API 31.
  private var displayInfoCallback: Any? = null
  // Last overrideNetworkType reported by TelephonyDisplayInfo. Stays null on API < 30
  // or when READ_PHONE_STATE has not been granted at runtime.
  @Volatile private var cachedOverrideNetworkType: Int? = null


  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "connection_network_type")
    channel.setMethodCallHandler(this)

    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger,"connection_network_type_status")
    eventChannel.setStreamHandler(this)

    // Record context
    context = flutterPluginBinding.applicationContext
    connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    ensureDisplayInfoListenerRegistered()
  }

  @RequiresApi(Build.VERSION_CODES.N)
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "networkStatus") {
      ensureDisplayInfoListenerRegistered()
      result.success(getNetworkState(connectivityManager, context, cachedOverrideNetworkType))
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    unregisterDisplayInfoListener()
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    // Sign up for notifications
    if (broadcastReceiver == null) {
      broadcastReceiver = NetworkBroadcastReceiver(events, connectivityManager, context) {
        ensureDisplayInfoListenerRegistered()
        cachedOverrideNetworkType
      }
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

  // Idempotent: re-checked on every query so the listener is registered as soon as
  // READ_PHONE_STATE is granted at runtime, not only at engine attach time.
  private fun ensureDisplayInfoListenerRegistered() {
    if (displayInfoListener != null || displayInfoCallback != null) return
    registerDisplayInfoListenerIfSupported()
  }

  // 5G NSA detection requires TelephonyDisplayInfo, which only exists on API 30+ (Android 11).
  // On older devices the listener is never registered and LTE keeps mapping to mobile4G.
  // API 31+ uses the non-deprecated TelephonyCallback; API 30 uses PhoneStateListener.
  private fun registerDisplayInfoListenerIfSupported() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
        != PackageManager.PERMISSION_GRANTED) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      registerDisplayInfoCallbackS()
    } else {
      registerDisplayInfoListenerR()
    }
  }

  @RequiresApi(Build.VERSION_CODES.R)
  private fun registerDisplayInfoListenerR() {
    val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager ?: return
    val listener = object : PhoneStateListener() {
      override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
        cachedOverrideNetworkType = telephonyDisplayInfo.overrideNetworkType
      }
    }
    try {
      tm.listen(listener, PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED)
      telephonyManager = tm
      displayInfoListener = listener
    } catch (e: SecurityException) {
      Log.w("ConnectionNetworkType", "Could not register display info listener: ${e.message}")
    }
  }

  @RequiresApi(Build.VERSION_CODES.S)
  private fun registerDisplayInfoCallbackS() {
    val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager ?: return
    val callback = object : TelephonyCallback(), TelephonyCallback.DisplayInfoListener {
      override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
        cachedOverrideNetworkType = telephonyDisplayInfo.overrideNetworkType
      }
    }
    try {
      tm.registerTelephonyCallback(context.mainExecutor, callback)
      telephonyManager = tm
      displayInfoCallback = callback
    } catch (e: SecurityException) {
      Log.w("ConnectionNetworkType", "Could not register telephony display info callback: ${e.message}")
    }
  }

  private fun unregisterDisplayInfoListener() {
    val tm = telephonyManager
    if (tm != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        unregisterDisplayInfoCallbackS(tm)
      } else if (displayInfoListener != null) {
        tm.listen(displayInfoListener, PhoneStateListener.LISTEN_NONE)
      }
    }
    displayInfoListener = null
    displayInfoCallback = null
    telephonyManager = null
    cachedOverrideNetworkType = null
  }

  @RequiresApi(Build.VERSION_CODES.S)
  private fun unregisterDisplayInfoCallbackS(tm: TelephonyManager) {
    (displayInfoCallback as? TelephonyCallback)?.let {
      tm.unregisterTelephonyCallback(it)
    }
  }
}

private class NetworkBroadcastReceiver(
  val events: EventChannel.EventSink?,
  val connectivityManager: ConnectivityManager,
  val context: Context,
  val overrideNetworkTypeProvider: () -> Int?
) : BroadcastReceiver() {
  @RequiresApi(Build.VERSION_CODES.N)
  override fun onReceive(p0: Context?, p1: Intent?) {
    events?.success(getNetworkState(connectivityManager, context, overrideNetworkTypeProvider()))
  }
}

// Get network status
@RequiresApi(Build.VERSION_CODES.N)
private fun getNetworkState(
  connectivityManager: ConnectivityManager,
  context: Context,
  overrideNetworkType: Int?
): String {
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
      return getMobileNetworkType(context, connectivityManager, overrideNetworkType)
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
        return getMobileNetworkType(context, connectivityManager, overrideNetworkType)
      }
      else -> return NetworkState.unReachable.toString()
    }
  }
  return NetworkState.unReachable.toString()
}

@RequiresApi(Build.VERSION_CODES.N)
private fun getMobileNetworkType(
  context: Context,
  connectivityManager: ConnectivityManager,
  overrideNetworkType: Int?
): String {
  if (context == null) {
    return NetworkState.mobileOther.toString()
  }

  if (isStrict5GSA(context)) {
    return NetworkState.mobile5G.toString()
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
          TelephonyManager.NETWORK_TYPE_TD_SCDMA,
          TelephonyManager.NETWORK_TYPE_HSPAP
  )
  if (networkInfo.subtype in mobile3G_types) {
    return NetworkState.mobile3G.toString()
  }
  if (networkInfo.subtype == TelephonyManager.NETWORK_TYPE_LTE) {
    if (isNrOverride(overrideNetworkType)) {
      return NetworkState.mobile5G.toString()
    }
    return NetworkState.mobile4G.toString()
  }
  if (networkInfo.subtype == TelephonyManager.NETWORK_TYPE_NR) {
    return NetworkState.mobile5G.toString()
  }
  return NetworkState.mobileOther.toString()
}

// Constants from android.telephony.TelephonyDisplayInfo, inlined so isNrOverride
// stays callable on API < 30 without referencing the class.
private const val OVERRIDE_NETWORK_TYPE_NR_NSA = 3        // API 30
private const val OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE = 4 // API 30, deprecated 31
private const val OVERRIDE_NETWORK_TYPE_NR_ADVANCED = 5   // API 31

// True when the LTE radio is anchoring a 5G NSA carrier (or NR Advanced).
private fun isNrOverride(overrideNetworkType: Int?): Boolean {
  if (overrideNetworkType == null) return false
  return overrideNetworkType == OVERRIDE_NETWORK_TYPE_NR_NSA ||
    overrideNetworkType == OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE ||
    overrideNetworkType == OVERRIDE_NETWORK_TYPE_NR_ADVANCED
}

// Strict 5G SA detection via TelephonyManager.getDataNetworkType(). Preferred over
// NetworkInfo.subtype on API 29+, where subtype is deprecated and may be capped to
// UNKNOWN without READ_PHONE_STATE. Returns false on older APIs and falls back silently
// when the permission has not been granted.
private fun isStrict5GSA(context: Context): Boolean {
  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
  val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager ?: return false
  return try {
    tm.dataNetworkType == TelephonyManager.NETWORK_TYPE_NR
  } catch (e: SecurityException) {
    Log.d("ConnectionNetworkType", "READ_PHONE_STATE not granted; cannot read dataNetworkType")
    false
  }
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
