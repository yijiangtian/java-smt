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


import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

public class SolutionSet {

  private final Formula var;
  private final DomainOptimizer optimizer;
  private Integer[] bounds = new Integer[2];

  public SolutionSet(IntegerFormula variable, DomainOptimizer pOptimizer) {
    this.var = variable;
    this.optimizer = pOptimizer;
    this.setLowerBound(Integer.MIN_VALUE);
    this.setUpperBound(Integer.MAX_VALUE);
  }

  public void setLowerBound(Integer lBound) {
    this.bounds[0] = lBound;
  }

  public void setUpperBound(Integer uBound) {
    this.bounds[1] = uBound;
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

}
