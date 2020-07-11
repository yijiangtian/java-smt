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

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import junit.framework.Assert;
import org.junit.runners.Parameterized;
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

import static com.google.common.truth.Truth.assertThat;

@RunWith(Parameterized.class)
public class DomainOptimizerTest {

  public SolutionSet[] initializeTest()
      throws InterruptedException, SolverException, InvalidConfigurationException {
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
        y = imgr.makeVariable("y"),
        z = imgr.makeVariable("z");

    BooleanFormula constraint_1 =
        imgr.lessOrEquals(x, imgr.makeNumber(7));

    BooleanFormula constraint_2 =
        imgr.lessOrEquals(imgr.makeNumber(4), x);

    BooleanFormula constraint_3 =
        imgr.lessOrEquals(
            imgr.subtract(y, imgr.makeNumber(3)), imgr.makeNumber(7));

    BooleanFormula constraint_4 =
        imgr.greaterOrEquals(
            imgr.multiply(y, imgr.makeNumber(3)), imgr.makeNumber(3));

    BooleanFormula constraint_5 =
        imgr.lessOrEquals(
            imgr.add(z, y), imgr.makeNumber(5));

    BooleanFormula constraint_6 =
        imgr.lessOrEquals(
            imgr.add(y, imgr.makeNumber(4)), imgr.add(x, imgr.makeNumber(5)));

    BooleanFormula constraint_7 =
        imgr.greaterOrEquals(
            imgr.add(imgr.multiply(z, imgr.makeNumber(3)), imgr.makeNumber(2)),
            imgr.makeNumber(-50));

    DomainOptimizer optimizer = new BasicDomainOptimizer((DomainOptimizerSolverContext) delegate,
        wrapped);

    optimizer.pushConstraint(constraint_1);
    optimizer.pushConstraint(constraint_2);
    optimizer.pushConstraint(constraint_3);
    optimizer.pushConstraint(constraint_4);
    optimizer.pushConstraint(constraint_5);
    optimizer.pushConstraint(constraint_6);
    optimizer.pushConstraint(constraint_7);
    boolean isUnsat = optimizer.isUnsat();
    System.out.println(isUnsat);
    SolutionSet[] domains = new SolutionSet[3];
    List<Formula> usedVariables = optimizer.getVariables();
    for (int i = 0; i <= usedVariables.size(); i++) {
      Formula var = usedVariables.get(i);
      SolutionSet domain = optimizer.getSolutionSet(var);
      domains[i] = domain;
    }
    return domains;
  }

  @Test
  public void test_Solutions()
      throws InterruptedException, SolverException, InvalidConfigurationException {
   SolutionSet[] solutionSets = initializeTest();
    Assert.assertEquals(java.util.Optional.ofNullable(solutionSets[0].getLowerBound()),4);
    Assert.assertEquals(java.util.Optional.ofNullable(solutionSets[0].getUpperBound()),7);
    Assert.assertEquals(java.util.Optional.ofNullable(solutionSets[1].getLowerBound()),3);
    Assert.assertEquals(java.util.Optional.ofNullable(solutionSets[1].getUpperBound()),5);
  }

}
