package com.jvoegele.gradle.tasks.android.adb

import org.apache.tools.ant.util.TeeOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskAction
import org.gradle.util.GUtil

class AdbExec extends DefaultTask {
  def device
  def arguments = []
  def failureDetectionRegexp = /(?i)failure|error/

  def exec = new Exec()

  @TaskAction
  def exec() {
    def selectedDevice = SelectedDevice.find(device)
    selectedDevice.assertConnected()

    def stdout = new ByteArrayOutputStream() // output is small, we can safely read it into memory
    def stderr = new ByteArrayOutputStream() // adb prints error messages both on stdout and stderr

    exec.executable project.ant['adb']
    exec.args selectedDevice.argsForAdb
    exec.args this.arguments

    // both stdout and stderr should be logged AND inspected for error messages at the same time
    exec.standardOutput = new TeeOutputStream(System.out, stdout)
    exec.errorOutput = new TeeOutputStream(System.err, stderr)

    exec.ignoreExitValue = true

    project.logger.info("running '${exec.commandLine.join(" ")}'")

    exec.exec()

    checkForErrors stdout
    checkForErrors stderr

    // exit value was ignored to inspect stdout/stderr first and possibly throw exception with reasonable message
    // now it's sure that the exception wasn't thrown, but the exit value really should be checked
    exec.execResult.assertNormalExitValue()
  }

  def checkForErrors(ByteArrayOutputStream stream) {
    def reader = new InputStreamReader(new ByteArrayInputStream(stream.toByteArray()))
    reader.eachLine {
      if (it =~ failureDetectionRegexp) {
        throw new AdbErrorException("ADB said '${it.trim()}'")
      }
    }
  }

  def AdbExec setDevice(String device) {
    this.device = device
    return this
  }

  def String getDevice() {
    return this.device
  }

  def AdbExec args(Object... args) {
    if (args == null) {
      throw new IllegalArgumentException("args == null!")
    }
    this.arguments.addAll(Arrays.asList(args))
    return this
  }

  public AdbExec args(Iterable<?> args) {
    GUtil.addToCollection(this.arguments, args)
    return this
  }

  public AdbExec setArgs(Iterable<?> args) {
    this.arguments.clear()
    GUtil.addToCollection(this.arguments, args)
    return this
  }

  public List<String> getArgs() {
    return this.arguments.collect { it.toString() }
  }

  public AdbExec setFailureDetectionRegexp(String failureDetectionRegexp) {
    this.failureDetectionRegexp = failureDetectionRegexp
    return this
  }

  public String getFailureDetectionRegexp() {
    return this.failureDetectionRegexp
  }
}
