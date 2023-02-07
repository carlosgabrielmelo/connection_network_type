import 'dart:async';

import 'package:connection_network_type/connection_network_type.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _networkStatus = NetworkStatus.unreachable.name;
  final ConnectionNetworkType _connectionNetworkTypePlugin =
      ConnectionNetworkType();

  @override
  void initState() {
    super.initState();
    initPlatformState();
    initPlatformNetworkListen();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    // On Android this plugin needs the READ_PHONE_STATE permission
    // If this plugin is used on Android release remember to ask for READ_PHONE_STATE permission
    // We recommend you to use `permission_handler` package as shown below

    // if(Platform.isAndroid) {
    //   Permission.phone.request();
    // }

    late NetworkStatus networkStatus;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      networkStatus = await _connectionNetworkTypePlugin.currentNetworkStatus();
    } on PlatformException {
      networkStatus = NetworkStatus.unreachable;
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _networkStatus = networkStatus.name;
    });
  }

  void initPlatformNetworkListen() {
    _connectionNetworkTypePlugin.onNetworkStateChanged
        .listen((NetworkStatus networkStatus) {
      if (networkStatus.name != _networkStatus) {
        // If the widget was removed from the tree while the asynchronous platform
        // message was in flight, we want to discard the reply rather than calling
        // setState to update our non-existent appearance.
        if (!mounted) return;

        setState(() {
          _networkStatus = networkStatus.name;
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Running on: $_networkStatus\n'),
        ),
      ),
    );
  }
}
