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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.FunctionDeclaration;
import org.sosy_lab.java_smt.api.FunctionDeclarationKind;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.visitors.DefaultFormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.TraversalProcess;

public class DomainOptimizerFormulaRegister {

  private final DomainOptimizerProverEnvironment env;
  private final DomainOptimizer opt;
  private final DomainOptimizerSolverContext delegate;

  //when parsing a constraint,the data for every path is stored in here
  private LinkedHashMap<Integer,String> declarations = new LinkedHashMap<>();

  public DomainOptimizerFormulaRegister(DomainOptimizerProverEnvironment env,
                                       DomainOptimizer opt) {
    this.env = env;
    this.opt = opt;
    this.delegate = opt.getDelegate();
  }

  //forms tuples of variables along with their domains
  public void visit(Formula f) {
    FormulaManager fmgr = delegate.getFormulaManager();
    FormulaVisitor<TraversalProcess> nameExtractor =
        new DefaultFormulaVisitor<>() {
          @Override
          protected TraversalProcess visitDefault(Formula formula) {
            return TraversalProcess.CONTINUE;
          }

          @Override
          public TraversalProcess visitFreeVariable(Formula formula, String name) {
            IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
            IntegerFormula var = imgr.makeVariable(name);
            opt.pushVariable(var);
            SolutionSet domain = new SolutionSet(var, opt);
            opt.pushDomain(var, domain);
            return TraversalProcess.CONTINUE;
          }
        };
    fmgr.visitRecursively(f, nameExtractor);
  }

  public void processConstraint(Formula f) {
    FormulaManager fmgr = delegate.getFormulaManager();
    if (declarations.size() > 1) {
      declarations = sortHashMapByValues(declarations);
    }

    FormulaVisitor<TraversalProcess> constraintExtractor =
        new DefaultFormulaVisitor<>() {
          @Override
          protected TraversalProcess visitDefault(Formula f) {
            return TraversalProcess.CONTINUE;
          }
          @Override
          public TraversalProcess visitFunction(Formula f, List<Formula> pArgs,
                                                FunctionDeclaration<?> pFunctionDeclaration) {
            IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
            FunctionDeclarationKind declaration = pFunctionDeclaration.getKind();
            //iterate through the function arguments and retrieve the corresponding variables in the
            //domain-dictionary
            for (Formula argument: pArgs) {
                IntegerFormula var = imgr.makeVariable(argument.toString());
                //if a number is encountered, the visitConstant-method is called
                if (argument.toString().matches("[0-9]+")) {
                  visitConstant(var, argument);
                }
            }
            IntegerFormula var_1 = (IntegerFormula) pArgs.get(0);
            SolutionSet domain_1 = opt.getSolutionSet(var_1);
            IntegerFormula var_2 = (IntegerFormula) pArgs.get(1);
            SolutionSet domain_2 = opt.getSolutionSet(var_2);
            //SolutionSets of the variables are adjusted according to the function-declaration
            switch (declaration.toString()) {
              case "LTE":
                if (var_2.toString().matches(".*\\d.*")) {
                  Integer val_2 = Integer.parseInt(var_2.toString());
                  declarations.put(val_2, declaration.toString());
                  //if argument 1 is a formula rather than a variable or a constant, the tree is
                  //searched recursively
                  if (var_1.toString().contains(" ")) {
                    processConstraint(var_1);
                    break;
                  }
                  domain_1.setUpperBound(val_2);

                } else if (var_1.toString().matches(".*\\d.*")) {
                  Integer val_1 = Integer.parseInt(var_1.toString());
                  declarations.put(val_1, declaration.toString());
                  //analog for the case of argument 2 being a formula
                  if (var_2.toString().contains(" ")) {
                    processConstraint(var_2);
                    break;
                  }
                  domain_2.setLowerBound(val_1);
                }
                break;
              case "GTE":
                if (var_2.toString().matches(".*\\d.*")) {
                  Integer val_2 = Integer.parseInt(var_2.toString());
                  declarations.put(val_2, declaration.toString());
                  domain_1.setLowerBound(val_2);
                } else if (var_1.toString().matches(".*\\d.*")) {
                  Integer val_1 = Integer.parseInt(var_1.toString());
                  declarations.put(val_1, declaration.toString());
                  domain_2.setUpperBound(val_1);
                }
                break;
              case "SUB":
                if (var_2.toString().matches(".*\\d.*")) {
                  Integer val_2 = Integer.parseInt(var_2.toString());
                  domain_1.setLowerBound(val_2);
                }
                break;
            }
            return TraversalProcess.CONTINUE;
          }
          @Override
          public TraversalProcess visitConstant(Formula f, Object value) {
            IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
            IntegerFormula constant = imgr.makeNumber(value.toString());
            SolutionSet domain = new SolutionSet(constant, opt);
            opt.pushDomain(constant, domain);
            return TraversalProcess.CONTINUE;
          }
        };
    fmgr.visitRecursively(f, constraintExtractor);
  }


  /*
        sorting method for declarations, serving the purpose of aggregating the data gathered when
        processing the constraints
         */
  public LinkedHashMap<Integer, String> sortHashMapByValues(
      HashMap<Integer, String> passedMap) {
    List<Integer> mapKeys = new ArrayList<>(passedMap.keySet());
    List<String> mapValues = new ArrayList<>(passedMap.values());
    Collections.sort(mapValues);
    Collections.sort(mapKeys);

    LinkedHashMap<Integer, String> sortedMap =
        new LinkedHashMap<>();

    Iterator<String> valueIt = mapValues.iterator();
    while (valueIt.hasNext()) {
      String val = valueIt.next();
      Iterator<Integer> keyIt = mapKeys.iterator();

      while (keyIt.hasNext()) {
        Integer key = keyIt.next();
        String comp1 = passedMap.get(key);
        String comp2 = val;

        if (comp1.equals(comp2)) {
          keyIt.remove();
          sortedMap.put(key, val);
          break;
        }
      }
    }
    return sortedMap;
  }

}