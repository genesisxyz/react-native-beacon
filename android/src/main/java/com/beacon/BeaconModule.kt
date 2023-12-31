package com.beacon

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener


class BeaconModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext), PermissionListener {

  private val beaconServiceConnection: ConnectService by lazy { ConnectService(reactContext, BeaconService::class.java) }

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun setOptions(options: ReadableMap) {
    beaconServiceConnection.setOptions(options)
  }

  @ReactMethod
  fun requestPermissions(promise: Promise) {
    val activity = currentActivity as PermissionAwareActivity?

    val permissions = mutableListOf<String>();

    val accessFineLocationSelfPermission = activity?.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    if (accessFineLocationSelfPermission != PackageManager.PERMISSION_GRANTED) {
      permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val bluetoothConnectSelfPermission = activity?.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)

      if (bluetoothConnectSelfPermission != PackageManager.PERMISSION_GRANTED) {
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
      }

      val bluetoothScanSelfPermission = activity?.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)

      if (bluetoothScanSelfPermission != PackageManager.PERMISSION_GRANTED) {
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
      }
    }

    if (permissions.isNotEmpty()) {
      activity?.requestPermissions(permissions.toTypedArray(), 1, this)
    }
    promise.resolve(true)
  }

  @ReactMethod
  fun enableBluetooth() {
    val bluetoothManager = reactApplicationContext.currentActivity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    if (!bluetoothAdapter.isEnabled) {
      val intentBtEnabled = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
      if (ActivityCompat.checkSelfPermission(reactApplicationContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
        currentActivity?.startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BLUETOOTH)
      }
    }
  }

  @ReactMethod
  fun startService(promise: Promise) {
    beaconServiceConnection.startService(promise)
  }

  @ReactMethod
  fun stopService(promise: Promise) {
    beaconServiceConnection.stopService(promise)
  }

  @ReactMethod
  fun startBeaconScan(beacons: ReadableArray) {
    beaconServiceConnection.mService?.run {
      this as BeaconService
      startBeaconScan(beacons)
    }
  }

  @ReactMethod
  fun stopBeaconScan() {
    beaconServiceConnection.mService?.run {
      this as BeaconService
      stopBeaconScan()
    }
  }

  companion object {
    const val NAME = "Beacon"

    const val TAG = "BeaconModule"

    const val REQUEST_ENABLE_BLUETOOTH = 1
  }

  // region PermissionListener

  override fun onRequestPermissionsResult(p0: Int, p1: Array<out String>?, p2: IntArray?): Boolean {
    return true
  }

  // endregion PermissionListener
}
