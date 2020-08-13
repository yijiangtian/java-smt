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
import org.sosy_lab.java_smt.api.visitors.DefaultFormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor;
import org.sosy_lab.java_smt.api.visitors.TraversalProcess;

public class DomainOptimizerFormulaRegister {

  private static class Function {

    private final List<Formula> args;
    private final FunctionDeclarationKind declaration;

    Function(List<Formula> args, FunctionDeclarationKind declaration) {
      this.args = args;
      this.declaration = declaration;
    }
  }

  private final DomainOptimizer opt;
  private final DomainOptimizerSolverContext delegate;
  private Function functionBuffer;
  private Map<Formula, Formula> substitution;
  private boolean isSubstituted = false;

  enum Argtypes {
    VAR,
    CONST,
    FUNC
  }

  enum Operators {
    LT,
    GT,
    LTE,
    GTE,
    ADD,
    SUB
  }

  public DomainOptimizerFormulaRegister(DomainOptimizer opt) {
    this.opt = opt;
    this.delegate = opt.getDelegate();
  }

  // forms tuples of variables along with their domains
  public void visit(Formula pFormula) {
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
            opt.addVariable(var);

            return TraversalProcess.CONTINUE;
          }
        };
    fmgr.visitRecursively(pFormula, nameExtractor);
  }


  public boolean isCaterpillar(Formula pFormula) {
    FormulaManager fmgr = delegate.getFormulaManager();
    FormulaVisitor<Boolean> isCaterpillar =
        new FormulaVisitor<>() {
          @Override
          public Boolean visitFreeVariable(Formula f, String name) {
            return true;
          }

          @Override
          public Boolean visitBoundVariable(Formula f, int deBruijnIdx) {
            return true;
          }

          @Override
          public Boolean visitConstant(Formula f, Object value) {
            return true;
          }

          @Override
          public Boolean visitFunction(
              Formula f, List<Formula> args, FunctionDeclaration<?> functionDeclaration) {
            return getFormulaType(args.get(0)) != Argtypes.FUNC
                || getFormulaType(args.get(1)) != Argtypes.FUNC;
          }

          @Override
          public Boolean visitQuantifier(
              BooleanFormula f,
              Quantifier quantifier,
              List<Formula> boundVariables,
              BooleanFormula body) {
            return true;
          }
        };
    return fmgr.visit(pFormula, isCaterpillar);
  }



  public Argtypes getFormulaType(Formula pFormula) {
    FormulaManager fmgr = delegate.getFormulaManager();
    FormulaVisitor<Argtypes> getFormulaType =
        new FormulaVisitor<>() {
          @Override
          public Argtypes visitFreeVariable(Formula f, String name) {
            return Argtypes.VAR;
          }

          @Override
          public Argtypes visitBoundVariable(Formula f, int deBruijnIdx) {
            return null;
          }

          @Override
          public Argtypes visitConstant(Formula f, Object value) {
            return Argtypes.CONST;
          }

          @Override
          public Argtypes visitFunction(
              Formula f, List<Formula> pArgs, FunctionDeclaration<?> pFunctionDeclaration) {
            FunctionDeclarationKind decl = pFunctionDeclaration.getKind();
            Function buffer = new Function(pArgs, decl);
            putToBuffer(buffer);
            return Argtypes.FUNC;
          }

          @Override
          public Argtypes visitQuantifier(
              BooleanFormula f,
              Quantifier quantifier,
              List<Formula> boundVariables,
              BooleanFormula body) {
            return null;
          }
        };
    return fmgr.visit(pFormula, getFormulaType);
  }

  private void putToBuffer(Function f) {
    this.functionBuffer = f;
  }

  private Function readFromBuffer() {
    return this.functionBuffer;
  }

  public void setSubstitutionFlag(boolean pIsSubstituted) {
    this.isSubstituted = pIsSubstituted;
  }

  public boolean getSubstitutionFlag() {
    return this.isSubstituted;
  }

  public void setSubstitution(Map<Formula, Formula> pSubstitution) {
    this.substitution = pSubstitution;
  }

  public Map<Formula, Formula> getSubstitution() {
    return this.substitution;
  }



  public int countVariables(Formula f) {
    return delegate.getFormulaManager().extractVariables(f).size();
  }


  public void processConstraint(Formula pFormula) {
    FormulaManager fmgr = delegate.getFormulaManager();
    FormulaVisitor<TraversalProcess> constraintExtractor =
        new DefaultFormulaVisitor<>() {
          @Override
          protected TraversalProcess visitDefault(Formula f) {
            return TraversalProcess.CONTINUE;
          }

          @Override
          public TraversalProcess visitFunction(
              Formula f, List<Formula> pArgs, FunctionDeclaration<?> pFunctionDeclaration) {
            FunctionDeclarationKind declaration = pFunctionDeclaration.getKind();
            Formula variableOne = pArgs.get(0);
            Formula variableTwo = pArgs.get(1);
            // Intervals of the variables are adjusted according to the function-declaration
            switch (declaration.toString()) {
              case "LT":
                return adjustBounds(variableOne, variableTwo,
                    Operators.LT, pArgs, pFunctionDeclaration);

              case "GT":
                return adjustBounds(variableOne, variableTwo,
                    Operators.GT, pArgs, pFunctionDeclaration);

              case "LTE":
                return adjustBounds(variableOne, variableTwo,
                    Operators.LTE, pArgs, pFunctionDeclaration);

              case "GTE":
                return adjustBounds(variableOne, variableTwo,
                    Operators.GTE, pArgs, pFunctionDeclaration);

              case "ADD":
                return processDeclaration(variableOne, variableTwo, Operators.ADD);

              case "SUB":
                return processDeclaration(variableOne, variableTwo, Operators.SUB);

            }
            return TraversalProcess.CONTINUE;
          }

          @Override
          public TraversalProcess visitConstant(Formula f, Object value) {
            IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
            String name = format(value.toString());
            IntegerFormula constant = imgr.makeNumber(name);
            Interval domain = new Interval();
            opt.addDomain(constant, domain);
            return TraversalProcess.CONTINUE;
          }
        };
    fmgr.visitRecursively(pFormula, constraintExtractor);
  }

  /*
  performs depth-search on a function in order to retrieve variables { f(x,y) -> x,y }
   */
  public List<Formula> digDeeper(List<Formula> args) {
    List<Formula> vars = new ArrayList<>();
    for (Formula var : args) {
      if (getFormulaType(var) == Argtypes.VAR) {
        opt.addVariable(var);
        vars.add(var);
      } else if (getFormulaType(var) == Argtypes.FUNC) {
        Function func = readFromBuffer();
        vars = digDeeper(func.args);
      }
    }
    return vars;
  }

  /*
  retrieves constants from function
   */
  public List<Formula> digDeeperForConstants(List<Formula> args) {
    List<Formula> constants = new ArrayList<>();
    for (Formula var : args) {
      if (getFormulaType(var) == Argtypes.CONST) {
        constants.add(var);
      } else if (getFormulaType(var) == Argtypes.FUNC) {
        Function func = readFromBuffer();
        constants = digDeeperForConstants(func.args);
      }
    }
    return constants;
  }

  /*
  parses a formula containing a numeral relation as an operator
   */
  private TraversalProcess adjustBounds(
      Formula variableOne,
      Formula variableTwo,
      Operators operator,
      List<Formula> pArgs,
      FunctionDeclaration<?> pFunctionDeclaration) {

    Argtypes argOne = getFormulaType(variableOne);
    Argtypes argTwo = getFormulaType(variableTwo);

    if (argOne == Argtypes.FUNC && argTwo == Argtypes.VAR) {
      Interval domainTwo = opt.getInterval(variableTwo);
      Interval domainOne;
      List<Formula> args = functionBuffer.args;
      Function func = new Function(pArgs, pFunctionDeclaration.getKind());
      putToBuffer(func);
      List<Formula> vars = digDeeper(args);
      Formula variable = vars.get(0);
      domainOne = opt.getInterval(variable);
      if (operator == Operators.LTE) {
        domainOne.setUpperBound(domainTwo.getUpperBound());
      } else if (operator == Operators.LT) {
        domainOne.setUpperBound(domainTwo.getUpperBound() - 1);
      } else if (operator == Operators.GTE) {
        domainOne.setLowerBound(domainTwo.getUpperBound());
      } else if (operator == Operators.GT) {
        domainOne.setLowerBound(domainTwo.getUpperBound() + 1);
      }
    }

    if (argOne == Argtypes.FUNC && argTwo == Argtypes.CONST) {
      String name = format(variableTwo.toString());
      Integer valueTwo = Integer.parseInt(name);
      List<Formula> args = functionBuffer.args;
      Function func = new Function(pArgs, pFunctionDeclaration.getKind());
      List<Formula> vars = digDeeper(args);
      Formula varOne = vars.get(0);
      Interval domainOne = opt.getInterval(varOne);
      if (vars.size() > 1) {
        Formula varTwo = vars.get(1);
        Interval domainTwo = opt.getInterval(varTwo);
        if (!domainTwo.isSet()) {
          return TraversalProcess.CONTINUE;
        }
        if (operator == Operators.LTE) {
          domainOne.setUpperBound(valueTwo);
          processDeclaration(varOne, varTwo, Operators.LTE);
        } else if (operator == Operators.LT) {
          domainOne.setUpperBound(valueTwo - 1);
          processDeclaration(varOne, varTwo, Operators.LT);
        } else if (operator == Operators.GTE) {
          domainOne.setLowerBound(valueTwo);
          processDeclaration(varOne, varTwo, Operators.GTE);
        } else if (operator == Operators.GT) {
          domainOne.setLowerBound(valueTwo);
          processDeclaration(varOne, varTwo, Operators.GT);
        }
      } else {
        if (operator == Operators.LTE) {
          domainOne.setUpperBound(valueTwo);
        } else if (operator == Operators.LT) {
          domainOne.setUpperBound(valueTwo - 1);
        } else if (operator == Operators.GTE) {
          domainOne.setLowerBound(valueTwo);
        } else if (operator == Operators.GT) {
          domainOne.setLowerBound(valueTwo + 1);
        }
      }
      putToBuffer(func);
    }

    if (argOne == Argtypes.VAR && argTwo == Argtypes.FUNC) {
      Interval domainOne = opt.getInterval(variableOne);
      List<Formula> args = functionBuffer.args;
      Function func = new Function(pArgs, pFunctionDeclaration.getKind());
      putToBuffer(func);
      List<Formula> vars = digDeeper(args);
      Formula variable = vars.get(1);
      Interval domainTwo = opt.getInterval(variable);
      if (operator == Operators.LTE) {
        domainOne.setUpperBound(domainTwo.getUpperBound());
      } else if (operator == Operators.LT) {
        domainOne.setUpperBound(domainTwo.getUpperBound() - 1);
      } else if (operator == Operators.GTE) {
        domainOne.setLowerBound(domainTwo.getUpperBound());
      } else if (operator == Operators.GT) {
        domainOne.setLowerBound(domainTwo.getUpperBound() + 1);
      }
    }

    if (argOne == Argtypes.VAR && argTwo == Argtypes.VAR) {
      Interval domainOne = opt.getInterval(variableOne);
      Interval domainTwo = opt.getInterval(variableTwo);
      if (operator == Operators.LTE) {
        domainOne.setUpperBound(domainTwo.getUpperBound());
      } else if (operator == Operators.LT) {
        domainOne.setUpperBound(domainTwo.getUpperBound() - 1);
      } else if (operator == Operators.GTE) {
        domainOne.setLowerBound(domainTwo.getUpperBound());
      } else if (operator == Operators.GT) {
        domainOne.setLowerBound(domainTwo.getUpperBound() + 1);
      }
    }

    if (argOne == Argtypes.VAR && argTwo == Argtypes.CONST) {
      String name = format(variableTwo.toString());
      Integer valueTwo = Integer.parseInt(name);
      Interval domainOne = opt.getInterval(variableOne);
      if (operator == Operators.LTE) {
        domainOne.setUpperBound(valueTwo);
      } else if (operator == Operators.GTE) {
        domainOne.setLowerBound(valueTwo);
      }
    }

    if (argOne == Argtypes.CONST && argTwo == Argtypes.FUNC) {
      String name = format(variableOne.toString());
      Integer valueOne = Integer.parseInt(name);
      List<Formula> args = functionBuffer.args;
      List<Formula> vars = digDeeper(args);
      Formula varOne = vars.get(0);
      Interval domainOne = opt.getInterval(varOne);
      if (vars.size() > 1) {
        Formula varTwo = vars.get(1);
        Interval domainTwo = opt.getInterval(varTwo);
        if (!domainTwo.isSet()) {
          return TraversalProcess.CONTINUE;
        }
        if (operator == Operators.LTE) {
          domainOne.setUpperBound(valueOne);
          processDeclaration(varOne, varTwo, Operators.LTE);
        } else if (operator == Operators.LT) {
          domainOne.setUpperBound(valueOne - 1);
          processDeclaration(varOne, varTwo, Operators.LT);
        } else if (operator == Operators.GTE) {
          domainOne.setLowerBound(valueOne);
          processDeclaration(varOne, varTwo, Operators.GTE);
        } else if (operator == Operators.GT) {
          domainOne.setLowerBound(valueOne);
          processDeclaration(varOne, varTwo, Operators.GT);
        }
      }
    }

    if (argOne == Argtypes.CONST && argTwo == Argtypes.VAR) {
      String name = format(variableOne.toString());
      int valueOne = Integer.parseInt(name);
      Interval domainTwo = opt.getInterval(variableTwo);
      if (valueOne > domainTwo.getUpperBound() || valueOne < domainTwo.getLowerBound()) {
        return TraversalProcess.CONTINUE;
      }
      if (operator == Operators.LTE) {
        domainTwo.setLowerBound(valueOne);
      }
      if (operator == Operators.GTE) {
        domainTwo.setUpperBound(valueOne);
      }
      if (operator == Operators.LT) {
        domainTwo.setLowerBound(valueOne + 1);
      }
      if (operator == Operators.GT) {
        domainTwo.setUpperBound(valueOne - 1);
      }
    }

    return TraversalProcess.CONTINUE;
  }

  /*
  parses a formula containing an arithmetic relation as an operator
  */
  private TraversalProcess processDeclaration(
      Formula variableOne, Formula variableTwo, Operators op) {
    Function func = readFromBuffer();
    FunctionDeclarationKind dec = func.declaration;

    if (getFormulaType(variableOne) == Argtypes.FUNC
        && getFormulaType(variableTwo) == Argtypes.CONST) {
      List<Formula> args = func.args;
      List<Formula> vars = digDeeper(args);
      Formula variable = vars.get(0);
      Interval domain = opt.getInterval(variable);
      String name = format(variableTwo.toString());
      Integer valueTwo = Integer.parseInt(name);
      if (dec == FunctionDeclarationKind.LTE) {
        Integer upperBound = domain.getUpperBound();
        if (op == Operators.ADD) {
          domain.setUpperBound(upperBound - valueTwo);
        } else if (op == Operators.SUB) {
          domain.setUpperBound(upperBound + valueTwo);
        }
      } else if (dec == FunctionDeclarationKind.GTE) {
        Integer lowerBound = domain.getLowerBound();
        if (op == Operators.ADD) {
          domain.setLowerBound(lowerBound - valueTwo);
        } else if (op == Operators.SUB) {
          domain.setLowerBound(lowerBound + valueTwo);
        }
      } else if (dec == FunctionDeclarationKind.LT) {
        Integer upperBound = domain.getUpperBound();
        if (op == Operators.ADD) {
          domain.setUpperBound(upperBound - valueTwo - 1);
        } else if (op == Operators.SUB) {
          domain.setUpperBound(upperBound + valueTwo - 1);
        }
      } else if (dec == FunctionDeclarationKind.GT) {
        Integer lowerBound = domain.getLowerBound();
        if (op == Operators.ADD) {
          domain.setLowerBound(lowerBound - valueTwo + 1);
        } else if (op == Operators.SUB) {
          domain.setLowerBound(lowerBound + valueTwo + 1);
        }
      }
    }

    if (getFormulaType(variableOne) == Argtypes.VAR
        && getFormulaType(variableTwo) == Argtypes.CONST) {
      String name = format(variableTwo.toString());
      Integer valueTwo = Integer.parseInt(name);
      Interval domainOne = opt.getInterval(variableOne);
      if (dec == FunctionDeclarationKind.LTE) {
        Integer upperBound = domainOne.getUpperBound();
        if (op == Operators.ADD) {
          domainOne.setUpperBound(upperBound - valueTwo);
        } else if (op == Operators.SUB) {
          domainOne.setUpperBound(upperBound + valueTwo);
        }
      } else if (dec == FunctionDeclarationKind.GTE) {
        Integer lowerBound = domainOne.getLowerBound();
        if (op == Operators.ADD) {
          domainOne.setLowerBound(lowerBound - valueTwo);
        } else if (op == Operators.SUB) {
          domainOne.setLowerBound(lowerBound + valueTwo);
        }
      }
      if (dec == FunctionDeclarationKind.LT) {
        Integer upperBound = domainOne.getUpperBound();
        if (op == Operators.ADD) {
          domainOne.setUpperBound(upperBound - valueTwo - 1);
        } else if (op == Operators.SUB) {
          domainOne.setUpperBound(upperBound + valueTwo - 1);
        }
      }
      if (dec == FunctionDeclarationKind.GT) {
        Integer lowerBound = domainOne.getLowerBound();
        if (op == Operators.ADD) {
          domainOne.setLowerBound(lowerBound - valueTwo + 1);
        } else if (op == Operators.SUB) {
          domainOne.setLowerBound(lowerBound + valueTwo + 1);
        }
      }
    }

    if (getFormulaType(variableOne) == Argtypes.CONST
        && getFormulaType(variableTwo) == Argtypes.VAR) {
      String name = format(variableOne.toString());
      Integer valueOne = Integer.parseInt(name);
      Interval domainTwo = opt.getInterval(variableTwo);
      if (dec == FunctionDeclarationKind.LTE) {
        Integer upperBound = domainTwo.getUpperBound();
        if (op == Operators.ADD) {
          domainTwo.setUpperBound(upperBound - valueOne);
        } else if (op == Operators.SUB) {
          domainTwo.setLowerBound(upperBound - valueOne);
        }
      }
      if (dec == FunctionDeclarationKind.GTE) {
        Integer lowerBound = domainTwo.getLowerBound();
        if (op == Operators.ADD) {
          domainTwo.setLowerBound(lowerBound - valueOne);
        } else if (op == Operators.SUB) {
          domainTwo.setLowerBound(lowerBound + valueOne);
        }
      }
      if (dec == FunctionDeclarationKind.LT) {
        Integer upperBound = domainTwo.getUpperBound();
        if (op == Operators.ADD) {
          domainTwo.setUpperBound(upperBound - valueOne - 1);
        } else if (op == Operators.SUB) {
          domainTwo.setLowerBound(upperBound - valueOne - 1);
        }
      }
      if (dec == FunctionDeclarationKind.GT) {
        Integer lowerBound = domainTwo.getLowerBound();
        if (op == Operators.ADD) {
          domainTwo.setLowerBound(lowerBound - valueOne + 1);
        } else if (op == Operators.SUB) {
          domainTwo.setLowerBound(lowerBound + valueOne + 1);
        }
      }
    }

    if (getFormulaType(variableOne) == Argtypes.CONST
        && getFormulaType(variableTwo) == Argtypes.FUNC) {
      String name = format(variableOne.toString());
      Integer valueOne = Integer.parseInt(name);
      List<Formula> args = func.args;
      List<Formula> vars = digDeeper(args);
      Formula variable = vars.get(0);
      Interval domain = opt.getInterval(variable);
      if (dec == FunctionDeclarationKind.LTE) {
        Integer upperBound = domain.getUpperBound();
        if (op == Operators.ADD) {
          domain.setUpperBound(upperBound - valueOne);
        } else if (op == Operators.SUB) {
          domain.setUpperBound(upperBound + valueOne);
        }
      }
      if (dec == FunctionDeclarationKind.GTE) {
        Integer lowerBound = domain.getLowerBound();
        if (op == Operators.ADD) {
          domain.setLowerBound(lowerBound - valueOne);
        } else if (op == Operators.SUB) {
          domain.setLowerBound(lowerBound + valueOne);
        }
      }
      if (dec == FunctionDeclarationKind.LT) {
        Integer upperBound = domain.getUpperBound();
        if (op == Operators.ADD) {
          domain.setUpperBound(upperBound - valueOne + 1);
        } else if (op == Operators.SUB) {
          domain.setUpperBound(upperBound + valueOne + 1);
        }
      }
      if (dec == FunctionDeclarationKind.GT) {
        Integer lowerBound = domain.getLowerBound();
        if (op == Operators.ADD) {
          domain.setLowerBound(lowerBound - valueOne - 1);
        } else if (op == Operators.SUB) {
          domain.setLowerBound(lowerBound + valueOne - 1);
        }
      }
    }

    return TraversalProcess.CONTINUE;
  }


  /*
  removes parantheses and spaces from variable-name so that it can be parsed as a constant
   */
  private String format(String name) {
    name = name.replaceAll("[()]", "");
    name = name.replaceAll("\\s", "");
    return name;
  }
}
