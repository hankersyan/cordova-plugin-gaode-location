# cordova-plugin-geolocation-gaode

基于cordova封装的高德地图定位插件(暂时只支持单次定位)

# Install

```bash
cordova plugin add cordova-plugin-geolocation-gaode --variable ANDROIDKEY=YOU_ANDROIDKEY --variable IOSKEY=YOU_IOSKEY
```

# Parameters

Android端和iOS端各自有各自的参数

## configLocation方法

### Android:

- locationMode(number)：定位的模式（精度逐级递减，具体对应的模式参考官网），默认： 1
  - 1：Hight_Accuracy
  - 2：Device_Sensors
  - 3：Battery_Saving

### iOS

- accuracy(number)：定位精度（精度逐级递减，具体对应的模式参考官网），默认：4
  - 1: kCLLocationAccuracyBestForNavigation
  - 2: kCLLocationAccuracyBest
  - 3: kCLLocationAccuracyNearestTenMeters
  - 4: kCLLocationAccuracyHundredMeters
  - 5: kCLLocationAccuracyKilometer
  - 6: kCLLocationAccuracyThreeKilometers
- locationTimeout：定位超时时间，默认：3
- reGeoCodeTimeout：逆地址超时时间，默认：5

  ## getLocation方法

  - retGeo: 是否返回逆地址，默认：false

# Success return data

- latitude：经度
- longitude：纬度
- country： 国家
- province：省
- city：市
- district：区
- address：具体地址

# Useage

```Javascript
var para = {
	appName: 'your app name',
	android: {
		// set some parameters
		locationMode:1
	},
	ios: {
		// set some parameters
		accuracy:1
	}
}
// 配置
GaodeLocation.configLocation(para, function (successMsg) {
	// 定位
	GaodeLocation.getLocation({ retGeo: true }, function (locationInfo) {
		// do something
		console.log(JSON.stringify(locationInfo));
	}, function (err) {
		console.log(err);
	});
});
```
