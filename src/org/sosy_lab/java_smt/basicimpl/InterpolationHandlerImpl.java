/*
 *  JavaSMT is an API wrapper for a collection of SMT solvers.
 *  This file is part of JavaSMT.
 *
 *  Copyright (C) 2007-2016  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sosy_lab.java_smt.basicimpl;

import java.util.Objects;
import org.sosy_lab.java_smt.api.InterpolationHandle;

/**
 * Implementation for {@link org.sosy_lab.java_smt.api.InterpolationHandle}
 * where the handle is given by a native pointer.
 */
public class InterpolationHandlerImpl<E> implements InterpolationHandle {
  private final E handle;

  public InterpolationHandlerImpl(E pHandle) {
    handle = pHandle;
  }

  @Override
  public E getValue() {
    return handle;
  }

  @Override
  public boolean equals(Object pO) {
    if (this == pO) {
      return true;
    }
    if (pO == null || getClass() != pO.getClass()) {
      return false;
    }
    InterpolationHandlerImpl that = (InterpolationHandlerImpl) pO;
    return handle.equals(that.handle);
  }

  @Override
  public int hashCode() {
    return Objects.hash(handle);
  }
}
