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

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverException;


public class FileReader {
  FormulaManager fmgr;
  SolverContext context;
  DomainOptimizerBasicProverEnvironment wrapped;
  DomainOptimizer optimizer;

  public FileReader() throws InvalidConfigurationException {
    initialize();
  }

  public String parseHeader(String path) throws FileNotFoundException {
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

  public ArrayList<String> parseAsserts(String path) throws FileNotFoundException {
    ArrayList<String> asserts = new ArrayList<>();
    String toAppend = "( assert";
    Scanner scanner = new Scanner(new File(path), Charset.defaultCharset().name());
    scanner.useDelimiter("assert");
    while (scanner.hasNext()) {
      String toAssert = scanner.next();
      asserts.add(toAssert);
    }
    asserts.remove(0);
    ArrayList<String> processedAsserts = new ArrayList<>();
    for (String s : asserts) {
      s = toAppend.concat(s);
      s = s.substring(0, s.length() - 1);
      processedAsserts.add(s);
    }
    return processedAsserts;
  }

  public void initialize() throws InvalidConfigurationException {
    ConfigurationBuilder builder = Configuration.builder();
    builder.setOption("useDomainOptimizer", "true");
    Configuration config = builder.build();
    LogManager logger = BasicLogManager.create(config);
    ShutdownManager shutdown = ShutdownManager.create();
    SolverContext context = SolverContextFactory.createSolverContext(
        config, logger, shutdown.getNotifier(), Solvers.SMTINTERPOL);
    FormulaManager fmgr = context.getFormulaManager();
    DomainOptimizerProverEnvironment wrapped = new DomainOptimizerProverEnvironment(context);
    DomainOptimizer optimizer = new BasicDomainOptimizer((DomainOptimizerSolverContext) context,
        wrapped);
    this.fmgr = fmgr;
    this.context = context;
    this.wrapped = wrapped;
    this.optimizer = optimizer;
  }

  public static void main(String[] args)
      throws InvalidConfigurationException, FileNotFoundException, InterruptedException,
             SolverException {
      String filePath = System.getProperty("user.dir") + File.separator + "benchmark_2.smt2";
    FileReader reader = new FileReader();
    String header = reader.parseHeader(filePath);
    ArrayList<String> asserts = reader.parseAsserts(filePath);
      for (String toAssert : asserts) {
        BooleanFormula constraint = reader.fmgr.parse(header + toAssert);
        reader.optimizer.visit(constraint);
        reader.optimizer.pushConstraint(constraint);
      }
        List<Formula> usedVariables = reader.optimizer.getVariables();
        for (Formula var : usedVariables) {
          System.out.println(var.toString());
          SolutionSet domain = reader.optimizer.getSolutionSet(var);
          domain.show();
        }
        Set<Formula> newConstraints = reader.optimizer.getNewConstraints();
        for (Formula constraint : newConstraints) {
          reader.wrapped.addConstraint((BooleanFormula) constraint);
        }
        System.out.println(reader.wrapped.isUnsat());
    }
}