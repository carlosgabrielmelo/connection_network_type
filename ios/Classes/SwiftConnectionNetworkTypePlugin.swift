import Flutter
import UIKit
import Reachability
import CoreTelephony

public class SwiftConnectionNetworkTypePlugin: NSObject, FlutterPlugin {
  
  private var reachability: Reachability?
  private var eventSink: FlutterEventSink?
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "connection_network_type", binaryMessenger: registrar.messenger())
    let instance = SwiftConnectionNetworkTypePlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)

    let streamChannel = FlutterEventChannel(name: "connection_network_type_status", binaryMessenger: registrar.messenger())
    streamChannel.setStreamHandler(instance)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    if call.method == "networkStatus" {
      result(statusForNetWork())
    }else{
      result(FlutterMethodNotImplemented)
    }
  }
}

extension SwiftConnectionNetworkTypePlugin: FlutterStreamHandler {
  public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
    // Add notification
    NotificationCenter.default.addObserver(self, selector:#selector(networkStatusChange(_:)) , name: .reachabilityChanged, object: nil)
    do {
      reachability = try Reachability()
      try reachability?.startNotifier()
    }catch(let erro as NSError){
      return FlutterError(code: "\(erro.code)", message: erro.domain, details: "Failed to initialize network monitoring");
    }
    eventSink = events
    return nil
  }

  public func onCancel(withArguments arguments: Any?) -> FlutterError? {
    if reachability != nil {
      do {
        try reachability?.startNotifier()
        reachability = nil
      }catch(let erro as NSError){
        return FlutterError(code: "\(erro.code)", message: erro.domain, details: "Failed to stop network monitoring");
      }
    }
    NotificationCenter.default.removeObserver(self)
    return nil
  }


  @objc func networkStatusChange(_ notice: Notification) {
    eventSink?(statusForNetWork())
  }

  private func statusForNetWork() -> String {
    // 0:unreachable 1:2G 2:3G 3:Wi-Fi 4:4G 5:5G 6:othermobie
    if reachability == nil {
      reachability = try! Reachability()
    }
    guard let netReach = reachability else { return "0" }
    switch netReach.connection {
    case .wifi:
      return NetworkStatus.wifi.value
    case .unavailable:
      return NetworkStatus.unreach.value
    case .cellular:
        if Float(String(UIDevice.current.systemVersion.split(separator: ".").first!)) ?? 0 >= 7.0 {
          let teleInfo = CTTelephonyNetworkInfo()
          guard let access = teleInfo.currentRadioAccessTechnology else {
            return  NetworkStatus.other.value
          }
          if [CTRadioAccessTechnologyEdge,
            CTRadioAccessTechnologyGPRS,
            CTRadioAccessTechnologyCDMA1x].contains(access) {
            return NetworkStatus.mobile2G.value
          }

          if [CTRadioAccessTechnologyHSDPA,
            CTRadioAccessTechnologyWCDMA,
            CTRadioAccessTechnologyHSUPA,
            CTRadioAccessTechnologyCDMAEVDORev0,
            CTRadioAccessTechnologyCDMAEVDORevA,
            CTRadioAccessTechnologyCDMAEVDORevB,
            CTRadioAccessTechnologyeHRPD].contains(access) {
            return NetworkStatus.mobile3G.value
          }

          if [CTRadioAccessTechnologyLTE].contains(access) {
            return NetworkStatus.mobile4G.value
          }
          if #available(iOS 14.1, *), [CTRadioAccessTechnologyNRNSA,
                                      CTRadioAccessTechnologyNR].contains(access) {
            return NetworkStatus.mobile5G.value
        }
          return NetworkStatus.other.value

        }else{
          return NetworkStatus.other.value
        }
    default:
      return NetworkStatus.unreach.value
    }
  }

  enum NetworkStatus: String {
    case unreach = "unreach"
    case mobile2G = "mobile2G"
    case mobile3G = "mobile3G"
    case wifi = "wifi"
    case mobile4G = "mobile4G"
    case mobile5G = "mobile5G"
    case other = "mobileOther"
    var value: String{
      return "\(self.rawValue)"
    }
  }
}
