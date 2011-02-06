package com.jvoegele.gradle.tasks.android.adb

import org.gmock.WithGMock
import org.junit.Test
import static org.junit.Assert.assertEquals
import static com.jvoegele.gradle.tests.Tests.shouldFail

@WithGMock
class SelectedDeviceTest {
  @Test
  void parseDeviceProp() {
    def mock = mock(System)

    mock.static.getProperty("device").returns("emulator")
    mock.static.getProperty("device").returns("usb")
    mock.static.getProperty("device").returns("12345")

    play {
      assertEquals SelectedDevice.Type.SINGLE_EMULATOR, SelectedDevice.find().type

      assertEquals SelectedDevice.Type.SINGLE_USB, SelectedDevice.find().type

      def device = SelectedDevice.find()
      assertEquals SelectedDevice.Type.BY_SERIAL_NUMBER, device.type
      assertEquals "12345", device.serialNumber
    }
  }

  @Test
  void parseAdbDeviceArgProp() {
    def mock = mock(System)

    mock.static.getProperty("device").returns(null).stub()
    mock.static.getProperty("adb.device.arg").returns("-e")
    mock.static.getProperty("adb.device.arg").returns("-d")
    mock.static.getProperty("adb.device.arg").returns("-s 12345")

    play {
      assertEquals SelectedDevice.Type.SINGLE_EMULATOR, SelectedDevice.find().type

      assertEquals SelectedDevice.Type.SINGLE_USB, SelectedDevice.find().type

      def device = SelectedDevice.find()
      assertEquals SelectedDevice.Type.BY_SERIAL_NUMBER, device.type
      assertEquals "12345", device.serialNumber
    }
  }

  @Test
  void parseManualSetting() {
    def mock = mock(System)

    mock.static.getProperty("device").returns(null).stub()
    mock.static.getProperty("adb.device.arg").returns(null).stub()

    play {
      assertEquals SelectedDevice.Type.SINGLE_EMULATOR, SelectedDevice.find("emulator").type

      assertEquals SelectedDevice.Type.SINGLE_USB, SelectedDevice.find("usb").type

      def device = SelectedDevice.find("12345")
      assertEquals SelectedDevice.Type.BY_SERIAL_NUMBER, device.type
      assertEquals "12345", device.serialNumber
    }
  }

  @Test
  void parseFallbackToSingleDevice() {
    def mock = mock(System)

    mock.static.getProperty("device").returns(null).stub()
    mock.static.getProperty("adb.device.arg").returns(null)
    mock.static.getProperty("adb.device.arg").returns("asdf") // adb.device.arg can containg nonsense

    play {
      2.times {
        assertEquals SelectedDevice.Type.SINGLE_DEVICE, SelectedDevice.find().type
      }
    }
  }

  // ---

  @Test
  void assertSingleDeviceConnected() {
    def mockSystem = mock(System)
    def mockConnectedDevices = mock(ConnectedDevices)

    mockSystem.static.getProperty("device").returns(null).stub()
    mockSystem.static.getProperty("adb.device.arg").returns(null).stub()

    mockConnectedDevices.static.get().returns([new Device(true, "emulator-nnnn")])
    mockConnectedDevices.static.get().returns([new Device(false, "realdevice-nnnn")])
    mockConnectedDevices.static.get().returns([])
    mockConnectedDevices.static.get().returns([new Device(true, "emulator-nnnn"), new Device(false, "realdevice-nnnn")])

    play {
      2.times {
        SelectedDevice.find().assertConnected()
      }
      2.times {
        shouldFail(AdbErrorException) { SelectedDevice.find().assertConnected() }
      }
    }
  }

  @Test
  void assertSingleEmulatorConnected() {
    def mockSystem = mock(System)
    def mockConnectedDevices = mock(ConnectedDevices)

    mockSystem.static.getProperty("device").returns("emulator").stub()

    mockConnectedDevices.static.get().returns([new Device(true, "emulator-nnnn")])
    mockConnectedDevices.static.get().returns([new Device(true, "emulator-nnnn"), new Device(false, "realdevice-nnnn")])
    mockConnectedDevices.static.get().returns([])
    mockConnectedDevices.static.get().returns([new Device(true, "emulator-nnnn"), new Device(true, "emulator-qqqq")])
    mockConnectedDevices.static.get().returns([new Device(false, "realdevice-nnnn")])

    play {
      2.times {
        SelectedDevice.find().assertConnected()
      }
      3.times {
        shouldFail(AdbErrorException) { SelectedDevice.find().assertConnected() }
      }
    }
  }

  @Test
  void assertSingleUsbConnected() {
    def mockSystem = mock(System)
    def mockConnectedDevices = mock(ConnectedDevices)

    mockSystem.static.getProperty("device").returns("usb").stub()

    mockConnectedDevices.static.get().returns([new Device(false, "realdevice-nnnn")])
    mockConnectedDevices.static.get().returns([new Device(true, "emulator-nnnn"), new Device(false, "realdevice-nnnn")])
    mockConnectedDevices.static.get().returns([])
    mockConnectedDevices.static.get().returns([new Device(false, "realdevice-nnnn"), new Device(false, "realdevice-qqqq")])
    mockConnectedDevices.static.get().returns([new Device(true, "emulator-nnnn")])

    play {
      2.times {
        SelectedDevice.find().assertConnected()
      }
      3.times {
        shouldFail(AdbErrorException) { SelectedDevice.find().assertConnected() }
      }
    }
  }

  @Test
  void assertDeviceBySerialNumberConnected() {
    def mockSystem = mock(System)
    def mockConnectedDevices = mock(ConnectedDevices)

    mockSystem.static.getProperty("device").returns("12345").stub()

    mockConnectedDevices.static.get().returns([new Device(false, "12345")])
    mockConnectedDevices.static.get().returns([new Device(false, "12345"), new Device(true, "emulator-nnnn"), new Device(false, "realdevice-nnnn")])
    mockConnectedDevices.static.get().returns([])
    mockConnectedDevices.static.get().returns([new Device(false, "realdevice-nnnn"), new Device(false, "realdevice-qqqq")])
    mockConnectedDevices.static.get().returns([new Device(true, "emulator-nnnn")])

    play {
      2.times {
        SelectedDevice.find().assertConnected()
      }
      3.times {
        shouldFail(AdbErrorException) { SelectedDevice.find().assertConnected() }
      }
    }
  }
}
