#import "ConnectionNetworkTypePlugin.h"
#if __has_include(<connection_network_type/connection_network_type-Swift.h>)
#import <connection_network_type/connection_network_type-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "connection_network_type-Swift.h"
#endif

@implementation ConnectionNetworkTypePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftConnectionNetworkTypePlugin registerWithRegistrar:registrar];
}
@end
