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
import static com.google.common.truth.Truth.assertThat;
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
    try (DomainOptimizerSolverContext delegate =
        (DomainOptimizerSolverContext) SolverContextFactory.createSolverContext(
            config, logger, shutdown.getNotifier(), Solvers.SMTINTERPOL)) {
      try (ProverEnvironment basicEnv = delegate.newProverEnvironment()) {

        FormulaManager fmgr = delegate.getFormulaManager();

        IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
        IntegerFormula x = imgr.makeVariable("x");
        IntegerFormula y = imgr.makeVariable("y");
        BooleanFormula constraintOne = imgr.lessOrEquals(x, imgr.makeNumber(7));
        BooleanFormula constraintTwo = imgr.lessOrEquals(imgr.makeNumber(4), x);
        BooleanFormula constraintThree =
            imgr.lessOrEquals(imgr.subtract(y, imgr.makeNumber(3)), imgr.makeNumber(7));
        BooleanFormula constraintFour =
            imgr.greaterOrEquals(imgr.add(y, imgr.makeNumber(3)), imgr.makeNumber(3));
        constraints.add(constraintOne);
        constraints.add(constraintTwo);
        constraints.add(constraintThree);
        constraints.add(constraintFour);

        for (Formula constraint : constraints) {
          basicEnv.addConstraint((BooleanFormula) constraint);
        }
        isUnsatWithoutDomainOptimizer = basicEnv.isUnsat();
      }
      try (DomainOptimizerProverEnvironment env = new DomainOptimizerProverEnvironment(delegate)) {
        for (Formula constraint : constraints) {
          env.addConstraint((BooleanFormula) constraint);
        }
        isUnsatWithDomainOptimizer = env.isUnsat();
      }
    }
  }

  @Test
  public void isSatisfiabilityAffected() {
    assertThat(isUnsatWithDomainOptimizer).isEqualTo(isUnsatWithoutDomainOptimizer);
  }

}
