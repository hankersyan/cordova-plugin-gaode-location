package daihere.cordova.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.Settings;
import android.telecom.Call;
import android.util.Log;
import android.widget.Toast;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class GaodeLocation extends CordovaPlugin {

    public static String TAG = "GaodeLocation";
    public  Context context = null;

    private TencentLocationManager mLocationManager;

    // JS回掉接口对象
    public static CallbackContext cb = null;
    // 权限申请码
    private static final int PERMISSION_REQUEST_CODE = 500;
    // 需要进行检测的权限数组
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void pluginInitialize() {
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getLocation")) {
            getLocation(args, callbackContext);
            return true;
        } else if (action.equals("configLocationManager")) {
            if (this.isNeedCheckPermissions(needPermissions)) {
                this.checkPermissions(needPermissions);
            }
            configLocationClient(args, callbackContext);
            return true;
        }

        return false;
    }

    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;

    /**
     * 初始化locationClient
     * @param args
     * @param callbackContext
     */
    public void configLocationClient(final  CordovaArgs args, final CallbackContext callbackContext) {

        // 获取初始化定位参数
        JSONObject params;
        String appName;
        JSONObject androidPara;
        try {
            params = args.getJSONObject(0);
            appName = params.has("appName") ? params.getString("appName") : "当前应用";
            androidPara = params.has("android") ? params.getJSONObject("android") : new JSONObject();
        } catch (JSONException e) {
            callbackContext.error("参数格式错误");
            return;
        }

        mLocationManager = TencentLocationManager.getInstance(this.cordova.getActivity().getApplicationContext());
        // 设置坐标系为 gcj-02, 缺省坐标为 gcj-02, 所以通常不必进行如下调用
        mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);

        TencentLocationRequest request = TencentLocationRequest.create()
                .setInterval(5*1000) // 设置定位周期
                .setAllowGPS(true)  //当为false时，设置不启动GPS。默认启动
                .setQQ("10001")
                .setRequestLevel(3); // 设置定位level
        //也可以使用子线程，但是必须包含Looper
        int re = mLocationManager.requestSingleFreshLocation(request, new TencentLocationListener(){
            @Override
            public void onLocationChanged(TencentLocation location, int error,
                                          String reason) {
                Log.i(TAG, "lat: " + location.getLatitude() + ",long: " + location.getLongitude() + ",address: " + location.getAddress());
            }

            @Override
            public void onStatusUpdate(String name, int status, String desc) {
                Log.i(TAG, "name: " + name + "status: " + status + "desc: " + desc);
            }
        } , this.cordova.getActivity().getMainLooper());
        Log.i(TAG, "re: " + re);

        if (Build.VERSION.SDK_INT >= 23) {
            requirePermission();
        }

        if (!judgeLocationServerState()) {
            //没有打开位置服务开关，这里设计交互逻辑引导用户打开位置服务开关
        }

        callbackContext.success("初始化成功");
    }

    /**
     * 获取定位
     */
    public void getLocation(final CordovaArgs args, final CallbackContext callbackContext) {
//        Boolean retGeo;
//        JSONObject params;
//        cb = callbackContext;
//
//        try {
//            params = args.getJSONObject(0);
//            retGeo = params.has("retGeo") ? params.getBoolean("retGeo") : false;
//
//            locationOption.setNeedAddress(retGeo);
//            locationClient.setLocationOption(locationOption);
//            locationClient.startLocation();
//        } catch (JSONException e) {
//            callbackContext.error("参数格式错误");
//            return;
//        }
    }

    /**
     *  启动应用的设置
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        intent.setData(Uri.parse("package:" + getPackageName()));
//        startActivity(intent);
    }

    /**
     * 检查权限
     */
    private void checkPermissions(String... permissions) {
        try {
            List<String> needRequestPermissionList = findNeedPermissions(permissions);
            if (null != needRequestPermissionList && needRequestPermissionList.size() > 0) {
                String[] array = needRequestPermissionList.toArray(new String[needRequestPermissionList.size()]);
                cordova.requestPermissions(this, PERMISSION_REQUEST_CODE, array);
            }
        } catch (Throwable e) {

        }
    }

    /**
     * 判断是否需要权限校验
     */
    private boolean isNeedCheckPermissions(String... permission) {
        List<String> needRequestPermissionList = findNeedPermissions(permission);
        if (null != needRequestPermissionList && needRequestPermissionList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取需要获取权限的集合
     */
    private  List<String> findNeedPermissions(String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<String>();
        try {
            for (String perm : permissions) {
                if (!cordova.hasPermission(perm)) {
                    needRequestPermissionList.add(perm);
                }
            }
        } catch (Throwable e) {

        }
        return needRequestPermissionList;
    }

    /**
     * 权限检测回调
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
                isNeedCheck = false;
            }
        }
    }

    /**
     * 显示提示信息
     */
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setMessage("当前应用缺少必要权限。\\n\\n请点击\\\"设置\\\"-\\\"权限\\\"-打开所需权限。");

        // 拒绝, 退出应用
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
                    }
                });

        builder.setPositiveButton("设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }

    /**
     * 检测是否所有的权限都已经授权
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @AfterPermissionGranted(1)
    private void requirePermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        String[] permissionsForQ = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                //Manifest.permission.ACCESS_BACKGROUND_LOCATION, //target为Q时，动态请求后台定位权限
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (Build.VERSION.SDK_INT >= 29 ? EasyPermissions.hasPermissions(this.cordova.getContext(), permissionsForQ) :
                EasyPermissions.hasPermissions(this.cordova.getContext(), permissions)) {
            Toast.makeText(this.cordova.getContext(), "权限OK", Toast.LENGTH_LONG).show();
        } else {
            EasyPermissions.requestPermissions(this.cordova.getActivity(), "需要权限",
                    1, Build.VERSION.SDK_INT >= 29 ? permissionsForQ : permissions);
        }
    }

    private boolean judgeLocationServerState() {
        try {
            return Settings.Secure.getInt(this.cordova.getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE) > 1;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

}
