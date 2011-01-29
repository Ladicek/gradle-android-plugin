package com.jvoegele.gradle.tasks.android.adb

import org.gradle.api.GradleException

class AdbErrorException extends GradleException {
  def AdbErrorException(String message) {
    super(message)
  }
}
