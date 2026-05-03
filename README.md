# connection_network_type

This plugin allows Flutter apps to detect network changes. You can know the detailed mobile network types, such as 2G, 3G, 4G, 5G. This plugin is suitable for iOS and Android, it can also detect if the internet connection status is real or not working and even if it is unstable.

This library is based on and inspired by the library "[network_type_reachability](https://pub.dev/packages/network_type_reachability)", which in turn is based on and inspired by the library "[flutter_reachability](https://pub.dev/packages/flutter_reachability)"

The main difference is that the code has been refactored to remove the need to manage permissions and typing issues have been fixed. In this way, this library contains fewer functions, but maintains greater compatibility of use in different versions.

## Examples

1. To detect the current Network Type:

```dart
    // If this plugin is used on Android, request the READ_PHONE_STATE permission.
    if(Platform.isAndroid) {
        await Permission.phone.request();
    }

    NetworkStatus networkStatus = await ConnectionNetworkType().currentNetworkStatus();
    
    switch(networkStatus) {
      case NetworkStatus.unreachable:
        // unreachable
      case NetworkStatus.wifi:
        // wifi
      case NetworkStatus.mobile2G:
        // 2G
      case NetworkStatus.mobile3G:
        // 3G
      case NetworkStatus.mobile4G:
        // 4G
      case NetworkStatus.mobile5G:
        // 5G
      case NetworkStatus.otherMoblie:
        // other connection
    }
```

2. To listen changes on Network Type:

```dart
    ConnectionNetworkType().onNetworkStateChanged
        .listen((NetworkStatus networkStatus) {
        // Trigger one function or manage state from here
    });
```

## 5G NSA Detection on Android

On Android, `NetworkStatus.mobile5G` covers both 5G Standalone (SA) and 5G Non-Standalone (NSA). 5G SA is detected via `TelephonyManager.getDataNetworkType()` on Android 10+ (API 29), with a fallback to `NetworkInfo.subtype` on older devices. 5G NSA reuses the LTE infrastructure as an anchor, so the radio subtype keeps reporting `NETWORK_TYPE_LTE` even when a 5G NR bearer is active; to distinguish it from real LTE, the plugin observes `TelephonyDisplayInfo.overrideNetworkType` through `TelephonyCallback.DisplayInfoListener` on Android 12+ (API 31) and `PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED` on Android 11 (API 30).

### Limitations

The Android APIs used for NSA detection impose three constraints. The first two are platform-imposed and cannot be removed by the plugin. The third could be removed only by changing the plugin's public contract.

1. Android 11 (API 30) or higher. `TelephonyDisplayInfo` was introduced in Android 11. On older devices, an LTE radio carrying a 5G NSA bearer is reported as `mobile4G`, just as it was before this feature was added. There is no public API on earlier Android versions that exposes the override network type.

2. `READ_PHONE_STATE` must be granted at runtime. The listener used to read `TelephonyDisplayInfo` requires the runtime `READ_PHONE_STATE` permission. If the permission is denied, the plugin falls back to LTE → `mobile4G`. The permission is re-checked on every query, so granting it after plugin initialization is fully supported.

3. First query immediately after permission grant. `TelephonyDisplayInfo` has no synchronous getter — it is delivered asynchronously through the telephony display info callback. There is a short window (typically a few milliseconds) between registering the callback and receiving the first event, during which the override network type is not yet known. A query made inside this window on a 5G NSA connection returns `mobile4G`. Subsequent queries, and any event delivered through `onNetworkStateChanged`, return the correct value.

### Why limitation 3 is not fixed

Removing it would require either suspending `currentNetworkStatus()` until the first callback arrives — turning a fast lookup into an unbounded async wait — or introducing a new initialization step that consumers would have to call explicitly. Both options break the existing Dart API contract and were intentionally avoided to preserve backward compatibility for current users of the plugin.

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter development, view the
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

