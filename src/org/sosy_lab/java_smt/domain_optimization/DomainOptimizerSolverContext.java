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

import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.InterpolatingProverEnvironment;
import org.sosy_lab.java_smt.api.OptimizationProverEnvironment;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;

public class DomainOptimizerSolverContext implements SolverContext {

  private final DomainOptimizer optimizer;
  private final SolverContext delegate;

  public DomainOptimizerSolverContext(DomainOptimizer optimizer, SolverContext delegate) {
    this.optimizer = optimizer;
    this.delegate = delegate;
  }

  @Override
  public FormulaManager getFormulaManager() {
    return null;
  }

  @Override
  public ProverEnvironment newProverEnvironment(ProverOptions... options) {
    return null;
  }

  @Override
  public InterpolatingProverEnvironment<?> newProverEnvironmentWithInterpolation(
      ProverOptions... options) {
    return null;
  }

  @Override
  public OptimizationProverEnvironment newOptimizationProverEnvironment(ProverOptions... options) {
    return null;
  }

  @Override
  public String getVersion() {
    return null;
  }

  @Override
  public Solvers getSolverName() {
    return null;
  }

  @Override
  public void close() {

  }
}
