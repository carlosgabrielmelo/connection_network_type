import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'abstract/connection_network_type_platform_interface.dart';
import 'enum/network_status.enum.dart';

/// An implementation of [ConnectionNetworkTypePlatform] that uses method channels.
class MethodChannelConnectionNetworkType extends ConnectionNetworkTypePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('connection_network_type');

  /// The event channel used to interact with the native platform.
  @visibleForTesting
  final eventChannel = const EventChannel("connection_network_type_status");

  Stream<NetworkStatus>? _onNetworkStateChanged;

  @override
  Stream<NetworkStatus> get onNetworkStateChanged {
    _onNetworkStateChanged ??= eventChannel
        .receiveBroadcastStream()
        .map((event) => event.toString())
        .map(_convertFromState);
    return _onNetworkStateChanged!;
  }

  @override
  Future<NetworkStatus> currentNetworkStatus() async {
    final String state = await methodChannel.invokeMethod("networkStatus");
    return _convertFromState(state);
  }

  /// NetworkStatus identify tipe of connection
  NetworkStatus _convertFromState(String state) {
    switch (state) {
      case "unreach":
        return NetworkStatus.unreachable;
      case "mobile2G":
        return NetworkStatus.mobile2G;
      case "mobile3G":
        return NetworkStatus.mobile3G;
      case "wifi":
        return NetworkStatus.wifi;
      case "mobile4G":
        return NetworkStatus.mobile4G;
      case "mobile5G":
        return NetworkStatus.mobile5G;
      case "mobileOther":
        return NetworkStatus.otherMobile;
      default:
        return NetworkStatus.unreachable;
    }
  }
}
