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

import java.util.HashSet;
import java.util.Set;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.visitors.DefaultFormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.TraversalProcess;

public class BasicDomainOptimizer implements DomainOptimizer{
  private final SolverContext delegate;
  private final ProverEnvironment wrapped;
  final Set<String> usedVariables = new HashSet<>();
  final BooleanFormula query;
  final Set<BooleanFormula> constraints;

  public BasicDomainOptimizer(SolverContext delegate, ProverEnvironment wrapped,
                              BooleanFormula query, Set<BooleanFormula> constraints) {

    this.delegate = delegate;
    this.wrapped = wrapped;
    this.query = query;
    this.constraints = constraints;
  }

  @Override
  public SolverContext getDelegate() {
    return this.delegate;
  }

  @Override
  public ProverEnvironment getWrapped() {
    return this.wrapped;
  }

  @Override
  public DomainOptimizer create(SolverContext delegate, ProverEnvironment wrapped,
                                BooleanFormula query, Set<BooleanFormula> constraints) {
    return new BasicDomainOptimizer(delegate, wrapped, query, constraints);
  }


  public void visit(Formula f) {
    SolverContext delegate = getDelegate();
    FormulaManager fmgr = delegate.getFormulaManager();
    FormulaVisitor<TraversalProcess> nameExtractor =
        new DefaultFormulaVisitor<>() {
          @Override
          protected TraversalProcess visitDefault(Formula formula) {
            return TraversalProcess.CONTINUE;
          }

          @Override
          public TraversalProcess visitFreeVariable(Formula formula, String name) {
            usedVariables.add(name);
            return TraversalProcess.CONTINUE;
          }
        };
    fmgr.visitRecursively(f, nameExtractor);
        }

  public Set<String> getVariables() {
    return this.usedVariables;
  }

}

