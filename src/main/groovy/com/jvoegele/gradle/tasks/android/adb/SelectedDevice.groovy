package com.jvoegele.gradle.tasks.android.adb

final class SelectedDevice {
  static enum Type {
    SINGLE_DEVICE, SINGLE_USB, SINGLE_EMULATOR, BY_SERIAL_NUMBER
  }

  static def find(manualSetting = null) {
    def found

    // system properties first

    found = fromDeviceProp(System.getProperty("device"))
    if (found) return found

    found = fromAdbDeviceArgProp(System.getProperty("adb.device.arg")) // this is to be considered deprecated
    if (found) return found

    // manual setting second

    found = fromDeviceProp(manualSetting)
    if (found) return found

    // not set at all

    return new SelectedDevice(Type.SINGLE_DEVICE, null)
  }

  static def fromDeviceProp(deviceProp) {
    if (deviceProp && !deviceProp.trim().isEmpty()) {
      switch (deviceProp) {
        case "usb": return new SelectedDevice(Type.SINGLE_USB, null)
        case "emulator": return new SelectedDevice(Type.SINGLE_EMULATOR, null)
        default: return new SelectedDevice(Type.BY_SERIAL_NUMBER, deviceProp)
      }
    }

    return null
  }

  static def fromAdbDeviceArgProp(adbDeviceArgProp) {
    if (adbDeviceArgProp && !adbDeviceArgProp.trim().isEmpty()) {
      switch (adbDeviceArgProp) {
        case "-e": return new SelectedDevice(Type.SINGLE_EMULATOR, null)
        case "-d": return new SelectedDevice(Type.SINGLE_USB, null)
        case ~/^-s.*/: return new SelectedDevice(Type.BY_SERIAL_NUMBER, adbDeviceArgProp.replaceFirst(/^-s/, "").trim());
      }
    }

    return null
  }

  def type
  def serialNumber // only makes sense if type == BY_SERIAL_NUMBER

  def SelectedDevice(def type, def serialNumber) {
    this.type = type
    this.serialNumber = serialNumber
  }

  def getArgsForAdb() {
    switch (type) {
      case Type.SINGLE_DEVICE: return []
      case Type.SINGLE_EMULATOR: return ["-e"]
      case Type.SINGLE_USB: return ["-d"]
      case Type.BY_SERIAL_NUMBER: return ["-s", serialNumber]
    }
  }

  def assertConnected() {
    switch (type) {
      case Type.SINGLE_DEVICE: assertSingleDeviceConnected(); break
      case Type.SINGLE_EMULATOR: assertSingleEmulatorConnected(); break
      case Type.SINGLE_USB: assertSingleUsbConnected(); break
      case Type.BY_SERIAL_NUMBER: assertSerialNumberConnected(); break
    }
  }

  def assertSingleDeviceConnected() {
    def devicesCount = ConnectedDevice.getAll().size()

    if (devicesCount == 0) {
      throw new AdbErrorException("There is no connected device")
    } else if (devicesCount > 1) {
      throw new AdbErrorException("There are ${devicesCount} connected devices, you must specify which one to use")
    }
  }

  def assertSingleEmulatorConnected() {
    def emulatorsCount = ConnectedDevice.getAll().findAll { it.isEmulator }.size()

    if (emulatorsCount == 0) {
      throw new AdbErrorException("There is no connected emulator")
    } else if (emulatorsCount > 1) {
      throw new AdbErrorException("There are ${emulatorsCount} connected emulators, you must specify which one to use")
    }
  }

  def assertSingleUsbConnected() {
    def usbDevicesCount = ConnectedDevice.getAll().findAll { !it.isEmulator }.size()

    if (usbDevicesCount == 0) {
      throw new AdbErrorException("There is no connected USB device")
    } else if (usbDevicesCount > 1) {
      throw new AdbErrorException("There are ${usbDevicesCount} USB devices connected, you must specify which one to use")
    }
  }

  def assertSerialNumberConnected() {
    def serialNumberDevices = ConnectedDevice.getAll().findAll { it.serialNumber == serialNumber }.size()

    if (serialNumberDevices == 0) {
      throw new AdbErrorException("There is no connected device with serial number '${serialNumber}'")
    } else if (serialNumberDevices > 1) {
      throw new AdbErrorException("There are ${serialNumberDevices} connected devices with serial number '${serialNumber}', this should never happen")
    }
  }
}
