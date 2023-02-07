import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import '../connection_network_type_method_channel.dart';
import '../enum/network_status.enum.dart';

abstract class ConnectionNetworkTypePlatform extends PlatformInterface {
  /// Constructs a ConnectionNetworkTypePlatform.
  ConnectionNetworkTypePlatform() : super(token: _token);

  static final Object _token = Object();

  static ConnectionNetworkTypePlatform _instance =
      MethodChannelConnectionNetworkType();

  /// The default instance of [ConnectionNetworkTypePlatform] to use.
  ///
  /// Defaults to [MethodChannelConnectionNetworkType].
  static ConnectionNetworkTypePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ConnectionNetworkTypePlatform] when
  /// they register themselves.
  static set instance(ConnectionNetworkTypePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Stream<NetworkStatus> get onNetworkStateChanged;

  /// currentNetworkStatus obtain the status network
  Future<NetworkStatus> currentNetworkStatus();
}
