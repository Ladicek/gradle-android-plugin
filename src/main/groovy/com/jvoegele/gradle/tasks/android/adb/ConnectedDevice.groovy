package com.jvoegele.gradle.tasks.android.adb

@Immutable
final class ConnectedDevice {
  boolean isEmulator
  String serialNumber

  static List<ConnectedDevice> getAll() {
    Adb.devices.collect { new ConnectedDevice(it.isEmulator(), it.serialNumber) }
  }
}
