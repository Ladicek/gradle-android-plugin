/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jvoegele.gradle.tests

import org.codehaus.groovy.runtime.ScriptBytecodeAdapter
import org.junit.Assert

// this is copied from GroovyTestCase to help asserting exceptions without the need to inherit from specific class
// TODO couldn't GMock help us with this?
class Tests {
  /**
   * Asserts that the given code closure fails when it is evaluated
   *
   * @param code
   * @return the message of the thrown Throwable
   */
  public static String shouldFail(Closure code) {
    boolean failed = false;
    String result = null;
    try {
      code.call();
    }
    catch (GroovyRuntimeException gre) {
      failed = true;
      result = ScriptBytecodeAdapter.unwrap(gre).getMessage();
    }
    catch (Throwable e) {
      failed = true;
      result = e.getMessage();
    }
    Assert.assertTrue("Closure " + code + " should have failed", failed);
    return result;
  }

  /**
   * Asserts that the given code closure fails when it is evaluated
   * and that a particular exception is thrown.
   *
   * @param clazz the class of the expected exception
   * @param code the closure that should fail
   * @return the message of the expected Throwable
   */
  public static String shouldFail(Class clazz, Closure code) {
    Throwable th = null;
    try {
      code.call();
    } catch (GroovyRuntimeException gre) {
      th = ScriptBytecodeAdapter.unwrap(gre);
    } catch (Throwable e) {
      th = e;
    }

    if (th == null) {
      Assert.fail("Closure " + code + " should have failed with an exception of type " + clazz.getName());
    } else if (!clazz.isInstance(th)) {
      Assert.fail("Closure " + code + " should have failed with an exception of type " + clazz.getName() + ", instead got Exception " + th);
    }
    return th.getMessage();
  }
}
