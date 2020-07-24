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

  // forms tuples of variables along with their domains
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
            opt.addVariable(var);

            return TraversalProcess.CONTINUE;
          }
        };
    fmgr.visitRecursively(f, nameExtractor);
  }

  public boolean isCaterpillar(Formula f) {
    FormulaManager fmgr = delegate.getFormulaManager();
    FormulaVisitor<Boolean> isCaterpillar =
        new FormulaVisitor<>() {
          @Override
          public Boolean visitFreeVariable(Formula f, String name) {
            return null;
          }

          @Override
          public Boolean visitBoundVariable(Formula f, int deBruijnIdx) {
            return null;
          }

          @Override
          public Boolean visitConstant(Formula f, Object value) {
            return null;
          }

          @Override
          public Boolean visitFunction(
              Formula f, List<Formula> args, FunctionDeclaration<?> functionDeclaration) {
            return getFormulaType(args.get(0)) != argTypes.FUNC
                || getFormulaType(args.get(1)) != argTypes.FUNC;
          }

          @Override
          public Boolean visitQuantifier(
              BooleanFormula f,
              Quantifier quantifier,
              List<Formula> boundVariables,
              BooleanFormula body) {
            return null;
          }
        };
    return fmgr.visit(f, isCaterpillar);
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
              Formula f, List<Formula> pArgs, FunctionDeclaration<?> pFunctionDeclaration) {
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

  public void setSubstitutionFlag(boolean isSubstituted) {
    this.isSubstituted = isSubstituted;
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

  public void foldFunction(Formula f) {
    final int[] i = {0};
    final FunctionDeclarationKind[] declaration = new FunctionDeclarationKind[1];
    FormulaManager fmgr = delegate.getFormulaManager();
    FormulaVisitor<TraversalProcess> folder =
        new FormulaVisitor<>() {
          @Override
          public TraversalProcess visitFreeVariable(Formula f, String name) {
            SolutionSet domain = opt.getSolutionSet(f);
            Function func = readFromBuffer();
            FunctionDeclarationKind dec = func.declaration;
            switch (dec) {
              case LTE:
                domain.setUpperBound(i[0]);
                break;
              case LT:
                domain.setUpperBound(i[0] - 1);
                break;
              case GTE:
                domain.setLowerBound(i[0]);
                break;
              case GT:
                domain.setLowerBound(i[0] + 1);
                break;
              default:
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
            int val = Integer.parseInt(value.toString());
            i[0] += val;
            return TraversalProcess.CONTINUE;
          }

          @Override
          public TraversalProcess visitFunction(
              Formula f, List<Formula> args, FunctionDeclaration<?> functionDeclaration) {
            FunctionDeclarationKind dec = functionDeclaration.getKind();
            if (dec == FunctionDeclarationKind.LTE
                || dec == FunctionDeclarationKind.LT
                || dec == FunctionDeclarationKind.GTE
                || dec == FunctionDeclarationKind.GT) {
              Function func = new Function(args, dec);
              putToBuffer(func);
            }
            if (dec == FunctionDeclarationKind.ADD
                || dec == FunctionDeclarationKind.SUB
                || dec == FunctionDeclarationKind.MUL
                || dec == FunctionDeclarationKind.DIV) {
              i[0] = 0;
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
    fmgr.visitRecursively(f, folder);
  }

  public int countVariables(Formula f) {
    return delegate.getFormulaManager().extractVariables(f).size();
  }

  public void solveOperations(Formula f) {
    FormulaManager fmgr = delegate.getFormulaManager();
    FormulaVisitor<TraversalProcess> solver =
        new FormulaVisitor<>() {

          @Override
          public TraversalProcess visitFreeVariable(Formula f, String name) {
            return TraversalProcess.CONTINUE;
          }

          @Override
          public TraversalProcess visitBoundVariable(Formula f, int deBruijnIdx) {
            return TraversalProcess.CONTINUE;
          }

          @Override
          public TraversalProcess visitConstant(Formula f, Object value) {
            return TraversalProcess.CONTINUE;
          }

          @Override
          public TraversalProcess visitFunction(
              Formula f, List<Formula> args, FunctionDeclaration<?> functionDeclaration) {
            List<Formula> vars = digDeeper(args);
            FunctionDeclarationKind dec = functionDeclaration.getKind();
            if (dec == FunctionDeclarationKind.ADD
                || dec == FunctionDeclarationKind.MUL
                || dec == FunctionDeclarationKind.DIV
                || dec == FunctionDeclarationKind.SUB) {
              if (vars.size() == 0) {
                List<Formula> constants = digDeeperForConstants(args);
                Formula substitute =
                    processOperation(
                        constants.get(0), constants.get(1), functionDeclaration.getKind());
                Map<Formula, Formula> substitution = new HashMap<>();
                substitution.put(f, substitute);
                setSubstitution(substitution);
                setSubstitutionFlag(true);
              }
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
    fmgr.visitRecursively(f, solver);
  }

  public Formula replaceVariablesWithSolutionSets(Formula f) {
    FormulaManager fmgr = delegate.getFormulaManager();
    IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
    FormulaVisitor<Formula> replacer =
        new FormulaVisitor<>() {
          @Override
          public Formula visitFreeVariable(Formula f, String name) {
            SolutionSet domain = opt.getSolutionSet(f);
            Function func = readFromBuffer();
            FunctionDeclarationKind dec = func.declaration;
            if (dec == FunctionDeclarationKind.LTE) {
              IntegerFormula upperBound = imgr.makeNumber(domain.getUpperBound());
              return upperBound;
            } else if (dec == FunctionDeclarationKind.LT) {
              IntegerFormula upperBound = imgr.makeNumber(domain.getUpperBound() + 1);
              return upperBound;
            } else if (dec == FunctionDeclarationKind.GTE) {
              IntegerFormula lowerBound = imgr.makeNumber(domain.getLowerBound());
              return lowerBound;
            } else if (dec == FunctionDeclarationKind.GT) {
              IntegerFormula lowerBound = imgr.makeNumber(domain.getLowerBound() - 1);
              return lowerBound;
            }
            return null;
          }

          @Override
          public Formula visitBoundVariable(Formula f, int deBruijnIdx) {
            return null;
          }

          @Override
          public Formula visitConstant(Formula f, Object value) {
            return null;
          }

          @Override
          public Formula visitFunction(
              Formula f, List<Formula> args, FunctionDeclaration<?> functionDeclaration) {
            FunctionDeclarationKind dec = functionDeclaration.getKind();
            if (dec == FunctionDeclarationKind.LTE
                || dec == FunctionDeclarationKind.LT
                || dec == FunctionDeclarationKind.GTE
                || dec == FunctionDeclarationKind.GT) {
              for (Formula arg : args) {
                if (getFormulaType(arg) == argTypes.FUNC) {
                  arg = digDeeper(args).get(0);
                }
                Function func = new Function(args, dec);
                putToBuffer(func);
                Formula substitute = visitFreeVariable(arg, arg.toString());
                Map<Formula, Formula> substitution = new HashMap<>();
                substitution.put(arg, substitute);
                f = fmgr.substitute(f, substitution);
              }
            }
            return f;
          }

          @Override
          public Formula visitQuantifier(
              BooleanFormula f,
              Quantifier quantifier,
              List<Formula> boundVariables,
              BooleanFormula body) {
            return null;
          }
        };
    f = fmgr.visit(f, replacer);
    return f;
  }

  public void processConstraint(Formula f) {
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
            Formula var_1 = pArgs.get(0);
            Formula var_2 = pArgs.get(1);
            // SolutionSets of the variables are adjusted according to the function-declaration
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
                return processDeclaration(var_1, var_2, operators.ADD, pArgs);

              case "SUB":
                return processDeclaration(var_1, var_2, operators.SUB, pArgs);

              case "MUL":
                return processDeclaration(var_1, var_2, operators.MULT, pArgs);

              case "DIV":
                return processDeclaration(var_1, var_2, operators.DIV, pArgs);
            }
            return TraversalProcess.CONTINUE;
          }

          @Override
          public TraversalProcess visitConstant(Formula f, Object value) {
            IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
            String name = format(value.toString());
            IntegerFormula constant = imgr.makeNumber(name);
            SolutionSet domain = new SolutionSet();
            opt.addDomain(constant, domain);
            return TraversalProcess.CONTINUE;
          }
        };
    fmgr.visitRecursively(f, constraintExtractor);
  }

  /*
  performs depth-search on a function in order to retrieve variables { f(x,y) -> x,y }
   */
  public List<Formula> digDeeper(List<Formula> args) {
    List<Formula> vars = new ArrayList<>();
    for (Formula var : args) {
      if (getFormulaType(var) == argTypes.VAR) {
        opt.addVariable(var);
        vars.add(var);
      } else if (getFormulaType(var) == argTypes.FUNC) {
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
      if (getFormulaType(var) == argTypes.CONST) {
        constants.add(var);
      } else if (getFormulaType(var) == argTypes.FUNC) {
        Function func = readFromBuffer();
        constants = digDeeperForConstants(func.args);
      }
    }
    return constants;
  }

  /*
  parses a formula containing a numeral relation as an operator
   */
  public TraversalProcess adjustBounds(
      Formula var_1,
      Formula var_2,
      operators operator,
      List<Formula> pArgs,
      FunctionDeclaration<?> pFunctionDeclaration) {

    argTypes arg_1 = getFormulaType(var_1);
    argTypes arg_2 = getFormulaType(var_2);

    if (arg_1 == argTypes.FUNC && arg_2 == argTypes.VAR) {
      SolutionSet domain_2 = opt.getSolutionSet(var_2);
      SolutionSet domain_1;
      List<Formula> args = functionBuffer.args;
      Function func = new Function(pArgs, pFunctionDeclaration.getKind());
      putToBuffer(func);
      List<Formula> vars = digDeeper(args);
      Formula variable = vars.get(0);
      domain_1 = opt.getSolutionSet(variable);
      if (operator == operators.LTE) {
        domain_1.setUpperBound(domain_2.getUpperBound());
      } else if (operator == operators.LT) {
        domain_1.setUpperBound(domain_2.getUpperBound() - 1);
      } else if (operator == operators.GTE) {
        domain_1.setLowerBound(domain_2.getUpperBound());
      } else if (operator == operators.GT) {
        domain_1.setLowerBound(domain_2.getUpperBound() + 1);
      }
    }

    if (arg_1 == argTypes.FUNC && arg_2 == argTypes.CONST) {
      String name = format(var_2.toString());
      Integer val_2 = Integer.parseInt(name);
      List<Formula> args = functionBuffer.args;
      Function func = new Function(pArgs, pFunctionDeclaration.getKind());
      List<Formula> vars = digDeeper(args);
      Formula variable_1 = vars.get(0);
      SolutionSet domain_1 = opt.getSolutionSet(variable_1);
      if (vars.size() > 1) {
        Formula variable_2 = vars.get(1);
        SolutionSet domain_2 = opt.getSolutionSet(variable_2);
        if (!domain_2.isSet()) {
          return TraversalProcess.CONTINUE;
        }
        if (operator == operators.LTE) {
          domain_1.setUpperBound(val_2);
          processDeclaration(variable_1, variable_2, operators.LTE, vars);
        } else if (operator == operators.LT) {
          domain_1.setUpperBound(val_2 - 1);
          processDeclaration(variable_1, variable_2, operators.LT, vars);
        } else if (operator == operators.GTE) {
          domain_1.setLowerBound(val_2);
          processDeclaration(variable_1, variable_2, operators.GTE, vars);
        } else if (operator == operators.GT) {
          domain_1.setLowerBound(val_2);
          processDeclaration(variable_1, variable_2, operators.GT, vars);
        }
      } else {
        if (operator == operators.LTE) {
          domain_1.setUpperBound(val_2);
        } else if (operator == operators.LT) {
          domain_1.setUpperBound(val_2 - 1);
        } else if (operator == operators.GTE) {
          domain_1.setLowerBound(val_2);
        } else if (operator == operators.GT) {
          domain_1.setLowerBound(val_2 + 1);
        }
      }
      putToBuffer(func);
    }

    if (arg_1 == argTypes.VAR && arg_2 == argTypes.FUNC) {
      SolutionSet domain_1 = opt.getSolutionSet(var_1);
      List<Formula> args = functionBuffer.args;
      Function func = new Function(pArgs, pFunctionDeclaration.getKind());
      putToBuffer(func);
      List<Formula> vars = digDeeper(args);
      Formula variable = vars.get(1);
      SolutionSet domain_2 = opt.getSolutionSet(variable);
      if (operator == operators.LTE) {
        domain_1.setUpperBound(domain_2.getUpperBound());
      } else if (operator == operators.LT) {
        domain_1.setUpperBound(domain_2.getUpperBound() - 1);
      } else if (operator == operators.GTE) {
        domain_1.setLowerBound(domain_2.getUpperBound());
      } else if (operator == operators.GT) {
        domain_1.setLowerBound(domain_2.getUpperBound() + 1);
      }
    }

    if (arg_1 == argTypes.VAR && arg_2 == argTypes.VAR) {
      SolutionSet domain_1 = opt.getSolutionSet(var_1);
      SolutionSet domain_2 = opt.getSolutionSet(var_2);
      if (operator == operators.LTE) {
        domain_1.setUpperBound(domain_2.getUpperBound());
      } else if (operator == operators.LT) {
        domain_1.setUpperBound(domain_2.getUpperBound() - 1);
      } else if (operator == operators.GTE) {
        domain_1.setLowerBound(domain_2.getUpperBound());
      } else if (operator == operators.GT) {
        domain_1.setLowerBound(domain_2.getUpperBound() + 1);
      }
    }

    if (arg_1 == argTypes.VAR && arg_2 == argTypes.CONST) {
      String name = format(var_2.toString());
      Integer val_2 = Integer.parseInt(name);
      SolutionSet domain_1 = opt.getSolutionSet(var_1);
      if (operator == operators.LTE) {
        domain_1.setUpperBound(val_2);
      } else if (operator == operators.GTE) {
        domain_1.setLowerBound(val_2);
      }
    }

    if (arg_1 == argTypes.CONST && arg_2 == argTypes.FUNC) {
      String name = format(var_1.toString());
      Integer val_1 = Integer.parseInt(name);
      List<Formula> args = functionBuffer.args;
      List<Formula> vars = digDeeper(args);
      Formula variable_1 = vars.get(0);
      SolutionSet domain_1 = opt.getSolutionSet(variable_1);
      if (vars.size() > 1) {
        Formula variable_2 = vars.get(1);
        SolutionSet domain_2 = opt.getSolutionSet(variable_2);
        if (!domain_2.isSet()) {
          return TraversalProcess.CONTINUE;
        }
        if (operator == operators.LTE) {
          domain_1.setUpperBound(val_1);
          processDeclaration(variable_1, variable_2, operators.LTE, vars);
        } else if (operator == operators.LT) {
          domain_1.setUpperBound(val_1 - 1);
          processDeclaration(variable_1, variable_2, operators.LT, vars);
        } else if (operator == operators.GTE) {
          domain_1.setLowerBound(val_1);
          processDeclaration(variable_1, variable_2, operators.GTE, vars);
        } else if (operator == operators.GT) {
          domain_1.setLowerBound(val_1);
          processDeclaration(variable_1, variable_2, operators.GT, vars);
        }
      }
    }

    if (arg_1 == argTypes.CONST && arg_2 == argTypes.VAR) {
      String name = format(var_1.toString());
      int val_1 = Integer.parseInt(name);
      SolutionSet domain_2 = opt.getSolutionSet(var_2);
      if (val_1 > domain_2.getUpperBound() || val_1 < domain_2.getLowerBound()) {
        return TraversalProcess.CONTINUE;
      }
      if (operator == operators.LTE) {
        domain_2.setLowerBound(val_1);
      }
      if (operator == operators.GTE) {
        domain_2.setUpperBound(val_1);
      }
      if (operator == operators.LT) {
        domain_2.setLowerBound(val_1 + 1);
      }
      if (operator == operators.GT) {
        domain_2.setUpperBound(val_1 - 1);
      }
    }

    return TraversalProcess.CONTINUE;
  }

  /*
  parses a formula containing an arithmetic relation as an operator
  */
  public TraversalProcess processDeclaration(
      Formula var_1, Formula var_2, operators op, List<Formula> pArgs) {
    Function func = readFromBuffer();
    FunctionDeclarationKind dec = func.declaration;

    if (getFormulaType(var_1) == argTypes.FUNC && getFormulaType(var_2) == argTypes.CONST) {
      List<Formula> args = func.args;
      List<Formula> vars = digDeeper(args);
      Formula variable = vars.get(0);
      SolutionSet domain = opt.getSolutionSet(variable);
      String name = format(var_2.toString());
      Integer val_2 = Integer.parseInt(name);
      if (dec == FunctionDeclarationKind.LTE) {
        Integer upperBound = domain.getUpperBound();
        if (op == operators.ADD) {
          domain.setUpperBound(upperBound - val_2);
        } else if (op == operators.SUB) {
          domain.setUpperBound(upperBound + val_2);
        } else if (op == operators.MULT) {
          domain.setUpperBound(upperBound / val_2);
        } else if (op == operators.DIV) {
          domain.setUpperBound(upperBound * val_2);
        }
      } else if (dec == FunctionDeclarationKind.GTE) {
        Integer lowerBound = domain.getLowerBound();
        if (op == operators.ADD) {
          domain.setLowerBound(lowerBound - val_2);
        } else if (op == operators.SUB) {
          domain.setLowerBound(lowerBound + val_2);
        } else if (op == operators.MULT) {
          domain.setLowerBound(lowerBound / val_2);
        } else if (op == operators.DIV) {
          domain.setLowerBound(lowerBound * val_2);
        }
      } else if (dec == FunctionDeclarationKind.LT) {
        Integer upperBound = domain.getUpperBound();
        if (op == operators.ADD) {
          domain.setUpperBound(upperBound - val_2 - 1);
        } else if (op == operators.SUB) {
          domain.setUpperBound(upperBound + val_2 - 1);
        } else if (op == operators.MULT) {
          domain.setUpperBound(upperBound / val_2 - 1);
        } else if (op == operators.DIV) {
          domain.setUpperBound(upperBound * val_2 - 1);
        }
      } else if (dec == FunctionDeclarationKind.GT) {
        Integer lowerBound = domain.getLowerBound();
        if (op == operators.ADD) {
          domain.setLowerBound(lowerBound - val_2 + 1);
        } else if (op == operators.SUB) {
          domain.setLowerBound(lowerBound + val_2 + 1);
        } else if (op == operators.MULT) {
          domain.setLowerBound(lowerBound / val_2 + 1);
        } else if (op == operators.DIV) {
          domain.setLowerBound(lowerBound * val_2 + 1);
        }
      }
    }

    if (getFormulaType(var_1) == argTypes.VAR && getFormulaType(var_2) == argTypes.CONST) {
      String name = format(var_2.toString());
      Integer val_2 = Integer.parseInt(name);
      SolutionSet domain_1 = opt.getSolutionSet(var_1);
      if (dec == FunctionDeclarationKind.LTE) {
        Integer upperBound = domain_1.getUpperBound();
        if (op == operators.ADD) {
          domain_1.setUpperBound(upperBound - val_2);
        } else if (op == operators.SUB) {
          domain_1.setUpperBound(upperBound + val_2);
        } else if (op == operators.MULT) {
          domain_1.setUpperBound(upperBound / val_2);
        } else if (op == operators.DIV) {
          domain_1.setLowerBound(val_2 / upperBound);
        }
      } else if (dec == FunctionDeclarationKind.GTE) {
        Integer lowerBound = domain_1.getLowerBound();
        if (op == operators.ADD) {
          domain_1.setLowerBound(lowerBound - val_2);
        } else if (op == operators.SUB) {
          domain_1.setLowerBound(lowerBound + val_2);
        } else if (op == operators.MULT) {
          domain_1.setLowerBound(lowerBound / val_2);
        } else if (op == operators.DIV) {
          domain_1.setLowerBound(lowerBound * val_2);
        }
      }
      if (dec == FunctionDeclarationKind.LT) {
        Integer upperBound = domain_1.getUpperBound();
        if (op == operators.ADD) {
          domain_1.setUpperBound(upperBound - val_2 - 1);
        } else if (op == operators.SUB) {
          domain_1.setUpperBound(upperBound + val_2 - 1);
        } else if (op == operators.MULT) {
          domain_1.setUpperBound(upperBound / val_2 - 1);
        } else if (op == operators.DIV) {
          domain_1.setLowerBound(val_2 / upperBound - 1);
        }
      }
      if (dec == FunctionDeclarationKind.GT) {
        Integer lowerBound = domain_1.getLowerBound();
        if (op == operators.ADD) {
          domain_1.setLowerBound(lowerBound - val_2 + 1);
        } else if (op == operators.SUB) {
          domain_1.setLowerBound(lowerBound + val_2 + 1);
        } else if (op == operators.MULT) {
          domain_1.setLowerBound(lowerBound / val_2 + 1);
        } else if (op == operators.DIV) {
          domain_1.setLowerBound(lowerBound * val_2 + 1);
        }
      }
    }

    if (getFormulaType(var_1) == argTypes.CONST && getFormulaType(var_2) == argTypes.VAR) {
      String name = format(var_1.toString());
      Integer val_1 = Integer.parseInt(name);
      SolutionSet domain_2 = opt.getSolutionSet(var_2);
      if (dec == FunctionDeclarationKind.LTE) {
        Integer upperBound = domain_2.getUpperBound();
        if (op == operators.ADD) {
          domain_2.setUpperBound(upperBound - val_1);
        } else if (op == operators.SUB) {
          domain_2.setLowerBound(upperBound - val_1);
        } else if (op == operators.MULT) {
          domain_2.setUpperBound(upperBound / val_1);
        } else if (op == operators.DIV) {
          domain_2.setLowerBound(val_1 / upperBound);
        }
      }
      if (dec == FunctionDeclarationKind.GTE) {
        Integer lowerBound = domain_2.getLowerBound();
        if (op == operators.ADD) {
          domain_2.setLowerBound(lowerBound - val_1);
        } else if (op == operators.SUB) {
          domain_2.setLowerBound(lowerBound + val_1);
        } else if (op == operators.MULT) {
          domain_2.setLowerBound(lowerBound / val_1);
        } else if (op == operators.DIV) {
          domain_2.setLowerBound(val_1 / lowerBound);
        }
      }
      if (dec == FunctionDeclarationKind.LT) {
        Integer upperBound = domain_2.getUpperBound();
        if (op == operators.ADD) {
          domain_2.setUpperBound(upperBound - val_1 - 1);
        } else if (op == operators.SUB) {
          domain_2.setLowerBound(upperBound - val_1 - 1);
        } else if (op == operators.MULT) {
          domain_2.setUpperBound(upperBound / val_1 - 1);
        } else if (op == operators.DIV) {
          domain_2.setLowerBound(val_1 / upperBound - 1);
        }
      }
      if (dec == FunctionDeclarationKind.GT) {
        Integer lowerBound = domain_2.getLowerBound();
        if (op == operators.ADD) {
          domain_2.setLowerBound(lowerBound - val_1 + 1);
        } else if (op == operators.SUB) {
          domain_2.setLowerBound(lowerBound + val_1 + 1);
        } else if (op == operators.MULT) {
          domain_2.setLowerBound(lowerBound / val_1 + 1);
        } else if (op == operators.DIV) {
          domain_2.setLowerBound(val_1 / lowerBound + 1);
        }
      }
    }

    if (getFormulaType(var_1) == argTypes.CONST && getFormulaType(var_2) == argTypes.FUNC) {
      String name = format(var_1.toString());
      Integer val_1 = Integer.parseInt(name);
      List<Formula> args = func.args;
      List<Formula> vars = digDeeper(args);
      Formula variable = vars.get(0);
      SolutionSet domain = opt.getSolutionSet(variable);
      if (dec == FunctionDeclarationKind.LTE) {
        Integer upperBound = domain.getUpperBound();
        if (op == operators.ADD) {
          domain.setUpperBound(upperBound - val_1);
        } else if (op == operators.SUB) {
          domain.setUpperBound(upperBound + val_1);
        } else if (op == operators.MULT) {
          domain.setUpperBound(upperBound / val_1);
        } else if (op == operators.DIV) {
          domain.setUpperBound(val_1 / upperBound);
        }
      }
      if (dec == FunctionDeclarationKind.GTE) {
        Integer lowerBound = domain.getLowerBound();
        if (op == operators.ADD) {
          domain.setLowerBound(lowerBound - val_1);
        } else if (op == operators.SUB) {
          domain.setLowerBound(lowerBound + val_1);
        } else if (op == operators.MULT) {
          domain.setLowerBound(lowerBound / val_1);
        } else if (op == operators.DIV) {
          domain.setLowerBound(lowerBound * val_1);
        }
      }
      if (dec == FunctionDeclarationKind.LT) {
        Integer upperBound = domain.getUpperBound();
        if (op == operators.ADD) {
          domain.setUpperBound(upperBound - val_1 + 1);
        } else if (op == operators.SUB) {
          domain.setUpperBound(upperBound + val_1 + 1);
        } else if (op == operators.MULT) {
          domain.setUpperBound(upperBound / val_1 + 1);
        } else if (op == operators.DIV) {
          domain.setUpperBound(val_1 / upperBound + 1);
        }
      }
      if (dec == FunctionDeclarationKind.GT) {
        Integer lowerBound = domain.getLowerBound();
        if (op == operators.ADD) {
          domain.setLowerBound(lowerBound - val_1 - 1);
        } else if (op == operators.SUB) {
          domain.setLowerBound(lowerBound + val_1 - 1);
        } else if (op == operators.MULT) {
          domain.setLowerBound(lowerBound / val_1 - 1);
        } else if (op == operators.DIV) {
          domain.setLowerBound(lowerBound * val_1 - 1);
        }
      }
    }

    return TraversalProcess.CONTINUE;
  }

  /*
  solves operation containing two constants as arguments
   */
  public IntegerFormula processOperation(
      Formula var_1, Formula var_2, FunctionDeclarationKind declaration) {
    FormulaManager fmgr = delegate.getFormulaManager();
    IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
    String name_1 = format(var_1.toString());
    Integer val_1 = Integer.parseInt(name_1);
    String name_2 = format(var_2.toString());
    Integer val_2 = Integer.parseInt(name_2);
    int result;
    switch (declaration.toString()) {
      case "ADD":
        result = val_1 + val_2;
        break;

      case "SUB":
        result = val_1 - val_2;
        break;

      case "MUL":
        result = val_1 * val_2;
        break;

      case "DIV":
        result = val_1 / val_2;
        break;

      default:
        throw new IllegalStateException("Unexpected value: " + declaration.toString());
    }
    return imgr.makeNumber(result);
  }

  /*
  removes parantheses and spaces from variable-name so that it can be parsed as a constant
   */
  public String format(String name) {
    name = name.replaceAll("[()]", "");
    name = name.replaceAll("\\s", "");
    return name;
  }
}
