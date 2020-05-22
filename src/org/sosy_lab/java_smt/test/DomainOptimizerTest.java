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

package org.sosy_lab.java_smt.test;

import java.util.Set;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.ConfigurationBuilder;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverException;
import org.sosy_lab.java_smt.domain_optimization.BasicDomainOptimizer;
import org.sosy_lab.java_smt.domain_optimization.DomainOptimizer;
import org.sosy_lab.java_smt.domain_optimization.DomainOptimizerProverEnvironment;
import org.sosy_lab.java_smt.domain_optimization.DomainOptimizerSolverContext;
import org.sosy_lab.java_smt.domain_optimization.SolutionSet;

public class DomainOptimizerTest {

  public static void main(String[] args) throws InvalidConfigurationException,
                                                InterruptedException, SolverException {
    ConfigurationBuilder builder = Configuration.builder();
    builder.setOption("useDomainOptimizer", "true");
    Configuration config = builder.build();

    LogManager logger = BasicLogManager.create(config);
    ShutdownManager shutdown = ShutdownManager.create();

    SolverContext delegate = SolverContextFactory.createSolverContext(
        config, logger, shutdown.getNotifier(), Solvers.SMTINTERPOL);
    DomainOptimizerProverEnvironment wrapped = new DomainOptimizerProverEnvironment(delegate);
    
    FormulaManager fmgr = delegate.getFormulaManager();
    IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();

    IntegerFormula x = imgr.makeVariable("x"),
        y = imgr.makeVariable("y");
    BooleanFormula query =
        imgr.equal(
            imgr.add(x, imgr.makeNumber(2)), y);

    BooleanFormula constraint_1 =
        imgr.lessOrEquals(x, imgr.makeNumber(7));

    BooleanFormula constraint_2 =
        imgr.lessOrEquals(imgr.makeNumber(4), x);

    BooleanFormula constraint_3 =
        imgr.lessOrEquals(
            imgr.subtract(y,imgr.makeNumber(3)),imgr.makeNumber(7));

    BooleanFormula constraint_4 =
        imgr.greaterOrEquals(
            imgr.multiply(y,imgr.makeNumber(3)), imgr.makeNumber(3));

    DomainOptimizer optimizer = new BasicDomainOptimizer((DomainOptimizerSolverContext) delegate,
        wrapped, query);

    optimizer.pushQuery(query);
    optimizer.visit(query);
    optimizer.pushConstraint(constraint_1);
    optimizer.pushConstraint(constraint_2);
    optimizer.pushConstraint(constraint_3);
    optimizer.pushConstraint(constraint_4);
    boolean isUnsat = optimizer.isUnsat();
    System.out.println(isUnsat);

    Set<Formula> usedVariables = optimizer.getVariables();
    for (Formula var : usedVariables) {
      System.out.println(var.toString());
      SolutionSet domain = optimizer.getSolutionSet(var);
      domain.show();
    }
  }
}
