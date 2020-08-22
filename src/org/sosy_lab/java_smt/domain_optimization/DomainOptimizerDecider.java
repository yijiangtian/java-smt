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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.FunctionDeclaration;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverException;
import org.sosy_lab.java_smt.api.visitors.BooleanFormulaTransformationVisitor;
import org.sosy_lab.java_smt.api.visitors.DefaultFormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.TraversalProcess;

public class DomainOptimizerDecider {

  private final DomainOptimizer opt;
  private boolean fallBack;
  private final DomainOptimizerSolverContext delegate;
  private final ProverEnvironment wrapped;
  private List<Formula> variables = new ArrayList<>();

  public DomainOptimizerDecider(DomainOptimizer pOpt, DomainOptimizerSolverContext pDelegate) {
    opt = pOpt;
    delegate = pDelegate;
    this.wrapped = opt.getWrapped();
    this.fallBack = false;
  }

  public boolean getFallback() {
    return this.fallBack;
  }

  public List<Formula> performSubstitutions(Formula f) {
    FormulaManager fmgr = delegate.getFormulaManager();
    IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
    List<Formula> variables = new ArrayList<>();
    List<Formula> substitutedFormulas = new ArrayList<>();
    FormulaVisitor<TraversalProcess> varExtractor =
        new DefaultFormulaVisitor<>() {
          @Override
          protected TraversalProcess visitDefault(Formula f) {
            return TraversalProcess.CONTINUE;
          }
          @Override
          public TraversalProcess visitFreeVariable(Formula formula, String name) {
            variables.add(formula);
            return TraversalProcess.CONTINUE;
          }
        };
    fmgr.visitRecursively(f, varExtractor);

    this.variables = variables;
    int[][] decisionMatrix = constructDecisionMatrix();

    for (int i = 0; i < Math.pow(2, variables.size()); i++) {
      List<Map<Formula, Formula>> substitutions = new ArrayList<>();
      for (int j = 0; j < variables.size(); j++) {
        Formula var = variables.get(j);
        Interval domain = opt.getInterval(var);
        Map<Formula, Formula> substitution = new HashMap<>();
        if (decisionMatrix[j][i] == 1) {
          substitution.put(var, imgr.makeNumber(domain.getUpperBound()));
        } else if (decisionMatrix[j][i] == 0) {
          substitution.put(var, imgr.makeNumber(domain.getLowerBound()));
        }
        substitutions.add(substitution);
      }
      Formula buffer = f;
      for (Map<Formula, Formula> substitution : substitutions) {
        f = fmgr.substitute(f, substitution);
      }
      substitutedFormulas.add(f);
      f = buffer;
    }
    return substitutedFormulas;
  }


  public int[][] constructDecisionMatrix() {
    int[][] decisionMatrix = new int[variables.size()][(int) Math.pow(2,variables.size())];
    int rows = (int) Math.pow(2,variables.size());
    for (int i=0; i<rows; i++) {
      for (int j=variables.size() - 1; j>=0; j--) {
        decisionMatrix[j][i] = (i/(int) Math.pow(2, j))%2;
      }
    }
    return decisionMatrix;
  }

  public boolean decide(BooleanFormula query, int maxIterations) throws InterruptedException,
                                                              SolverException {
    List<Formula> readyForDecisisionPhase = performSubstitutions(query);
    int count = 0;
    for (Formula f : readyForDecisisionPhase) {
      this.wrapped.push();
      this.wrapped.addConstraint((BooleanFormula) f);
      this.wrapped.addConstraint(query);
      if (!this.wrapped.isUnsat()) {
        return true;
      }
      count++;
      this.wrapped.pop();
      if (count == maxIterations) {
        this.fallBack = true;
        return false;
      }
    }
    return false;
  }


  public BooleanFormula pruneTree(Formula pFormula, int maxIterations) throws InterruptedException,
                                                              SolverException {
    FormulaManager fmgr = delegate.getFormulaManager();
    BooleanFormulaManager bmgr = fmgr.getBooleanFormulaManager();
    List<BooleanFormula> operands = new ArrayList<>();
    List<Map<Formula, Formula>> substitutions = new ArrayList<>();
    BooleanFormulaTransformationVisitor visitor = new BooleanFormulaTransformationVisitor(fmgr) {
      @Override
      public BooleanFormula visitAtom(BooleanFormula pAtom,
                                      FunctionDeclaration<BooleanFormula> decl) {
        operands.add(pAtom);
        return super.visitAtom(pAtom, decl);
      }
      @Override
      public BooleanFormula visitAnd(List<BooleanFormula> processedOperands) {
        operands.addAll(processedOperands);
        return super.visitAnd(processedOperands);
      }
      @Override
      public BooleanFormula visitOr(List<BooleanFormula> processedOperands) {
        operands.addAll(processedOperands);
        return super.visitOr(processedOperands);
      }
    };
    bmgr.visit((BooleanFormula) pFormula, visitor);
    for (BooleanFormula toProcess : operands) {
      Map<Formula, Formula> substitution = new HashMap<>();
      if (decide((BooleanFormula) pFormula, maxIterations)) {
        substitution.put(toProcess, bmgr.makeTrue());
      } else {
        substitution.put(toProcess, bmgr.makeFalse());
      }
      substitutions.add(substitution);
    }
    for (Map<Formula, Formula> substitution : substitutions) {
      pFormula = fmgr.substitute(pFormula, substitution);
    }
    return (BooleanFormula) pFormula;
  }


}