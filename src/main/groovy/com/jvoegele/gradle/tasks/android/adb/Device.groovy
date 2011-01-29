package com.jvoegele.gradle.tasks.android.adb

class Device {
  static enum Type {
    SINGLE_DEVICE, SINGLE_USB, SINGLE_EMULATOR, BY_SERIAL_NUMBER
  }

  def type
  def serialNumber // only makes sense if type == BY_SERIAL_NUMBER

  static def find() {
    def deviceProp = System.getProperty("device")
    if (deviceProp && !deviceProp.trim().isEmpty()) {
      switch (deviceProp) {
        case "usb": return new Device(Type.SINGLE_USB, null)
        case "emulator": return new Device(Type.SINGLE_EMULATOR, null)
        default: return new Device(Type.BY_SERIAL_NUMBER, deviceProp)
      }
    }

    // adb.device.arg is to be considered deprecated
    def adbDeviceArgProp = System.getProperty("adb.device.arg")
    if (adbDeviceArgProp && !adbDeviceArgProp.trim().isEmpty()) {
      switch (adbDeviceArgProp) {
        case "-e": return new Device(Type.SINGLE_EMULATOR, null)
        case "-d": return new Device(Type.SINGLE_USB, null)
        case ~/^-s.*/: return new Device(Type.BY_SERIAL_NUMBER, adbDeviceArgProp.replaceFirst(/^-s/, "").trim());
      }
    }

    return new Device(Type.SINGLE_DEVICE, null)
  }

  def Device(def type, def serialNumber) {
    this.type = type
    this.serialNumber = serialNumber
  }

  def getArgsForAdb() {
    switch (type) {
      case Type.SINGLE_DEVICE: return [];
      case Type.SINGLE_EMULATOR: return ["-e"];
      case Type.SINGLE_USB: return ["-d"];
      case Type.BY_SERIAL_NUMBER: return ["-s", serialNumber];
    }
  }

  def assertConnected() {
    switch (type) {
      case Type.SINGLE_DEVICE: assertSingleDeviceConnected(); break;
      case Type.SINGLE_EMULATOR: assertSingleEmulatorConnected(); break;
      case Type.SINGLE_USB: assertSingleUsbConnected(); break;
      case Type.BY_SERIAL_NUMBER: assertSerialNumberConnected(); break;
    }
  }

  def assertSingleDeviceConnected() {
    def devicesCount = Adb.devices.length

    if (devicesCount == 0) {
      throw new AdbErrorException("There is no connected device")
    } else if (devicesCount > 1) {
      throw new AdbErrorException("There are ${devicesCount} connected devices, you must specify which one to use")
    }
  }

  def assertSingleEmulatorConnected() {
    def emulatorsCount = Adb.devices.findAll { it.isEmulator() }.size()

    if (emulatorsCount == 0) {
      throw new AdbErrorException("There is no connected emulator")
    } else if (emulatorsCount > 1) {
      throw new AdbErrorException("There are ${emulatorsCount} connected emulators, you must specify which one to use")
    }
  }

  def assertSingleUsbConnected() {
    def usbDevicesCount = Adb.devices.findAll { !it.isEmulator() }.size()

    if (usbDevicesCount == 0) {
      throw new AdbErrorException("There is no connected USB device")
    } else if (usbDevicesCount > 1) {
      throw new AdbErrorException("There are ${usbDevicesCount} USB devices connected, you must specify which one to use")
    }
  }

  def assertSerialNumberConnected() {
    def serialNumberDevices = Adb.devices.findAll { it.serialNumber == serialNumber }.size()

    if (serialNumberDevices == 0) {
      throw new AdbErrorException("There is no connected device with serial number '${serialNumber}'")
    } else if (serialNumberDevices > 1) {
      throw new AdbErrorException("There are ${serialNumberDevices} connected devices with serial number '${serialNumber}', this should never happen")
    }
  }
}
