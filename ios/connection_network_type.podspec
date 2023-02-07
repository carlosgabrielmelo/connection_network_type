#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint connection_network_type.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'connection_network_type'
  s.version          = '0.0.1'
  s.summary          = 'This plugin allows Flutter apps to detect network changes. You can know the detailed mobile network types, such as 2G, 3G, 4G, 5G. This plugin is suitable for iOS and Android, it can also detect if the internet connection status is real or not working and even if it is unstable.'
  s.description      = <<-DESC
This plugin allows Flutter apps to detect network changes. You can know the detailed mobile network types, such as 2G, 3G, 4G, 5G. This plugin is suitable for iOS and Android, it can also detect if the internet connection status is real or not working and even if it is unstable.
                       DESC
  s.homepage         = 'https://github.com/carlosgabrielmelo/connection_network_type'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.dependency 'ReachabilitySwift'
  s.platform = :ios, '9.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'
end
