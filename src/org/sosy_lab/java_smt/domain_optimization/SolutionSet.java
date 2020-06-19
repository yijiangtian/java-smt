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


public class SolutionSet {

  private Integer[] bounds = new Integer[2];
  public boolean isSet = false;

  public SolutionSet() {
    this.bounds[0] = Integer.MIN_VALUE;
    this.bounds[1] = Integer.MAX_VALUE;
  }

  public void setLowerBound(Integer lBound) {
    if (lBound == 0) {
      lBound += 1;
    }
    if (lBound > this.getLowerBound() && lBound <= this.getUpperBound()) this.bounds[0] = lBound;
    if (!this.isSet) {
      this.isSet = true;
    }
  }

  public void setUpperBound(Integer uBound) {
    if (uBound == 0) {
      uBound += 1;
    }
    if (uBound < this.getUpperBound() && uBound >= this.getLowerBound()) this.bounds[1] = uBound;
    if (!this.isSet) {
      this.isSet = true;
    }
  }

  public Integer[] getBounds() {
    return this.bounds;
  }

  public Integer getLowerBound() {
    return this.bounds[0];
  }

  public Integer getUpperBound() {
    return this.bounds[1];
  }

  public void show() {
    System.out.println("lBound: " + bounds[0]);
    System.out.println("uBound: " + bounds[1]);
  }




}
