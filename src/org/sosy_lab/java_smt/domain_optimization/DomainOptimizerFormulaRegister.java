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

import java.util.List;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.FunctionDeclaration;
import org.sosy_lab.java_smt.api.FunctionDeclarationKind;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.QuantifiedFormulaManager.Quantifier;
import org.sosy_lab.java_smt.api.visitors.DefaultFormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.TraversalProcess;

class Function {
  List<Formula> args;
  FunctionDeclarationKind declaration;
  public Function(List<Formula> args, FunctionDeclarationKind declaration) {
    this.args = args;
    this.declaration = declaration;
  }
}

public class DomainOptimizerFormulaRegister {

  private final DomainOptimizerProverEnvironment env;
  private final DomainOptimizer opt;
  private final DomainOptimizerSolverContext delegate;
  private Function functionBuffer;

  enum argTypes {
    VAR,
    CONST,
    FUNC
  }

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
          protected TraversalProcess visitDefault(Formula f) {
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

  public argTypes getFormulaType(Formula f) {
    FormulaManager fmgr = delegate.getFormulaManager();
    FormulaVisitor<argTypes> getFormulaType =
        new FormulaVisitor<>() {
          @Override
          public argTypes visitFreeVariable(Formula f, String name) {
            return argTypes.VAR;
          }

          @Override
          public argTypes visitBoundVariable(Formula f, int deBruijnIdx) {
            return null;
          }

          @Override
          public argTypes visitConstant(Formula f, Object value) {
            return argTypes.CONST;
          }

          @Override
          public argTypes visitFunction(
              Formula f, List<Formula> pArgs,
              FunctionDeclaration<?> pFunctionDeclaration) {
            FunctionDeclarationKind decl = pFunctionDeclaration.getKind();
            Function buffer = new Function(pArgs, decl);
            putToBuffer(buffer);
            return argTypes.FUNC;
          }

          @Override
          public argTypes visitQuantifier(
              BooleanFormula f,
              Quantifier quantifier,
              List<Formula> boundVariables,
              BooleanFormula body) {
            return null;
          }
        };
    return fmgr.visit(f, getFormulaType);
  }

  public void putToBuffer(Function f) {
    this.functionBuffer = f;
  }

  public Function readFromBuffer() {
    return this.functionBuffer;
  }




    public void processConstraint (Formula f){
      FormulaManager fmgr = delegate.getFormulaManager();
      FormulaVisitor<TraversalProcess> constraintExtractor =
          new DefaultFormulaVisitor<>() {
            @Override
            protected TraversalProcess visitDefault(Formula f) {
              return TraversalProcess.CONTINUE;
            }

            @Override
            public TraversalProcess visitFunction(
                Formula f, List<Formula> pArgs,
                FunctionDeclaration<?> pFunctionDeclaration) {
              IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
              FunctionDeclarationKind declaration = pFunctionDeclaration.getKind();
              //iterate through the function arguments and retrieve the corresponding variables in the
              //domain-dictionary
              for (Formula argument : pArgs) {
                IntegerFormula var = imgr.makeVariable(argument.toString());
                //if a number is encountered, the visitConstant-method is called
                if (getFormulaType(argument) == argTypes.CONST) {
                  visitConstant(var, argument);
                }
                if (getFormulaType(argument) == argTypes.FUNC) {
                  Function func = readFromBuffer();
                  List<Formula> args = func.args;
                  IntegerFormula variable = digDeeper(args);
                  System.out.println(variable.toString());
                }
              }
              IntegerFormula var_1 = (IntegerFormula) pArgs.get(0);
              SolutionSet domain_1 = opt.getSolutionSet(var_1);
              IntegerFormula var_2 = (IntegerFormula) pArgs.get(1);
              SolutionSet domain_2 = opt.getSolutionSet(var_2);
              //SolutionSets of the variables are adjusted according to the function-declaration
              switch (declaration.toString()) {

                case "LTE":
                  if (getFormulaType(var_2) == argTypes.CONST) {
                    Integer val_2 = Integer.parseInt(var_2.toString());
                    if (getFormulaType(var_1)== argTypes.FUNC) {
                      List<Formula> args = functionBuffer.args;
                      Function func = new Function(pArgs, pFunctionDeclaration.getKind());
                      putToBuffer(func);
                      IntegerFormula variable = digDeeper(args);
                      domain_1 = opt.getSolutionSet(variable);
                      domain_1.setUpperBound(val_2);
                    }
                  } else if (getFormulaType(var_1) == argTypes.CONST) {
                    Integer val_1 = Integer.parseInt(var_1.toString());
                    domain_2.setLowerBound(val_1);
                  }
                  break;
                case "GTE":
                  if (var_2.toString().matches(".*\\d.*")) {
                    Integer val_2 = Integer.parseInt(var_2.toString());
                    domain_1.setLowerBound(val_2);
                  } else if (var_1.toString().matches(".*\\d.*")) {
                    Integer val_1 = Integer.parseInt(var_1.toString());
                    domain_2.setUpperBound(val_1);
                  }
                  break;

                case "SUB":
                  if (getFormulaType(var_1) == argTypes.VAR) {
                  if (getFormulaType(var_2) == argTypes.CONST) {
                    Function func = readFromBuffer();
                    Integer val_2 = Integer.parseInt(var_2.toString());
                    domain_1 = opt.getSolutionSet(var_1);
                    FunctionDeclarationKind dec = functionBuffer.declaration;
                    if (dec== FunctionDeclarationKind.LTE) {
                      Integer upperBound = domain_1.getUpperBound();
                      domain_1.setUpperBound(upperBound + val_2);
                    }
                  }
                }
                  if (getFormulaType(var_1) == argTypes.FUNC) {
                    Function func = readFromBuffer();
                    List<Formula> args = func.args;
                    IntegerFormula variable = digDeeper(args);
                    SolutionSet domain = new SolutionSet(variable, opt);
                    opt.pushDomain(variable, domain);

                    if (getFormulaType(var_2) == argTypes.CONST) {
                      Integer val_2 = Integer.parseInt(var_2.toString());
                      SolutionSet domain1 = opt.getSolutionSet(variable);
                      FunctionDeclarationKind dec = func.declaration;
                      if (dec == FunctionDeclarationKind.LTE) {
                        Integer upperBound = domain.getUpperBound();
                        domain.setUpperBound(upperBound + val_2);
                      }
                    }
                  }

                  if (getFormulaType(var_2) == argTypes.FUNC) {
                    Function func = readFromBuffer();
                    List<Formula> args = func.args;
                    IntegerFormula variable = digDeeper(args);
                    if (getFormulaType(var_1) == argTypes.CONST) {
                      Integer val_1 = Integer.parseInt(var_1.toString());
                      SolutionSet domain1 = opt.getSolutionSet(variable);
                    }
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
  performs depth-search on a function in order to retrieve variable
   */
  public IntegerFormula digDeeper(List<Formula> args) {
    for (Formula var : args) {
        if (getFormulaType(var) == argTypes.VAR) {
          opt.pushVariable(var);
          SolutionSet domain = new SolutionSet(var, opt);
          opt.pushDomain(var, domain);
          return (IntegerFormula) var;
      }
    }
    return null;
  }
}