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

import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;

public class BasicDomainOptimizer implements DomainOptimizer{
  private final SolverContext delegate;
  private final ProverEnvironment wrapped;

  public BasicDomainOptimizer(SolverContext delegate, ProverEnvironment wrapped) {
    this.delegate = delegate;
    this.wrapped = wrapped;
    FormulaManager fManager = delegate.getFormulaManager();
    IntegerFormulaManager ifmgr = fManager.getIntegerFormulaManager();
    BooleanFormulaManager bfmgr = fManager.getBooleanFormulaManager();

  }

  @Override
  public DomainOptimizer create(SolverContext delegate, ProverEnvironment wrapped) {
    return new BasicDomainOptimizer(delegate, wrapped);
  }

  @Override
  public void calculateDomains(Formula f) {

  }
}