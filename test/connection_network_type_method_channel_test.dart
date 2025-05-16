import 'package:connection_network_type/src/connection_network_type_method_channel.dart';
import 'package:connection_network_type/src/enum/network_status.enum.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  MethodChannelConnectionNetworkType platform =
      MethodChannelConnectionNetworkType();
  const MethodChannel channel = MethodChannel('connection_network_type');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
      return NetworkStatus.wifi.name;
    });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
      return null;
    });
  });

  test(
      'testing if MethodChannelConnectionNetworkType currentNetworkStatus returns NetworkStatus as expected',
      () async {
    expect(await platform.currentNetworkStatus(), NetworkStatus.wifi);
  });
}
