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
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.FunctionDeclaration;
import org.sosy_lab.java_smt.api.FunctionDeclarationKind;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.QuantifiedFormulaManager.Quantifier;
import org.sosy_lab.java_smt.api.SolverException;
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.TraversalProcess;

public class DomainOptimizerDecider {
  private final DomainOptimizer opt;
  private final DomainOptimizerSolverContext delegate;
  private final DomainOptimizerFormulaRegister register;
  private final List<Formula> satisfiableQueries = new ArrayList<>();

  public DomainOptimizerDecider(
      DomainOptimizer pOpt,
      DomainOptimizerSolverContext pDelegate) {
    opt = pOpt;
    delegate = pDelegate;
    register = opt.getRegister();
  }


    public Formula performSubstitutions(Formula f) {
      FormulaManager fmgr = delegate.getFormulaManager();
      IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
      List<Map<Formula,Formula>> substitutions = new ArrayList<>();
      final FunctionDeclarationKind[] dec = new FunctionDeclarationKind[1];
      FormulaVisitor<TraversalProcess> convertToArray =
          new FormulaVisitor<>() {
            @Override
            public TraversalProcess visitFreeVariable(Formula f, String name) {
              SolutionSet domain = opt.getSolutionSet(f);
              IntegerFormula substitute;
              switch (dec[0]) {
                case LTE:
                  substitute = imgr.makeNumber(domain.getUpperBound());
                  break;
                case LT:
                  substitute = imgr.makeNumber(domain.getUpperBound() - 1);
                  break;
                case GTE:
                  substitute = imgr.makeNumber(domain.getLowerBound());
                  break;
                case GT:
                  substitute = imgr.makeNumber(domain.getLowerBound() + 1);
                  break;
                default:
                  throw new IllegalStateException("Unexpected value: " + dec[0]);
              }
              Map<Formula,Formula> substitution = new HashMap<>();
              substitution.put(f,substitute);
              substitutions.add(substitution);
              return TraversalProcess.CONTINUE;
            }

            @Override
            public TraversalProcess visitBoundVariable(Formula f, int deBruijnIdx) {
              return null;
            }

            @Override
            public TraversalProcess visitConstant(Formula f, Object value) {
              return TraversalProcess.CONTINUE;
            }

            @Override
            public TraversalProcess visitFunction(
                Formula f, List<Formula> args, FunctionDeclaration<?> functionDeclaration) {
              FunctionDeclarationKind declaration = functionDeclaration.getKind();
              if (declaration == FunctionDeclarationKind.LTE || declaration == FunctionDeclarationKind.LT ||
              declaration == FunctionDeclarationKind.GTE || declaration == FunctionDeclarationKind.GT) {
                dec[0] = declaration;
              }
              return TraversalProcess.CONTINUE;
            }

            @Override
            public TraversalProcess visitQuantifier(
                BooleanFormula f,
                Quantifier quantifier,
                List<Formula> boundVariables,
                BooleanFormula body) {
              return TraversalProcess.CONTINUE;
            }
          };
      fmgr.visitRecursively(f, convertToArray);
      for (Map<Formula,Formula> substitution :
          substitutions) {
        f = fmgr.substitute(f,substitution);
      }
      return f;
    }


    public void decide(BooleanFormula query) throws InterruptedException, SolverException {
      query = (BooleanFormula) performSubstitutions(query);
      DomainOptimizerProverEnvironment wrapped = opt.getWrapped();
      wrapped.addConstraint(query);
      if (!wrapped.isUnsat()) {
        satisfiableQueries.add(query);
      }
    }


    public List<Formula> getSatisfiableQueries() {
    return satisfiableQueries;
    }


}