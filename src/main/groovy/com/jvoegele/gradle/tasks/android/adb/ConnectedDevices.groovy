package com.jvoegele.gradle.tasks.android.adb

final class ConnectedDevices {
  static List<Device> get() {
    Adb.devices.collect { new Device(it.isEmulator(), it.serialNumber) }
  }
}
