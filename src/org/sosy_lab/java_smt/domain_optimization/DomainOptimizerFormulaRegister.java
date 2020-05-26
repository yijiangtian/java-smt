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

  private final DomainOptimizer opt;
  private final DomainOptimizerSolverContext delegate;
  private Function functionBuffer;

  enum argTypes {
    VAR,
    CONST,
    FUNC
  }

  enum operators {
    LT,
    GT,
    LTE,
    GTE,
    ADD,
    SUB,
    MULT,
    DIV
  }

  public DomainOptimizerFormulaRegister(DomainOptimizer opt) {
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
            SolutionSet domain = new SolutionSet();
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

    public void processConstraint (Formula f)  {
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
              }
              IntegerFormula var_1 = (IntegerFormula) pArgs.get(0);
              IntegerFormula var_2 = (IntegerFormula) pArgs.get(1);
              if ((getFormulaType(var_1) == argTypes.VAR) && (getFormulaType(var_2) == argTypes.VAR)) {
                return TraversalProcess.CONTINUE;
              }
              //SolutionSets of the variables are adjusted according to the function-declaration
              switch (declaration.toString()) {

                case "LT":
                  return adjustBounds(var_1, var_2, operators.LT, pArgs, pFunctionDeclaration);

                case "GT":
                  return adjustBounds(var_1, var_2, operators.GT, pArgs, pFunctionDeclaration);

                case "LTE":
                  return adjustBounds(var_1, var_2, operators.LTE, pArgs, pFunctionDeclaration);

                case "GTE":
                  return adjustBounds(var_1, var_2, operators.GTE, pArgs, pFunctionDeclaration);

                case "ADD":
                  return processDeclaration(var_1, var_2, operators.ADD, pArgs,
                      pFunctionDeclaration);

                case "SUB":
                  return processDeclaration(var_1, var_2, operators.SUB, pArgs,
                      pFunctionDeclaration);

                case "MUL":
                  return processDeclaration(var_1, var_2, operators.MULT, pArgs,
                      pFunctionDeclaration);

                case "DIV":
                  return processDeclaration(var_1, var_2, operators.DIV, pArgs,
                      pFunctionDeclaration);

              }
              return TraversalProcess.CONTINUE;
            }

            @Override
            public TraversalProcess visitConstant(Formula f, Object value) {
              IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
              IntegerFormula constant = imgr.makeNumber(value.toString());
              SolutionSet domain = new SolutionSet();
              opt.pushDomain(constant, domain);
              return TraversalProcess.CONTINUE;
            }
          };
      fmgr.visitRecursively(f, constraintExtractor);
    }

  /*
  performs depth-search on a function in order to retrieve variable { f(x) -> x }
   */
  public IntegerFormula digDeeper(List<Formula> args) {
    //if both arguments are variables, the visitor skips the functions
    if ((getFormulaType(args.get(0)) == argTypes.VAR) && (getFormulaType(args.get(1)) == argTypes.VAR)) {
      return null;
    }
    for (Formula var : args) {
        if (getFormulaType(var) == argTypes.VAR) {
          opt.pushVariable(var);
          return (IntegerFormula) var;
      }
    }
    return null;
  }

  /*
  parses a formula containing a numeral relation as an operator
   */
  public TraversalProcess adjustBounds(IntegerFormula var_1, IntegerFormula var_2, operators operator,
                           List<Formula> pArgs, FunctionDeclaration<?> pFunctionDeclaration) {
    if (getFormulaType(var_2) == argTypes.CONST) {
      Integer val_2 = Integer.parseInt(var_2.toString());
      if (getFormulaType(var_1) == argTypes.FUNC) {
        List<Formula> args = functionBuffer.args;
        Function func = new Function(pArgs, pFunctionDeclaration.getKind());
        putToBuffer(func);
        IntegerFormula variable = digDeeper(args);
        if (!check(variable)) {
          return TraversalProcess.CONTINUE;
        }
        SolutionSet domain_1 = opt.getSolutionSet(variable);
        if (val_2 > domain_1.getUpperBound() || val_2 < domain_1.getLowerBound()) {
          return TraversalProcess.CONTINUE;
        }
          if (operator == operators.LTE) {
            domain_1.setUpperBound(val_2);
          } else if (operator == operators.LT) {
            domain_1.setUpperBound(val_2 - 1);
          }
        else if (operator == operators.GTE) {
            domain_1.setLowerBound(val_2);
        } else if (operator == operators.GT) {
            domain_1.setLowerBound(val_2 + 1);
        }
      } else if (getFormulaType(var_1) == argTypes.VAR) {
        SolutionSet domain_1 = opt.getSolutionSet(var_1);
        if (val_2 > domain_1.getUpperBound() || val_2 < domain_1.getLowerBound()) {
          return TraversalProcess.CONTINUE;
        }
        if (operator == operators.LTE) {
          domain_1.setUpperBound(val_2);
        } else {
          domain_1.setLowerBound(val_2);
        }
      }
    } else if (getFormulaType(var_1) == argTypes.CONST) {
      Integer val_1 = Integer.parseInt(var_1.toString());

      if (getFormulaType(var_2) == argTypes.FUNC) {
        List<Formula> args = functionBuffer.args;
        Function func = new Function(pArgs, pFunctionDeclaration.getKind());
        putToBuffer(func);
        IntegerFormula variable = digDeeper(args);
        if (!check(variable)) {
          return TraversalProcess.CONTINUE;
        }
        SolutionSet domain_2 = opt.getSolutionSet(variable);
        if (val_1 > domain_2.getUpperBound() || val_1 < domain_2.getLowerBound()) {
          return TraversalProcess.CONTINUE;
        }
        domain_2.setLowerBound(val_1);
      } else if (getFormulaType(var_2) == argTypes.VAR) {
        SolutionSet domain_2 = opt.getSolutionSet(var_2);
        if (val_1 > domain_2.getUpperBound() || val_1 < domain_2.getLowerBound()) {
          return TraversalProcess.CONTINUE;
        }
        if (operator == operators.LTE) {
          domain_2.setLowerBound(val_1);
        } else if (operator == operators.LT) {
          domain_2.setLowerBound(val_1 + 1);
        } else if (operator == operators.GTE) {
          domain_2.setUpperBound(val_1);
        } else if (operator == operators.GT) {
          domain_2.setUpperBound(val_1 - 1);
        }
      }
    }
    return TraversalProcess.CONTINUE;
  }

  /*
  parses a formula containing an arithmetic relation as an operator
  */
  public TraversalProcess processDeclaration(IntegerFormula var_1, IntegerFormula var_2,
                                            operators op,
                                 List<Formula> pArgs, FunctionDeclaration<?> pFunctionDeclaration) {
    Function func = readFromBuffer();
    FunctionDeclarationKind dec = func.declaration;
    System.out.println(dec.toString());
    if (getFormulaType(var_1) == argTypes.VAR) {
      if (getFormulaType(var_2) == argTypes.CONST) {
        Integer val_2 = Integer.parseInt(var_2.toString());
        SolutionSet domain_1 = opt.getSolutionSet(var_1);
        if (dec == FunctionDeclarationKind.LTE) {
          Integer upperBound = domain_1.getUpperBound();
          if (op == operators.ADD) {
            domain_1.setUpperBound(upperBound - val_2);
          }
          else if (op == operators.SUB) {
            domain_1.setUpperBound(upperBound + val_2);
          }
          else if (op == operators.MULT) {
            domain_1.setUpperBound(upperBound / val_2);
          }
          else if (op == operators.DIV) {
            domain_1.setUpperBound(upperBound * val_2);
          }
        }
        else if (dec == FunctionDeclarationKind.GTE) {
          Integer lowerBound = domain_1.getLowerBound();
          if (op == operators.ADD) {
            domain_1.setLowerBound(lowerBound - val_2);
          }
          else if (op == operators.SUB) {
            domain_1.setLowerBound(lowerBound + val_2);
          }
          else if (op == operators.MULT) {
            domain_1.setLowerBound(lowerBound / val_2);
          }
          else if (op == operators.DIV) {
            domain_1.setLowerBound(lowerBound * val_2);
          }
        }
      }
    }
    if (getFormulaType(var_1) == argTypes.FUNC) {
      List<Formula> args = func.args;
      IntegerFormula variable = digDeeper(args);
      if (!check(variable)) {
        return TraversalProcess.CONTINUE;
      }
      SolutionSet domain = opt.getSolutionSet(variable);

      if (getFormulaType(var_2) == argTypes.CONST) {
        Integer val_2 = Integer.parseInt(var_2.toString());
        if (dec == FunctionDeclarationKind.LTE) {
          Integer upperBound = domain.getUpperBound();
          if (op == operators.ADD) {
            domain.setUpperBound(upperBound - val_2);
          }
          else if (op == operators.SUB) {
            domain.setUpperBound(upperBound + val_2);
          }
          else if (op == operators.MULT) {
            domain.setUpperBound(upperBound / val_2);
          }
          else if (op == operators.DIV) {
            domain.setUpperBound(upperBound * val_2);
          }
        }
        else if (dec == FunctionDeclarationKind.GTE) {
          Integer lowerBound = domain.getLowerBound();
          if (op == operators.ADD) {
            domain.setLowerBound(lowerBound - val_2);
          }
          else if (op == operators.SUB) {
            domain.setLowerBound(lowerBound + val_2);
          }
          else if (op == operators.MULT) {
            domain.setLowerBound(lowerBound / val_2);
          }
          else if (op == operators.DIV) {
            domain.setLowerBound(lowerBound * val_2);
          }
        }
      }
    }
    if (getFormulaType(var_2) == argTypes.FUNC) {
      List<Formula> args = func.args;
      IntegerFormula variable = digDeeper(args);
      if (!check(variable)) {
        return TraversalProcess.CONTINUE;
      }
      SolutionSet domain = opt.getSolutionSet(variable);
      List<Formula> extArgs = func.args;
      Integer val_2 = Integer.parseInt(extArgs.get(1).toString());
      if (getFormulaType(var_1) == argTypes.CONST) {
        Integer val_1 = Integer.parseInt(var_1.toString());
        SolutionSet domain2 = opt.getSolutionSet(variable);
        if (dec == FunctionDeclarationKind.LTE) {
          if (op == operators.ADD) {
            domain2.setUpperBound(val_2 - val_1);
          }
          else if (op == operators.SUB) {
            domain.setLowerBound(val_2 - val_1);
          }
          else if (op == operators.MULT) {
            domain.setLowerBound(val_2 / val_1);
          }
          else if (op == operators.DIV) {
            domain.setLowerBound(val_2 * val_1);
          }
        }
        else if (dec == FunctionDeclarationKind.GTE) {
          if (op == operators.ADD) {
            domain.setUpperBound(val_2 - val_1);
          }
          else if (op == operators.SUB) {
            domain.setUpperBound(val_2 - val_1);
          }
          else if (op == operators.MULT) {
            domain.setUpperBound(val_2 / val_1);
          }
          else if (op == operators.DIV) {
            domain.setUpperBound(val_2 * val_1);
          }
        }
      }
    }
    return TraversalProcess.CONTINUE;
  }


  public boolean check(IntegerFormula variable) {
    return variable != null;
  }


}