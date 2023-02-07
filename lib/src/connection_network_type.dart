import 'abstract/connection_network_type_platform_interface.dart';
import 'enum/network_status.enum.dart';

class ConnectionNetworkType {
  Stream<NetworkStatus> get onNetworkStateChanged {
    return ConnectionNetworkTypePlatform.instance.onNetworkStateChanged;
  }

  Future<NetworkStatus> currentNetworkStatus() async {
    return ConnectionNetworkTypePlatform.instance.currentNetworkStatus();
  }
}
