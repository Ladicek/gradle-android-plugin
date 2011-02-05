package com.jvoegele.gradle.tasks.android.adb

final class Adb {
  static def instance

  static def init(project, sdkDir) {
    if (!instance) {
      instance = new Adb(project, sdkDir)
    }
  }

  static def getDevices() {
    if (!instance) {
      throw new IllegalStateException("Not initialized, call Adb.init first!")
    }

    return instance.bridge.devices
  }

  def project
  def sdkDir

  def bridge // initialized lazily

  def Adb(project, sdkDir) {
    this.project = project
    this.sdkDir = sdkDir
  }

  def getBridge() {
    if (!bridge) {
      createBridge()
    }

    bridge
  }

  def createBridge() {
    // I would love to write this without reflection, I really would
    // but that would make gradle-android-plugin dependent on ddmlib, which isn't redistributable
    String ddmlib = "${sdkDir}/tools/lib/ddmlib.jar"
    def classloader = new URLClassLoader([new URL("file://${ddmlib}")] as URL[])
    def clazz = Class.forName("com.android.ddmlib.AndroidDebugBridge", true, classloader)
    clazz.getMethod("init", Boolean.TYPE).invoke(null, false)
    bridge = clazz.getMethod("createBridge").invoke(null)
    Thread.sleep(500) // give it some time to connect
    if (!bridge.connected) {
      clazz.getMethod("disconnectBridge").invoke(null)
      project.logger.warn "ADB server wasn't running, starting one myself"
      bridge = clazz.getMethod("createBridge", String.class, Boolean.TYPE).invoke(null, project.ant['adb'], true)
    }
    Thread.sleep(500) // give it some time to connect
    if (!bridge.connected) {
      project.logger.warn "Couldn't connect to ADB server, errors may happen"
    }

    project.gradle.buildFinished {
      clazz.getMethod("disconnectBridge").invoke(null)
      clazz.getMethod("terminate").invoke(null)
    }
  }
}
