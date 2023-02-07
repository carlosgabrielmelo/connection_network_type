import 'package:connection_network_type/connection_network_type.dart';
import 'package:connection_network_type/src/abstract/connection_network_type_platform_interface.dart';
import 'package:connection_network_type/src/connection_network_type_method_channel.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockConnectionNetworkTypePlatform
    with MockPlatformInterfaceMixin
    implements ConnectionNetworkTypePlatform {
  @override
  Future<NetworkStatus> currentNetworkStatus() =>
      Future.value(NetworkStatus.wifi);

  @override
  Stream<NetworkStatus> get onNetworkStateChanged =>
      Stream.value(NetworkStatus.mobile4G);
}

void main() {
  final ConnectionNetworkTypePlatform initialPlatform =
      ConnectionNetworkTypePlatform.instance;

  test('$MethodChannelConnectionNetworkType is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelConnectionNetworkType>());
  });

  test('testing if currentNetworkStatus returns NetworkStatus as expected',
      () async {
    ConnectionNetworkType connectionNetworkTypePlugin = ConnectionNetworkType();
    MockConnectionNetworkTypePlatform fakePlatform =
        MockConnectionNetworkTypePlatform();
    ConnectionNetworkTypePlatform.instance = fakePlatform;

    expect(await connectionNetworkTypePlugin.currentNetworkStatus(),
        NetworkStatus.wifi);
  });
}
