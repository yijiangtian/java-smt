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

package org.sosy_lab.java_smt.domain_optimization;

public class Interval {

  private Integer lower;
  private Integer upper;
  private boolean isSet = false;

  public Interval() {
    lower = Integer.MIN_VALUE;
    upper = Integer.MAX_VALUE;
  }

  public void setLowerBound(Integer lBound) {
    if (lBound == 0) {
      lBound += 1;
      //in order to prevent division by zero
    }
    if (lBound > getLowerBound() && lBound < getUpperBound()) {
      lower = lBound;
    }
    if (!this.isSet) {
      this.isSet = true;
    }
  }
  public void setUpperBound(Integer uBound) {
    if (uBound == 0) {
      uBound += 1;
    }
    if (uBound < getUpperBound() && uBound > getLowerBound()) {
      upper = uBound;
    }
    if (!this.isSet) {
      this.isSet = true;
    }
  }


  public Integer getLowerBound() {
    return lower;
  }

  public Integer getUpperBound() {
    return upper;
  }

  public boolean isSet() {
    return isSet;
  }

  public boolean isUpperBoundSet() {
    return upper != Integer.MAX_VALUE;
  }

  public boolean isLowerBoundSet() {
    return lower != Integer.MIN_VALUE;
  }

  @Override
  public String toString() {
    return String.format("lBound: %s ; uBound: %s", lower, upper);
  }
}
