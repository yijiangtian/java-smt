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

import static org.sosy_lab.java_smt.test.ProverEnvironmentSubject.assertThat;

import com.google.common.truth.Truth;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
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
import org.sosy_lab.java_smt.api.Model;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;
import org.sosy_lab.java_smt.api.SolverException;

public class FileReader {
  FormulaManager fmgr;
  SolverContext context;
  DomainOptimizerProverEnvironment wrapped;
  DomainOptimizer optimizer;
  DomainOptimizerDecider decider;

  public static String parseHeader(String path) throws FileNotFoundException {
    String header = "";
    Scanner scanner = new Scanner(new File(path), Charset.defaultCharset().name());
    while (scanner.hasNextLine()) {
      String s = scanner.nextLine();
      if (s.contains("(declare-fun")) {
        header = header.concat(s);
      }
    }
    return header;
  }

  public static ArrayList<String> parseAsserts(String path) throws FileNotFoundException {
    ArrayList<String> asserts = new ArrayList<>();
    String toAppend = "( assert";
    Scanner scanner = new Scanner(new File(path), Charset.defaultCharset().name());
    scanner.useDelimiter("assert");
    while (scanner.hasNext()) {
      String toAssert = scanner.next();
      asserts.add(toAssert);
    }
    scanner.close();
    asserts.remove(0);
    ArrayList<String> processedAsserts = new ArrayList<>();
    for (String s : asserts) {
      s = toAppend.concat(s);
      s = s.substring(0, s.length() - 1);
      processedAsserts.add(s);
    }
    return processedAsserts;
  }

  public static void main(String[] args)
      throws InvalidConfigurationException, FileNotFoundException, InterruptedException {
    String filePath = System.getProperty("user.dir") + File.separator + "benchmark_2.smt2";
    String header = parseHeader(filePath);
    ArrayList<String> asserts = parseAsserts(filePath);

    Configuration config = Configuration.builder().setOption("useDomainOptimizer", "true").build();
    LogManager logger = BasicLogManager.create(config);
    ShutdownManager shutdown = ShutdownManager.create();

    DomainOptimizerSolverContext delegate =
        (DomainOptimizerSolverContext) SolverContextFactory.createSolverContext(
        config, logger, shutdown.getNotifier(), Solvers.SMTINTERPOL);

    FormulaManager fmgr = delegate.getFormulaManager();
    IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
    DomainOptimizerProverEnvironment env = new DomainOptimizerProverEnvironment(delegate);

    for (String toAssert : asserts) {
      BooleanFormula constraint = fmgr.parse(header + toAssert);
      env.addConstraint(constraint);
    }

    List<Formula> usedVariables = env.getVariables();
    for (Formula var : usedVariables) {
      System.out.println(var.toString());
      SolutionSet domain = env.getSolutionSet(var);
      System.out.println(domain);
    }

    IntegerFormula var_1 = (IntegerFormula) usedVariables.get(0);
    IntegerFormula var_2 = (IntegerFormula) usedVariables.get(1);
    BooleanFormula query = imgr.lessThan(imgr.add(var_1, var_2), imgr.makeNumber(10000));

  }
}
