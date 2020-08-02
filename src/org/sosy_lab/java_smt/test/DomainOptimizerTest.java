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


import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
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
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverException;
import org.sosy_lab.java_smt.domain_optimization.DomainOptimizerProverEnvironment;
import org.sosy_lab.java_smt.domain_optimization.DomainOptimizerSolverContext;

public class DomainOptimizerTest {

  private boolean isUnsatWithoutDomainOptimizer;
  private boolean isUnsatWithDomainOptimizer;

  @Before
  public void setupTest()
      throws InvalidConfigurationException, InterruptedException, SolverException {
    Configuration config = Configuration.builder().setOption("useDomainOptimizer", "true").build();
    LogManager logger = BasicLogManager.create(config);
    ShutdownManager shutdown = ShutdownManager.create();
    List<Formula> constraints = new ArrayList<>();
    List<Formula> queries = new ArrayList<>();
    DomainOptimizerSolverContext delegate =
        (DomainOptimizerSolverContext) SolverContextFactory.createSolverContext(
            config, logger, shutdown.getNotifier(), Solvers.SMTINTERPOL);
    try (ProverEnvironment basicEnv = delegate.newProverEnvironment()) {

      FormulaManager fmgr = delegate.getFormulaManager();

      IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
      IntegerFormula x = imgr.makeVariable("x"),
          y = imgr.makeVariable("y"),
          z = imgr.makeVariable("z");
      BooleanFormula constraint_1 = imgr.lessOrEquals(x, imgr.makeNumber(7));
      BooleanFormula constraint_2 = imgr.lessOrEquals(imgr.makeNumber(4), x);
      BooleanFormula constraint_3 =
          imgr.lessOrEquals(imgr.subtract(y, imgr.makeNumber(3)), imgr.makeNumber(7));
      BooleanFormula constraint_4 =
          imgr.greaterOrEquals(imgr.add(y, imgr.makeNumber(3)), imgr.makeNumber(3));
      BooleanFormula constraint_5 = imgr.lessOrEquals(imgr.add(z, y), imgr.makeNumber(5));
      BooleanFormula constraint_6 =
          imgr.lessOrEquals(imgr.add(y, imgr.makeNumber(4)), imgr.add(x, imgr.makeNumber(5)));
      BooleanFormula constraint_7 =
          imgr.greaterOrEquals(
              imgr.add(imgr.add(z, imgr.makeNumber(3)), imgr.makeNumber(2)),
              imgr.makeNumber(-50));
      constraints.add(constraint_1);
      constraints.add(constraint_2);
      constraints.add(constraint_3);
      constraints.add(constraint_4);
      constraints.add(constraint_5);
      constraints.add(constraint_6);
      constraints.add(constraint_7);

      BooleanFormula query_1 = imgr.greaterThan(imgr.add(x, imgr.makeNumber(7)), z);
      BooleanFormula query_2 = imgr.lessOrEquals(imgr.subtract(y, z), imgr.makeNumber(8));
      BooleanFormula query_3 = imgr.lessThan(imgr.add(x, y), imgr.makeNumber(100));
      queries.add(query_1);
      queries.add(query_2);
      queries.add(query_3);
      for (Formula constraint : constraints) {
        basicEnv.addConstraint((BooleanFormula) constraint);
      }
      for (Formula query : queries) {
        basicEnv.addConstraint((BooleanFormula) query);
      }
      boolean isBasicEnvUnsat = basicEnv.isUnsat();
      try (DomainOptimizerProverEnvironment env = new DomainOptimizerProverEnvironment(delegate)) {
        for (Formula constraint : constraints) {
          env.addConstraint((BooleanFormula) constraint);
        }
        for (Formula query : queries) {
          env.pushQuery(query);
        }
        boolean isUnsat = env.isUnsat();
        isUnsatWithoutDomainOptimizer = isBasicEnvUnsat;
        isUnsatWithDomainOptimizer = isUnsat;
      }
    }
  }

  @Test
  public void isSatisfiabilityAffected() {
    Assert.assertEquals(isUnsatWithDomainOptimizer,isUnsatWithoutDomainOptimizer);
  }

}
