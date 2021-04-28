cordova create test_gaode io.hankers.gaode TEST_GAODE
cd test_gaode
# downgrade gradle to 3.1.3
cordova platform add ios android@8.1.0
cordova plugin add cordova-plugin-device --searchpath ~/cordova/plugins
cordova plugin add cordova-plugin-ionic-webview --searchpath ~/cordova/plugins
cordova plugin add cordova-plugin-geolocation-gaode --searchpath ../../.. --variable ANDROIDKEY=deprecated --variable IOSKEY=deprecated --variable TENCENTMAPSDK=LMCBZ-OZRCX-XPB4D-7CMXH-AQZWJ-UQBIQ
cp -r ../index.html ./www/
cp ../config.xml .
cordova prepare
