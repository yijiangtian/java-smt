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

import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import java.util.logging.LogManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.SolverException;
import org.sosy_lab.java_smt.domain_optimization.DomainOptimizerProverEnvironment;
import org.sosy_lab.java_smt.domain_optimization.DomainOptimizerSolverContext;
import org.sosy_lab.java_smt.domain_optimization.SolutionSet;

@RunWith(Parameterized.class)
public class DomainOptimizerTest {

  public SolutionSet[] initializeTest()
      throws InvalidConfigurationException, InterruptedException, SolverException {

    Configuration config = Configuration.builder().setOption("useDomainOptimizer", "true").build();
    LogManager logger = (LogManager) BasicLogManager.create(config);
    ShutdownManager shutdown = ShutdownManager.create();

    DomainOptimizerSolverContext delegate =
        (DomainOptimizerSolverContext) SolverContextFactory.createSolverContext(
        config, (org.sosy_lab.common.log.LogManager) logger, shutdown.getNotifier(), Solvers.SMTINTERPOL);

    FormulaManager fmgr = delegate.getFormulaManager();
    IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
    DomainOptimizerProverEnvironment env = new DomainOptimizerProverEnvironment(delegate);

    IntegerFormula x = imgr.makeVariable("x"),
        y = imgr.makeVariable("y"),
        z = imgr.makeVariable("z");

    BooleanFormula constraint_1 = imgr.lessOrEquals(x, imgr.makeNumber(7));

    BooleanFormula constraint_2 = imgr.lessOrEquals(imgr.makeNumber(4), x);

    BooleanFormula constraint_3 =
        imgr.lessOrEquals(imgr.subtract(y, imgr.makeNumber(3)), imgr.makeNumber(7));

    BooleanFormula constraint_4 =
        imgr.greaterOrEquals(imgr.multiply(y, imgr.makeNumber(3)), imgr.makeNumber(3));

    BooleanFormula constraint_5 = imgr.lessOrEquals(imgr.add(z, y), imgr.makeNumber(5));

    BooleanFormula constraint_6 =
        imgr.lessOrEquals(imgr.add(y, imgr.makeNumber(4)), imgr.add(x, imgr.makeNumber(5)));

    BooleanFormula constraint_7 =
        imgr.greaterOrEquals(
            imgr.add(imgr.multiply(z, imgr.makeNumber(3)), imgr.makeNumber(2)),
            imgr.makeNumber(-50));

    env.addConstraint(constraint_1);
    env.addConstraint(constraint_2);
    env.addConstraint(constraint_3);
    env.addConstraint(constraint_4);
    env.addConstraint(constraint_5);
    env.addConstraint(constraint_6);
    env.addConstraint(constraint_7);
    boolean isUnsat = env.isUnsat();
    System.out.println(isUnsat);
    SolutionSet[] domains = new SolutionSet[3];
    List<Formula> usedVariables = env.getVariables();
    for (int i = 0; i <= usedVariables.size(); i++) {
      Formula var = usedVariables.get(i);
      SolutionSet domain = env.getSolutionSet(var);
      domains[i] = domain;
    }
    return domains;

  }

  @Test
  public void test_Solutions()
      throws InterruptedException, SolverException, InvalidConfigurationException {
    SolutionSet[] solutionSets = initializeTest();
    assertThat(solutionSets[0].getLowerBound()).isEqualTo(4);
  }
}
