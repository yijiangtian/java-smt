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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.FunctionDeclaration;
import org.sosy_lab.java_smt.api.FunctionDeclarationKind;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.QuantifiedFormulaManager.Quantifier;
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.TraversalProcess;

public class DomainOptimizerDecider {
  private final DomainOptimizer opt;
  private final DomainOptimizerSolverContext delegate;

  public DomainOptimizerDecider(
      DomainOptimizer pOpt,
      DomainOptimizerSolverContext pDelegate) {
    opt = pOpt;
    delegate = pDelegate;
  }

  /*
  substitutes every variable in a formula with the domain that was calculated in the learning-phase.
  If the operator is LT or LTE, the upper bound is considered, the lower bound for GT or GTE
   */
  public Formula performSubstitutions(Formula f) {
    FormulaManager fmgr = delegate.getFormulaManager();
    IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
    List<Map<Formula, Formula>> substitutions = new ArrayList<>();
    final FunctionDeclarationKind[] declaration = new FunctionDeclarationKind[1];
    FormulaVisitor<TraversalProcess> substitutor =
        new FormulaVisitor<>() {

          @Override
          public TraversalProcess visitFreeVariable(Formula f, String name) {
            SolutionSet domain = opt.getSolutionSet(f);
            switch (declaration[0]) {
              case LTE:
                IntegerFormula upperBound = imgr.makeNumber(domain.getUpperBound());
                Map<Formula, Formula> substitution = new HashMap<>();
                substitution.put(f,upperBound);
                substitutions.add(substitution);
                break;

              case LT:
                upperBound = imgr.makeNumber(domain.getUpperBound() - 1);
                substitution = new HashMap<>();
                substitution.put(f,upperBound);
                substitutions.add(substitution);
                break;

              case GTE:
                IntegerFormula lowerBound = imgr.makeNumber(domain.getLowerBound());
                substitution = new HashMap<>();
                substitution.put(f,lowerBound);
                substitutions.add(substitution);
                break;

              case GT:
                lowerBound = imgr.makeNumber(domain.getUpperBound());
                substitution = new HashMap<>();
                substitution.put(f,lowerBound);
                substitutions.add(substitution);
                break;

            }
            return TraversalProcess.CONTINUE;
          }

          @Override
          public TraversalProcess visitBoundVariable(Formula f, int deBruijnIdx) {
            return null;
          }

          @Override
          public TraversalProcess visitConstant(Formula f, Object value) {
            return null;
          }

          @Override
          public TraversalProcess visitFunction(
              Formula f, List<Formula> args, FunctionDeclaration<?> functionDeclaration) {
            FunctionDeclarationKind dec = functionDeclaration.getKind();
            if (dec == FunctionDeclarationKind.LTE || dec == FunctionDeclarationKind.LT ||
            dec == FunctionDeclarationKind.GTE || dec == FunctionDeclarationKind.GT) {
              declaration[0] = dec;
            }
            return TraversalProcess.CONTINUE;
          }

          @Override
          public TraversalProcess visitQuantifier(
              BooleanFormula f,
              Quantifier quantifier,
              List<Formula> boundVariables,
              BooleanFormula body) {
            return null;
          }
        };
    fmgr.visitRecursively(f,substitutor);

    for (Map<Formula, Formula> substitute : substitutions) {
      f = fmgr.substitute(f,substitute);
    }

    return f;
  }


  public Set<Formula> replaceAll() {
    Set<Formula> newConstraints = new LinkedHashSet<>();
    for (Formula constraint : opt.getConstraints()) {
      constraint = performSubstitutions(constraint);
      newConstraints.add(constraint);
    }
    return newConstraints;
  }


}
