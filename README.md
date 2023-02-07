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

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter development, view the
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

