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

// TODO rename to Interval
public class SolutionSet {

  private Integer lower;
  private Integer upper;
  private boolean isSet = false;

  public SolutionSet() {
    lower = Integer.MIN_VALUE;
    upper = Integer.MAX_VALUE;
  }

  // TODO void modifying methods and return a new immutable object?
  public void setLowerBound(Integer lBound) {
    if (lBound == 0) {
      lBound += 1; // TODO why???
      //in order to prevent division by zero
    }
    if (lBound > getLowerBound() && lBound < getUpperBound()) {
      lower = lBound;
    }
    if (!this.isSet) {
      this.isSet = true;
    }
  }

  // TODO void modifying methods and return a new immutable object?
  public void setUpperBound(Integer uBound) {
    if (uBound == 0) {
      uBound += 1; // TODO why???
    }
    if (uBound < getUpperBound() && uBound > getLowerBound()) {
      upper = uBound;
    }
    if (!this.isSet) {
      this.isSet = true;
    }
  }


  // TODO add methods for operators, like PLUS, MINUS, TIMES, DIV
  // to avoid those operations all over the Optimizer code

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
